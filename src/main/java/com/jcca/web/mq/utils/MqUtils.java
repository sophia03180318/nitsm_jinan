package com.jcca.web.mq.utils;

import cn.hutool.core.util.ObjectUtil;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.jcca.web.mq.entity.MqConnection;
import com.jcca.web.mq.entity.MqMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.Hashtable;

/**
 * @description:
 * @author: sophia
 * @create: 2026/01/28 17:11
 **/
@Slf4j
public class MqUtils {
    public static MqSystemFattenBean disposeResult(MqConnection connection) {
        MqSystemFattenBean bean = new MqSystemFattenBean();
        PCFMessageAgent agent = null;
        try {
            String ip = connection.getConnectHost();
            Integer port = connection.getConnectPort();
            String qmgr = connection.getConnectName();
            String channel = connection.getChannelName();
            agent = newAgent(ip, port, channel, qmgr);
            inquireQmgrStatus(agent, bean);
            inquireAllQueues(agent, bean);
            inquireAllChannels(agent, bean);
            if (ObjectUtil.isNotNull(bean.getQueues())) {
                inquireQueueDepths(agent, bean);
            }

            if (ObjectUtil.isNotNull(bean.getChannels())) {
                inquireChannelFlows(agent, bean);
            }

            return bean;
        } catch (Exception e) {
            log.error("MQ采集结果解析异常", e);
        } finally {
            if (agent != null) {
                try {
                    agent.disconnect();
                } catch (Exception ignore) {
                }
            }
        }
        return bean;
    }

    /**
     * 获取连接
     */
    private static PCFMessageAgent newAgent(String host, int port, String channel, String qmgr) throws Exception {
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
    private static void inquireQmgrStatus(PCFMessageAgent agent, MqSystemFattenBean bean) throws Exception {
        PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_MGR_STATUS);
        req.addParameter(MQConstants.MQIACF_Q_MGR_STATUS_ATTRS, new int[]{
                MQConstants.MQIACF_Q_MGR_STATUS,
                MQConstants.MQIACF_CONNECTION_COUNT
        });

        PCFMessage[] resp = agent.send(req);
        bean.setQmgrStatus("UNKNOWN");
        bean.setConnectionCount(0);
        if (resp == null || resp.length == 0) {
            return;
        }
        PCFMessage r = resp[0];
        // 1) QMgr 状态（值是 int 枚举）
        int st = r.getIntParameterValue(MQConstants.MQIACF_Q_MGR_STATUS);
        if (st == 2) {
            bean.setQmgrStatus("RUNNING");
        } else {
            bean.setQmgrStatus("STOPPING");//不在运行状态就统称为关闭
        }
        // 2) 连接数
        int cc = r.getIntParameterValue(MQConstants.MQIACF_CONNECTION_COUNT);
        bean.setConnectionCount(cc);
    }

    /**
     * 所有队列
     */
    private static void inquireAllQueues(PCFMessageAgent agent, MqSystemFattenBean bean) throws Exception {
        PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
        req.addParameter(MQConstants.MQCA_Q_NAME, "*");
        req.addParameter(MQConstants.MQIACF_Q_ATTRS, new int[]{
                MQConstants.MQCA_Q_NAME, MQConstants.MQIA_Q_TYPE, MQConstants.MQIA_USAGE
        });

        PCFMessage[] resp = agent.send(req);
        if (resp == null) return;

        for (PCFMessage m : resp) {
            String qName = m.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
            int qType = m.getIntParameterValue(MQConstants.MQIA_Q_TYPE);

            if (qType == MQConstants.MQQT_LOCAL) {
                int usage = m.getIntParameterValue(MQConstants.MQIA_USAGE);
                if (usage == MQConstants.MQUS_TRANSMISSION) bean.getTransmissionQueues().add(qName);
                else bean.getLocalQueues().add(qName);
            } else if (qType == MQConstants.MQQT_REMOTE) {
                bean.getRemoteQueues().add(qName);
            }
        }
    }

    /**
     * 所有通道
     */
    private static void inquireAllChannels(PCFMessageAgent agent, MqSystemFattenBean bean) throws Exception {
        PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL);
        req.addParameter(MQConstants.MQCACH_CHANNEL_NAME, "*");
        req.addParameter(MQConstants.MQIACF_CHANNEL_ATTRS, new int[]{
                MQConstants.MQCACH_CHANNEL_NAME, MQConstants.MQIACH_CHANNEL_TYPE
        });

        PCFMessage[] resp = agent.send(req);
        if (resp == null) return;

        for (PCFMessage m : resp) {
            String ch = m.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME).trim();
            int t = m.getIntParameterValue(MQConstants.MQIACH_CHANNEL_TYPE);

            if (t == MQConstants.MQCHT_SENDER || t == MQConstants.MQCHT_CLUSSDR) bean.getSenderChannels().add(ch);
            else if (t == MQConstants.MQCHT_RECEIVER || t == MQConstants.MQCHT_CLUSRCVR)
                bean.getReceiverChannels().add(ch);
        }
    }

    /**
     * 队列深度
     */
    private static void inquireQueueDepths(PCFMessageAgent agent, MqSystemFattenBean bean) {
        for (MqMonitor q : bean.getQueues()) {
            try {
                PCFMessage req = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
                req.addParameter(MQConstants.MQCA_Q_NAME, q.getName());
                req.addParameter(MQConstants.MQIACF_Q_ATTRS, new int[]{
                        MQConstants.MQCA_Q_NAME, MQConstants.MQIA_CURRENT_Q_DEPTH
                });
                PCFMessage[] resp = agent.send(req);
                q.setDepths(resp[0].getIntParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH));
            } catch (Exception e) {
                q.setDepths(0);
                q.setState("UNKNOWN");
            }
            //直接判定阈值
            if (ObjectUtil.isNotNull(q.getRuleValue()) && q.getRuleValue() > 0 && q.getRuleValue() < q.getDepths()) {
                q.setState("NO");
            }
        }
    }

    /**
     * 通道数据
     */
    private static void inquireChannelFlows(PCFMessageAgent agent, MqSystemFattenBean
            bean) {


        for (MqMonitor ch : bean.getChannels()) {
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
        }
    }

    private static long safeLong(PCFMessage msg, int paramId) {
        try {
            return msg.getInt64ParameterValue(paramId);
        } catch (Exception ignore) {
            return -1;
        }
    }
}