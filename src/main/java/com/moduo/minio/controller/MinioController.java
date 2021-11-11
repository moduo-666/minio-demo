package com.moduo.minio.controller;

import com.moduo.minio.model.ResultFile;
import com.moduo.minio.model.result.RespBean;
import com.moduo.minio.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * @author Wu Zicong
 * @create 2021-11-09 15:07
 */
@Slf4j
@RestController
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    private MinioService minioService;

    /**
     * 下载文件
     * @param response
     * @param bucketName
     * @param fileName
     * @return
     */
    @GetMapping("/downloadFile")
    public RespBean download(HttpServletResponse response, String bucketName, String fileName){
        return minioService.downloadFile(response,bucketName,fileName);
    }

    /**
     * 获取下载链接
     * @param bucketName
     * @param fileName
     * @return
     */
    @GetMapping("/getUploadUrl")
    public RespBean getUploadUrl(String bucketName,String fileName){
       return minioService.getUploadUrl(bucketName,fileName);
    }

    /**
     * 上传文件
     * @param bucketName
     * @param file
     * @return
     */
    @PostMapping("/putFile")
    public RespBean<ResultFile> putFile(String bucketName, MultipartFile file) {
        InputStream inputStream = null;
        String fileName = null;
        try{
            fileName = file.getOriginalFilename();
            String objectName = DigestUtils.md5DigestAsHex(file.getBytes())+ fileName.substring(fileName.lastIndexOf("."));
            inputStream = file.getInputStream();
            String url = minioService.putFile(bucketName,objectName,fileName,inputStream);
            if(StringUtils.isEmpty(url)){
                return RespBean.info(400,"上传失败");
            }
            ResultFile resultFile = new ResultFile();
            resultFile.setLink(url);
            resultFile.setName(objectName);
            resultFile.setOriginalName(fileName);
            return RespBean.info(200,"上传成功",resultFile);
        }catch (Exception e){
            log.info("Minio文件【"+fileName+"】【上传】异常信息：{}",e);
            return RespBean.info(500,"上传失败");
        }finally{
            try{
                if(inputStream!=null)
                inputStream.close();
            }catch (Exception e){
                log.info("Minio文件【"+fileName+"】【上传】异常信息：{}",e);
            }
        }
    }

    /**
     * 删除文件
     * @param bucketName
     * @param fileName
     * @return
     */
    @PostMapping("/deleteFile")
    public RespBean deleteFile(String bucketName,String fileName){
        return minioService.deleteFile(bucketName,fileName);
    }

    /**
     * 创建桶
     * @param bucketName
     * @return
     */
    @PostMapping("/createBucket")
    public RespBean createBucket(String bucketName){
        return minioService.createBucket(bucketName);
    }
}
