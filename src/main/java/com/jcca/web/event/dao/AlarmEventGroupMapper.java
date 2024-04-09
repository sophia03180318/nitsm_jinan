package com.jcca.web.event.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.event.entity.AlarmEventGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 事件告警配置
 *
 * @author lyp
 */
@Mapper
public interface AlarmEventGroupMapper extends BaseMapper<AlarmEventGroup> {

    @Select("select e.STAGE_CONFIG from ALARM_EVENT_GROUP e join  ALARM_REPOSITORY r on  e.EVENT_TYPE_IDS = r.EVENT_TYPE_ID where r.ALARM_CODE = #{eventUniqueCode}")
    List<String> selectStageConfigByAlarmCode(@Param("eventUniqueCode") String eventUniqueCode);
}
