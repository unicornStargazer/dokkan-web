package com.hb.dokkan.infrastructure.mysql.challenges;

import com.hb.dokkan.common.utils.JsonUtils;
import com.hb.dokkan.common.utils.DateUtils;
import com.hb.dokkan.common.constants.CardSyncConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @Description 活动挑战数据持久化访问类
 * @Author Codex
 * @Date 2026/11/9
 **/
@Repository
public class ChallengeRepository {

    /**
     * 挑战域数据库访问模板。
     **/
    private final JdbcTemplate jdbcTemplate;

    /**
     * 注入挑战域数据库访问模板。
     *
     * @param jdbcTemplate Spring JDBC 模板
     */
    public ChallengeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 幂等保存活动、关卡和难度目录；源 payload 用于兼容未建模字段。
     *
     * @param region 源地区
     * @param language 源语言
     * @param event 活动目录记录
     * @param detail 活动详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDirectory(String region, String language, Map<String, Object> event,
                              Map<String, Object> detail) {
        Map<String, Object> sourceEvent = mapValue(detail.get("event"));
        long eventId = longValue(event.get("id"));
        String eventRecordId = findOrCreateEvent(region, language, event, sourceEvent, eventId);
        jdbcTemplate.update("UPDATE challenge_stage SET source_present=0 WHERE event_record_id=?", eventRecordId);
        jdbcTemplate.update("UPDATE challenge_map SET source_present=0 WHERE stage_record_id IN " +
                "(SELECT id FROM challenge_stage WHERE event_record_id=?)", eventRecordId);
        List<?> stages = listValue(sourceEvent.get("stages"));
        for (int stageIndex = 0; stageIndex < stages.size(); stageIndex++) {
            Map<String, Object> stage = mapValue(stages.get(stageIndex));
            String stageRecordId = saveStage(region, language, eventRecordId, stage, stageIndex);
            List<?> maps = listValue(stage.get("maps"));
            for (int mapIndex = 0; mapIndex < maps.size(); mapIndex++) {
                saveMapDirectory(region, language, stageRecordId,
                        mapValue(maps.get(mapIndex)), mapIndex);
            }
        }
    }

