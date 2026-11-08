package com.hb.dokkan.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.hb.dokkan.common.domain.po.es.cards.CardEsPO;
import com.hb.dokkan.infrastructure.es.card.mapper.DokkanEsCardMapper;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpHost;
import org.dromara.easyes.common.property.GlobalConfig;
import org.dromara.easyes.core.biz.EntityInfo;
import org.dromara.easyes.core.cache.GlobalConfigCache;
import org.dromara.easyes.core.toolkit.EntityInfoHelper;
import org.dromara.easyes.core.toolkit.IndexUtils;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 在本地模拟 ES，验证同一实体元数据可用于重复创建索引，无需连接真实集群。
 **/
public class RepeatedIndexCreationTest {

    /**
     * 重用元数据连续构造并提交两次建索引请求，settings 与嵌套 mapping 均应保留。
     **/
    @Test
    public void cachedEntityMetadataShouldSupportRepeatedIndexCreation() throws Exception {
        List<String> requests = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/" + DokkanEsCardMapper.INDEX_NAME, exchange -> {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = ("{\"acknowledged\":true,\"shards_acknowledged\":true,\"index\":\""
                    + DokkanEsCardMapper.INDEX_NAME + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("X-Elastic-Product", "Elasticsearch");
            exchange.sendResponseHeaders(200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            }
        });
        GlobalConfig previousConfig = GlobalConfigCache.getGlobalConfig();
        EntityInfo previousEntity = EntityInfoHelper.ENTITY_INFO_CACHE.remove(CardEsPO.class);
        GlobalConfig config = new GlobalConfig();
        config.setPrintDsl(false);
        GlobalConfigCache.setGlobalConfig(config);
        server.start();
        try (RestClient restClient = RestClient.builder(new HttpHost("127.0.0.1",
                server.getAddress().getPort())).build();
             RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper())) {
            ElasticsearchClient client = new ElasticsearchClient(transport);
            EntityInfo entity = EntityInfoHelper.initIndexInfo(config, CardEsPO.class);
            // 两次使用同一份实体缓存，复现全量重建时再次 createIndex 的调用路径。
            assertTrue(IndexUtils.createIndex(client, entity, IndexUtils.getCreateIndexParam(entity, CardEsPO.class)));
            assertTrue(IndexUtils.createIndex(client, entity, IndexUtils.getCreateIndexParam(entity, CardEsPO.class)));
            assertEquals(2, requests.size());
            assertEquals(requests.get(0), requests.get(1));
            assertTrue(requests.getFirst().contains("comma_analyzer"));
            assertTrue(requests.getFirst().contains("specialSkills"));
        } finally {
            server.stop(0);
            GlobalConfigCache.setGlobalConfig(previousConfig);
            EntityInfoHelper.ENTITY_INFO_CACHE.remove(CardEsPO.class);
            if (previousEntity != null) {
                EntityInfoHelper.ENTITY_INFO_CACHE.put(CardEsPO.class, previousEntity);
            }
        }
    }
}
