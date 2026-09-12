package com.hb.dokkan.common.constants;

/** Responses 请求与响应协议字段。 */
public final class LlmResponsesConstants {
    /** 输入消息字段。 */
    public static final String INPUT = "input";
    /** 系统翻译指令字段。 */
    public static final String INSTRUCTIONS = "instructions";
    /** 推理配置对象。 */
    public static final String REASONING = "reasoning";
    /** 推理程度字段。 */
    public static final String EFFORT = "effort";
    /** 是否以 SSE 流式返回，本客户端明确使用非流式响应。 */
    public static final String STREAM = "stream";
    /** 是否存储响应，本客户端不需要服务端保存会话。 */
    public static final String STORE = "store";
    /** 完成状态；中断、失败或处理中均不可视作翻译成功。 */
    public static final String COMPLETED = "completed";
    /** 输出消息类型。 */
    public static final String MESSAGE = "message";
    /** 助手角色。 */
    public static final String ASSISTANT = "assistant";
    /** 输出文本片段类型。 */
    public static final String OUTPUT_TEXT = "output_text";
    /** 模型拒绝类型。 */
    public static final String REFUSAL = "refusal";
    /** 未完成或拒绝响应的统一错误，不包含上游正文及密钥。 */
    public static final String INVALID_RESPONSE = "LLM Responses response not completed or refused";

    /** 常量类不允许实例化。 */
    private LlmResponsesConstants() {
    }
}
