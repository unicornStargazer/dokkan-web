-- 仅回滚 challenge-boss-data.sql 新建的数据表；执行前确认不再需要已同步数据。
DROP TABLE IF EXISTS `challenge_enemy`;
DROP TABLE IF EXISTS `challenge_phase`;
DROP TABLE IF EXISTS `challenge_battle`;
DROP TABLE IF EXISTS `challenge_map`;
DROP TABLE IF EXISTS `challenge_stage`;
DROP TABLE IF EXISTS `challenge_event`;
