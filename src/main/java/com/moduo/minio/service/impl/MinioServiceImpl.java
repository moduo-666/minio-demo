package com.moduo.minio.service.impl;

import com.moduo.minio.model.result.RespBean;
import com.moduo.minio.service.MinioService;
import com.moduo.minio.util.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * @author Wu Zicong
 * @create 2021-11-10 14:40
 */
@Service
public class MinioServiceImpl implements MinioService {
    @Autowired
    private MinioUtil minioUtil;
    @Override
    public RespBean downloadFile(HttpServletResponse response, String bucketName, String objectName) {
        if(minioUtil.statObject(bucketName,objectName)){
            try{
                minioUtil.downloadFile(response,bucketName,objectName);
                return RespBean.info(200,"下载成功");
            }catch (Exception e){
                return RespBean.info(500,"下载失败，服务器异常");
            }
        }
        return RespBean.info(400,"下载失败,请检查桶名和文件名");
    }



    @Override
    public String putFile(String bucketName, String objectName, String originalName,InputStream inputStream) {
        if(minioUtil.putObject(bucketName, objectName,originalName, inputStream)){
            //如果上传成功，返回下载链接
            return minioUtil.getDownLoadUrl(bucketName,objectName);
        }
        return null;
    }

    @Override
    public RespBean deleteFile(String bucketName, String fileName) {
        if(!minioUtil.bucketExists(bucketName)){
            return RespBean.info(400,"桶: " + bucketName + "不存在");
        }
        if(minioUtil.statObject(bucketName,fileName)){
            try{
                minioUtil.deleteFile(bucketName,fileName);
            }catch (Exception e){
                return RespBean.info(500,"删除失败",e.getMessage());
            }
            return RespBean.info(200,"删除成功");
        }
        return RespBean.info(400,"没有这个文件");
    }

    @Override
    public boolean statObject(String bucketName, String fileName) {
        return minioUtil.statObject(bucketName,fileName);
    }

    @Override
    public RespBean createBucket(String bucketName) {
        if(minioUtil.bucketExists(bucketName)){
            return RespBean.info(400,bucketName+"已存在，请勿重复创建");
        }
        if(minioUtil.createBucket(bucketName)){
            return RespBean.info(200,bucketName+"创建成功");
        }
        return RespBean.info(500,"创建失败,服务器异常");
    }

    @Override
    public RespBean getUploadUrl(String bucketName, String fileName) {
        if(minioUtil.statObject(bucketName,fileName)){
            String downLoadUrl = minioUtil.getDownLoadUrl(bucketName, fileName);
            if(downLoadUrl!=null)
            return RespBean.info(200,"获取成功",downLoadUrl);
        }
        return RespBean.info(400,"获取失败，请检查桶名与文件名");
    }
}
