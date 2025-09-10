package com.hb.dokkan.service.job.sync.strategy.context;

import com.hb.dokkan.service.domain.dto.CardBaseInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/9/11 0:28
 **/
@Data
public class WikiCardBaseInfoContext extends WikiContext {

    private List<CardBaseInfoDTO> data;
}
