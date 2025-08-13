package com.jcca.dataProcessing;

import com.jcca.dataProcessing.Entity.SnmpEventInfoEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;


@Component
@Slf4j
public class SnmpReceiver implements CommandResponder {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    public void init(String host) throws IOException {
        ThreadPool threadPool = ThreadPool.create("Trap", 5);
        MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool,
                new MessageDispatcherImpl());
        Address listenAddress = GenericAddress
                .parse("udp:" + host + "/" + SnmpConstants.DEFAULT_NOTIFICATION_RECEIVER_PORT); // 本地IP与监听端口
        TransportMapping transport;

        // 对TCP与UDP协议进行处理
        if (listenAddress instanceof UdpAddress) {
            transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
        } else {
            transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
        }
        Snmp snmp = new Snmp(dispatcher, transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.listen();

        snmp.addCommandResponder(this);
    }

    /**
     * IBM设备snmp告警信息
     * .1.3.6.1.4.1.2.6.158.5.1.1 "Timestamp of Local Date and Time when alert was generated" 时间
     * .1.3.6.1.4.1.2.6.158.5.1.3 "SP SystemIdentification - Text Identification"
     * .1.3.6.1.4.1.2.6.158.5.1.5 "Host System UUID(Universal Unique ID)" 系统UUID
     * .1.3.6.1.4.1.2.6.158.5.1.6 "Host System Serial Number" 序列号
     * .1.3.6.1.4.1.2.6.158.5.1.8 "Alert Severity Value - Critical Alert(0) - Non-Critical Alert(2) - System Alert(4)- Recovery(8)" 级别
     * .1.3.6.1.4.1.2.6.158.5.1.9 "Alert Message Text"
     * .1.3.6.1.4.1.2.6.158.5.1.10 "Alert Message ID"
     * .1.3.6.1.4.1.2.6.158.5.1.11 "Alert Message ID"
     * .1.3.6.1.4.1.2.6.158.5.1.12 "Host Contact" .1.3.6.1.4.1.2.6.158.5.1.13 "Host
     * Location"
     */
    @Override
    public void processPdu(CommandResponderEvent event) {
        if (Objects.isNull(event)) {
            log.info("【SNMP收到原始信息】：空的消息");
            return;
        }
//        log.info("【SNMP收到原始信息】：{}", JSONUtil.toJsonStr(event));

        Map<String, String> snmpMap = new HashMap<String, String>();

        PDU pdu = event.getPDU();
        if (Objects.isNull(pdu)) {
            log.info("【SNMP收到原始信息总PDU属性空】：忽略此消息");
            return;
        }

        Vector<VariableBinding> recVBs = (Vector<VariableBinding>) pdu.getVariableBindings();
        for (VariableBinding recVB : recVBs) {
            OID recVBOid = recVB.getOid();
            if (Objects.isNull(recVBOid)) {
                continue;
            }
            String oid = recVBOid.toString();
            Variable variable = recVB.getVariable();

            if (Objects.isNull(variable)) {
                continue;
            }
            snmpMap.put(oid, variable.toString());
        }

        IAdapter adapter = dataProcessManager.getAdapter(CollectConst.SNMP);

        String ip = event.getPeerAddress().toString().split("/")[0];
        SnmpEventInfoEntity snmpEventInfoEntity = new SnmpEventInfoEntity();
        snmpEventInfoEntity.setIp(ip);
        snmpEventInfoEntity.setMap(snmpMap);
        adapter.dispose(snmpEventInfoEntity);
        //获取当前处理数量
        adapter.dataProcess();
    }
}
