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

 Date: 05/01/2026 23:25:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for link
-- ----------------------------
DROP TABLE IF EXISTS `link`;
CREATE TABLE `link`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID (UUID)',
  `link_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '链接id (建议使用UUID或唯一标识)',
  `link_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '链接名称',
  `level_1_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '1级链接功能描述',
  `level_10_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '满级链接功能描述',
  `attributes` json NULL COMMENT '扩展属性 (建议使用 JSON 类型)',
  `creator` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '链接表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of link
-- ----------------------------
INSERT INTO `link` VALUES ('01692136b0fc4a6e', '26', '天才', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('025f95c2b1f945f1', '51', '弗力札军', 'DEF提升20%', 'ATK提升10%、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('05d80617ea6c4c08', '86', '征服的野心', 'ATK提升15%', 'ATK与DEF提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('061ffeb8eaf04366', '105', '自我修复机能', 'HP回复3%', 'HP回复5%、伤害减轻5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('082fb000d4594ede', '22', '战斗民族赛亚人', 'ATK提升5%', 'ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('08412f31e0454e10', '37', '精确的支援', 'ATK提升10%、敌方全体的DEF降低15%', 'ATK提升15%、敌方全体的DEF降低20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('0915e7fb50004094', '80', '神的次元', 'ATK提升15%', 'ATK提升15%、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('0a421d09645f487d', '88', '银河战士', 'ATK提升20%', '气力+2、ATK与DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('0bd741e28ea0419d', '107', '弱点补强', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('0c2c517b17fb4b4d', '130', '力量大会', '气力+3', '气力+3、ATK与DEF提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('0f90c225025f4811', '18', '残虐', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('105abb28813b427f', '96', '无限的再生', 'HP回复3%', '气力+2、HP回复3%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('13da3f9837234334', '128', '企鹅村的大冒险', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('141e3e61ad0e4ee0', '75', '来自异世界的访客', '气力+1', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('16a4664969ab45e6', '64', '女战士', '气力+2', '气力+3、回避率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('16c4bf9580da4cef', '31', '双胞胎', '气力+2', '气力+2、ATK、DEF、回避率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('1783d534b764462b', '121', '超宇宙', 'ATK提升20%', 'ATK提升20%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('17c7615ef31b4e02', '8', '动脑派', 'ATK与DEF提升10%', 'ATK与DEF提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('1a77dd86b06f4cc9', '57', '强袭', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('1cdd8ddf80744d1c', '97', '蓄势待发', '气力+2', '气力+2、ATK与DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('1d555fe1841d4f5d', '106', '融合', '气力+2', '气力+2、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('1f1e46b496e84900', '110', '永久能源炉', '气力+2', '气力+2、ATK与DEF及发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('1fc56e391c8546b7', '15', '英雄', 'DEF提升20%', 'DEF提升25%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('2008a0a46bbe449b', '35', '人造人', 'DEF提升10%', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('250cbfb536b14463', '41', '勇士特战队', 'ATK提升25%', 'ATK提升25%、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('25aa5c197b9f4950', '7', '魔之力', 'ATK提升20%', 'ATK提升20%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('272d0910e04f4ba1', '1000', '燃烧激战', '使敌方「暴走」无效 & ATK提升15%', '使敌方「暴走」无效 & ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('27c714f1914742e3', '28', '变身类型', 'HP回复5%', 'HP回复5%、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('2c5e105e762a460e', '101', '占卜婆婆的斗士', '气力+2', '气力+3、ATK与DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('2da5a8be328341db', '20', '新生‧天下第一武道大会', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('2e14cac5c4d54e97', '39', '冷静的判断', 'DEF提升20%', 'DEF提升25%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('2e174d0a1b684f73', '19', '来自未来的使者', 'ATK提升5%', 'ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('392b81742efc4c8b', '13', '超能力', '敌方全体的DEF降低10%', '敌方全体的DEF降低20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('396b24c1c66a432d', '108', '究极生命体的系谱', '气力+2', '气力+2、HP回复3%、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('3a7527773e3e4268', '1', '绝佳契合度', '气力+1', '气力+2、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('3ac4ca47d0de4940', '125', '传说之力', '发动必杀技时ATK提升10%', '发动必杀技时ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('3cb0077aa39a4ff9', '92', '克维拉机甲战队', 'ATK提升25%', 'ATK与DEF提升25%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('3d5563a566334b57', '72', '新生弗力札军', 'ATK与DEF提升20%', 'ATK与DEF提升25%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('3f6d82c819324c48', '100', '达列斯军团', '气力+1', '气力+2、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('3fe63ad2ecd24aed', '1002', '黄金的Z战士', '气力+2', '气力+3、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('42734d3456554ae0', '119', '恶梦', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('42a93430305e426b', '1001', '魂vs魂', '降低敌方「再生」效果 & 气力+1', '降低敌方「再生」效果 & \n气力+2、ATK与DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('47207c663b824dd1', '93', '正义的英雄', 'ATK提升25%', 'ATK提升25%、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('4870222bd31f4120', '48', '纳美克星人', 'HP回复5%', 'HP回复7%、ATK与DEF提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('4aca18929d33495e', '81', '能量吸收方程式', '气力+2', '气力+3、HP回复3%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('4b1b315da7344d7a', '67', '有机质改造', '气力+2', '气力+2、ATK与DEF及发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('4b3ae2e4521549a2', '76', '魔术师', 'ATK提升15%', 'ATK提升15%、敌方全体的DEF降低10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('50f76bda9bde49ae', '55', 'RR军', 'ATK提升10%', 'ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('542eb817598245c4', '89', '短期决战', '气力+3', '气力+3、ATK提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('56a56dd2cd654847', '4', '天真', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('5b6eaff686f64cac', '123', '巨大化', '气力+2', '气力+3、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('5c7d9a41d29e4e0e', '32', '胆小鬼', '气力+1', '气力+2、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('5d9e8e4131944816', '60', '惊异的速度', '气力+2', '气力+2、DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('5e47928bb20642a8', '40', '王室的血统', '气力+1', '气力+2、ATK提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('5f756ab0130d4472', '94', '招牌动作', '气力+2', '气力+3、ATK提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('5fb798565dd14f80', '45', 'Z战士', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('631188ac1b86456a', '116', '超越极限的姿态', '发动必杀技时ATK提升5%', '发动必杀技时ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('686c85eeb1384bb0', '30', '身经百战的战士', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('696f473552bf409f', '126', '第6宇宙的战士', '气力+2', '气力+2、ATK与DEF提升6%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('6c2e5d2c226744fb', '52', '铁血战士', 'DEF提升15%', 'DEF提升20%、伤害减轻5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('70658c38763143b9', '58', '来势汹汹的战斗力', 'ATK提升10%', 'ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('76f551a4575f47f4', '73', '忠诚心', '气力+1', '气力+2、伤害减轻5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('7a5a76176e5d402a', '63', '赛亚人的骄傲', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('7b75efa1827f4f07', '82', '希望之星', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('7c974d44e9a549f9', '36', '龟仙流', 'ATK与DEF提升10%', '气力+2、ATK与DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('7cd7fae6528d40d4', '25', '尊敬', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('833bcc87bc8c4b02', '17', '绅士', '气力+2', '气力+2、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('860e829d37644bdf', '47', '龟派气功', '发动必杀技时ATK提升5%', '发动必杀技时ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('8b0df369339e4c4d', '27', '天下第一武道大会优胜者', '气力+1', '气力+2、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('8b190d95b7ee4cf6', '98', '宇宙的破坏者', 'ATK提升25%', 'ATK提升25%、DEF提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('8cb8e0746d6d40cb', '109', '超激战', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('8cdff37a80914da8', '6', '魔之作风', '气力+1', '气力+2、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('8ecc6599b89842f3', '44', '冠军之力', '气力+1', '气力+2、伤害减轻5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('91a858f0f6cf467f', '124', '赛亚的咆哮', 'ATK提升25%', 'ATK提升25%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('920a65f6a24c4e31', '91', '克维拉的部下', '气力+1', '气力+2、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('934f961d67ca40d4', '10', '富裕', '气力+1', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('938232f98bc240c2', '59', '宇宙最凶恶', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('947deb3e4cfa4527', '43', '弗力札的手下', 'ATK提升20%', 'ATK提升20%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('967261c015984dee', '1003', '卓越的力量', 'ATK与DEF提升5%，得到「贯通」效果', 'ATK与DEF提升10%，得到「贯通」效果', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('973a64d1af9b4847', '112', '合体战士', '气力+2', '气力+2、ATK与DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('98ad17253e874d57', '127', '邪恶龙', 'ATK提升15%', 'ATK与DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('9b80a4c9e9f14080', '74', '永恒羁绊', '气力+2', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('9bfde9cd263a444a', '83', '魔人', 'ATK与DEF提升10%', '气力+2、ATK与DEF提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('9ee2a1f0cdfc4ef7', '90', '不可思议的大冒险', '气力+2', '气力+3、ATK与DEF提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('a0783b9868a4432a', '1004', '阻挡去路的高墙', '使敌方「真实之力」无效 & ATK提升15%', '使敌方「真实之力」无效 & ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('a0f3218da1214da8', '85', '宇宙最强的一族', '气力+2', '敌方全体的DEF降低10%，气力+2', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('a20486d57ae34b31', '24', '心灵相通', '气力+1', '气力+2、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('a48cf641f8a9451c', '23', '孙氏家族', 'DEF提升15%', 'DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('a7ba82414c494e80', '68', '复活的「F」', 'ATK提升10%', '气力+1、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('aa5efc450b514242', '61', '承传的意志', '气力+2', '气力+2、ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('aab0a09292854039', '111', '超强敌', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('abfb10c332d2457a', '65', '复活', '气力+2', '气力+2、HP50%以下时\nATK与DEF提升5%、HP回复5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('b0d06f6428894251', '33', '分身', '气力+1', '气力+2、回避率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('b16980bc88d54eb4', '129', '死后世界的战士', 'ATK提升20%', 'ATK提升20%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('b16a5792824e4052', '54', '联合攻击', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('b61766e9a6954478', '95', 'GT', '气力+2', '气力+2、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('b6785428ac2a475c', '71', '超神激战', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('b7ddce93f7b14590', '5', '鹤仙流', 'ATK提升15%', 'ATK提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('bfb3065d27d348a4', '53', '速度型', 'ATK提升10%', 'ATK提升15%、回避率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c04d152823974753', '104', '对贝吉达王的恨意', '气力+1', '气力+2、ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c0aecf274da1417b', '49', '狂战士', 'HP50%以下时ATK提升20%', 'HP50%以下时ATK提升30%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c11e7b25cc2b45b0', '117', '觉醒的先驱者', 'ATK提升25%', 'ATK提升25%、DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c15e4bf7712549b1', '46', '一指功', '发动必杀技时ATK提升10%', '发动必杀技时ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c1fb412576fa49de', '62', '巴达克小队', '气力+1', '气力+2、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c6f241ca89014f35', '50', '大魔王角色', 'HP80%以下时ATK与DEF提升25%', 'ATK与DEF提升25%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('c953334f42b444bc', '9', '金色战士', '敌方全体的DEF降低5%，气力+1', '敌方全体的DEF降低10%，气力+1', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('cb9b84001a754744', '14', '表里不一', 'ATK提升10%', 'ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('cc7c8a9489704008', '66', '绝望的未来', '气力+1', '气力+2、发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('ce20977bd09c4e99', '29', '超级赛亚人', 'ATK提升10%', 'ATK提升15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('d37207b6cb7d430f', '77', '魔人复活计划', '气力+2', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('d37a7007786e475d', '120', '恐怖与绝望', '气力+2', '敌方全体的DEF降低10%，气力+2', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('d3b8e76331a947fa', '21', '重生', 'ATK提升20%', 'ATK与DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('d3f63f0d33524b5f', '70', '神战士', 'ATK提升10%', 'ATK提升10%，\n发动必杀技时ATK再提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('d4e15f7882db4b15', '11', '邪恶掌权者', '气力+1', '敌方全体的DEF降低20%，气力+2', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('d9f58783fe9046a5', '115', '对赛亚人的憎恨', '气力+2', '气力+3、ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('da658cbadfc24a31', '12', '飞逃腿', 'HP30%以下时气力+1', 'HP50%以下时气力+2、回避率提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('da9eefd0d6b743ff', '102', '龙珠的指引', 'ATK提升20%', 'ATK提升20%、发动奋力一击的机率提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('db3c661f21c0413c', '3', '徒弟', 'DEF提升20%', 'DEF提升30%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('dc2d7a0dfde64cf3', '87', '海勒一族', '气力+2', '气力+2、ATK与发动奋力一击的机率提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('de6f1a722810467e', '118', '突破极限', '气力+2', '气力+2、ATK与DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('e2cf6e099e054d58', '84', '一心同体', '气力+1', '气力+2、HP回复3%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('e3e91b58c77c464f', '79', '美食', 'HP回复5%', 'HP回复7%、DEF提升7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('e8710048eb314e6f', '103', '神所赐予的力量', '发动必杀技时ATK提升5%', '发动必杀技时ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('eb0e20a01059471c', '16', '登峰造极的战士', '气力+1', '气力+2、ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('efb00055af834969', '114', '科学家', '气力+2', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('f47b1680954e4afc', '56', '尊敬的眼神', '气力+2', '气力+2、ATK与DEF提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('f8ab047461584cbb', '69', '巡逻', '气力+2', '气力+2、DEF提升20%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('f9120a537200467d', '34', '赛亚人之血', '气力+1', '气力+2、ATK与DEF提升5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('fa51cb71d1794781', '113', '合体失败', 'HP回复3%', 'HP回复7%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('fb57df782c5c40e3', '122', '超强袭', 'ATK提升20%', 'ATK提升25%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('fc587c29e2b54093', '42', '近战能手', 'ATK提升10%、敌方全体的DEF降低10%', 'ATK提升15%、敌方全体的DEF降低15%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('fcfc1386186f40b2', '2', '勇气', '气力+1', '气力+2、ATK提升10%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');
INSERT INTO `link` VALUES ('fd74a59d31cf4bde', '38', '机械型', '气力+1', '气力+2、伤害减轻5%', NULL, 'system', '2025-11-09 08:12:13', 'system', '2025-11-09 08:12:13');

SET FOREIGN_KEY_CHECKS = 1;
