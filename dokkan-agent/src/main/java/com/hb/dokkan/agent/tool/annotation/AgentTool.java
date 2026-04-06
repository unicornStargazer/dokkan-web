package com.hb.dokkan.agent.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法为 Agent 可以调用的工具。
 * <p>
 * Agent 框架在启动时会扫描所有带有此注解的方法，并自动将其注册到 {@link com.hb.dokkan.agent.tool.ToolRegistry} 中。
 * 建议在被注解的方法上提供清晰的参数注释，以供大模型理解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface  AgentTool {

    /**
     * 工具的名称，唯一标识。如果不指定，默认使用方法名。
     * 
     * @return 工具名称
     */
    String name() default "";

    /**
     * 工具的功能描述。非常重要，大模型依赖此描述来判断是否应该调用该工具。
     * 
     * @return 工具描述
     */
    String description();
}