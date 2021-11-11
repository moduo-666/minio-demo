package com.moduo.minio.service;


import com.moduo.minio.model.result.RespBean;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * @author Wu Zicong
 * @create 2021-11-10 14:40
 */
public interface MinioService {
    /**
     * 下载文件
     * @param bucketName
     * @param ObjectName
     * @return
     */
    RespBean downloadFile(HttpServletResponse response, String bucketName, String ObjectName);

    /**
     * 获取文件下载链接
     * @param bucketName
     * @param fileName
     * @return
     */
    RespBean getUploadUrl(String bucketName, String fileName);

    /**
     * 上传文件
     * @param bucketName
     * @param objectName
     * @param inputStream
     * @return
     */
    String putFile(String bucketName, String objectName, String originalName, InputStream inputStream);

    /**
     * 删除文件
     * @param bucketName
     * @param fileName
     * @return
     */
    RespBean deleteFile(String bucketName, String fileName);

    /**
     * 检查文件是否存在
     * @param bucketName
     * @param fileName
     * @return
     */
    boolean statObject(String bucketName, String fileName);

    /**
     * 创建存储桶
     * @param bucketName
     * @return
     */
    RespBean createBucket(String bucketName);
}
