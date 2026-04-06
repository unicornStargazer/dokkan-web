package com.hb.dokkan.agent.model.router;

import com.hb.dokkan.agent.model.provider.BaseModelProvider;
import com.hb.dokkan.agent.model.schema.ChatChunk;
import com.hb.dokkan.agent.model.schema.ChatMessage;
import com.hb.dokkan.agent.model.schema.ChatResponse;
import com.hb.dokkan.agent.model.schema.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型路由器
 * 负责管理各种模型提供者（Provider），并根据配置或请求进行动态路由
 */
@Slf4j
@Component
public class ModelRouter {

    /**
     * 模型提供者注册表，Key 为提供者名称，Value 为具体实现类
     */
    private final Map<String, BaseModelProvider> providers = new ConcurrentHashMap<>();

    /**
     * 默认的模型提供者名称，从配置中读取
     */
    @Value("${agent.default-provider:bailian}")
    private String defaultProviderName;

    /**
     * 构造函数，利用 Spring 的依赖注入自动收集所有的模型提供者
     *
     * @param providerList 所有实现了 BaseModelProvider 接口的 Bean
     */
    public ModelRouter(List<BaseModelProvider> providerList) {
        for (BaseModelProvider provider : providerList) {
            providers.put(provider.getName(), provider);
            log.info("Registered Model Provider: {}", provider.getName());
        }
    }

    /**
     * 获取指定的模型提供者
     *
     * @param name 提供者名称，如果为空则使用默认提供者
     * @return 模型提供者实例
     * @throws IllegalArgumentException 当指定的提供者不存在时抛出异常
     */
    public BaseModelProvider getProvider(String name) {
        String actualName = (name != null && !name.isEmpty()) ? name : defaultProviderName;
        BaseModelProvider provider = providers.get(actualName);
        if (provider == null) {
            throw new IllegalArgumentException("Model Provider not found: " + actualName);
        }
        return provider;
    }

    /**
     * 路由并执行同步对话
     *
     * @param providerName 目标提供者名称
     * @param messages     对话历史消息
     * @param tools        可用工具定义
     * @param model        指定模型名称
     * @param kwargs       其他扩展参数
     * @return 对话响应
     */
    public ChatResponse chat(String providerName, List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        return getProvider(providerName).chat(messages, tools, model, kwargs);
    }

    /**
     * 路由并执行流式对话
     *
     * @param providerName 目标提供者名称
     * @param messages     对话历史消息
     * @param tools        可用工具定义
     * @param model        指定模型名称
     * @param kwargs       其他扩展参数
     * @return 流式对话数据流
     */
    public Flux<ChatChunk> streamChat(String providerName, List<ChatMessage> messages, List<ToolDefinition> tools, String model, Map<String, Object> kwargs) {
        return getProvider(providerName).streamChat(messages, tools, model, kwargs);
    }
}