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

    List<String> selectStageConfigByAlarmCode(@Param("eventUniqueCode") String eventUniqueCode);
}
