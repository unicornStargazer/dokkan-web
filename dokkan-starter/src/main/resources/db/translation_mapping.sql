SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for translation_mapping
-- ----------------------------
DROP TABLE IF EXISTS `translation_mapping`;
CREATE TABLE `translation_mapping`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID (UUID)',
  `source_text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原文术语或短语',
  `target_text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '中文译文',
  `source_language` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'auto' COMMENT '源语言，auto 表示自动判断',
  `mapping_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CUSTOM' COMMENT '映射类型',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注说明',
  `attributes` json NULL COMMENT '扩展属性 (建议使用 JSON 类型)',
  `creator` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_translation_mapping_source_text` (`source_text`) USING BTREE,
  KEY `idx_translation_mapping_type` (`mapping_type`) USING BTREE,
  KEY `idx_translation_mapping_enabled` (`enabled`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '翻译映射表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
