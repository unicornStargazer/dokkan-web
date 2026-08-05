package com.hb.dokkan.common.domain.request.cards;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Request for manually synchronizing cards by their project card IDs.
 */
@Data
public class CardIdSyncRequest implements Serializable {
    private static final long serialVersionUID = 1392758263980969060L;

    private List<Long> cardIds;
}
