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
    List<AlarmEventRel> selectByAlarmId(@Param("alarmId") String alarmId);

    List<String> selectEventIdByAlarmId(@Param("alarmId") String alarmId);

}
