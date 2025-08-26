package com.hb.dokkan.common.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/8/24 21:33
 **/
@Component
@Slf4j
public class HttpPoolMonitor {

    public void monitorConnectorPools(){
        log.info("=== http连接池状态监控===");
    }


}
