package com.jcca.web.ibmMQ.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.ibmMQ.command.ICommandProcessor;
import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.StatisticalDataException;
import com.jcca.web.ibmMQ.common.StatisticalDataNotFoundException;
import com.jcca.web.ibmMQ.common.Time;
import com.jcca.web.ibmMQ.dao.*;
import com.jcca.web.ibmMQ.domain.*;
import com.jcca.web.ibmMQ.entity.*;
import com.jcca.web.ibmMQ.service.*;
import com.jcca.web.ibmMQ.util.Dates;
import com.jcca.web.ibmMQ.vo.HealthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaozheng
 * @date 2020-07-14 14:59
 **/
@Service
public class StatisticalDataServiceImpl extends ServiceImpl<StatisticalDataMapper, IBMStatisticalData> implements StatisticalDataService {
    private static final Logger log = LoggerFactory.getLogger(StatisticalDataServiceImpl.class);
    @Resource
    StatisticalDataMapper statisticalDataMapper;
    @Resource
    QMgrDataMapper qMgrDataMapper;
    @Resource
    ChannelDataMapper channelDataMapper;
    @Resource
    QueueDataMapper queueDataMapper;
    @Resource
    TopicDataMapper topicDataMapper;
    @Resource
    ListenerDataMapper listenerDataMapper;
    @Resource
    MonitorMapper monitorMapper;
    @Resource
    AlarmSendService alarmSendService;
    @Resource
    private AssetService assetServ;
    @Resource
    private EventLogicService eventServ;
    @Resource
    private ThresholdAssetService thresholdServ;

    @Resource(name = "commandProcessor")
    private ICommandProcessor commandProcessor;

    @Resource(name = "messageService")
    protected PCFMessageService messageService;

