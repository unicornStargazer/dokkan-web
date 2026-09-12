package com.hb.dokkan.common.domain.dto.translation;

import java.util.List;

/**
 * 模型额度快照；remainingTokens 是快照时的剩余额度，不是历史累计额度。
 *
 * @param snapshotId 账期标识，重新校准全部余额时更换
 * @param timeZone 到期日期采用的时区
 * @param models 模型列表，同日到期时按此顺序选择
 */
public record LlmModelCatalogDTO(String snapshotId, String timeZone, List<ModelQuota> models) {

    /**
     * 单模型剩余额度和有效期。
     *
     * @param model 服务商模型 ID
     * @param remainingTokens 快照时剩余 token 数
     * @param expiresOn 最后有效日期，ISO yyyy-MM-dd，当天结束后失效
     * @param enabled 是否允许选择
     * @param unlimited 是否跳过本地 token 及到期限制，省略时为 false
     * @param reasoningEffort 可选思考程度，配置后替代 temperature 参数
     */
    public record ModelQuota(String model, Long remainingTokens, String expiresOn, Boolean enabled,
                             Boolean unlimited, String reasoningEffort) {
        /** 兼容已有有限额度配置构造，默认不启用无限额度和思考参数。 */
        public ModelQuota(String model, Long remainingTokens, String expiresOn, Boolean enabled) {
            this(model, remainingTokens, expiresOn, enabled, false, null);
        }
    }
}
