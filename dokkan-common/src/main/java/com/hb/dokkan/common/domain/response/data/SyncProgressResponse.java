package com.hb.dokkan.common.domain.response.data;

import lombok.Data;

import java.io.Serializable;

/** Snapshot returned to the admin UI while a sync job is running. */
@Data
public class SyncProgressResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private volatile String jobId;
    private volatile String type;
    private volatile String status;
    private volatile String stage;
    private volatile String message;
    private volatile String error;
    private volatile int current;
    private volatile int total;
    private volatile int percent;
    private volatile long startedAt;
    private volatile long finishedAt;
}
