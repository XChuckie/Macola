package com.auzcean.macolabackend.manage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.auzcean.macolabackend.config.CosClientConfig;
import com.auzcean.macolabackend.exception.BusinessException;
import com.auzcean.macolabackend.exception.ErrorCode;
import com.auzcean.macolabackend.exception.ThrowUtils;
import com.auzcean.macolabackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class FileManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(multipartFile);
        // 2. 重制定图片上传地址: 上传日期 + 16位uuid随机数
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        // 3. 上传图片并解析结果
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);  // 本地构建临时文件
            multipartFile.transferTo(file);  // 将multipartFile中文件传输到本地临时文件
            // 上传图片
            PutObjectResult putObjectResult = cosManager.pubPictureObject(uploadPath, file);
            // 通过数据万象服务获取图片解析信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装解析信息
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到存储对象失败" + e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 4. 临时文件清理
            deleteTempFile(file);
        }
    }

    /**
     * 校验文件(通用校验)
     *
     * @param multipartFile multipart 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件上传不能大于2MB");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOW_FORMATS = Arrays.asList("jpg", "jpeg", "png");
        ThrowUtils.throwIf(!ALLOW_FORMATS.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "上传文件类型错误，只允许上传:JPG、JPEG、PNG！");
    }

    /**
     * 删除临时文件
     */
    private static void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete){
            log.error("file delete fail, filepath: {}", file.getAbsolutePath());
        }
    }
}