    @Override
    public void removeExpiredStatistics(Monitor monitor) {
        try {
            Date now = new Date();
            Time time = Time.parseMHD(monitor.getDataExpirationTime());
            Date expiredTime = Dates.before(now, time.getValue(), time.getUnit());
            // QueueManager("QueueManager"), Channel("Channel"), Queue("Queue"), Topic("Topic"), Listener("Listener");
            switch (monitor.getCategory()) {
                case QueueManager:
                    qMgrDataMapper.removeExpiredStatistics(monitor.getId(), expiredTime);
                    break;
                case Channel:
                    channelDataMapper.removeExpiredStatistics(monitor.getId(), expiredTime);
                    break;
                case Queue:
                    queueDataMapper.removeExpiredStatistics(monitor.getId(), expiredTime);
                    break;
                case Topic:
                    topicDataMapper.removeExpiredStatistics(monitor.getId(), expiredTime);
                    break;
                case Listener:
                    listenerDataMapper.removeExpiredStatistics(monitor.getId(), expiredTime);
                    break;
                default:
                    this.statisticalDataMapper.removeExpiredStatistics(monitor.getId(), expiredTime);
            }


        } catch (Exception e) {
            throw new StatisticalDataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{e
                    .getMessage()}, e);
        }
        if (log.isDebugEnabled())
            log.debug("Data for monitor '{}' is expired from connection '{}', clear cache with key '{}'.", new Object[]{
                    Integer.valueOf(monitor.getId()), monitor.getName(), monitor.getConnection().getName()
            });
    }

    //如果之前是告警状态现在是恢复状态，
    public static Boolean getISChange(String oldStatus, String newStatus, StatisticalData paramStatisticalData) {
        if (
                (HealthState.ERROR.getValue().equals(oldStatus) || HealthState.WARN.getValue().equals(oldStatus))
                        && (HealthState.OK.getValue().equals(newStatus) || HealthState.UNKNOWN.getValue().equals(newStatus))
        ) {
            HealthMessage healthMessage = new HealthMessage();
            healthMessage.setFlag(true);
            paramStatisticalData.addMessage(healthMessage);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void saveStatisticalData(StatisticalData paramStatisticalData) {
        //true：表示从告警变为了恢复状态
        Boolean resume = false;
        if (paramStatisticalData instanceof ChannelData) {
            ChannelData channelData = paramStatisticalData.adapt(ChannelData.class);
            IBMChannelData ibmChannelData = new IBMChannelData();
            ibmChannelData.setCaptureTime(channelData.getCaptureTime());
            ibmChannelData.setHealthState(channelData.getHealthState().getValue());
            ibmChannelData.setBuffersReceived(channelData.getBuffersReceived());
            ibmChannelData.setBuffersSent(channelData.getBuffersSent());
            ibmChannelData.setBytesReceived(channelData.getBytesReceived());
            ibmChannelData.setBytesSent(channelData.getBytesSent());
            ibmChannelData.setChannelStatus(channelData.getChannelStatus().getValue());
            ibmChannelData.setMessagesTransferred(channelData.getMessagesTransferred());
            ibmChannelData.setMonitorId(channelData.getMonitor().getId());
            ChannelDataService channelDataService = SpringContextUtil.getBean(ChannelDataService.class);


            IBMChannelData ibmChannelDataOld = channelDataService.queryLastDate(channelData.getMonitor().getId());
            if (ibmChannelDataOld != null) {
                String oldStatus = ibmChannelDataOld.getHealthState();
                String newStatus = ibmChannelData.getHealthState();
                //从告警变成了恢复
                resume = getISChange(oldStatus, newStatus, paramStatisticalData);
            }


            channelDataService.save(ibmChannelData);
        } else if (paramStatisticalData instanceof QueueData) {
            QueueData queueData = paramStatisticalData.adapt(QueueData.class);
            IBMQueueData ibmQueueData = new IBMQueueData();
            ibmQueueData.setCaptureTime(queueData.getCaptureTime());
            ibmQueueData.setHealthState(queueData.getHealthState().getValue());
            ibmQueueData.setCurrentQDepth(queueData.getCurrentQDepth());
            ibmQueueData.setMaxQDepth(queueData.getMaxQDepth());
            ibmQueueData.setOpenInputCount(queueData.getOpenInputCount());
            ibmQueueData.setOpenOutputCount(queueData.getOpenOutputCount());
            ibmQueueData.setMonitorId(queueData.getMonitor().getId());
            QueueDataService queueDataService = SpringContextUtil.getBean(QueueDataService.class);

            IBMQueueData ibmQueueDataOld = queueDataService.queryLastDate(queueData.getMonitor().getId());
            if (ibmQueueDataOld != null) {
                String oldStatus = ibmQueueDataOld.getHealthState();
                String newStatus = ibmQueueData.getHealthState();
                //从告警变成了恢复
                resume = getISChange(oldStatus, newStatus, paramStatisticalData);
            }

            queueDataService.save(ibmQueueData);
        } else if (paramStatisticalData instanceof TopicData) {
            TopicData topicData = paramStatisticalData.adapt(TopicData.class);
            IBMTopicData ibmTopicData = new IBMTopicData();
            ibmTopicData.setCaptureTime(topicData.getCaptureTime());
            ibmTopicData.setHealthState(topicData.getHealthState().getValue());
            ibmTopicData.setPublishedMessagesCount(topicData.getPublishedMessagesCount());
            ibmTopicData.setPublishersCount(topicData.getPublishersCount());
            ibmTopicData.setSubscriberscount(topicData.getSubscribersCount());
            ibmTopicData.setMonitorId(topicData.getMonitor().getId());
            TopicDataService topicDataService = SpringContextUtil.getBean(TopicDataService.class);

            IBMTopicData ibmTopicDataOld = topicDataService.queryLastDate(topicData.getMonitor().getId());
            if (ibmTopicDataOld != null) {
                String oldStatus = ibmTopicDataOld.getHealthState();
                String newStatus = ibmTopicData.getHealthState();
                //从告警变成了恢复
                resume = getISChange(oldStatus, newStatus, paramStatisticalData);
            }

            topicDataService.save(ibmTopicData);
        } else if (paramStatisticalData instanceof ListenerData) {
            ListenerData listenerData = paramStatisticalData.adapt(ListenerData.class);
            IBMListenerData ibmListenerData = new IBMListenerData();
            ibmListenerData.setCaptureTime(listenerData.getCaptureTime());
            ibmListenerData.setHealthState(listenerData.getHealthState().getValue());
            ibmListenerData.setListenerStatus(listenerData.getListenerStatus().getValue());
            ibmListenerData.setMonitorId(listenerData.getMonitor().getId());
            ListenerDataService listenerDataService = SpringContextUtil.getBean(ListenerDataService.class);


            IBMListenerData ibmListenerDataOld = listenerDataService.queryLastDate(listenerData.getMonitor().getId());
            if (ibmListenerDataOld != null) {
                String oldStatus = ibmListenerDataOld.getHealthState();
                String newStatus = ibmListenerData.getHealthState();
                //从告警变成了恢复
                resume = getISChange(oldStatus, newStatus, paramStatisticalData);
            }


            listenerDataService.save(ibmListenerData);
        } else if (paramStatisticalData instanceof QMgrData) {
            QMgrData QMgrData = paramStatisticalData.adapt(QMgrData.class);
            IBMQMgrData ibmqMgrData = new IBMQMgrData();
            ibmqMgrData.setCaptureTime(QMgrData.getCaptureTime());
            ibmqMgrData.setHealthState(QMgrData.getHealthState().getValue());
            ibmqMgrData.setQueueManagerStatus(QMgrData.getQueueManagerStatus().getValue());
            ibmqMgrData.setMonitorId(QMgrData.getMonitor().getId());
            QMgrDataService qMgrDataService = SpringContextUtil.getBean(QMgrDataService.class);

            IBMQMgrData ibmqMgrDataOld = qMgrDataService.queryLastDate(QMgrData.getMonitor().getId());
            if (ibmqMgrDataOld != null) {
                String oldStatus = ibmqMgrDataOld.getHealthState();
                String newStatus = ibmqMgrData.getHealthState();
                //从告警变成了恢复
                resume = getISChange(oldStatus, newStatus, paramStatisticalData);
            }

            qMgrDataService.save(ibmqMgrData);
        } else {
            IBMStatisticalData statisticalData = new IBMStatisticalData();
            statisticalData.setCaptureTime(paramStatisticalData.getCaptureTime());
            statisticalData.setHealthState(paramStatisticalData.getHealthState().getValue());
            statisticalData.setMonitorId(paramStatisticalData.getMonitor().getId());
            this.save(statisticalData);
        }
        //更新监控状态
        monitorMapper.updateHealthStatus(paramStatisticalData.getHealthState().getValue(), paramStatisticalData.getMonitor().getId());
        if ((paramStatisticalData.getMessages() != null && paramStatisticalData.getMessages().size() > 0) || resume) {
            alarmSendService.sendAlarm(paramStatisticalData);
        }
        try {
            Monitor monitor = paramStatisticalData.getMonitor();
            if (monitor.getCategory().getValue().equals("Queue")) {
                String assetIp = monitor.getConnection().getHost();
                Asset asset = assetServ.findOneByIp(assetIp);
                if (Objects.isNull(asset)) {
                    log.error("【MQ数据处理失败】：IP：{}的资产不存在！", assetIp);
                    return;
                }
                if (ObjectUtil.isNotNull(monitor.getValue()) && ObjectUtil.isNotNull(monitor.getValue())) {
                    String resultVale = monitor.getValue() + "";
                    log.info("分级事件判断:现阶段值:" + resultVale);
                    VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(resultVale), EventUniqueCode.MQ_UNIQUE_CODE);
                    List<CreateEventReq> mqEventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.MQ, new Date(), asset.getId(), resultVale, "MQ队列");
                    for (CreateEventReq createEventReq : mqEventList) {
                        log.info("【MQ分级事件添加】：REQ:{}", JSONUtil.toJsonStr(createEventReq));
                        eventServ.addEvent(createEventReq);
                    }
                }
            }
        } catch (Exception e) {
            log.error("【MQ添加事件出现异常!!!】：" + e.getMessage(), e);
        }
    }


    @Override
    public StatisticalData findLatestStatisticalData(Monitor monitor) {
        return null;

    }

    @Override
    public StatisticalData queryMonitorDetails(Monitor monitor) {
        StatisticalData data = null;

        try {
            List<StatisticalData> datas = (List<StatisticalData>) this.commandProcessor.process(monitor, new HashMap<String, Object>(2) {
                {
                    this.put("context.monitor", monitor);
                    this.put("context.measureonly", false);
                }
            });
            if (datas != null && datas.size() > 0) {
                data = datas.get(0);
            }
        } catch (Exception var4) {
            PCFMessageServiceImpl.checkConnectionException(monitor.getConnection(), var4);
            throw new StatisticalDataException(50000, ErrorConstants.Message.MSG_INTERNAL_SERVER_ERROR, new Object[]{var4.getMessage()}, var4);
        }

        if (data == null) {
            throw new StatisticalDataNotFoundException(40405, ErrorConstants.Message.MSG_STATISTICS_NOT_FOUND, new Object[]{monitor.getName(), monitor.getConnection().getName()});
        } else {
            return data;
        }
    }
}