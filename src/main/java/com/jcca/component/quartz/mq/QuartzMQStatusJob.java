package com.jcca.component.quartz.mq;

import cn.hutool.core.util.ObjectUtil;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
import com.jcca.dataProcessing.dataAdpater.MQAdapter;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.mq.entity.CollectMq;
import com.jcca.web.mq.entity.MqConnection;
import com.jcca.web.mq.entity.MqMonitor;
import com.jcca.web.mq.service.CollectMqService;
import com.jcca.web.mq.service.MqConnectionService;
import com.jcca.web.mq.service.MqMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * MQ队列管理器状态
 *
 * @author sophia
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzMQStatusJob extends QuartzJobBean {

    @Resource
    private MqMonitorService monitorService;
    @Resource
    private CollectMqService collectMqService;
    @Resource
    MqConnectionService connectionService;
    @Resource
    private AssetService assetServ;
    @Resource
    private DataProcessManager dataProcessManager;
    private final Map<String, ChannelFlowSnapshot> channelFlowCache = new HashMap<>();

    private static class ChannelFlowSnapshot {
        long sentBytes;
        long rcvdBytes;
        long ts;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<MqConnection> connections = connectionService.list();
        if (ObjectUtil.isNotNull(connections)) {
            for (MqConnection connection : connections) {
                Asset asset = assetServ.findOneByIp(connection.getConnectHost());
                if (Objects.isNull(asset)) {
                    log.error("MQ未录入关联资产" + connection.getConnectHost());
                }
                MqPcfHolder holder = null;
                try {
                    String ip = connection.getConnectHost();
                    Integer port = connection.getConnectPort();
                    String qmgr = connection.getConnectName();
                    String channel = connection.getChannelName();
                    String id = connection.getId();
                    String userId = connection.getUserId();
                    holder = newAgent(ip, port, channel, qmgr, userId);
                    PCFMessageAgent agent = holder.agent;

                    inquireQmgrStatus(agent, connection, asset);
                    if ("RUNNING".equals(connection.getStatus())) {
                        collectMqService.removeCollectData(id);
                        inquireAllQueues(agent, id);
                        inquireAllChannels(agent, id);

                        List<MqMonitor> queueList = monitorService.getQueueByConnectionId(id);
                        if (ObjectUtil.isNotNull(queueList)) {
                            inquireQueueDepths(agent, queueList, asset);
                        }

                        List<MqMonitor> channelList = monitorService.getChannelByConnectionId(id);
                        if (ObjectUtil.isNotNull(channelList)) {
                            inquireChannelFlows(agent, channelList);
                        }
                    }

                } catch (Exception e) {
                    log.error("MQ采集结果解析异常", e);
                } finally {
                    if (holder != null) {
                        try {
                            if (holder.agent != null) holder.agent.disconnect();
                        } catch (Exception ignore) {
                        }
                        try {
                            if (holder.qm != null) holder.qm.disconnect();
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        }
    }


    /**
     * 获取连接
     */
    private MqPcfHolder newAgent(String host, int port, String channel, String qmgr,String userId) throws Exception {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(MQConstants.HOST_NAME_PROPERTY, host);
        props.put(MQConstants.PORT_PROPERTY, port);
        props.put(MQConstants.CHANNEL_PROPERTY, channel);
        props.put(MQConstants.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
        props.put(MQConstants.USER_ID_PROPERTY, userId);
        MqPcfHolder h = new MqPcfHolder();
        h.qm = new MQQueueManager(qmgr, props);
        h.agent = new PCFMessageAgent(h.qm);
        return h;
    }

    /**
     * 获取连接状态和连接数
     */
    private void inquireQmgrStatus(PCFMessageAgent agent, MqConnection bean, Asset asset) {
        String oldStatus = bean.getStatus();
        try {
            PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_MGR_STATUS);
            req.addParameter(MQConstants.MQIACF_Q_MGR_STATUS_ATTRS, new int[]{
                    MQConstants.MQIACF_Q_MGR_STATUS,
                    MQConstants.MQIACF_CONNECTION_COUNT
            });

            PCFMessage[] resp = agent.send(req);
            if (resp == null || resp.length == 0) {
                // 不明确：不更新、不告警（按你规则当正常）
                return;
            }

            PCFMessage r = resp[0];

            // 能拿到 st 才算“明确”
            int st = r.getIntParameterValue(MQConstants.MQIACF_Q_MGR_STATUS);
            String newStatus = (st == 2) ? "RUNNING" : "STOPPED";

            int cc = r.getIntParameterValue(MQConstants.MQIACF_CONNECTION_COUNT);

            bean.setStatus(newStatus);
            bean.setConnectionCount(cc);

            // 只有“明确不运行”才推告警；明确运行可推恢复
            if (ObjectUtil.isNotNull(asset) && ObjectUtil.isNotNull(oldStatus) && !oldStatus.equals(newStatus)) {
                MQMonitorEntity monitorEntity = new MQMonitorEntity();
                monitorEntity.setCollectTime(System.currentTimeMillis());
                monitorEntity.setAssetId(asset.getId());
                monitorEntity.setAssetIp(asset.getIp());
                monitorEntity.setMessage("队列管理器状态:" + newStatus);
                monitorEntity.setStatus("RUNNING".equals(newStatus)
                        ? EventLevelEnum.NORMAL.getCode()
                        : EventLevelEnum.ABNORMAL.getCode());
                ((MQAdapter) dataProcessManager.getAdapater("MQAdapter")).dispose(monitorEntity);
            }
            connectionService.updateById(bean);

        } catch (Exception e) {
            bean.setStatus("STOPPED");
            log.warn("MQ队列管理器状态采集异常: {}", bean.getConnectHost(), e);
        }
    }


    /**
     * 所有队列
     */
    private void inquireAllQueues(PCFMessageAgent agent, String connectionId) {
        try {
            PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
            req.addParameter(MQConstants.MQCA_Q_NAME, "*");
            req.addParameter(MQConstants.MQIACF_Q_ATTRS, new int[]{
                    MQConstants.MQCA_Q_NAME, MQConstants.MQIA_Q_TYPE, MQConstants.MQIA_USAGE
            });

            PCFMessage[] resp = agent.send(req);
            if (resp == null) return;

            ArrayList<CollectMq> collectMqs = new ArrayList<>();
            for (PCFMessage m : resp) {
                CollectMq collectMq = new CollectMq();
                collectMq.setId(MyIdUtil.getId());
                collectMq.setConnectionId(connectionId);
                String qName = m.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
                collectMq.setName(qName);
                int qType = m.getIntParameterValue(MQConstants.MQIA_Q_TYPE);
                if (qType == MQConstants.MQQT_LOCAL) {
                    int usage = m.getIntParameterValue(MQConstants.MQIA_USAGE);
                    if (usage == MQConstants.MQUS_TRANSMISSION) {
                        collectMq.setCategory("MQQT_TRANSMISSION");
                    } else {
                        collectMq.setCategory("MQQT_LOCAL");
                    }
                    collectMqs.add(collectMq);
                } else if (qType == MQConstants.MQQT_REMOTE) {
                    collectMq.setCategory("MQQT_REMOTE");
                    collectMqs.add(collectMq);
                }
            }
            if (!collectMqs.isEmpty()) {
                collectMqService.saveBatch(collectMqs);
            }
        } catch (Exception e) {
            log.error("MQ队列列表获取失败", e);
        }
    }

    /**
     * 所有通道
     */
    // 返回哪些“定义属性”
    private void inquireAllChannels(PCFMessageAgent agent, String connectionId) {
        try {
            PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL);
            req.addParameter(MQConstants.MQCACH_CHANNEL_NAME, "*");
            // 返回哪些“定义属性”
            req.addParameter(MQConstants.MQIACF_CHANNEL_ATTRS, new int[]{
                    MQConstants.MQCACH_CHANNEL_NAME,
                    MQConstants.MQIACH_CHANNEL_TYPE
            });

            PCFMessage[] resp = agent.send(req);
            if (resp == null || resp.length == 0) return;
            ArrayList<CollectMq> collectMqs = new ArrayList<>();
            for (PCFMessage m : resp) {
                String ch = m.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME).trim();
                int t = m.getIntParameterValue(MQConstants.MQIACH_CHANNEL_TYPE);

                CollectMq collectMq = new CollectMq();
                collectMq.setId(MyIdUtil.getId());
                collectMq.setConnectionId(connectionId);
                collectMq.setName(ch);

                if (t == MQConstants.MQCHT_SENDER || t == MQConstants.MQCHT_CLUSSDR) {
                    collectMq.setCategory("SEND_CHANNEL");
                    collectMqs.add(collectMq);
                } else if (t == MQConstants.MQCHT_RECEIVER || t == MQConstants.MQCHT_CLUSRCVR) {
                    collectMq.setCategory("RECEIVER_CHANNEL");
                    collectMqs.add(collectMq);
                }
            }
            if (!collectMqs.isEmpty()) {
                collectMqService.saveBatch(collectMqs);
            }

        } catch (Exception e) {
            log.error("MQ通道列表获取失败", e);
        }
    }

    /**
     * 队列深度
     */
    private void inquireQueueDepths(PCFMessageAgent agent, List<MqMonitor> queueList, Asset asset) {
        for (MqMonitor q : queueList) {
            try {
                PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
                req.addParameter(MQConstants.MQCA_Q_NAME, q.getName());
                req.addParameter(MQConstants.MQIACF_Q_ATTRS, new int[]{
                        MQConstants.MQCA_Q_NAME, MQConstants.MQIA_CURRENT_Q_DEPTH
                });
                PCFMessage[] resp = agent.send(req);
                if (ObjectUtil.isNull(resp) || resp.length == 0) {
                    continue;
                }
                q.setDepths(resp[0].getIntParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH));

                if (ObjectUtil.isNotNull(q.getRuleValue()) && q.getRuleValue() > 0) {
                    if (q.getDepths() > q.getRuleValue()) {
                        q.setState("NO");
                    } else {
                        q.setState("OK");
                    }
                    //推送告警或恢复
                    if (ObjectUtil.isNotNull(asset)) {
                        sendQueueAlarm(q, asset);
                    }
                }
                monitorService.updateById(q);
            } catch (Exception e) {
                log.error("MQ队列深度采集失败", e);
            }

        }
    }

    /**
     * 通道数据
     */
    private void inquireChannelFlows(PCFMessageAgent agent, List<MqMonitor> channelList) {
        for (MqMonitor ch : channelList) {
            try {
                PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL_STATUS);
                req.addParameter(MQConstants.MQCACH_CHANNEL_NAME, ch.getName());
                req.addParameter(MQConstants.MQIACH_CHANNEL_INSTANCE_TYPE, MQConstants.MQOT_CURRENT_CHANNEL);
                req.addParameter(MQConstants.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[]{
                        MQConstants.MQIACH_CHANNEL_STATUS,
                        MQConstants.MQIACH_BYTES_SENT,
                        MQConstants.MQIACH_BYTES_RCVD
                });
                PCFMessage[] resp = agent.send(req);
                if (ObjectUtil.isNull(resp) || resp.length == 0) {
                    continue;
                }
                PCFMessage r = resp[0];
                int st = r.getIntParameterValue(MQConstants.MQIACH_CHANNEL_STATUS);
                ch.setState(String.valueOf(st));
                long now = System.currentTimeMillis();

                long sentBytes = safeLong(r, MQConstants.MQIACH_BYTES_SENT);
                long rcvdBytes = safeLong(r, MQConstants.MQIACH_BYTES_RCVD);

                String key = ch.getConnectId() + "|" + ch.getName();
                ChannelFlowSnapshot last = channelFlowCache.get(key);

                long sentSpeedKb = 0;
                long rcvdSpeedKb = 0;

                if (last != null) {
                    long dt = now - last.ts;
                    if (dt > 0) {
                        sentSpeedKb = (sentBytes - last.sentBytes) * 1000 / dt / 1024;
                        rcvdSpeedKb = (rcvdBytes - last.rcvdBytes) * 1000 / dt / 1024;
                    }
                }

// 更新缓存
                ChannelFlowSnapshot snap = new ChannelFlowSnapshot();
                snap.sentBytes = sentBytes;
                snap.rcvdBytes = rcvdBytes;
                snap.ts = now;
                channelFlowCache.put(key, snap);

// 你真正要的“速度”
                ch.setSentKb(Math.max(sentSpeedKb, 0));
                ch.setRcvdKb(Math.max(rcvdSpeedKb, 0));
            } catch (Exception e) {
                ch.setState("UNKNOWN");
                ch.setSentKb(0);
                ch.setRcvdKb(0);
                log.error("通道状态采集失败: {}", ch.getName(), e);
            }
            monitorService.updateById(ch);
        }
    }

    private long safeLong(PCFMessage msg, int paramId) {
        try {
            return msg.getInt64ParameterValue(paramId);
        } catch (Exception ignore) {
            return 0;
        }
    }


    protected void sendQueueAlarm(MqMonitor queue, Asset asset) {
        MqMonitor oldQueue = monitorService.getById(queue.getId());
        if (ObjectUtil.isNotNull(oldQueue) && ObjectUtil.isNotNull(oldQueue.getState()) && !oldQueue.getState().equals(queue.getState())) {
            MQMonitorEntity monitorEntity = new MQMonitorEntity();
            monitorEntity.setCollectTime(new Date().getTime());
            monitorEntity.setAssetId(asset.getId());
            monitorEntity.setName(queue.getName());
            String messageFormat;
            if (queue.getState().equals("NO")) {
                monitorEntity.setStatus(EventLevelEnum.ABNORMAL.getCode());
                messageFormat = "MQ队列" + queue.getName() + "当前队列深度：" + queue.getDepths() + "，配置阈值深度：" + queue.getRuleValue() + "。";
                monitorEntity.setMessage(messageFormat);
            } else {
                monitorEntity.setStatus(EventLevelEnum.NORMAL.getCode());
                messageFormat = "MQ队列" + queue.getName() + "队列深度阈值状态恢复。";
                monitorEntity.setMessage(messageFormat);
            }
            MQAdapter mqAdapter = (MQAdapter) dataProcessManager.getAdapater("MQAdapter");
            mqAdapter.dispose(monitorEntity);
        }
    }

    private static class MqPcfHolder {
        MQQueueManager qm;
        PCFMessageAgent agent;
    }
}





