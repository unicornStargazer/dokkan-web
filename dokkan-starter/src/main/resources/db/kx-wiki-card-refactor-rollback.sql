-- 先停止同步并备份当前新表。保留新数据以便问题追踪。
RENAME TABLE card TO card_kx_rollback, card_state TO card_state_kx_rollback,
    card_skill TO card_skill_kx_rollback, card_category_rel TO card_category_rel_kx_rollback,
    card_link_rel TO card_link_rel_kx_rollback,
    card_legacy TO card, eza_card_legacy TO eza_card, special_legacy TO special,
    skill_legacy TO skill, translation_mapping_legacy TO translation_mapping;
