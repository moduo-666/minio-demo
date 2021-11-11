package com.moduo.minio.util;


import com.moduo.minio.config.MinIoConfigProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Minio工具类
 * @author Wu Zicong
 * @create 2021-11-09 15:07
 */
@Slf4j
@Component
public class MinioUtil {

	@Autowired
	private MinIoConfigProperties minioConfig;

	private static MinioClient minioClient;
	private static final int DEFAULT_EXPIRY_SECONDS = 20;

	@PostConstruct
	public void init() {
		try {
			MinioUtil.minioClient = MinioClient.builder().endpoint(minioConfig.getUrl())
					.credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
		} catch (Exception e) {
			throw new RuntimeException("初始化minio失败:[" + e.getMessage() + "]");
		}
	}

	/**
	 * 获取下载url
	 * @return
	 */
	public String getDownLoadUrl(String bucketName,String objectName){
		if(!bucketExists(bucketName)){
			return null;
		}
		return getExtranetUrl() + bucketName + "/" + objectName;
	}

	/**
	 * 获取外网url
	 * @return
	 */
	public String getExtranetUrl(){
		return minioConfig.getExtranetUrl();
	}
	/**
	 * 检查存储桶是否存在
	 *
	 * @param bucketName 存储桶名称
	 * @return
	 */
	@SneakyThrows
	public boolean bucketExists(String bucketName) {
		try{
			return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		}catch (Exception e){
			log.info("Minio桶【"+bucketName+"】【检查】异常信息：{}",e);
		}
		return false;
	}
	@SneakyThrows
	public boolean createBucket(String bucketName){
		try{
		MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
		minioClient.makeBucket(makeBucketArgs);
		}catch (Exception e){
			log.info("Minio桶【"+bucketName+"】【创建】异常信息：{}",e);
		}
		return bucketExists(bucketName);
	}
	/**
	 * 通过InputStream上传对象
	 *
	 * @param bucketName 存储桶名称
	 * @param objectName 存储桶里的对象名称
	 * @param stream     要上传的流
	 */
	@SneakyThrows
	public boolean putObject(String bucketName, String objectName, String originalName, InputStream stream) {
		try{
			if(bucketExists(bucketName)){
				PutObjectArgs objectArgs = PutObjectArgs.builder().object(objectName)
						.bucket(bucketName)
						.contentType("application/octet-stream")
						.stream(stream,stream.available(),-1).build();
				//上传文件
				minioClient.putObject(objectArgs);
				return statObject(bucketName,objectName);
				}
			}catch (Exception e){
				log.info("Minio文件【"+originalName+"】【上传】异常信息：{}",e);
			}
		return false;
	}

