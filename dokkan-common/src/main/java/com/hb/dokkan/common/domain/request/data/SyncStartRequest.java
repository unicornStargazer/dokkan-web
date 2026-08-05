package com.hb.dokkan.common.domain.request.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** Starts an asynchronous data synchronization job. */
@Data
public class SyncStartRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** mysql, es or manual. */
    private String type;
    private List<Long> cardIds;
}
