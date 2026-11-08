package com.hb.dokkan.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import com.hb.dokkan.infrastructure.es.card.DokkanEsCardRepository;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 验证 ES 创建、重复覆盖以及部分失败的返回语义。
 **/
public class CardEsBulkWriteTest {
    /**
     * 相同主键重复写入均成功，整文档替换不会遗留已清空的旧字段。
     **/
    @Test
    public void shouldAcceptCreatedAndRepeatedReplacedDocuments() throws Exception {
        AtomicInteger count = new AtomicInteger();
        List<String> bodies = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/_bulk", exchange -> {
            bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            boolean created = count.incrementAndGet() == 1;
            String response = "{\"took\":1,\"errors\":false,\"items\":[{\"index\":{\"_index\":\"dokkan_card\","
                    + "\"_id\":\"1\",\"status\":" + (created ? 201 : 200) + ",\"result\":\""
                    + (created ? "created" : "updated") + "\"}}]}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("X-Elastic-Product", "Elasticsearch");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        try (RestClient rest = RestClient.builder(new HttpHost("127.0.0.1", server.getAddress().getPort())).build();
             RestClientTransport transport = new RestClientTransport(rest, new JacksonJsonpMapper())) {
            DokkanEsCardRepository repository = new DokkanEsCardRepository(null, new ElasticsearchClient(transport));
            CardEsPO document = new CardEsPO();
            document.setId("1");
            document.setCardId(1L);
            for (int attempt = 0; attempt < 3; attempt++) {
                assertEquals(1, repository.saveDocuments(List.of(document)));
            }
            assertEquals(3, bodies.size());
            assertTrue(bodies.stream().allMatch(body -> body.startsWith("{\"index\":")));
        } finally {
            server.stop(0);
        }
    }

    /**
     * HTTP 成功但 bulk item 失败时，调用方必须收到失败而非成功卡数。
     **/
    @Test
    public void shouldRejectPartialBulkFailure() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/_bulk", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] bytes = ("{\"took\":1,\"errors\":true,\"items\":[{\"index\":{\"_index\":\"dokkan_card\","
                    + "\"_id\":\"1\",\"status\":400,\"error\":{\"type\":\"mapper_parsing_exception\",\"reason\":\"invalid\"}}}]}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("X-Elastic-Product", "Elasticsearch");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        try (RestClient rest = RestClient.builder(new HttpHost("127.0.0.1", server.getAddress().getPort())).build();
             RestClientTransport transport = new RestClientTransport(rest, new JacksonJsonpMapper())) {
            DokkanEsCardRepository repository = new DokkanEsCardRepository(null, new ElasticsearchClient(transport));
            CardEsPO document = new CardEsPO();
            document.setId("1");
            assertThrows(DokkanBizException.class, () -> repository.saveDocuments(List.of(document)));
        } finally {
            server.stop(0);
        }
    }
}
