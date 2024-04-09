package com.jcca.web.ibmMQ.command.impl;


import com.ibm.mq.MQException;
import com.ibm.mq.pcf.PCFMessage;
import com.jcca.web.ibmMQ.annotation.Command;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.QMgrData;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.health.HealthResult;
import com.jcca.web.ibmMQ.schedule.IMonitorScheduler;
import com.jcca.web.ibmMQ.service.ConnectionService;
import com.jcca.web.ibmMQ.service.MonitorService;
import com.jcca.web.ibmMQ.service.PCFMessageService;
import com.jcca.web.ibmMQ.vo.HealthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*     */

/**
 * 管理队列执行
 */
@Command("INQUIRE_QMGR")
public class InquireQMgrCommand
        extends InquireDataCommand {
    private static final Logger log = LoggerFactory.getLogger(InquireQMgrCommand.class);

    private static final String PROP_QMGR_STATUS = "queueManagerStatus";

    private PCFMessage inquireQMgrStatusRequest;
    private PCFMessage inquireQMgrRequest;
    private int timeout;

    @Resource(name = "messageService")
    private PCFMessageService messageService;
    @Resource(name = "monitorScheduler")
    private IMonitorScheduler monitorScheduler;
    @Resource
    private MonitorService monitorManager;
    //连接状态是否改变过
    public static ConcurrentHashMap<String, Boolean> change = new ConcurrentHashMap<>();
    @Resource
    private ConnectionService connectionManager;


    public Object execute(Map<String, Object> context) throws Exception {
        Monitor monitor = getMonitor(context);
        boolean autoConnect = isAutoConnect(context);
        if (this.inquireQMgrRequest == null) {
            this.inquireQMgrRequest = PCFMessageFactory.createInquireQMgr();
            this.inquireQMgrStatusRequest = PCFMessageFactory.createInquireQMgrStatus();
            this.timeout = getTimeout(monitor);
        }
        List<StatisticalData> dataList = new ArrayList<StatisticalData>(1);
        PCFMessage[] responsesInquireQM = null;
        boolean unavailable = false;
        try {
            responsesInquireQM = sendRequest(monitor.getConnection(), this.inquireQMgrRequest, this.timeout, autoConnect);
            if (log.isDebugEnabled()) {
                log.debug("Inquire QMgr and got responses size: {}", Integer.valueOf(responsesInquireQM.length));
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("QMGR is unavailable for connection '{}'!", monitor.getConnection().getName(), e);
            }
            if (e instanceof MQException) {
                if (((MQException) e).getReason() == 2067) {
                    if (log.isDebugEnabled()) {
                        log.debug("Retrying with MQ V7 command...");
                    }
                    this.inquireQMgrRequest = PCFMessageFactory.createInquireQMgrV7();
                    try {
                        responsesInquireQM = sendRequest(monitor.getConnection(), this.inquireQMgrRequest, this.timeout, autoConnect);
                    } catch (Exception ex) {
                        unavailable = true;
                        if (log.isDebugEnabled()) {
                            log.debug("QMGR(V7) is unavailable for connection '{}'!", monitor
                                    .getConnection().getName(), e);
                        }
                    }
                } else {
                    unavailable = true;
                }
            } else {
                try {
                    //如果连接出问题，重新再连接一次 //因时间过长  现取消二次连接
                    Connection connection = Connection.getConnection(connectionManager.getById(monitor.getConnection().getId()));
                    this.messageService.connect(connection);
                } catch (Exception ex) {
                    log.error(String.format("Failed to connect to QM with '%s'", new Object[]{monitor.getConnection().getName()}), ex);
                }
                unavailable = true;
            }
        }
        if (unavailable) {
            dataList.add(buildUnavailableQMgrData(monitor));
            //连接状态已经改变
            change.put(monitor.getConnection().getId(), true);
            return dataList;
        }
        if (change.get(monitor.getConnection().getId()) != null && change.get(monitor.getConnection().getId()) == true) {//曾经改变过状态
            for (IBMMonitor ibmMonitor : this.monitorManager.listMonitors(monitor.getConnection().getName(), Monitor.State.Active.getValue(), null)) {
                Monitor monitorObject = Monitor.getMonitor(ibmMonitor);
                //检查是否有监控对象被清除
                if (monitorScheduler.isMonitorScheduled(monitorObject) == false) {
                    monitorScheduler.scheduleMonitor(monitorObject);
                }
            }
            //将连接状态删除
            change.remove(monitor.getConnection().getId());
        }

        StatisticalData data = handleResponse(monitor, responsesInquireQM[0], context, false);
        QMgrData qmgrData = (QMgrData) data.adapt(QMgrData.class);
        if (qmgrData.isZOS()) {
            qmgrData.setQueueManagerStatus(QMgrData.QMgrStatus.Running);
            qmgrData.setHealthState(HealthState.OK);
        } else {
            PCFMessage[] responsesInquireQMStatus = sendRequest(monitor.getConnection(), this.inquireQMgrStatusRequest, this.timeout, autoConnect);
            if (log.isDebugEnabled()) {
                log.debug("Inquire QMgr status and got responses size: {}", Integer.valueOf(responsesInquireQMStatus.length));
            }
            data = handleResponse(data, monitor, responsesInquireQMStatus[0], context, true);
        }
        dataList.add(data);
        return dataList;
    }

    private QMgrData buildUnavailableQMgrData(Monitor monitor) {
        QMgrData data = new QMgrData(monitor);
        data.setCaptureTime(new Date());
        data.setHealthState(HealthState.UNKNOWN);
        data.setQueueManagerStatus(QMgrData.QMgrStatus.Unavailable);
        Map<String, Object> evaluationContext = new HashMap<String, Object>(1);
        evaluationContext.put("queueManagerStatus", data.getQueueManagerStatus());
        HealthResult result = evaluateHealth(monitor, evaluationContext);
        data.setHealthState(result.getHealthState());
        if (result.getHealthState() != HealthState.OK) {
            data.addMessages(result.getMessages());
        }
        return data;
    }
}