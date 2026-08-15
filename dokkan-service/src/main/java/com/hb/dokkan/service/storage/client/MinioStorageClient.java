package com.hb.dokkan.service.storage.client;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.config.storage.MinioStorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @Description MinIO存储客户端
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Component
public class MinioStorageClient {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioStorageProperties minioStorageProperties;

    /**
     * 判断对象是否存在。
     *
     * @param objectName 对象名称
     * @return 存在返回 true，不存在返回 false
     * @throws Exception MinIO 查询异常
     */
    public boolean objectExists(String objectName) throws Exception {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioStorageProperties.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (Objects.nonNull(e.errorResponse())
                    && CardSyncConstants.MINIO_NO_SUCH_KEY_CODE.equals(e.errorResponse().code())) {
                return false;
            }
            log.warn("minio stat object failed, bucket={}, objectName={}",
                    minioStorageProperties.getBucket(), objectName, e);
            throw e;
        }
    }

    /**
     * 上传本地文件到 MinIO。
     *
     * @param objectName 对象名称
     * @param iconPath   本地文件路径
     * @throws Exception 上传失败时抛出
     */
    public void uploadObject(String objectName, Path iconPath) throws Exception {
        try (InputStream inputStream = Files.newInputStream(iconPath)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioStorageProperties.getBucket())
                    .object(objectName)
                    .stream(inputStream, Files.size(iconPath), -1)
                    .contentType(CardSyncConstants.CARD_ICON_CONTENT_TYPE)
                    .build());
        } catch (Exception e) {
            log.warn("minio upload object failed, bucket={}, objectName={}, path={}",
                    minioStorageProperties.getBucket(), objectName, iconPath, e);
            throw e;
        }
    }

    /**
     * 确保存储桶存在并设置公开读策略。
     *
     * @throws Exception MinIO bucket 初始化失败时抛出
     */
    public void ensureBucketReady() throws Exception {
        String bucket = minioStorageProperties.getBucket();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());
            }
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(buildPublicReadPolicy(bucket))
                    .build());
        } catch (Exception e) {
            log.warn("minio ensure bucket ready failed, bucket={}", bucket, e);
            throw e;
        }
    }

    /**
     * 构建 MinIO 公开读策略。
     *
     * @param bucket 存储桶名称
     * @return 公开读策略 JSON
     */
    private String buildPublicReadPolicy(String bucket) {
        return CardSyncConstants.MINIO_PUBLIC_READ_POLICY_TEMPLATE.formatted(bucket);
    }
}
