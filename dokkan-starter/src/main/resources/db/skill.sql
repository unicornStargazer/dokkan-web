/*
 Navicat Premium Data Transfer

 Source Server         : dokkan_admin
 Source Server Type    : MySQL
 Source Server Version : 80022 (8.0.22)
 Source Host           : 192.168.15.48:3306
 Source Schema         : dokkan_db

 Target Server Type    : MySQL
 Target Server Version : 80022 (8.0.22)
 File Encoding         : 65001

 Date: 05/01/2026 01:09:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for skill
-- ----------------------------
DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID (UUID)',
  `skill_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '技能ID (建议使用UUID或唯一标识)',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `condition_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发动条件描述',
  `effect_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '效果描述',
  `label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
  `increase_rate` int NULL DEFAULT NULL COMMENT '提升倍率 (百分比或倍数)',
  `special_category_id` int NULL DEFAULT NULL COMMENT '类别ID',
  `special_category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类别名称',
  `attributes` json NULL COMMENT '扩展属性 (建议使用 JSON 类型)',
  `creator` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill
-- ----------------------------
INSERT INTO `skill` VALUES ('1f8d7c5280904c3f', '15', '被勾起的心理创伤', '蓄力计量24以下时可发动\n(仅限1次)', '结束自身的预备模式\n且3回合内自身的气力+3、\nATK提升30%', 'sp_atk_label_a_0004_a.png', NULL, NULL, NULL, NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('2ce2021101eb4309', '23', '大魔王亲自制裁', '蓄力计量6以下时可发动\n(仅限1次)', '每1蓄力计量\n可暂时使ATK提升100%\n并对目标造成究极伤害', 'sp_atk_label_a_0004_a.png', 550, 1, '气弹系', NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('37f8db145fcd4312', '18', '羁绊之拳', '蓄力计量40以下时可发动\n(仅限1次)', '每1蓄力计量\n可暂时使ATK提升15%\n并对目标造成究极伤害', 'sp_atk_label_a_0004_a.png', 550, 2, '格斗系', NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('57900fba01de454b', '17', '被唤醒的全力', '蓄力计量25以上时可发动\n(仅限1次)', '觉醒为吉连(全力)', 'sp_atk_label_a_0004_a.png', NULL, NULL, NULL, NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('5b61df7813e34ab7', '14', '搏命的全能量释放', '蓄力计量1以上时可发动\n(仅限1次)', '每1蓄力计量\n可暂时使ATK提升100%\n并对目标造成超究极伤害\n且使其晕眩1回合 & \n与伽玛1号交替', 'sp_atk_label_a_0004_a.png', 750, NULL, NULL, NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('88ebf5cd79f44297', '19', '奇迹之拳', '蓄力计量41以上时可发动\n(仅限1次)', '每1蓄力计量\n可暂时使ATK提升20%\n并对目标造成超究极伤害', 'sp_atk_label_a_0004_a.png', 750, 2, '格斗系', NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('ce258be8e0e8484f', '25', '年轻时的力量', '蓄力计量7时可发动\n(仅限1次)', '返老还童成比克大魔王', 'sp_atk_label_a_0004_a.png', NULL, NULL, NULL, NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('d61a7368deda4116', '26', '充满气魄的界王拳攻击', '自身的复活技能发动时发动\n(仅限1次)', '以超激烈威力对将参加攻击中的我方角色\n(包含自己)KO的目标反击', 'sp_atk_label_a_0003.png', 500, 2, '格斗系', NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('f7d47194b46a419d', '10', '终极希望斩击', '蓄力计量1以上时可发动\n(仅限1次)', '每1蓄力计量\n可暂时使ATK提升50%\n并对目标造成超究极伤害', 'sp_atk_label_a_0004_a.png', 750, NULL, NULL, NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');
INSERT INTO `skill` VALUES ('f90df8291b2c4f25', '5', '超究极生命球', '自身的复活技能发动时发动\n(仅限1次)', '以超激烈威力对将参加攻击中的我方角色\n(包含自己)KO的目标反击', 'sp_atk_label_a_0003.png', 500, 1, '气弹系', NULL, 'system', '2026-01-04 16:46:23', 'system', '2026-01-04 16:46:23');

SET FOREIGN_KEY_CHECKS = 1;
