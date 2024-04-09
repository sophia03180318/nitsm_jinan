package com.jcca.web.event.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.event.entity.AlarmEventType;
import org.apache.ibatis.annotations.Mapper;


/**
 * 事件类型
 *
 * @author lyp
 */
@Mapper
public interface AlarmEventTypeMapper extends BaseMapper<AlarmEventType> {

}
