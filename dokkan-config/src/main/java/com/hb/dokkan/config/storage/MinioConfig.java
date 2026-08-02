package com.hb.dokkan.config.storage;

import io.minio.MinioClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(normalizeEndpoint(properties.getEndpoint()))
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
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
}
