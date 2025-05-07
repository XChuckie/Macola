package com.auzcean.macolabackend.service;

import com.auzcean.macolabackend.model.dto.picture.PictureQueryRequest;
import com.auzcean.macolabackend.model.dto.picture.PictureUploadRequest;
import com.auzcean.macolabackend.model.entity.Picture;
import com.auzcean.macolabackend.model.entity.User;
import com.auzcean.macolabackend.model.v0.PictureV0;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author xchuckie
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-05-05 20:41:33
*/
public interface PictureService extends IService<Picture> {

    /**
     *  获取图片的包装类(单个)
     *
     * @param picture 需要脱敏的图片
     * @param request 请求服务
     * @return
     */
    PictureV0 getPictureV0(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage 需要脱敏的图片列
     * @return 脱敏后的图片列
     */
    Page<PictureV0> getPictureV0Page(Page<Picture> picturePage);

    /**
     * 上传图片
     *
     * @param multipartFile 文件
     * @param pictureUploadRequest 图片id：参数是由前端传递
     * @param loginUser 用户登录信息
     * @return 封装的图片信息
     */
    PictureV0 uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest 查询参数
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 校验图片(更新和修改时的图片校验)
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);
}
