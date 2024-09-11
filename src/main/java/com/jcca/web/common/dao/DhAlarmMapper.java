package com.jcca.web.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.common.entity.Alarm;
import com.jcca.web.common.entity.Device;
import org.apache.ibatis.annotations.Select;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:02
 **/
public interface DhAlarmMapper extends BaseMapper<Alarm> {
    @Select("select  * from DH_ALARM where CREATE_TIME=(select max(CREATE_TIME) from DH_ALARM)")
    List<Alarm> getAlarm();

}