    /**
     * 在单地图事务中原子替换战斗树并更新有效快照状态。
     *
     * @param region 源地区
     * @param language 源语言
     * @param mapId 源难度地图 ID
     * @param detail 地图详情
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceMapSnapshot(String region, String language, long mapId, Map<String, Object> detail) {
        String mapRecordId = findMapId(region, language, mapId);
        String sourceHash = JsonUtils.sourceHash(JsonUtils.object2Json(detail));
        String existingHash = jdbcTemplate.queryForObject(
                "SELECT source_hash FROM challenge_map WHERE id = ?", String.class, mapRecordId);
        if (Objects.equals(sourceHash, existingHash)) {
            jdbcTemplate.update("""
                    UPDATE challenge_map SET last_sync_status='success', last_checked_at=UTC_TIMESTAMP(),
                    last_error=NULL WHERE id=?
                    """, mapRecordId);
            return false;
        }
        List<Object> battles = listValue(detail.get("battles"));
        jdbcTemplate.update("""
                DELETE FROM challenge_enemy WHERE phase_record_id IN (
                    SELECT phase.id FROM challenge_phase phase
                    INNER JOIN challenge_battle battle ON battle.id = phase.battle_record_id
                    WHERE battle.map_record_id = ?)
                """, mapRecordId);
        jdbcTemplate.update("""
                DELETE FROM challenge_phase WHERE battle_record_id IN (
                    SELECT id FROM challenge_battle WHERE map_record_id = ?)
                """, mapRecordId);
        jdbcTemplate.update("DELETE FROM challenge_battle WHERE map_record_id = ?", mapRecordId);
        for (int battleIndex = 0; battleIndex < battles.size(); battleIndex++) {
            Map<String, Object> battle = mapValue(battles.get(battleIndex));
            String battleRecordId = UUID.randomUUID().toString();
            jdbcTemplate.update("""
                    INSERT INTO challenge_battle
                    (id, map_record_id, battle_index, battle_number, space, boss_flag, attributes)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, battleRecordId, mapRecordId, battleIndex, intValue(battle.get("number")),
                    stringValue(battle.get("space")), booleanValue(battle.get("boss")), json(battle));
            List<Object> rounds = listValue(battle.get("rounds"));
            for (int phaseIndex = 0; phaseIndex < rounds.size(); phaseIndex++) {
                savePhase(battleRecordId, phaseIndex, mapValue(rounds.get(phaseIndex)));
            }
        }
        jdbcTemplate.update("""
                UPDATE challenge_map SET source_available = ?, source_partial = ?, capture_status = ?,
                data_status = ?, last_sync_status = ?, phase_grouping_confirmed = ?, display_type = ?,
                source_hash = ?, source_payload = ?, last_observation = ?, last_checked_at = UTC_TIMESTAMP(),
                synced_time = UTC_TIMESTAMP(), last_error = NULL WHERE id = ?
                """, booleanValue(detail.get("available")), booleanValue(detail.get("partial")),
                stringValue(detail.get("capture_status")), Boolean.TRUE.equals(detail.get("partial"))
                        ? CardSyncConstants.CHALLENGE_DATA_STATUS_PARTIAL
                        : CardSyncConstants.CHALLENGE_DATA_STATUS_CAPTURED,
                CardSyncConstants.CHALLENGE_MAP_SYNC_STATUS_SUCCESS,
                booleanValue(detail.get("phase_grouping_confirmed")), stringValue(detail.get("display_type")),
                sourceHash, json(without(detail, "battles")), json(detail), mapRecordId);
        return true;
    }

    /**
     * 记录一次源站未采集观察，保留已存战斗快照。
     *
     * @param region 源地区
     * @param language 源语言
     * @param mapId 源地图 ID
     * @param detail 源地图响应
     */
    public void recordNotCaptured(String region, String language, long mapId, Map<String, Object> detail) {
        jdbcTemplate.update("""
                UPDATE challenge_map SET source_available = ?, source_partial = ?, capture_status = ?,
                last_sync_status = ?, last_observation = ?, last_checked_at = UTC_TIMESTAMP(),
                last_error = NULL WHERE region = ? AND language = ? AND map_id = ?
                """, booleanValue(detail.get("available")), booleanValue(detail.get("partial")),
                stringValue(detail.get("capture_status")), CardSyncConstants.CHALLENGE_MAP_SYNC_STATUS_SUCCESS,
                json(detail), region, language, mapId);
    }

    /**
     * 记录地图请求或校验失败，同时保留地图目录和当前敌人快照。
     *
     * @param region 源地区
     * @param language 源语言
     * @param mapId 源地图 ID
     * @param error 错误摘要
     */
    public void recordMapFailure(String region, String language, long mapId, String error) {
        jdbcTemplate.update("""
                UPDATE challenge_map SET last_sync_status = ?, last_checked_at = UTC_TIMESTAMP(),
                last_error = ? WHERE region = ? AND language = ? AND map_id = ?
                """, CardSyncConstants.CHALLENGE_MAP_SYNC_STATUS_FAILED, error, region, language, mapId);
    }

    /**
     * 查询活动分页及每个活动下已采集地图数量。
     *
     * @param region 源地区
     * @param language 源语言
     * @param keyword 活动名称关键字
     * @param dataStatus 快照状态筛选
     * @param offset 起始行
     * @param limit 返回行数
     * @return 活动分页记录
     */
    public List<Map<String, Object>> listEvents(String region, String language, String keyword,
                                                String dataStatus, int offset, int limit) {
        String statusClause = dataStatus == null ? "" :
                " HAVING SUM(CASE WHEN m.data_status = ? THEN 1 ELSE 0 END) > 0";
        String sql = """
                SELECT e.event_id AS eventId, e.event_name AS eventName, e.order_by_time AS orderByTime,
                e.stage_count AS stageCount, COUNT(m.id) AS mapCount,
                SUM(CASE WHEN m.data_status IN (?, ?) THEN 1 ELSE 0 END) AS capturedCount,
                MAX(m.last_checked_at) AS lastCheckedAt, MAX(m.last_error) AS lastError
                FROM challenge_event e LEFT JOIN challenge_stage s ON s.event_record_id = e.id
                LEFT JOIN challenge_map m ON m.stage_record_id = s.id AND m.source_present = 1
                WHERE e.region = ? AND e.language = ? AND e.source_present = 1 AND e.event_name LIKE ?
                """ + " GROUP BY e.id" + statusClause +
                " ORDER BY e.order_by_time DESC, e.event_id DESC LIMIT ? OFFSET ?";
        List<Object> args = new ArrayList<>(List.of(CardSyncConstants.CHALLENGE_DATA_STATUS_CAPTURED,
                CardSyncConstants.CHALLENGE_DATA_STATUS_PARTIAL, region, language, "%" + keyword + "%"));
        if (dataStatus != null) {
            args.add(dataStatus);
        }
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.queryForList(sql, args.toArray());
    }

