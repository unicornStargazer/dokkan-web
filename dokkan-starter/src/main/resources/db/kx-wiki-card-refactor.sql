-- KX Wiki 卡片表重构上线脚本。执行前请使用 mysqldump 备份当前数据库。
SET FOREIGN_KEY_CHECKS = 0;
RENAME TABLE card TO card_legacy,
    eza_card TO eza_card_legacy,
    special TO special_legacy,
    skill TO skill_legacy,
    translation_mapping TO translation_mapping_legacy;

CREATE TABLE `card` (
  `id` varchar(36) NOT NULL,
  `card_id` bigint NOT NULL,
  `card_name` varchar(255) NOT NULL,
  `unit_name` varchar(255) DEFAULT NULL,
  `title` varchar(512) DEFAULT NULL,
  `character_id` int DEFAULT NULL,
  `rarity` int NOT NULL,
  `element` smallint NOT NULL,
  `type` int NOT NULL,
  `prop_type` varchar(50) NOT NULL,
  `cost` int NOT NULL,
  `resource_id` bigint DEFAULT NULL,
  `base_card_id` bigint DEFAULT NULL,
  `form_kind` varchar(20) DEFAULT NULL,
  `form_trigger` varchar(1024) DEFAULT NULL,
  `publish_time` datetime DEFAULT NULL,
  `eza_flag` tinyint(1) NOT NULL DEFAULT 0,
  `seza_flag` tinyint(1) NOT NULL DEFAULT 0,
  `dokkan_fes_flag` tinyint(1) NOT NULL DEFAULT 0,
  `carnival_flag` tinyint(1) NOT NULL DEFAULT 0,
  `free_card_flag` tinyint(1) NOT NULL DEFAULT 0,
  `tag_locked` tinyint(1) NOT NULL DEFAULT 0,
  `thumbnail` varchar(512) DEFAULT NULL,
  `potential` json DEFAULT NULL,
  `source_hash` char(64) DEFAULT NULL,
  `synced_time` datetime DEFAULT NULL,
  `attributes` json DEFAULT NULL,
  `creator` varchar(128) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifier` varchar(128) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_card_id` (`card_id`), KEY `idx_base_card_id` (`base_card_id`), KEY `idx_rarity_cost` (`rarity`,`cost`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KX Wiki 卡片主表';

CREATE TABLE `card_state` (
  `id` varchar(36) NOT NULL, `card_id` bigint NOT NULL, `state_key` varchar(10) NOT NULL, `step` tinyint NOT NULL DEFAULT 0,
  `lv_max` int DEFAULT NULL, `sa_lv_max` int DEFAULT NULL, `hp_value` bigint DEFAULT NULL, `atk_value` bigint DEFAULT NULL, `def_value` bigint DEFAULT NULL,
  `leader_skill_id` bigint DEFAULT NULL, `leader_skill_name` varchar(255) DEFAULT NULL, `leader_skill_desc` varchar(1024) DEFAULT NULL,
  `passive_skill_id` bigint DEFAULT NULL, `passive_skill_name` varchar(255) DEFAULT NULL, `passive_skill_desc` text,
  `publish_time` datetime DEFAULT NULL, `attributes` json DEFAULT NULL, `creator` varchar(128) DEFAULT NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifier` varchar(128) DEFAULT NULL, `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_card_state` (`card_id`,`state_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KX Wiki 卡片状态';

CREATE TABLE `card_skill` (
  `id` varchar(36) NOT NULL, `card_id` bigint NOT NULL, `state_key` varchar(10) NOT NULL, `skill_type` varchar(16) NOT NULL, `skill_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL, `style` varchar(20) DEFAULT NULL, `ki` int DEFAULT NULL, `condition_desc` varchar(1024) DEFAULT NULL, `effect_desc` text,
  `increase_rate` int DEFAULT NULL, `lv_bonus` int DEFAULT NULL, `attack_category_id` int DEFAULT NULL, `attack_category_name` varchar(128) DEFAULT NULL,
  `sort` tinyint NOT NULL DEFAULT 0, `attributes` json DEFAULT NULL, `creator` varchar(128) DEFAULT NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifier` varchar(128) DEFAULT NULL, `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_card_skill` (`card_id`,`state_key`,`skill_type`,`skill_id`), KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KX Wiki 卡片技能';

CREATE TABLE `card_category_rel` (
  `id` varchar(36) NOT NULL, `card_id` bigint NOT NULL, `category_id` bigint NOT NULL, `creator` varchar(128) DEFAULT NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifier` varchar(128) DEFAULT NULL, `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_card_category` (`card_id`,`category_id`), KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡片分类关系';

CREATE TABLE `card_link_rel` (
  `id` varchar(36) NOT NULL, `card_id` bigint NOT NULL, `link_id` bigint NOT NULL, `sort` tinyint NOT NULL DEFAULT 0, `creator` varchar(128) DEFAULT NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifier` varchar(128) DEFAULT NULL, `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_card_link` (`card_id`,`link_id`), KEY `idx_link_id` (`link_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡片链接关系';

SET FOREIGN_KEY_CHECKS = 1;
