package com.jcca.web.event.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.event.entity.AlarmEventRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 告警事件关联表
 *
 * @author lyp
 */
@Mapper
public interface AlarmEventRelMapper extends BaseMapper<AlarmEventRel> {

    /**
     * 查询
     *
     * @param alarmId
     */
    @Select("SELECT * FROM ALARM_EVENT_REL e WHERE e.ALARM_ID = #{alarmId} ORDER BY ID DESC")
    List<AlarmEventRel> selectByAlarmId(@Param("alarmId") String alarmId);

    @Select("SELECT EVENT_ID FROM ALARM_EVENT_REL e WHERE e.ALARM_ID = #{alarmId}")
    List<String> selectEventIdByAlarmId(@Param("alarmId") String alarmId);

}