    /**
     * 统计活动挑战列表符合筛选条件的记录数。
     *
     * @param region 源地区
     * @param language 源语言
     * @param keyword 活动名称关键字
     * @param dataStatus 地图快照状态筛选
     * @return 活动总数
     */
    public long countEvents(String region, String language, String keyword, String dataStatus) {
        if (dataStatus == null) {
            String sql = "SELECT COUNT(DISTINCT e.id) FROM challenge_event e "
                    + "LEFT JOIN challenge_stage s ON s.event_record_id=e.id "
                    + "LEFT JOIN challenge_map m ON m.stage_record_id=s.id AND m.source_present=1 "
                    + "WHERE e.region=? AND e.language=? AND e.source_present=1 AND e.event_name LIKE ?";
            return jdbcTemplate.queryForObject(sql, Long.class, region, language, "%" + keyword + "%");
        }
        String sql = "SELECT COUNT(*) FROM (SELECT e.id FROM challenge_event e "
                + "LEFT JOIN challenge_stage s ON s.event_record_id=e.id "
                + "LEFT JOIN challenge_map m ON m.stage_record_id=s.id AND m.source_present=1 "
                + "WHERE e.region=? AND e.language=? AND e.source_present=1 AND e.event_name LIKE ? "
                + "GROUP BY e.id HAVING SUM(CASE WHEN m.data_status=? THEN 1 ELSE 0 END)>0) filtered_events";
        return jdbcTemplate.queryForObject(sql, Long.class, region, language, "%" + keyword + "%", dataStatus);
    }

    /**
     * 查询活动目录下按源顺序排列的所有关卡和难度地图。
     *
     * @param region 源地区
     * @param language 源语言
     * @param eventId 活动 ID
     * @return 活动和关卡地图目录
     */
    public Map<String, Object> getEventDetail(String region, String language, long eventId) {
        Map<String, Object> event = jdbcTemplate.queryForMap(
                "SELECT event_id AS eventId, event_name AS eventName, release_time AS releaseTime, " +
                        "latest_stage_time AS latestStageTime, stage_count AS stageCount " +
                        "FROM challenge_event WHERE region = ? AND language = ? AND event_id = ?",
                region, language, eventId);
        List<Map<String, Object>> stages = jdbcTemplate.queryForList(
                "SELECT stage_id AS stageId, stage_name AS stageName, display_order AS displayOrder " +
                        "FROM challenge_stage WHERE event_record_id = (SELECT id FROM challenge_event " +
                        "WHERE region=? AND language=? AND event_id=?) AND source_present=1 ORDER BY display_order",
                region, language, eventId);
        for (Map<String, Object> stage : stages) {
            stage.put("maps", jdbcTemplate.queryForList(
                    "SELECT map_id AS mapId, difficulty_name AS difficultyName, act, display_order AS displayOrder, " +
                            "capture_status AS captureStatus, data_status AS dataStatus, last_sync_status AS lastSyncStatus, " +
                            "source_available AS sourceAvailable, source_partial AS sourcePartial, synced_time AS syncedTime, " +
                            "last_checked_at AS lastCheckedAt, last_error AS lastError FROM challenge_map " +
                            "WHERE stage_record_id=(SELECT id FROM challenge_stage WHERE region=? AND language=? AND stage_id=?) " +
                            "AND source_present=1 ORDER BY display_order",
                    region, language, stage.get("stageId")));
        }
        event.put("stages", stages);
        return event;
    }

