package com.hb.dokkan.service.translation.client;

import com.hb.dokkan.config.translation.LlmTranslationProperties;
import com.hb.dokkan.common.domain.dto.translation.LlmTranslationResultDTO;
import com.hb.dokkan.common.utils.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** 验证 GPT 请求参数和空响应失败语义，不发送真实 HTTP 请求。 */
public class DokkanLlmTranslationClientTest {
    /** 完整响应样例：推理项在前，译文分片在后，usage 为 Responses 命名。 */
    private static final String COMPLETED_RESPONSE = """
            {"id":"test-response","status":"completed","output":[
              {"type":"reasoning","summary":[]},
              {"type":"message","role":"assistant","content":[
                {"type":"output_text","text":"气力+3，","annotations":[]},
                {"type":"output_text","text":"ATK提升100%"}]}],
             "usage":{"input_tokens":20,"output_tokens":30,"total_tokens":50,
                      "output_tokens_details":{"reasoning_tokens":10}}}
            """;

    /** 固定模型与低思考参数原样发送，不附加本地输出 token 限制或采样参数。 */
    @Test
    public void sendsLowReasoningWithoutTokenCapOrTemperature() {
        Map<String, Object> request = new DokkanLlmTranslationClient().buildRequest(
                "source", "prompt", new LlmTranslationProperties(), "gpt-5.6-luna", "low");
        assertEquals("gpt-5.6-luna", request.get("model"));
        assertEquals(Map.of("effort", "low"), request.get("reasoning"));
        assertEquals("prompt", request.get("instructions"));
        assertTrue(request.containsKey("input"));
        assertEquals(false, request.get("stream"));
        assertEquals(false, request.get("store"));
        assertFalse(request.containsKey("messages"));
        assertFalse(request.containsKey("reasoning_effort"));
        assertFalse(request.containsKey("temperature"));
        assertFalse(request.containsKey("max_tokens"));
        assertFalse(request.containsKey("max_completion_tokens"));
        assertFalse(request.containsKey("max_output_tokens"));
    }

    /** 未配置思考参数的旧模型保持原 temperature 请求方式。 */
    @Test
    public void preservesLegacyRequestParameters() {
        LlmTranslationProperties properties = new LlmTranslationProperties();
        Map<String, Object> request = new DokkanLlmTranslationClient().buildRequest(
                "source", "prompt", properties, "legacy", null);
        assertEquals(properties.getTemperature(), (Double) request.get("temperature"), 0.001);
        assertFalse(request.containsKey("reasoning_effort"));
        assertFalse(request.containsKey("reasoning"));
    }

    /** 空译文与不含候选的响应必须失败，不能成为成功率分子。 */
    @Test
    public void emptyResponseIsNotSuccessfulTranslation() throws Exception {
        DokkanLlmTranslationClient client = new DokkanLlmTranslationClient();
        Method parse = DokkanLlmTranslationClient.class.getDeclaredMethod(
                "responseResult", String.class, String.class);
        parse.setAccessible(true);
        InvocationTargetException empty = assertThrows(InvocationTargetException.class,
                () -> parse.invoke(client, "{\"status\":\"completed\",\"output\":[]}", "model"));
        assertTrue(empty.getCause() instanceof IllegalStateException);
        assertThrows(InvocationTargetException.class, () -> parse.invoke(client, "{}", "model"));
    }

    /** 未完成、失败或拒绝响应即使有文本，也不能记为成功翻译。 */
    @Test
    public void rejectsIncompleteFailedAndRefusedResponses() throws Exception {
        Method parse = DokkanLlmTranslationClient.class.getDeclaredMethod(
                "responseResult", String.class, String.class);
        parse.setAccessible(true);
        DokkanLlmTranslationClient client = new DokkanLlmTranslationClient();
        for (String status : new String[]{"incomplete", "failed", "in_progress"}) {
            assertThrows(InvocationTargetException.class, () -> parse.invoke(
                    client, COMPLETED_RESPONSE.replace("\"completed\"", "\"" + status + "\""), "model"));
        }
        assertThrows(InvocationTargetException.class, () -> parse.invoke(
                client, COMPLETED_RESPONSE.replace("\"output_text\"", "\"refusal\""), "model"));
    }

    /** 本地 HTTP 验证完整 URL 不追加路径、Bearer 鉴权、请求协议、正文和真实用量解析。 */
    @Test
    public void postsToExactResponsesEndpointAndReadsUsage() throws Exception {
        AtomicReference<String> requestPath = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> method = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestPath.set(exchange.getRequestURI().getPath());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            method.set(exchange.getRequestMethod());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = COMPLETED_RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            LlmTranslationProperties properties = new LlmTranslationProperties();
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/responses");
            properties.setApiKey("local-test-key");
            LlmTranslationResultDTO result = new DokkanLlmTranslationClient()
                    .translate("source", "prompt", properties, "gpt-5.6-luna", "low");
            assertEquals("/v1/responses", requestPath.get());
            assertEquals("POST", method.get());
            assertEquals("Bearer local-test-key", authorization.get());
            Map<String, Object> sent = JsonUtils.json2Map(requestBody.get(), String.class, Object.class);
            assertEquals(Map.of("effort", "low"), sent.get("reasoning"));
            assertEquals("gpt-5.6-luna", sent.get("model"));
            assertEquals("气力+3，ATK提升100%", result.getContent());
            assertEquals(20L, result.getUsage().getPromptTokens());
            assertEquals(30L, result.getUsage().getCompletionTokens());
            assertEquals(50L, result.getUsage().getTotalTokens());
        } finally {
            server.stop(0);
        }
    }
}
