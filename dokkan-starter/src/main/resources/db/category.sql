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

 Date: 05/01/2026 23:24:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID (UUID)',
  `category_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类id (建议使用UUID或唯一标识)',
  `category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `publish_time` date NULL DEFAULT NULL COMMENT '发布时间',
  `attributes` json NULL COMMENT '扩展属性 (建议使用 JSON 类型)',
  `creator` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES ('02aad38fa9b94a3d', '33', '少年、少女', '2019-05-20', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('03b4f185eb5243f7', '64', '侵蚀身心', '2021-04-01', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('0455be75d4334bc4', '42', '七龙珠英雄', '2020-01-16', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('06fa73183177493b', '1', '融合', '2017-11-05', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('09b43709cbd24c42', '53', '魔人之力', '2020-08-28', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('0fd79d15b1af4e1e', '68', '天才战士', '2021-05-31', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('10ab7c84f1474f9c', '19', '未来篇', '2018-12-11', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('16ce36d8715741c3', '22', '第7宇宙代表', '2018-09-12', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('1ba7f06ea7d5420c', '84', '超越超级赛亚人之力', '2022-07-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('1ef5aaedcd114408', '2', '邪恶龙篇', '2017-11-05', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('21ccdbb2b121474b', '24', '最凶恶的一族', '2019-04-02', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('2918e9b956f74c77', '46', '最后的杀手锏', '2020-07-08', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('2a2dcf7f66f84f5f', '40', '师徒的羁绊', '2019-12-08', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('2b0dbe9fc2f44455', '14', '纳美克星篇', '2018-07-06', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('2b53fc381ea94eaa', '54', '急遽成长', '2020-08-10', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('2b700348a2bd48c4', '4', '活泼女孩', '2017-11-05', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('2cdcc5e493e04442', '93', '魔之力', '2024-10-30', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('31bcbe73b6a2406a', '13', '巨大化', '2018-05-24', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('33c492e415b14e4c', '55', '全力奋斗', '2020-12-03', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('35502f58de334e46', '81', '被托付的意志', '2022-02-28', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('35e2ea910a704d30', '83', '赌上命运的战斗', '2022-03-31', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('382f851c50b14933', '35', '亲手足的羁绊', '2019-05-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('3a8fcbca90c9471b', '87', '亲子的羁绊', '2022-08-25', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('3ba568b081eb4364', '65', '龟仙流', '2021-07-06', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('3c0239e53dac412f', '32', '人工生命体', '2019-07-25', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('3fb9f976cc0a4205', '6', '宇宙生存篇', '2018-02-16', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('4020dcda25104408', '49', '巴达克小队', '2020-03-17', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('429bff9c9f2a4d60', '50', '暴虐无道', '2020-06-21', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('44d7e6b37c674ff6', '94', '无法控制的力量', '2024-08-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('47cec51e12cc47b5', '77', '传说的存在', '2021-08-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('4be199a405914d4f', '21', '人造人', '2018-10-03', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('4be860dc656a41c9', '88', '超HERO', '2022-12-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('4e52c1d8b44e45e5', '28', '组合', '2019-02-12', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('50e510c1c68746a3', '17', '纯粹赛亚人', '2018-10-25', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('554e57a65b5d4e18', '51', '地球人', '2020-06-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('57602fc762844a62', '80', '高速战斗', '2021-12-14', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('593b4c16284348bd', '52', '特殊姿势', '2020-11-05', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('65dbdd4def654821', '73', 'GT BOSS', '2021-04-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('68aa21e6f22a479a', '41', '恐怖的征服者', '2020-03-01', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('6a66a8b321914353', '16', '剧场版BOSS', '2018-08-12', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('6a67537b923747e5', '82', '世界的乱源', '2022-01-31', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('6b01c5f744624630', '30', '孙悟空的家谱', '2019-04-01', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('6fb78db630944568', '48', '复仇', '2020-03-17', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('7178e310fc7646f0', '95', '守护地球的英雄', '2025-01-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('7539994b8f8449a5', '31', '贝吉达的家谱', '2019-03-17', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('75a3432c149c45c7', '11', '下级战士', '2018-04-04', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('75c0eff8ce454f43', '15', '勇士特战队', '2018-07-29', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('75c4624a6a904e02', '63', '相系的希望', '2021-04-01', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('7635e530395349e5', '89', '地球长大的战士', '2023-02-15', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('772817b060474565', '12', '超级赛亚人3', '2018-05-24', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('78f1bd57b5bf454b', '43', '目标孙悟空', '2020-04-01', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('7d70bd30044846f1', '20', '力量全开', '2018-11-20', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('82113e5a95a64f75', '79', '挚友的羁绊', '2021-12-02', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('82ab7417c3374261', '78', '永远的宿敌', '2021-08-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('85440ef1bcc34c66', '71', '受传诵之人', '2021-06-20', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('917ed0f9bfd44577', '72', 'GT HERO', '2021-04-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('93ef5d7e94ea472f', '58', '智谋交锋', '2020-08-28', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('994635fdcc7845fd', '9', '魔人普乌篇', '2018-01-21', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('9998ca6a76eb4c13', '37', '劲敌', '2019-12-08', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('a0502ffd9a8746ad', '70', '正义的伙伴', '2021-04-08', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('a08e72a5e1bb4791', '36', '超级赛亚人', '2019-10-02', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('a310f45f0b77409d', '45', '超级赛亚人2', '2020-05-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('a4e87cc1fe584835', '3', '天下第一武道大会', '2017-11-05', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('ac10662f040a469a', '90', '愿望之力', '2023-03-17', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('ac2345ba79894932', '8', '神次元', '2017-12-20', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('b2efedb1ea3449d5', '74', '天界所发生的事', '2021-10-05', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('b3329deab7e34c1b', '76', '领悟到的进化', '2022-01-02', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('b5efb26a797d4496', '75', '时间限制', '2021-11-03', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('b8fe75324c4546a2', '29', '剧场版HERO', '2019-01-31', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('ba96d45aaa974931', '38', '人造人/赛鲁篇', '2019-08-26', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('bb2a0ed227004e0a', '91', '大会参赛者', '2024-01-31', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('bc7e8961bf1e4f78', '25', '龙珠追寻者', '2019-02-25', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('be534e488d924b41', '86', '赛亚人篇', '2022-06-19', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('be8fca2759624f1f', '97', '执行任务', '2025-05-29', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c01f687309bb47d0', '56', '第11宇宙', '2021-01-17', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c1e08a8f72a44888', '57', '救世主', '2020-10-04', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c62a4054c99e486c', '60', '巨猿力量', '2020-11-18', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c641962bd6f644ee', '85', '融合/合体战士', '2022-07-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c89f092ea66e43d5', '96', '继承者', '2025-01-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c8f2ae5232694573', '62', '纵横宇宙的战士', '2021-02-28', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('c90ea351896448c1', '26', '超越时空之人', '2019-01-31', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('d07da440bb5a42d7', '92', '超BOSS', '2024-02-20', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('d12a8a56e5ba4947', '69', '行星破坏', '2021-03-17', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('d2f1e26bbfa64819', '66', '奇迹的觉醒', '2021-07-08', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('d75f7c975918485c', '47', '愤怒爆发', '2020-06-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('d994d233f1474068', '67', '再起的力量', '2021-07-08', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('dc17145686e94cdf', '59', '吸收力量', '2020-08-28', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('dce20c88ef3849a9', '44', '死后世界的战士', '2020-05-07', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('ddec9cea213d404d', '23', '变身强化', '2019-01-15', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('de33222796454d1c', '10', '波特拉', '2018-03-01', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('e2ef1b3108614a28', '7', '复活战士', '2018-03-14', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('e564294f64c04b4e', '61', '跨界', '2020-11-18', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('e64bd9449e624a8e', '39', '龟派气功', '2019-08-28', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('e7a4c2dd83924ce6', '18', '纳美克星人', '2018-08-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('eee83344f9e5447f', '5', '混血赛亚人', '2017-12-27', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('f0832c8d405c4f66', '34', '少年篇', '2019-05-20', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');
INSERT INTO `category` VALUES ('fb4b16d872574585', '27', '第6宇宙', '2018-12-23', NULL, 'system', '2025-11-09 07:42:32', 'system', '2025-11-09 07:42:32');

SET FOREIGN_KEY_CHECKS = 1;