    /**
     * 查询一个地图的完整有序战斗阶段及敌人快照。
     *
     * @param region 源地区
     * @param language 源语言
     * @param mapId 源地图 ID
     * @return 地图详情及状态
     */
    public Map<String, Object> getMapDetail(String region, String language, long mapId) {
        Map<String, Object> map = jdbcTemplate.queryForMap(
                "SELECT map_id AS mapId, difficulty_name AS difficultyName, act, data_status AS dataStatus, " +
                        "capture_status AS captureStatus, last_sync_status AS lastSyncStatus, source_partial AS sourcePartial, " +
                        "phase_grouping_confirmed AS phaseGroupingConfirmed, display_type AS displayType, " +
                        "synced_time AS syncedTime, last_error AS lastError, id AS mapRecordId " +
                        "FROM challenge_map WHERE region = ? AND language = ? AND map_id = ?",
                region, language, mapId);
        List<Map<String, Object>> battles = jdbcTemplate.queryForList(
                "SELECT battle_number AS battleNumber, space, boss_flag AS bossFlag, id " +
                        "FROM challenge_battle WHERE map_record_id = (SELECT id FROM challenge_map WHERE region=? AND language=? AND map_id=?) " +
                        "ORDER BY battle_index", region, language, mapId);
        for (Map<String, Object> battle : battles) {
            List<Map<String, Object>> phases = jdbcTemplate.queryForList(
                    "SELECT phase_number AS phaseNumber, comment, id FROM challenge_phase WHERE battle_record_id = ? ORDER BY phase_index", battle.get("id"));
            for (Map<String, Object> phase : phases) {
                phase.put("enemies", jdbcTemplate.queryForList(
                        "SELECT enemy_id AS id, enemy_name AS name, rarity, element, hp_value AS hp, atk_value AS atk, " +
                                "def_value AS def, damage_reduction AS dr, max_attacks AS max_attacks, " +
                                "base_sa_weight_share AS base_sa_chance, thumbnail, skill_definitions AS skills, " +
                                "round_skill, super_attacks AS attacks, ai_rules AS ai " +
                                "FROM challenge_enemy WHERE phase_record_id = ? ORDER BY enemy_index", phase.get("id")));
                for (Map<String, Object> enemy : mapList(phase.get("enemies"))) {
                    enemy.replaceAll((key, value) -> value instanceof String text && isJsonColumn(key)
                            ? JsonUtils.json2Object(text, Object.class) : value);
                }
                phase.remove("id");
            }
            battle.remove("id");
            battle.put("rounds", phases);
        }
        map.remove("mapRecordId");
        map.put("battles", battles);
        return map;
    }

    /**
     * 保存或更新活动记录并返回本地主键。
     */
    private String findOrCreateEvent(String region, String language, Map<String, Object> event,
                                     Map<String, Object> source, long eventId) {
        String id = findId("challenge_event", "event_id", region, language, eventId);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        jdbcTemplate.update("""
                INSERT INTO challenge_event
                (id, region, language, event_id, event_name, group_id, source_present, category_id,
                stage_count, captured_stages, release_time, latest_stage_time, order_by_time, release_date_status,
                source_updated_text, banner_list, banner_top, source_hash, source_payload, synced_time)
                VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, UTC_TIMESTAMP())
                ON DUPLICATE KEY UPDATE event_name=VALUES(event_name), group_id=VALUES(group_id),
                category_id=VALUES(category_id), stage_count=VALUES(stage_count), captured_stages=VALUES(captured_stages),
                release_date_status=VALUES(release_date_status), source_updated_text=VALUES(source_updated_text),
                banner_list=VALUES(banner_list), banner_top=VALUES(banner_top), source_payload=VALUES(source_payload),
                release_time=VALUES(release_time), latest_stage_time=VALUES(latest_stage_time),
                order_by_time=VALUES(order_by_time), source_hash=VALUES(source_hash),
                synced_time=UTC_TIMESTAMP(), source_present=1
                """, id, region, language, eventId, stringValue(source.getOrDefault("name", event.get("name"))),
                intValue(source.getOrDefault("group", event.get("group"))), intValue(source.get("category")),
                intValue(source.get("stage_count")), intValue(source.get("captured_stages")),
                dateTime(source.get("release_at")), dateTime(source.get("latest_stage_at")),
                latestDateTime(dateTime(source.get("release_at")), dateTime(source.get("latest_stage_at"))),
                stringValue(source.get("release_date_status")), stringValue(source.get("updated")),
                stringValue(source.get("banner_list")), stringValue(source.get("banner_top")),
                JsonUtils.sourceHash(JsonUtils.object2Json(source)),
                json(without(source, "stages")));
        return findId("challenge_event", "event_id", region, language, eventId);
    }

