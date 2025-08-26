package com.hb.dokkan.service.job.sync.strategy.context;

import com.hb.dokkan.service.domain.sync.WikiCardDTO;
import lombok.*;

import java.util.List;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/6/2 21:10
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WikiContext {

    private List<WikiCardDTO> wikiCards;

    private String htmlContent;

}
