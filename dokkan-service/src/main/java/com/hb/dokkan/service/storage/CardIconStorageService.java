package com.hb.dokkan.service.storage;

import com.hb.dokkan.common.constants.CardSyncConstants;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.config.storage.MinioStorageProperties;
import com.hb.dokkan.service.storage.client.CardIconDownloadClient;
import com.hb.dokkan.service.storage.client.MinioStorageClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @Description 卡片头像存储服务
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
@Slf4j
@Component
public class CardIconStorageService {

    @Resource
    private MinioStorageProperties minioStorageProperties;

    @Resource
    private CardIconDownloadClient cardIconDownloadClient;

    @Resource
    private MinioStorageClient minioStorageClient;

    /** MinIO bucket 是否已完成初始化。 */
    private volatile boolean bucketReady = false;

    /**
     * 保存卡片头像并返回可访问 URL，MinIO 未启用时返回源站 URL。
     *
     * @param cardId 卡片 ID
     * @return 头像 URL，下载失败时返回 null
     */
    public String saveCardIcon(Long cardId) {
        if (cardId == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(minioStorageProperties.getEnabled())) {
            return buildSourceUrl(cardId);
        }
        String objectName = buildObjectName(cardId);
        try {
            // 先确保 bucket 可用，再判断对象是否已经存在，避免重复下载上传。
            ensureBucketReady();
            if (!minioStorageClient.objectExists(objectName)) {
                Path iconPath = downloadCardIconToLocal(cardId);
                if (iconPath == null) {
                    return null;
                }
                minioStorageClient.uploadObject(objectName, iconPath);
            }
            return buildPublicUrl(objectName);
        } catch (Exception e) {
            log.error("save card icon failed, cardId={}, message={}", cardId, e.getMessage(), e);
            throw new DokkanBizException("save card icon failed, cardId:" + cardId + ", message:" + e.getMessage());
        }
    }

    /**
     * 下载卡片头像到本地缓存，缓存已存在时直接复用。
     *
     * @param cardId 卡片 ID
     * @return 本地头像路径，下载失败时返回 null
     * @throws Exception 本地文件写入失败时抛出
     */
    private Path downloadCardIconToLocal(Long cardId) throws Exception {
        Path iconPath = buildLocalIconPath(cardId);
        if (Files.exists(iconPath) && Files.size(iconPath) > 0) {
            return iconPath;
        }
        byte[] iconBytes = cardIconDownloadClient.download(cardId, buildSourceUrl(cardId));
        if (cardIconDownloadClient.isEmpty(iconBytes)) {
            log.warn("download card icon empty, cardId={}", cardId);
            return null;
        }
        Files.createDirectories(iconPath.getParent());
        Files.write(iconPath, iconBytes);
        return iconPath;
    }

    /**
     * 确保 MinIO bucket 已创建并设置公开读策略。
     *
     * @throws Exception bucket 初始化失败时抛出
     */
    private void ensureBucketReady() throws Exception {
        if (bucketReady) {
            return;
        }
        synchronized (this) {
            if (bucketReady) {
                return;
            }
            minioStorageClient.ensureBucketReady();
            bucketReady = true;
        }
    }

    /**
     * 构建对象存储中的头像对象名称。
     *
     * @param cardId 卡片 ID
     * @return 对象名称
     */
    private String buildObjectName(Long cardId) {
        String prefix = StringUtils.stripEnd(minioStorageProperties.getCardIconPrefix(),
                CardSyncConstants.PATH_SEPARATOR);
        return prefix + CardSyncConstants.PATH_SEPARATOR + CardSyncConstants.CARD_ICON_DB_DIRECTORY
                + CardSyncConstants.PATH_SEPARATOR + cardId + CardSyncConstants.CARD_ICON_FILE_SUFFIX;
    }

    /**
     * 构建本地头像缓存路径。
     *
     * @param cardId 卡片 ID
     * @return 本地缓存路径
     */
    private Path buildLocalIconPath(Long cardId) {
        return Path.of(minioStorageProperties.getCardIconCacheDir(), CardSyncConstants.CARD_ICON_DB_DIRECTORY,
                cardId + CardSyncConstants.CARD_ICON_FILE_SUFFIX);
    }

    /**
     * 构建卡片头像源站 URL。
     *
     * @param cardId 卡片 ID
     * @return 源站 URL
     */
    private String buildSourceUrl(Long cardId) {
        long dokkanDbCardId = cardId - CardSyncConstants.DOKKAN_DB_CARD_ID_OFFSET;
        return String.format(CardSyncConstants.CARD_ICON_SOURCE_URL_TEMPLATE, dokkanDbCardId, dokkanDbCardId);
    }

    /**
     * 构建 MinIO 公开访问 URL。
     *
     * @param objectName 对象名称
     * @return 公开访问 URL
     */
    private String buildPublicUrl(String objectName) {
        String publicEndpoint = StringUtils.defaultIfBlank(
                minioStorageProperties.getPublicEndpoint(),
                minioStorageProperties.getEndpoint());
        return StringUtils.stripEnd(normalizeEndpoint(publicEndpoint), CardSyncConstants.PATH_SEPARATOR)
                + CardSyncConstants.PATH_SEPARATOR + minioStorageProperties.getBucket()
                + CardSyncConstants.PATH_SEPARATOR + objectName;
    }

    /**
     * 规范化 MinIO 访问端点，未带协议时默认补充 http。
     *
     * @param endpoint 原始端点
     * @return 规范化端点
     */
    private String normalizeEndpoint(String endpoint) {
        if (StringUtils.isBlank(endpoint)) {
            return endpoint;
        }
        if (StringUtils.startsWithAny(endpoint,
                CardSyncConstants.HTTP_PROTOCOL_PREFIX, CardSyncConstants.HTTPS_PROTOCOL_PREFIX)) {
            return endpoint;
        }
        return CardSyncConstants.HTTP_PROTOCOL_PREFIX + endpoint;
    }
}
