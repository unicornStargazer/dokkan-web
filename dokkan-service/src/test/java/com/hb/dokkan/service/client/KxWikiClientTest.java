package com.hb.dokkan.service.client;

import com.hb.dokkan.config.kx.KxWikiProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 本地 HTTP mock 验证客户端重试、限速、分页失败及繁转简边界。
 **/
public class KxWikiClientTest {
    /**
     * 请求超时也受同一重试上限约束，最终返回空结果。
     **/
    @Test
    public void shouldRetryTimeoutWithinConfiguredLimit() {
        AtomicInteger attempts = new AtomicInteger();
        KxWikiProperties properties = new KxWikiProperties();
        properties.setPermitsPerSecond(100D);
        properties.setTimeoutMills(20L);
        properties.setRetryTimes(2);
        WebClient webClient = WebClient.builder().exchangeFunction(request -> reactor.core.publisher.Mono.defer(() -> {
            attempts.incrementAndGet();
            return reactor.core.publisher.Mono.never();
        })).build();
        assertTrue(new KxWikiClient(webClient, properties).getStatus().isEmpty());
        assertEquals(3, attempts.get());
    }

    /**
     * 429 优先采用 Retry-After，成功响应中的中文被转换而 URL 保持原值。
     **/
    @Test
    public void shouldRetryRateLimitAndSimplifyDecodedText() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = server();
        server.createContext("/", exchange -> {
            int attempt = requests.incrementAndGet();
            exchange.getResponseHeaders().set("Retry-After", "0");
            byte[] response = (attempt == 1 ? "{}" : "{\"id\":1,\"name\":\"\\u8c9d\\u5409\\u9054\","
                    + "\"thumbnail\":\"https://example.test/貝吉達.png\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(attempt == 1 ? 429 : 200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            }
        });
        server.start();
        try {
            var detail = client(server, 100D, 2).getCardDetail(1L).orElseThrow();
            assertEquals(2, requests.get());
            assertEquals("贝吉达", detail.getName());
            assertEquals("https://example.test/貝吉達.png", detail.getThumbnail());
        } finally {
            server.stop(0);
        }
    }

    /**
     * 5xx 按配置重试；404 不重试，且分页失败不得伪装成空列表。
     **/
    @Test
    public void shouldBoundRetriesAndRejectFailedPage() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        AtomicInteger status = new AtomicInteger(503);
        HttpServer server = server();
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        try {
            KxWikiClient client = client(server, 100D, 2);
            assertTrue(client.getStatus().isEmpty());
            assertEquals(3, requests.get());
            requests.set(0);
            status.set(404);
            assertTrue(client.getStatus().isEmpty());
            assertEquals(1, requests.get());
            assertThrows(RuntimeException.class, () -> client.listCards(1));
            assertEquals(2, requests.get());
        } finally {
            server.stop(0);
        }
    }

    /**
     * 不同接口复用同一个客户端限速器，每次实际请求都消耗许可。
     **/
    @Test
    public void endpointsShouldShareRateLimiter() throws Exception {
        List<Long> timestamps = new ArrayList<>();
        HttpServer server = server();
        server.createContext("/", exchange -> {
            timestamps.add(System.nanoTime());
            byte[] response = "{\"updated_at\":\"version\",\"card_id\":1}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            }
        });
        server.start();
        try {
            KxWikiClient client = client(server, 2D, 0);
            client.getStatus();
            client.getEnrichment(1L);
            client.getExtras(1L);
            assertEquals(3, timestamps.size());
            // 允许运行时调度误差，但三个请求不能绕过每秒两次的共享限制。
            assertTrue((timestamps.getLast() - timestamps.getFirst()) / 1_000_000L >= 800L);
        } finally {
            server.stop(0);
        }
    }

    /**
     * 挑战接口使用源站 group 参数筛选目录，并返回活动及地图原始层级。
     */
    @Test
    public void shouldRequestChallengeCatalogEventAndMap() throws Exception {
        List<String> requests = new ArrayList<>();
        HttpServer server = server();
        server.createContext("/", exchange -> {
            requests.add(exchange.getRequestURI().toString());
            String path = exchange.getRequestURI().getPath();
            String response;
            if ("/api/vanilla-events".equals(path)) {
                response = "{\"events\":[{\"id\":1705,\"group\":5}],\"next\":null}";
            } else if ("/api/vanilla-events/events/1705".equals(path)) {
                response = "{\"event\":{\"id\":1705,\"stages\":[{\"id\":1,\"maps\":[{\"id\":2}]}]}}";
            } else {
                response = "{\"event_id\":1705,\"map\":{\"id\":2},\"battles\":[]}";
            }
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        try {
            KxWikiClient client = client(server, 100D, 0);
            java.util.Map<String, Object> catalog = client.listChallengeEvents(3);
            List<?> events = (List<?>) catalog.get("events");
            assertEquals(5, ((Number) ((java.util.Map<?, ?>) events.getFirst()).get("group")).intValue());
            java.util.Map<?, ?> event = (java.util.Map<?, ?>) client.getChallengeEvent(1705).get("event");
            assertEquals(1, ((List<?>) event.get("stages")).size());
            assertEquals(1705L, ((Number) client.getChallengeMap(2).get("event_id")).longValue());
            assertTrue(requests.getFirst().contains("group=5"));
            assertTrue(requests.getFirst().contains("page=3"));
            assertTrue(requests.get(1).contains("/events/1705"));
            assertTrue(requests.get(2).contains("/maps/2"));
        } finally {
            server.stop(0);
        }
    }

    /**
     * 创建随机本地端口的 mock 服务，避免依赖固定端口或外部网络。
     **/
    private HttpServer server() throws Exception {
        return HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    }

    /**
     * 构建测试客户端，真实经过 HTTP 和响应解析流程。
     **/
    private KxWikiClient client(HttpServer server, double permits, int retries) {
        KxWikiProperties properties = new KxWikiProperties();
        properties.setPermitsPerSecond(permits);
        properties.setRetryTimes(retries);
        WebClient webClient = WebClient.builder().baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
        return new KxWikiClient(webClient, properties);
    }
}
