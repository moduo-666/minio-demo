package com.moduo.minio.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "minio")
@EnableConfigurationProperties
@Data
@Component
public class MinIoConfigProperties {
	/**
	 * minio 服务地址
	 */
	private String url;
	/**
	 * 用户名
	 */
	private String accessKey;
	/**
	 * 密码
	 */
	private String secretKey;
	/**
	 * 桶名称
	 */
	private String bucketName;
	/**
	 * 外网地址
	 */
	private String extranetUrl;

	@Override
	public String toString() {
		return "MinIoConfigProperties{" +
				"url='" + url + '\'' +
				", accessKey='" + accessKey + '\'' +
				", secretKey='" + secretKey + '\'' +
				", bucketName='" + bucketName + '\'' +
				", extranetUrl='" + extranetUrl + '\'' +
				'}';
	}
}
