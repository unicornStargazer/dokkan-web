package com.hb.dokkan.config.mybatis;

import org.apache.ibatis.logging.Log;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description 仅打印SQL语句的MyBatis日志实现
 * @Author stargazer
 * @Date 2026/8/16 15:30
 **/
public class SqlOnlyStdOutImpl implements Log {

    /** MyBatis SQL 准备日志前缀，用于过滤非 SQL 查询明细。 */
    private static final String SQL_PREPARING_PREFIX = "==>  Preparing:";

    /** 控制台 SQL 输出前缀。 */
    private static final String SQL_OUTPUT_PREFIX = "SQL:";

    /**
     * 创建仅打印 SQL 的日志实现，构造签名需符合 MyBatis Log 实例化约定。
     *
     * @param clazz MyBatis 传入的 Mapper 类名
     */
    public SqlOnlyStdOutImpl(String clazz) {
    }

    /**
     * 判断是否启用 debug 日志，MyBatis SQL 准备语句通过 debug 输出。
     *
     * @return true 表示接收 debug 日志
     */
    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    /**
     * 判断是否启用 trace 日志，关闭后不输出查询列、查询行等明细。
     *
     * @return false 表示不接收 trace 日志
     */
    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    /**
     * 打印 SQL 准备语句，忽略参数、结果列、结果行和总数等查询明细。
     *
     * @param message MyBatis debug 日志内容
     */
    @Override
    public void debug(String message) {
        if (StringUtils.isBlank(message) || !message.startsWith(SQL_PREPARING_PREFIX)) {
            return;
        }
        System.out.println(SQL_OUTPUT_PREFIX + message.substring(SQL_PREPARING_PREFIX.length()));
    }

    /**
     * 忽略 trace 日志，避免输出查询结果明细。
     *
     * @param message MyBatis trace 日志内容
     */
    @Override
    public void trace(String message) {
    }

    /**
     * 打印错误日志。
     *
     * @param message 错误信息
     * @param e       原始异常
     */
    @Override
    public void error(String message, Throwable e) {
        System.err.println(message);
        e.printStackTrace(System.err);
    }

    /**
     * 打印错误日志。
     *
     * @param message 错误信息
     */
    @Override
    public void error(String message) {
        System.err.println(message);
    }

    /**
     * 打印警告日志。
     *
     * @param message 警告信息
     */
    @Override
    public void warn(String message) {
        System.out.println(message);
    }
}
