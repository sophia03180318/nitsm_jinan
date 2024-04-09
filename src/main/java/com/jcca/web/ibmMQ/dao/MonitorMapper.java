package com.jcca.web.ibmMQ.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:28:54
 **/
public interface MonitorMapper extends BaseMapper<IBMMonitor> {
    public List<IBMMonitor> listMonitors(String connectName, String paramState, String paramScope);

    public List<IBMMonitor> findMonitorByNameConnectionName(String connectionName, String monitorName);

    @Update("update IBMMQ_MONITOR set HEALTHSTATUS= #{healthStatus} where id= #{id}")
    public Boolean updateHealthStatus(String healthStatus, String id);

    @Update("update IBMMQ_MONITOR set HEALTHRULE = null where id= #{id}")
    public Boolean cleanWarn(String id);

    @Update("update IBMMQ_MONITOR set HEALTHRULE = #{rule} where id= #{id}")
    public Boolean updateWarn(String id, String rule);

    @Delete("delete from ALARM_INFO where id in (select ALARM_ID from ALARM_EVENT_REL where EVENT_ID in (select ID from ALARM_EVENT where FLAG = #{id}))")
    public void deleteAlarmInfo(String id);

    @Delete("delete from ALARM_EVENT_REL where EVENT_ID in (select ID from ALARM_EVENT where FLAG = #{id})")
    void deleteAlarmEventREL(String monitorId);

    @Delete("delete from ALARM_EVENT where FLAG = #{id}")
    void deleteAlarmEvent(String monitorId);
}
