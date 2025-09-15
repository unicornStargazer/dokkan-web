package com.hb.dokkan.service.domain.bo;

import com.hb.dokkan.service.domain.dto.base.CardBaseInfoDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description wiki card业务对象
 * @Author stargazer
 * @Date 2025/9/15 20:57
 **/
@Data
public class WikiCardBO implements Serializable {
    private static final long serialVersionUID = 4314575338046275359L;

    /**
     * 卡牌基础信息
     */
    private List<CardBaseInfoDTO> cardBaseData;
}
