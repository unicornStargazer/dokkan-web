-- 已部署卡片重构版本时单独执行；首次部署也可重复执行本脚本。
CREATE TABLE IF NOT EXISTS `kx_wiki_sync_checkpoint` (
  `id` varchar(36) NOT NULL COMMENT '同步范围主键，UUID',
  `sync_key` varchar(128) NOT NULL COMMENT '卡片同步范围，包含地区和语言',
  `source_updated_at` varchar(128) NOT NULL COMMENT 'MySQL及ES全部成功后的源版本原文',
  `creator` varchar(128) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sync_key` (`sync_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KX Wiki成功同步版本';
