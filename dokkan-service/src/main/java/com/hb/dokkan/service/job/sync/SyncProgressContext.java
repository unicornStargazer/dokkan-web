package com.hb.dokkan.service.job.sync;

/**
 * @Description 同步进度上下文
 * @Author stargazer
 * @Date 2026/8/15 21:20
 **/
public final class SyncProgressContext {

    /** 当前线程绑定的进度上报器。 */
    private static final ThreadLocal<Reporter> REPORTER = new ThreadLocal<>();

    /**
     * 工具类禁止实例化。
     */
    private SyncProgressContext() {
    }

    /**
     * 将进度上报器绑定到当前同步线程。
     *
     * @param reporter 进度上报器
     */
    static void bind(Reporter reporter) {
        REPORTER.set(reporter);
    }

    /**
     * 清理当前线程绑定的进度上报器。
     */
    static void clear() {
        REPORTER.remove();
    }

    /**
     * 上报当前同步进度，未绑定上报器时忽略。
     *
     * @param percent 当前进度
     * @param stage   当前阶段
     * @param message 进度描述
     */
    public static void update(int percent, String stage, String message) {
        Reporter reporter = REPORTER.get();
        if (reporter != null) {
            reporter.update(percent, stage, message);
        }
    }

    /**
     * 同步进度上报器。
     */
    @FunctionalInterface
    interface Reporter {

        /**
         * 更新同步进度。
         *
         * @param percent 当前进度
         * @param stage   当前阶段
         * @param message 进度描述
         */
        void update(int percent, String stage, String message);
    }
}
