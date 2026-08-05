package com.hb.dokkan.service.job.sync;

/** Propagates progress reporting through the synchronous call stack of a background job. */
public final class SyncProgressContext {
    private static final ThreadLocal<Reporter> REPORTER = new ThreadLocal<>();

    private SyncProgressContext() {
    }

    static void bind(Reporter reporter) {
        REPORTER.set(reporter);
    }

    static void clear() {
        REPORTER.remove();
    }

    public static void update(int percent, String stage, String message) {
        Reporter reporter = REPORTER.get();
        if (reporter != null) reporter.update(percent, stage, message);
    }

    @FunctionalInterface
    interface Reporter {
        void update(int percent, String stage, String message);
    }
}
