package com.jcca.web.event.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web2.dto.EventPageDto;
import com.jcca.web2.vo.EventPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 事件
 *
 * @author lyp
 */
@Mapper
public interface AlarmEventMapper extends BaseMapper<AlarmEvent> {

    /**
     * 查询该事件明细最后一次告警
     *
     * @param assetId
     * @param typeId
     * @param flag
     * @param uniqueCode
     * @return
     */
    @Select("select * from ALARM_EVENT where id = (select MAX(id) from ALARM_EVENT where ASSET_ID=#{assetId} and EVENT_TYPE_ID = #{typeId} and FLAG=#{flag} and  UNIQUE_CODE = #{uniqueCode})")
    AlarmEvent selectLastLog(@Param("assetId") String assetId, @Param("typeId") String typeId,
                             @Param("flag") String flag, @Param("uniqueCode") String uniqueCode);

    /**
     * 查询该事件最后一次告警
     *
     * @param assetId
     * @param typeId
     * @param flag
     * @return
     */
    @Select("select * from ALARM_EVENT where id = (select MAX(id) from ALARM_EVENT where ASSET_ID=#{assetId} and EVENT_TYPE_ID = #{typeId} and FLAG=#{flag})")
    AlarmEvent selectTypeLastLog(@Param("assetId") String assetId, @Param("typeId") String typeId,
                                 @Param("flag") String flag);

    /**
     * 查询该事件明细最后一次告警
     *
     * @param assetId
     * @param typeId
     * @param uniqueCode
     * @return
     */
    @Select("select * from ALARM_EVENT where id = (select MAX(id) from ALARM_EVENT where ASSET_ID=#{assetId} and EVENT_TYPE_ID = #{typeId} and UNIQUE_CODE = #{uniqueCode})")
    AlarmEvent selectLastLogExceptFlag(@Param("assetId") String assetId, @Param("typeId") String typeId,
                                       @Param("uniqueCode") String uniqueCode);

    /**
     * 查询该事件最后一次告警
     *
     * @param assetId
     * @param typeId
     * @return
     */
    @Select("select * from ALARM_EVENT where id = (select MAX(id) from ALARM_EVENT where ASSET_ID=#{assetId} and EVENT_TYPE_ID = #{typeId})")
    AlarmEvent selectTypeLastLogExceptFlag(@Param("assetId") String assetId, @Param("typeId") String typeId);

    @Select("<script> SELECT DISTINCT MATCH_FLAG FROM ALARM_EVENT where UNIQUE_CODE in "
            + "<foreach item='item' index='index' collection='codeList' open='(' separator=',' close=')'>"
            + "#{item} </foreach>" + " and MATCH_FLAG is not NULL GROUP BY MATCH_FLAG</script>")
    List<String> groupMatchFlagByCode(@Param("codeList") List<String> codeList);

    @Select("select * from ALARM_EVENT where id = (select MAX(id) from ALARM_EVENT where ASSET_ID=#{assetId} and (UNIQUE_CODE = 'PING_ALL_STOP' or UNIQUE_CODE = 'PING_OTHER_STOP' or UNIQUE_CODE = 'PING_STOP'))")
    AlarmEvent getLastPingLog(@Param("assetId") String assetId);

    /**
     * 批量更新时间类型ID
     *
     * @return
     */
    @Update("update ALARM_EVENT set event_type_id = #{eventTypeId} where REPOSITORY_ID = #{alarmRepoId}")
    int batchUpdateTypeById(@Param("eventTypeId") String eventTypeId, @Param("alarmRepoId") String alarmRepoId);

    /**
     * 通过告警ID查询下属所有的事件信息
     *
     * @param alarmId
     * @return
     */
    @Select("select e.* ,to_char(e.CREATE_TIME,'yyyy-mm-dd HH24:MI:SS')  as createTimeStr from ALARM_EVENT e where id in (select l.event_id from ALARM_EVENT_REL l where l.ALARM_ID = #{alarmId}) order by e.CREATE_TIME desc")
    List<AlarmEvent> selectAllEventByAlarmId(@Param("alarmId") String alarmId);

    @Select("SELECT temp.* FROM (SELECT ROWNUM, t.* FROM ALARM_EVENT t WHERE ROWNUM < #{end} AND t.ASSET_ID = #{assetId} AND t.UNIQUE_CODE LIKE 'event:log%') temp WHERE ROWNUM >= #{start}")
    List<AlarmEvent> listSyslog(String assetId, Integer start, Integer end);

    /**
     * 分页查询事件列表
     *
     * @param page
     * @param query
     * @return
     */
    IPage<EventPageVo> pageEventListV2(Page page, @Param("query") EventPageDto query);

    /**
     * 查询记录在表中白名单数量
     *
     * @param eventId
     * @return
     */
    Integer getWhiteSize(String eventId);

    /**
     * 更新
     *
     * @param eventTypeId
     * @param repositoryId
     * @param uniqueCode
     */
    void updateInfoByUniqueCode(@Param("eventTypeId") String eventTypeId, @Param("repositoryId") String repositoryId, @Param("uniqueCode") String uniqueCode);
}
