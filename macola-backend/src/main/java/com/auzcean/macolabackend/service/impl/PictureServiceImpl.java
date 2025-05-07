package com.auzcean.macolabackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.auzcean.macolabackend.exception.ErrorCode;
import com.auzcean.macolabackend.exception.ThrowUtils;
import com.auzcean.macolabackend.manage.FileManager;
import com.auzcean.macolabackend.mapper.PictureMapper;
import com.auzcean.macolabackend.model.dto.file.UploadPictureResult;
import com.auzcean.macolabackend.model.dto.picture.PictureQueryRequest;
import com.auzcean.macolabackend.model.dto.picture.PictureUploadRequest;
import com.auzcean.macolabackend.model.entity.Picture;
import com.auzcean.macolabackend.model.entity.User;
import com.auzcean.macolabackend.model.v0.PictureV0;
import com.auzcean.macolabackend.model.v0.UserV0;
import com.auzcean.macolabackend.service.PictureService;
import com.auzcean.macolabackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xchuckie
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-05-05 20:41:33
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private FileManager fileManager;

    @Override
    public PictureV0 getPictureV0(Picture picture, HttpServletRequest request) {
        // 1. 将对象转换为包装类
        PictureV0 pictureV0 = PictureV0.objToV0(picture);
        // 2. 关联查询用户信息
        Long userId = pictureV0.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserV0 userV0 = userService.getUserV0(user);
            pictureV0.setUser(userV0);
        }
        return pictureV0;
    }

    @Override
    public Page<PictureV0> getPictureV0Page(Page<Picture> picturePage) {
        // 1. 源数据 -> 目标对象
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureV0> pictureV0Page = new Page<>(picturePage.getCurrent(), picturePage.getSize());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureV0Page;
        }
        // 2. 对象列表 -> 封装对象列表
        List<PictureV0> pictureV0List = pictureList.stream().map(PictureV0::objToV0).collect(Collectors.toList());
        // 3. 关联用户信息：获取用户id集、基于id集进行搜索再分组、遍历pictureV0List进行用户信息填充
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        pictureV0List.forEach(pictureV0 -> {
            Long userId = pictureV0.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureV0.setUser(userService.getUserV0(user));
        });
        // 4. 封装对象列表转换为目标对象
        pictureV0Page.setRecords(pictureV0List);

        return pictureV0Page;
    }

    @Override
    public PictureV0 uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1. 判断用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2. 判断：新增图片 or 更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 若是更新图片，需要检索数据库校验图片是否存在
        if (pictureId != null && pictureId > 0) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 4. 上传图片
        // 按照用户id划分不同的目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 构造需要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 若是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureV0.objToV0(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        // 1. 参数校验
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 2. 获取分页查询中的参数信息
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 3. 设置搜索条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 从多字段进行模糊搜索：name和introduction
        if (StrUtil.isNotBlank(searchText)) {
            // 将查询字段拼接
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("introduction", searchText));
        }
        // 单字段查询：精确查询和模糊查询
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON数组查询，查询形式'%"tech"%'
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void validPicture(Picture picture) {
        // 1. 参数校验
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 2. 校验参数：id、url、introduction
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改参数时，id不能为空
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id参数不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }
}




