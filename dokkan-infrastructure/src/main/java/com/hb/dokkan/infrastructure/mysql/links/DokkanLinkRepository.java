package com.hb.dokkan.infrastructure.mysql.links;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hb.dokkan.common.utils.CollectionUtils;
import com.hb.dokkan.common.domain.po.mysql.link.DokkanLinkPO;
import com.hb.dokkan.infrastructure.mysql.links.mapper.DokkanLinkMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 链接数据库操作类
 * @Author stargazer
 * @Date 2025/11/8 22:12
 **/
@Repository
public class DokkanLinkRepository extends ServiceImpl<DokkanLinkMapper, DokkanLinkPO> {


    /**
     * 根据链接id列表查询链接列表
     */
    public List<DokkanLinkPO> queryLinksByIds(List<Integer> linkIds) {
        LambdaQueryWrapper<DokkanLinkPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DokkanLinkPO::getLinkId, linkIds);
        return queryLinks(wrapper);
    }

        /**
         * 根据查询条件查询链接列表
         */
        public List<DokkanLinkPO> queryLinks(LambdaQueryWrapper<DokkanLinkPO> wrapper) {
            List<DokkanLinkPO> list = this.list(wrapper);
            if (CollectionUtils.isEmpty(list)) {
                return Lists.newArrayList();
            }
            return list;
        }
}