    /**
     * 保存活动逻辑关卡元数据并返回本地主键。
     */
    private String saveStage(String region, String language, String eventRecordId,
                             Map<String, Object> stage, int index) {
        long stageId = longValue(stage.get("id"));
        String id = findId("challenge_stage", "stage_id", region, language, stageId);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        jdbcTemplate.update("""
                INSERT INTO challenge_stage
                (id, event_record_id, region, language, stage_id, stage_name, display_order, source_present,
                release_time, release_date_status, attributes)
                VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)
                ON DUPLICATE KEY UPDATE event_record_id=VALUES(event_record_id), stage_name=VALUES(stage_name),
                display_order=VALUES(display_order), source_present=1, release_time=VALUES(release_time),
                release_date_status=VALUES(release_date_status),
                attributes=VALUES(attributes)
                """, id, eventRecordId, region, language, stageId, stringValue(stage.get("name")), index,
                dateTime(stage.get("release_at")), stringValue(stage.get("release_date_status")),
                json(without(stage, "maps")));
        return findId("challenge_stage", "stage_id", region, language, stageId);
    }

    /**
     * 保存活动难度目录并返回地图本地主键。
     */
    private String saveMapDirectory(String region, String language, String stageRecordId,
                                    Map<String, Object> source, int index) {
        long mapId = longValue(source.get("id"));
        String id = findId("challenge_map", "map_id", region, language, mapId);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        jdbcTemplate.update("""
                INSERT INTO challenge_map
                (id, stage_record_id, region, language, map_id, difficulty, difficulty_name, act,
                display_order, source_present, release_time, release_date_status, enemy_data_flag)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)
                ON DUPLICATE KEY UPDATE stage_record_id=VALUES(stage_record_id), difficulty=VALUES(difficulty),
                difficulty_name=VALUES(difficulty_name), act=VALUES(act), display_order=VALUES(display_order),
                source_present=1, release_time=VALUES(release_time),
                release_date_status=VALUES(release_date_status), enemy_data_flag=VALUES(enemy_data_flag)
                """, id, stageRecordId, region, language, mapId, intValue(source.get("difficulty")),
                stringValue(source.get("difficulty_name")), intValue(source.get("act")), index,
                dateTime(source.get("release_at")), stringValue(source.get("release_date_status")),
                booleanValue(source.get("enemy_data")));
        return findId("challenge_map", "map_id", region, language, mapId);
    }

