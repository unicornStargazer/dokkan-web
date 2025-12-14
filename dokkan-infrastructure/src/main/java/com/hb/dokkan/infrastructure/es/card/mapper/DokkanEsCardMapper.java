package com.hb.dokkan.infrastructure.es.card.mapper;

import com.hb.dokkan.infrastructure.es.card.domain.CardEsPO;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.easyes.core.kernel.BaseEsMapper;

/**
 * @Description es卡片索引mapper
 * @Author stargazer
 * @Date 2025/10/18 17:34
 **/
@Mapper
public interface DokkanEsCardMapper extends BaseEsMapper<CardEsPO> {
    /**
     * 索引名称
     */
    String INDEX_NAME = "dokkan_card";

}
