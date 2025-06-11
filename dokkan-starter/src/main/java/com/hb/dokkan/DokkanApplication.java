package com.hb.dokkan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan(value = "com.hb.dokkan.infrastructure.*")
@EnableAsync
public class DokkanApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(DokkanApplication.class, args);
        } catch (Exception e) {
            throw new RuntimeException("spring启动错误，error:{}" + e.getMessage());
        }
    }
}