	/**
	 * 检查是否存在该文件
	 * @return
	 */
	@SneakyThrows
	public boolean statObject(String bucketName,String objectName){
		try{
			minioClient.statObject(StatObjectArgs.builder()
					.bucket(bucketName)
					.object(objectName)
					.build());
		}catch (Exception e){
			//不存在会抛异常
			return false;
		}
		return true;
	}
	/**
	 * 获取文件下载路径
	 * @param bucketName 桶名
	 * @param objectName 文件名
	 * @param expiry 过期时间
	 * @param timeUnit 时间单位
	 * @return
	 */
	@SneakyThrows
	public String getDownloadUrl(String bucketName,String objectName,int expiry,TimeUnit timeUnit){
		boolean flag = bucketExists(bucketName);
		if (flag) {
			 return minioClient.getPresignedObjectUrl(
				  GetPresignedObjectUrlArgs.builder()
					  .method(Method.GET)
					  .bucket(bucketName)
					  .expiry(expiry, timeUnit)
					  .object(objectName)
					  .build());
		}
    	return null;
	}
	/**
	 * 根据文件名下载文件
	 *
	 * @param bucketName 存储桶名称
	 * @param objectName 存储桶里的对象名称
	 * @return
	 */
	public void downloadFile(HttpServletResponse response, String bucketName, String objectName) {
		GetObjectResponse inputStream = null;
		try{
			boolean flag = bucketExists(bucketName);
			if (flag) {
				GetObjectArgs build = GetObjectArgs.builder()
													.bucket(bucketName)
													.object(objectName)
													.build();
				 inputStream = minioClient.getObject(build);
				setHttpServletResponse(inputStream,response,objectName);
			}
		}catch (Exception e){
			log.info("Minio文件【下载】异常信息：{}",e);
			throw new RuntimeException();
		}finally{
			try{
				if(inputStream!=null)
					inputStream.close();
			}catch (Exception e){
			}
		}
	}
	/**
	 * 根据文件名下载文件(通过url)
	 *
	 * @param bucketName 存储桶名称
	 * @param objectName 存储桶里的对象名称
	 * @return
	 */
	public void downloadFileEx(HttpServletResponse response, String bucketName, String objectName) {
		try{
			boolean flag = bucketExists(bucketName);
			if (flag) {
				String url = getDownloadUrl(bucketName, objectName,DEFAULT_EXPIRY_SECONDS,TimeUnit.SECONDS); //20秒超时
				if(StringUtils.isNotEmpty(url)){
					downloadFileByUrl(response,url,objectName);
				}
			}
		}catch (Exception e){
			log.info("Minio文件【下载】异常信息：{}",e);
			throw new RuntimeException();
	}

	}
	/**
	 * 根据地址获得数据的输入流
	 * @param strUrl 网络连接地址
	 * @return url的输入流
	 */
	@SneakyThrows
	private InputStream getInputStreamByUrl(String strUrl){
		HttpURLConnection conn = null;
		try {
			URL url = new URL(strUrl);
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(DEFAULT_EXPIRY_SECONDS * 1000); //20秒超时
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.copy(conn.getInputStream(),output);
			return new ByteArrayInputStream(output.toByteArray());
		}finally {
				if (conn != null) {
					conn.disconnect();
				}
		}
	}

	/**
	* 根据url下载文件
	* @param response
	* @param downUrl
	* @param fileName
	* @return
	*/
	@SneakyThrows
	private void downloadFileByUrl(HttpServletResponse response, String downUrl, String fileName) {
		InputStream inputStream = null;
		try{
		  	inputStream = getInputStreamByUrl(downUrl);
		  	setHttpServletResponse(inputStream,response,fileName);
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

    /**
     * Content-Type可以用MediaType中的常量，自行控制；
	 * Content-Disposition则看需求，
	 * 如果需要直接打开文件，则传"inline"，
	 * 若是想下载则传"attachment"
	 * @param inputStream
     * @param response
     * @param fileName
	 */
	private void setHttpServletResponse(InputStream inputStream, HttpServletResponse response, String fileName) throws IOException {
		ServletOutputStream responseOutputStream = null;
		try{
			// 设置下载协议头。下面fileName的两次转换主要是为了保证中文文件名
			fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
			response.setHeader(
					HttpHeaders.CONTENT_DISPOSITION,
					"attachment;filename=" + new String(fileName.getBytes(), StandardCharsets.UTF_8));
			//application/octet-stream : 可执行程序(二进制流)
//			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			responseOutputStream= response.getOutputStream();
			int len;
			byte[] buffer = new byte[1024];
			while ((len = inputStream.read(buffer)) > 0) {
				responseOutputStream.write(buffer, 0, len);
			}
		}finally{
			if(responseOutputStream!=null){
				responseOutputStream.close();
			}
			if(inputStream != null){
				inputStream.close();
			}
		}
	}
	/**
	 * 删除文件
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	public void deleteFile(String bucketName,String objectName){
		try{
			if(bucketExists(bucketName)){
				minioClient.removeObject(RemoveObjectArgs.builder()
														 .bucket(bucketName)
														 .object(objectName)
														 .build());
			}
		}catch (Exception e){
			log.info("Minio文件【"+objectName+"】【删除】异常信息：{}",e);
			throw new RuntimeException();
		}
	}
}