    /**
     * 保存一个阶段及该阶段所有独立敌人出场。
     */
    private void savePhase(String battleRecordId, int phaseIndex, Map<String, Object> phase) {
        String phaseRecordId = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO challenge_phase (id, battle_record_id, phase_index, phase_number, comment, attributes)
                VALUES (?, ?, ?, ?, ?, ?)
                """, phaseRecordId, battleRecordId, phaseIndex, intValue(phase.get("number")),
                stringValue(phase.get("comment")), json(without(phase, "enemies")));
        List<Object> enemies = listValue(phase.get("enemies"));
        for (int enemyIndex = 0; enemyIndex < enemies.size(); enemyIndex++) {
            saveEnemy(phaseRecordId, enemyIndex, mapValue(enemies.get(enemyIndex)));
        }
    }

    /**
     * 保存敌人出场的基础属性及完整技能、攻击和 AI JSON。
     */
    private void saveEnemy(String phaseRecordId, int index, Map<String, Object> enemy) {
        jdbcTemplate.update("""
                INSERT INTO challenge_enemy
                (id, phase_record_id, enemy_index, enemy_id, enemy_name, rarity, element, resource_id,
                thumbnail, hp_value, atk_value, def_value, damage_reduction, conditional_dr, max_attacks,
                base_sa_weight_share, super_only_finish, required_flag, available_flag, skill_definitions,
                round_skill, super_attacks, ai_rules, attributes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), phaseRecordId, index, longObject(enemy.get("id")),
                stringValue(enemy.get("name")), intValue(enemy.get("rarity")), intValue(enemy.get("element")),
                longObject(enemy.get("resource_id")), stringValue(enemy.get("thumbnail")), longObject(enemy.get("hp")),
                longObject(enemy.get("atk")), longObject(enemy.get("def")), enemy.get("dr"),
                booleanValue(enemy.get("conditional_dr")), intValue(enemy.get("max_attacks")), enemy.get("base_sa_chance"),
                booleanValue(enemy.get("super_only_finish")), booleanValue(enemy.get("required")),
                booleanValue(enemy.get("available")), json(enemy.get("skills")), json(enemy.get("round_skill")),
                json(enemy.get("attacks")), json(enemy.get("ai")), json(without(enemy, "skills", "round_skill", "attacks", "ai")));
    }

    /**
     * 按安全的内部表名查询源业务键对应的本地主键。
     */
    private String findId(String table, String sourceIdColumn, String region,
                          String language, long sourceId) {
        List<String> ids = jdbcTemplate.queryForList(
                "SELECT id FROM " + table + " WHERE region = ? AND language = ? AND " + sourceIdColumn + " = ?",
                String.class, region, language, sourceId);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    /**
     * 按唯一键查找地图本地主键；不存在时返回业务错误。
     */
    private String findMapId(String region, String language, long mapId) {
        List<String> ids = jdbcTemplate.queryForList(
                "SELECT id FROM challenge_map WHERE region = ? AND language = ? AND map_id = ?",
                String.class, region, language, mapId);
        if (ids.isEmpty()) {
            throw new IllegalStateException("Challenge map directory is missing, mapId=" + mapId);
        }
        return ids.getFirst();
    }

    /**
     * 生成排除已拆分子数组的有序对象副本。
     */
    private Map<String, Object> without(Map<String, Object> source, String... fields) {
        Map<String, Object> result = new LinkedHashMap<>(source);
        for (String field : fields) {
            result.remove(field);
        }
        return result;
    }

    /**
     * 将任意源对象序列化为 JSON 列值；null 保持 SQL NULL。
     */
    private String json(Object value) {
        return value == null ? null : JsonUtils.object2Json(value);
    }

    /**
     * 将源字符串转换为 nullable 字符串。
     */
    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 将源数字转换为 nullable Integer。
     */
    private Integer intValue(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * 将源数字转换为 long；缺少必填 ID 时拒绝写入。
     */
    private long longValue(Object value) {
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Missing numeric challenge source ID");
        }
        return number.longValue();
    }

    /**
     * 将可选源数字转换为 nullable Long。
     */
    private Long longObject(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * 将可选源布尔值转换为 nullable Boolean。
     */
    private Boolean booleanValue(Object value) {
        return value instanceof Boolean booleanValue ? booleanValue : null;
    }

    /**
     * 将可选数组值转换为列表；缺失字段按空数组处理。
     */
    private List<Object> listValue(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return new ArrayList<>(values);
    }

    /**
     * 将 ISO-8601 UTC 时间转换为 JDBC 时间戳；未知或非标准时间保留为空。
     */
    private Timestamp dateTime(Object value) {
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        try {
            return new Timestamp(DateUtils.parseUtcDate(text).getTime());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /**
     * 选取活动开放时间和最新关卡开放时间中较晚的可用值用于列表排序。
     */
    private Timestamp latestDateTime(Timestamp first, Timestamp second) {
        Date latest = DateUtils.latestDate(first, second);
        return latest == null ? null : new Timestamp(latest.getTime());
    }

    /**
     * 将 JDBC 查询结果转换为可修改的字段映射列表。
     */
    private List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : values) {
            result.add(mapValue(item));
        }
        return result;
    }

    /**
     * 判断是否为地图详情中需要反序列化的 JSON 字段。
     */
    private boolean isJsonColumn(String key) {
        if ("skills".equals(key)) {
            return true;
        } else if ("round_skill".equals(key)) {
            return true;
        } else if ("attacks".equals(key)) {
            return true;
        } else {
            return "ai".equals(key);
        }
    }

    /**
     * 将可选对象值转换为字段映射。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> values)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        values.forEach((key, item) -> result.put(Objects.toString(key), item));
        return result;
    }
}
