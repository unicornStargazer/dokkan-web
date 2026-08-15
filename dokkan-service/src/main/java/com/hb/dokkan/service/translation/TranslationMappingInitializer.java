package com.hb.dokkan.service.translation;

import com.hb.dokkan.common.constants.TranslationConstants;
import com.hb.dokkan.common.constants.TranslationMappingConstants;
import com.hb.dokkan.common.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * @Description 翻译映射初始化器
 * @Author stargazer
 * @Date 2026/8/15 23:30
 **/
@Slf4j
@Component
public class TranslationMappingInitializer {

    @Resource
    private TranslationMappingService translationMappingService;

    @Resource
    private ResourceLoader resourceLoader;

    @Value(TranslationMappingConstants.INIT_ENABLED_PROPERTY)
    private boolean initEnabled;

    @Value(TranslationMappingConstants.GLOSSARY_FILE_PROPERTY)
    private String glossaryFile;

    /**
     * 初始化翻译映射表，启动时将已有静态词表和历史自定义词表写入数据库。
     */
    @PostConstruct
    public void initialize() {
        if (!initEnabled) {
            log.info("translation mapping initialization disabled");
            return;
        }
        try {
            // 先导入系统静态词表，再导入历史自定义词表，保证自定义词表覆盖系统默认值。
            initializeDefaultMappings();
            initializeCustomMappings();
            translationMappingService.refreshMappingCache();
            log.info("translation mapping initialization completed");
        } catch (Exception e) {
            // 数据表未创建或初始化失败时不阻断应用启动，后续可手动导入 SQL 后刷新。
            log.warn("translation mapping initialization skipped", e);
        }
    }

    /**
     * 初始化系统默认翻译映射。
     */
    private void initializeDefaultMappings() {
        TranslationConstants.DEFAULT_TERM_GLOSSARY.forEach((source, target) -> translationMappingService
                .saveInitializationMapping(source, target, TranslationMappingConstants.MAPPING_TYPE_SYSTEM,
                        TranslationMappingConstants.SYSTEM_MAPPING_REMARK));
    }

    /**
     * 初始化历史自定义翻译映射。
     */
    private void initializeCustomMappings() {
        try (InputStream input = resourceLoader.getResource(glossaryFile).getInputStream()) {
            Map<String, String> values = JsonUtils.inputStream2Map(input, String.class, String.class);
            values.forEach((source, target) -> translationMappingService
                    .saveInitializationMapping(source, target, TranslationMappingConstants.MAPPING_TYPE_CUSTOM,
                            TranslationMappingConstants.CUSTOM_MAPPING_REMARK));
            log.info("custom translation mappings initialized, location={}, size={}", glossaryFile, values.size());
        } catch (FileNotFoundException e) {
            log.info("custom translation glossary does not exist, location={}", glossaryFile);
        } catch (Exception e) {
            log.warn("custom translation mapping initialization failed, location={}", glossaryFile, e);
        }
    }
}
