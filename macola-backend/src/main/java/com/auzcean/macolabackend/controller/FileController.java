package com.auzcean.macolabackend.controller;

import com.auzcean.macolabackend.annotation.AuthCheck;
import com.auzcean.macolabackend.common.BaseResponse;
import com.auzcean.macolabackend.common.ResultUtils;
import com.auzcean.macolabackend.constant.UserConstant;
import com.auzcean.macolabackend.exception.BusinessException;
import com.auzcean.macolabackend.exception.ErrorCode;
import com.auzcean.macolabackend.manage.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile 上传文件
     * @return
     */
    @PostMapping("test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);

        // 上传文件
        File file = null;
        try {
            file = File.createTempFile(filepath, null);  // 本地构建临时文件
            multipartFile.transferTo(file);  // 将multipartFile中文件传输到本地临时文件
            cosManager.putObject(filepath, file);
            return ResultUtils.success(filepath); // 返回可访问地址
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 删除临时文件
            boolean delete = file.delete();
            if (!delete){
                log.error("file delete fail, filepath: {}", filepath);
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @GetMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);  // 获取COS对象
            cosObjectInput = cosObject.getObjectContent();  // 将COS对象转换为响应流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);  // 将获取的文件流转换为字节数组

            // 设置响应头
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filepath);

            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();  // 将缓冲区内容刷新到响应体中，从而保证前端能够从响应体获取内容

        } catch (Exception e){
            log.error("file download error, filepath = " + filepath, e);
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }




    }
}
