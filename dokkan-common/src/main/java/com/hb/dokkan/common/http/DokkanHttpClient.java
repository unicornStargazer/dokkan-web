//package com.hb.dokkan.common.http;
//
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.List;
//
///**
// * @Description xxxxx
// * @Author stargazer
// * @Date 2025/6/13 22:32
// **/
//@Component
//@Slf4j
//public class DokkanHttpClient {
//
//    @Resource
//    private WebClient webClient;
//
//    public <R> R getForObject(String url, Class<R> responseType) {
//        return webClient
//                .get()
//                .uri(url)
//                .headers(this::getDefaultHeaders)
//                .retrieve()
//                .bodyToMono(responseType)
//                .block();
//    }
//
//    public <R> List<R> getForList(String url, Class<R> responseType) {
//        webClient
//                .get()
//                .uri(url)
//                .headers(this::getDefaultHeaders)
//                .retrieve()
//                .bo
//    }
//
//
//
//
//    private void getDefaultHeaders(HttpHeaders headers) {
//        if (headers == null) {
//            headers = new HttpHeaders();
//        }
//        headers.set("User-Agent", "PostmanRuntime/7.42.0");
//        headers.set("Connection", "keep-alive");
//        headers.set("Content-Type", "application/json");
//        headers.set("Accept", "application/json");
//    }
//}
//
//
//}
