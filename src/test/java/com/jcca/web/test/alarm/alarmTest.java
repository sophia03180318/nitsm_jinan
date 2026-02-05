package com.jcca.web.test.alarm;


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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;


/**
 * 测试资产
 *
 * @author lyp
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class alarmTest {

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


    @Test
    public void testMq() {
        List<MqConnection> connections = connectionService.list();
        if (ObjectUtil.isNotNull(connections)) {
            for (MqConnection connection : connections) {
                Asset asset = assetServ.findOneByIp(connection.getConnectHost());
                if (Objects.isNull(asset)) {
                    System.out.println("MQ未录入关联资产" + connection.getConnectHost());
                }
                PCFMessageAgent agent = null;
                try {
                    String ip = connection.getConnectHost();
                    Integer port = connection.getConnectPort();
                    String qmgr = connection.getConnectName();
                    String channel = connection.getChannelName();
                    String id = connection.getId();
                    agent = newAgent(ip, port, channel, qmgr);
                    inquireQmgrStatus(agent, connection, asset);
                    if ("RUNNING".equals(connection.getStatus())) {
                        collectMqService.removeCollectData(id);
                        inquireAllQueues(agent, id);
                        inquireAllChannels(agent, id);

                        List<MqMonitor> queueList = monitorService.getQueueByConnectionId(id);
                        if (ObjectUtil.isNotNull(queueList) && !queueList.isEmpty()) {
                            inquireQueueDepths(agent, queueList, asset);
                        }

                        List<MqMonitor> channelList = monitorService.getChannelByConnectionId(id);
                        if (ObjectUtil.isNotNull(channelList) && !queueList.isEmpty()) {
                            inquireChannelFlows(agent, channelList);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("MQ采集结果解析异常");
                } finally {
                    if (agent != null) {
                        try {
                            agent.disconnect();
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
    private PCFMessageAgent newAgent(String host, int port, String channel, String qmgr) throws Exception {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(MQConstants.HOST_NAME_PROPERTY, host);
        props.put(MQConstants.PORT_PROPERTY, port);
        props.put(MQConstants.CHANNEL_PROPERTY, channel);
        props.put(MQConstants.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
        MQQueueManager qm = new MQQueueManager(qmgr, props);
        return new PCFMessageAgent(qm);
    }

    /**
     * 获取连接状态和连接数
     */
    private void inquireQmgrStatus(PCFMessageAgent agent, MqConnection bean, Asset asset) {
        bean.setStatus("UNKNOWN");
        try {
            PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_MGR_STATUS);
            req.addParameter(MQConstants.MQIACF_Q_MGR_STATUS_ATTRS, new int[]{
                    MQConstants.MQIACF_Q_MGR_STATUS,
                    MQConstants.MQIACF_CONNECTION_COUNT
            });
            PCFMessage[] resp = agent.send(req);
            bean.setConnectionCount(0);
            PCFMessage r = resp[0];
            // 1) QMgr 状态（值是 int 枚举）
            int st = r.getIntParameterValue(MQConstants.MQIACF_Q_MGR_STATUS);
            if (st == 2) {
                bean.setStatus("RUNNING");
            } else {
                bean.setStatus("STOPPING");//不在运行状态就统称为关闭
            }
            // 2) 连接数
            int cc = r.getIntParameterValue(MQConstants.MQIACF_CONNECTION_COUNT);
            bean.setConnectionCount(cc);
            //推送告警
            if (ObjectUtil.isNotNull(asset)) {
                sendConnectionAlarm(bean, asset);
            }
            //存储连接
            connectionService.updateById(bean);
        } catch (Exception e) {
            System.out.println("MQ队列管理采集失败");
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
                    collectMqService.save(collectMq);
                } else if (qType == MQConstants.MQQT_REMOTE) {
                    collectMq.setCategory("MQQT_REMOTE");
                    collectMqService.save(collectMq);
                }
            }
        } catch (Exception e) {
            System.out.println("MQ队列列表获取失败");
        }
    }

    /**
     * 所有通道
     */
    // 返回哪些“定义属性”
    private void inquireAllChannels(PCFMessageAgent agent, String connectionId) {
        try {
            PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL);
            // 必选：通道名（支持通配）
            req.addParameter(MQConstants.MQCACH_CHANNEL_NAME, "*");
            // 返回哪些“定义属性”
            req.addParameter(MQConstants.MQIACF_CHANNEL_ATTRS, new int[]{
                    MQConstants.MQCACH_CHANNEL_NAME,
                    MQConstants.MQIACH_CHANNEL_TYPE
            });

            PCFMessage[] resp = agent.send(req);
            if (resp == null || resp.length == 0) return;

            for (PCFMessage m : resp) {
                String ch = m.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME).trim();
                int t = m.getIntParameterValue(MQConstants.MQIACH_CHANNEL_TYPE);

                CollectMq collectMq = new CollectMq();
                collectMq.setId(MyIdUtil.getId());
                collectMq.setConnectionId(connectionId);
                collectMq.setName(ch);

                if (t == MQConstants.MQCHT_SENDER || t == MQConstants.MQCHT_CLUSSDR) {
                    collectMq.setCategory("SEND_CHANNEL");
                    collectMqService.save(collectMq);
                } else if (t == MQConstants.MQCHT_RECEIVER || t == MQConstants.MQCHT_CLUSRCVR) {
                    collectMq.setCategory("RECEIVER_CHANNEL");
                    collectMqService.save(collectMq);
                }
            }
        } catch (Exception e) {
            System.out.println("MQ通道列表获取失败");
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
            } catch (Exception e) {
                System.out.println("MQ队列深度采集失败");
            }
            monitorService.updateById(q);
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
                PCFMessage r = resp[0];
                int st = r.getIntParameterValue(MQConstants.MQIACH_CHANNEL_STATUS);
                ch.setState(String.valueOf(st));
                ch.setSentKb(safeLong(r, MQConstants.MQIACH_BYTES_SENT) / 1024);
                ch.setRcvdKb(safeLong(r, MQConstants.MQIACH_BYTES_RCVD) / 1024);
            } catch (Exception e) {
                ch.setState("UNKNOWN");
                ch.setSentKb(0);
                ch.setRcvdKb(0);
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

    protected void sendConnectionAlarm(MqConnection connection, Asset asset) {
        System.out.println("队列异常");
    }

    protected void sendQueueAlarm(MqMonitor queue, Asset asset) {
       MqMonitor oldQueue = monitorService.getById(queue.getId());
        if (ObjectUtil.isNotNull(oldQueue.getState()) && !oldQueue.getState().equals(queue.getState())) {
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
            System.out.println(messageFormat);
        }
    }


}
