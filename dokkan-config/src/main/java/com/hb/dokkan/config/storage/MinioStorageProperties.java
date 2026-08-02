package com.hb.dokkan.config.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Minio存储配置属性类
 * 用于绑定application.yml中配置的oss.minio相关配置项
 * 通过@ConfigurationProperties注解将配置文件中的值映射到该类的属性上
 * @Data注解为Lombok提供，自动生成getter、setter等方法
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss.minio")
public class MinioStorageProperties {

    // 是否启用Minio存储，默认为true
    private Boolean enabled = true;

    // Minio服务的端点URL
    private String endpoint;

    // 公开访问的Minio服务端点URL
    private String publicEndpoint;

    // 访问Minio服务的访问密钥
    private String accessKey;

    // 访问Minio服务的秘密密钥
    private String secretKey;

    // 存储桶名称
    private String bucket;

    // 卡片图标文件的前缀路径
    private String cardIconPrefix;

    // 卡片图标的本地缓存目录
    private String cardIconCacheDir;
}
