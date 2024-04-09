package com.jcca.web.ibmMQ.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.entity.IBMMonitor;

import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:28:54
 **/
public interface MonitorService extends IService<IBMMonitor> {

    List<IBMMonitor> listMonitors(String connectName, String paramState, Monitor.Scope state);

    IBMMonitor updateMonitorState(String connectionName, String monitorName, Monitor.State state);

    IBMMonitor findMonitor(String connectionName);

    IBMMonitor findMonitor(String connectionName, String monitorName);

    Monitor getMonitor(String connectionName, String monitorName);

    void activateMonitor(String paramString);

    void activateMonitor(Monitor monitor);

    Monitor createMonitor(Connection connection);

    Monitor createMonitor(Monitor monitor);

    boolean existsMonitor(String connectionName, String monitorName);

    public void removeMonitor(Monitor monitor);

    public void validateWarn(String warn);

    public void cleanWarn(String id);


    public void updateWarn(String monitorId, String rule);

    public void restartTask(Monitor monitor);

    public void removeTask(Monitor monitor);

    public void removeEvent(String objectName);

}
