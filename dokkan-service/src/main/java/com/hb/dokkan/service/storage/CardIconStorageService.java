package com.hb.dokkan.service.storage;

import com.hb.dokkan.config.storage.MinioStorageProperties;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CardIconStorageService {

    private static final long DOKKAN_DB_CARD_ID_OFFSET = 1L;

    private static final String SOURCE_URL_TEMPLATE =
            "https://enaskhebnjtktdfszdcb.supabase.co/storage/v1/object/public/assets/character/thumb/"
                    + "card_%d_thumb_folder/card_%d_thumb.png";

    private static final String CONTENT_TYPE = MediaType.IMAGE_PNG_VALUE;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioStorageProperties minioStorageProperties;

    private volatile boolean bucketReady = false;

    public String saveCardIcon(Long cardId) {
        if (cardId == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(minioStorageProperties.getEnabled())) {
            return buildSourceUrl(cardId);
        }
        String objectName = buildObjectName(cardId);
        try {
            ensureBucketReady();
            if (!objectExists(objectName)) {
                Path iconPath = downloadCardIconToLocal(cardId);
                if (iconPath == null) {
                    return null;
                }
                uploadCardIcon(objectName, iconPath);
            }
            return buildPublicUrl(objectName);
        } catch (Exception e) {
            log.error("save card icon failed, cardId:{}, message:{}", cardId, e.getMessage(), e);
            throw new DokkanBizException("save card icon failed, cardId:" + cardId + ", message:" + e.getMessage());
        }
    }

    private Path downloadCardIconToLocal(Long cardId) throws Exception {
        Path iconPath = buildLocalIconPath(cardId);
        if (Files.exists(iconPath) && Files.size(iconPath) > 0) {
            return iconPath;
        }
        byte[] iconBytes = downloadCardIcon(cardId);
        if (iconBytes == null || iconBytes.length == 0) {
            log.warn("download card icon empty, cardId:{}", cardId);
            return null;
        }
        Files.createDirectories(iconPath.getParent());
        Files.write(iconPath, iconBytes);
        return iconPath;
    }

    private byte[] downloadCardIcon(Long cardId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, "dokkan-web/1.0");
        headers.setAccept(List.of(MediaType.IMAGE_PNG, MediaType.APPLICATION_OCTET_STREAM));
        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.exchange(
                    buildSourceUrl(cardId),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class);
        } catch (RestClientResponseException e) {
            log.warn("download card icon failed, cardId:{}, status:{}", cardId, e.getStatusCode());
            return null;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("download card icon failed, cardId:{}, status:{}", cardId, response.getStatusCode());
            return null;
        }
        return response.getBody();
    }

    private void uploadCardIcon(String objectName, Path iconPath) throws Exception {
        try (InputStream inputStream = Files.newInputStream(iconPath)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioStorageProperties.getBucket())
                    .object(objectName)
                    .stream(inputStream, Files.size(iconPath), -1)
                    .contentType(CONTENT_TYPE)
                    .build());
        }
    }

    private boolean objectExists(String objectName) throws Exception {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioStorageProperties.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (Objects.nonNull(e.errorResponse()) && "NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw e;
        }
    }

    private void ensureBucketReady() throws Exception {
        if (bucketReady) {
            return;
        }
        synchronized (this) {
            if (bucketReady) {
                return;
            }
            String bucket = minioStorageProperties.getBucket();
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
            bucketReady = true;
        }
    }

    private String buildObjectName(Long cardId) {
        String prefix = StringUtils.stripEnd(minioStorageProperties.getCardIconPrefix(), "/");
        return prefix + "/db/" + cardId + ".png";
    }

    private Path buildLocalIconPath(Long cardId) {
        return Path.of(minioStorageProperties.getCardIconCacheDir(), "db", cardId + ".png");
    }

    private String buildSourceUrl(Long cardId) {
        long dokkanDbCardId = cardId - DOKKAN_DB_CARD_ID_OFFSET;
        return String.format(SOURCE_URL_TEMPLATE, dokkanDbCardId, dokkanDbCardId);
    }

    private String buildPublicUrl(String objectName) {
        String publicEndpoint = StringUtils.defaultIfBlank(
                minioStorageProperties.getPublicEndpoint(),
                minioStorageProperties.getEndpoint());
        return StringUtils.stripEnd(normalizeEndpoint(publicEndpoint), "/")
                + "/" + minioStorageProperties.getBucket() + "/" + objectName;
    }

    private String normalizeEndpoint(String endpoint) {
        if (StringUtils.isBlank(endpoint)) {
            return endpoint;
        }
        if (StringUtils.startsWithAny(endpoint, "http://", "https://")) {
            return endpoint;
        }
        return "http://" + endpoint;
    }

    private String buildPublicReadPolicy(String bucket) {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
    }
}
