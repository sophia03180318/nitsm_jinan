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

    public Boolean updateHealthStatus(String healthStatus, String id);

    public Boolean cleanWarn(String id);

    public Boolean updateWarn(String id, String rule);

    public void deleteAlarmInfo(String id);

    void deleteAlarmEventREL(String monitorId);

    void deleteAlarmEvent(String monitorId);
}
