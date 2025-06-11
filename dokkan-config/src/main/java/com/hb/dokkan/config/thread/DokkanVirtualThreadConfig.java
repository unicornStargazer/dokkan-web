package com.hb.dokkan.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

/**
 * @Description 虚拟线程注入
 * @Author stargazer
 * @Date 2025/6/11 22:06
 **/
@Configuration
public class DokkanVirtualThreadConfig {

    @Bean
    public VirtualThreadTaskExecutor dokkanExecutor(){
        return new VirtualThreadTaskExecutor("dokkan-virtual-thread");
    }
}
