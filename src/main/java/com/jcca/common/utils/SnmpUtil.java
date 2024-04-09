package com.jcca.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.*;

/**
 * 新的SNMP工具
 * <p>
 * 此工具使用的是:version2c
 *
 * @author Lvyp
 */
@Slf4j
public class SnmpUtil {

    private static final String PROTOCOL = "udp";
    private static final Integer PORT = 161;
    private static final Integer RETRIES = 2;
    private static final Integer TIMEOUT = 1000 * 3;
    private static final Integer VERSION = SnmpConstants.version2c;

    private static Snmp snmp = null;


    /**
     * 测试ip和团体名是否可用
     *
     * @param ipAddress
     * @param community
     * @return
     */
    public static Boolean testSnmp(String ipAddress, String community) {
        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            log.error("设备IP：" + ipAddress + "进行snmp测试，初始化snmp服务异常！" + e.toString(), e);
            return false;
        }

        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, null);
        // 创建报文
        PDU pdu = createPdu(".1", PDU.GETNEXT);
        ResponseEvent responseEvent = null;
        try {
            responseEvent = snmp.send(pdu, target);
        } catch (IOException e) {
            return false;
        }

        if (Objects.isNull(responseEvent) || Objects.isNull(responseEvent.getResponse())) {
            return false;
        }
        PDU response = responseEvent.getResponse();
        if (response.getErrorStatus() != 0) {
            return false;
        }
        return true;
    }


    /**
     * 初始化SNMP
     *
     * @throws IOException
     */
    public synchronized static void initSnmp() throws IOException {
        // 初始化多线程消息转发类
        if (Objects.nonNull(snmp)) {
            return;
        }
        MessageDispatcher messageDispatcher = new MessageDispatcherImpl();
        messageDispatcher.addMessageProcessingModel(new MPv1());
        messageDispatcher.addMessageProcessingModel(new MPv2c());

        //UdpAddress updAddr = (UdpAddress) GenericAddress.parse("udp:192.168.51.210/161");
        //DefaultUdpTransportMapping transportMapping = new DefaultUdpTransportMapping(updAddr);
        TransportMapping<?> transportMapping = new DefaultUdpTransportMapping();
        // 正式创建SNMP
        snmp = new Snmp(messageDispatcher, transportMapping);
        // 开启监听
        snmp.listen();

    }

    /**
     * SNMP GET采集
     *
     * @param ipAddress 被采设备IP
     * @param community 团体名
     * @param oid       命令
     * @return
     */
    public static SnmpExecuteResult snmpGet(String ipAddress, String community, Integer loginPort, String oid) {

        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            throw new ResultException(ResultEnum.ERROR, "初始化SNMP服务失败检查采集器所在服务器161端口是否启用:" + e.getMessage());
        }

        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, loginPort);

        // 创建报文
        PDU pdu = createPdu(oid, PDU.GET);

        log.debug("【SNMP-GET-SEND-REQ】:【pdu】:{},【target】:{}", JSONUtil.toJsonStr(pdu), JSONUtil.toJsonStr(target));
        ResponseEvent responseEvent = null;
        try {
//			if (Objects.nonNull(performanceTarget) && performanceTarget.getPingTunnel() != null
//					&& performanceTarget.getPingTunnel() == 1) {
//				PingTunnelService.checkTunnel(performanceTarget.getAsset());
//			}
            responseEvent = snmp.send(pdu, target);
        } catch (IOException e) {
            throw new ResultException(ResultEnum.ERROR, "SNMP采集访问目标主机失败：检查团体名、IP、或OID是否有误:" + e.getMessage());
        } catch (Exception e) {
            throw new ResultException(ResultEnum.ERROR, "SNMP采集发送SNMP消息异常:" + e.getMessage());
        }

        log.debug("【SNMP-GET-SEND-RESP】:" + JSONUtil.toJsonStr(responseEvent));

        PDU response = responseEvent.getResponse();
        if (Objects.isNull(response)) {
            throw new ResultException(ResultEnum.ERROR, "SNMP未响应任何信息，请检查oid是否有误");
        }
        Vector<? extends VariableBinding> variableBindings = response.getVariableBindings();

        SnmpExecuteResult result = new SnmpExecuteResult();
        for (VariableBinding variable : variableBindings) {
            OID subOid = variable.getOid();
            Variable subVariable = variable.getVariable();

            result.setOid(subOid.toString());
            result.setValue(subVariable.toString());
            return result;
        }

        return result;
    }

    /**
     * bulk方式获取
     *
     * @return
     */
    public static List<SnmpExecuteResult> snmpWalkBulk(String ipAddress, String community, Integer loginPort, String oid) {
        if (oid.startsWith(".")) {
            oid = oid.substring(1, oid.length());
        }
        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            throw new ResultException(ResultEnum.ERROR, "初始化SNMP服务失败检查采集器所在服务器161端口是否启用:" + e.getMessage());
        }

        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, loginPort);
        // 创建报文
        TableUtils tableUtils = new TableUtils(snmp, new PDUFactory() {
            @Override
            public PDU createPDU(Target arg0) {
                PDU request = new PDU();
                request.setType(PDU.GETBULK);
                return request;
            }

            @Override
            public PDU createPDU(MessageProcessingModel arg0) {
                // TODO Auto-generated method stub
                return null;
            }
        });

        OID[] columns = new OID[1];
        columns[0] = new OID(oid);

        List<TableEvent> list = tableUtils.getTable(target, columns, null, null);

        // 循环遍历结果
        List<SnmpExecuteResult> resultList = new ArrayList<SnmpExecuteResult>();

        for (TableEvent tableEvent : list) {
            VariableBinding[] variableBindingList = tableEvent.getColumns();
            if (variableBindingList == null) {
                throw new ResultException(ResultEnum.ERROR, "没有采集到任何信息，mib:" + oid);
            }
            for (VariableBinding item : variableBindingList) {
                String nextOid = item.getOid().toDottedString();
                SnmpExecuteResult result = new SnmpExecuteResult();
                result.setOid(nextOid);
                result.setValue(item.getVariable().toString());
                resultList.add(result);
            }
        }
        if (resultList.isEmpty()) {
            throw new ResultException(ResultEnum.ERROR, "没有采集到任何信息，mib:" + oid);
        }
        return resultList;

    }

    /**
     * SNMP WALK 采集
     *
     * @param ipAddress
     * @param community
     * @param oid
     * @return
     */
    public static List<SnmpExecuteResult> snmpWalk(String ipAddress, String community, Integer loginPort, String oid) {
        if (oid.startsWith(".")) {
            oid = oid.substring(1, oid.length());
        }
        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            throw new ResultException(ResultEnum.ERROR, "初始化SNMP服务失败检查采集器所在服务器161端口是否启用:" + e.getMessage());
        }
        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, loginPort);

        TableUtils tableUtils = new TableUtils(snmp, new PDUFactory() {
            @Override
            public PDU createPDU(Target arg0) {
                PDU request = new PDU();
                request.setType(PDU.GETBULK);
                return request;
            }

            @Override
            public PDU createPDU(MessageProcessingModel messageProcessingModel) {
                return new PDU();
            }
        });

        OID[] columns = new OID[1];
        columns[0] = new OID(oid);

        List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
        // 循环遍历结果
        List<SnmpExecuteResult> resultList = new ArrayList<SnmpExecuteResult>();
        if (CollUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TableEvent tableEvent = list.get(i);
                VariableBinding[] variableBindingList = tableEvent.getColumns();
                if (variableBindingList == null) {
                    throw new ResultException(ResultEnum.ERROR, "设备IP:" + ipAddress + "团体名" + community + "没有采集到任何信息，mib:" + oid);
                }
                for (VariableBinding item : variableBindingList) {
                    String nextOid = item.getOid().toDottedString();
                    SnmpExecuteResult result = new SnmpExecuteResult();
                    result.setOid(nextOid);
                    result.setValue(item.getVariable().toString());
                    resultList.add(result);
                }
            }
        }
        if (resultList.isEmpty()) {
            throw new ResultException(ResultEnum.ERROR, "没有采集到任何信息，mib:" + oid);
        }
        return resultList;
    }

    /**
     * 创建目标主机信息
     *
     * @param ipAddress
     * @param community
     * @return
     */
    private static Target createTarget(String ipAddress, String community, Integer loginPort) {
        //现在没有字段存储这个snmp端口 所以先去掉
        if (Objects.isNull(loginPort)) {
            //loginPort = PORT;
        }
        loginPort = PORT;

        Target target = new CommunityTarget();
        ((CommunityTarget) target).setCommunity(new OctetString(community));
        target.setVersion(VERSION);
        target.setSecurityModel(SecurityModel.SECURITY_MODEL_SNMPv2c);
        target.setAddress(GenericAddress.parse(PROTOCOL + ":" + ipAddress + "/" + loginPort));
        target.setRetries(RETRIES);
        target.setTimeout(TIMEOUT);
        return target;
    }

    /**
     * 创建请求报文
     *
     * @param oid
     * @param pduType PDU
     * @return
     */
    private static PDU createPdu(String oid, Integer pduType) {
        PDU pdu = new PDUv1();
        pdu.setType(pduType);
        pdu.add(new VariableBinding(new OID(oid)));
        return pdu;
    }

    /**
     * 测试snmpWalk 仅用于页面的工具测试
     *
     * @param ipAddress
     * @param community
     * @param oid
     * @param limit
     * @return
     */
    public static Map<String, Object> snmpWalkLog(String ipAddress, String community, Integer loginPort, String oid, Long limit) {
        Map<String, Object> repMap = new HashMap<>();
        List<PDU> repPduList = new ArrayList<>();
        if (oid.startsWith(".")) {
            oid = oid.substring(1, oid.length());
        }
        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            log.error("初始化SNMP失败：" + e.getMessage(), e);
        }

        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, loginPort);
        // 创建报文
        PDU pdu = createPdu(oid, PDU.GETNEXT);
        // 循环遍历结果
        List<SnmpExecuteResult> resultList = new ArrayList<SnmpExecuteResult>();
        try {

            boolean matched = true;
            Long nextCount = 0L;
            while (matched) {
                if (limit != null && nextCount >= limit) {
                    break;
                }
                if (nextCount > 10000) {
                    Map<String, String> reqMap = new HashMap<String, String>();
                    reqMap.put("ip", ipAddress);
                    reqMap.put("community", community);
                    reqMap.put("oid", oid);
                    log.error("snmpWalkTest GETNEXT次数超过:{}次,强制结束,参数:{}", nextCount, JSONUtil.toJsonStr(reqMap));
                    break;
                }
                ResponseEvent responseEvent = snmp.send(pdu, target);
                PDU response = responseEvent.getResponse();
                repPduList.add(response);
                if (Objects.isNull(responseEvent.getResponse())) {
                    break;
                }

                String nextOid = null;
                Vector<? extends VariableBinding> variableBindings = response.getVariableBindings();
                for (VariableBinding item : variableBindings) {
                    nextOid = item.getOid().toDottedString();
                    if (!nextOid.startsWith(oid)) {
                        matched = false;
                        break;
                    }
                    SnmpExecuteResult result = new SnmpExecuteResult();
                    result.setOid(nextOid);
                    result.setValue(item.getVariable().toString());
                    resultList.add(result);
                }

                if (!matched) {
                    break;
                }
                pdu.clear();
                pdu.add(new VariableBinding(new OID(nextOid)));
                nextCount++;
            }
        } catch (IOException e) {
            log.error("snmpWalkTest发送报文到目标主机失败:" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("snmpWalkTest发送报文到目标主机异常:" + e.getMessage(), e);
        }
        repMap.put("repPduList", JSONUtil.toJsonStr(repPduList));
        repMap.put("resultList", resultList);
        log.info("snmpWalkTest 响应repMap:{}", JSONUtil.toJsonStr(repMap));
        return repMap;

    }

    /**
     * 测试 snmpWalkBulk
     *
     * @param ipAddress
     * @param community
     * @param oid
     * @return
     */
    public static Map<String, Object> snmpWalkBulkLog(String ipAddress, String community, Integer loginPort, String oid, Long limit) {
        Map<String, Object> repMap = new HashMap<>();
        if (oid.startsWith(".")) {
            oid = oid.substring(1, oid.length());
        }
        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            throw new ResultException(ResultEnum.ERROR, "初始化SNMP服务失败检查采集器所在服务器161端口是否启用");
        }

        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, loginPort);
        // 创建报文
        // PDU pdu = createPdu(oid, PDU.GET);

        TableUtils tableUtils = new TableUtils(snmp, new PDUFactory() {
            @Override
            public PDU createPDU(Target arg0) {
                PDU request = new PDU();
                request.setType(PDU.GETBULK);
                return request;
            }

            @Override
            public PDU createPDU(MessageProcessingModel arg0) {
                // TODO Auto-generated method stub
                return null;
            }
        });

        OID[] columns = new OID[1];
        columns[0] = new OID(oid);

        List<TableEvent> list = tableUtils.getTable(target, columns, null, null);
        // 循环遍历结果
        List<SnmpExecuteResult> resultList = new ArrayList<SnmpExecuteResult>();
        if (CollUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (limit != null && limit == i) {
                    break;
                }
                TableEvent tableEvent = list.get(i);
                VariableBinding[] variableBindingList = tableEvent.getColumns();
                for (VariableBinding item : variableBindingList) {
                    String nextOid = item.getOid().toDottedString();
                    SnmpExecuteResult result = new SnmpExecuteResult();
                    result.setOid(nextOid);
                    result.setValue(item.getVariable().toString());
                    resultList.add(result);
                }
            }
        }
        repMap.put("resultList", resultList);
        repMap.put("repPduList", JSONUtil.toJsonStr(list));

        return repMap;
    }

    /**
     * 仅用于页面的工具测试
     *
     * @param ipAddress
     * @param community
     * @param oid
     * @return
     */
    public static Map<String, Object> snmpGetLog(String ipAddress, String community, Integer loginPort, String oid) {

        Map<String, Object> repMap = new HashMap<>();
        List<SnmpExecuteResult> resultList = new ArrayList<SnmpExecuteResult>();
        List<PDU> repPduList = new ArrayList<>();
        // 初始化SNMP
        try {
            if (Objects.isNull(snmp)) {
                initSnmp();
            }
        } catch (IOException e) {
            throw new ResultException(ResultEnum.ERROR, "初始化SNMP服务失败检查采集器所在服务器161端口是否启用");
        }

        // 创建目标主机信息
        Target target = createTarget(ipAddress, community, loginPort);

        // 创建报文
        PDU pdu = createPdu(oid, PDU.GET);

        log.debug("【SNMP-snmpGetLog-SEND-REQ】:【pdu】:{},【target】:{}", JSONUtil.toJsonStr(pdu),
                JSONUtil.toJsonStr(target));
        ResponseEvent responseEvent = null;
        try {
            responseEvent = snmp.send(pdu, target);
        } catch (IOException e) {
            log.error("发送报文到目标主机失败:" + e.getMessage(), e);
            throw new ResultException(ResultEnum.ERROR, "snmpGetLog采集访问目标主机失败：检查团体名、IP、或OID是否有误");
        } catch (Exception e) {
            log.error("发送报文到目标主机异常:" + e.getMessage(), e);
            throw new ResultException(ResultEnum.ERROR, "snmpGetLog采集发送SNMP消息异常");
        }

        log.debug("【SNMP-snmpGetLog-SEND-RESP】:" + JSONUtil.toJsonStr(responseEvent));

        PDU response = responseEvent.getResponse();

        if (Objects.isNull(response)) {
            log.error("SNMP响应回来的信息是空的");
            throw new ResultException(ResultEnum.ERROR, "SNMP未响应任何信息，请检查oid是否有误");
        }
        Vector<? extends VariableBinding> variableBindings = response.getVariableBindings();

        SnmpExecuteResult result = new SnmpExecuteResult();
        for (VariableBinding variable : variableBindings) {
            OID subOid = variable.getOid();
            Variable subVariable = variable.getVariable();

            result.setOid(subOid.toString());
            result.setValue(subVariable.toString());
            resultList.add(result);
        }
        repPduList.add(response);

        repMap.put("resultList", resultList);
        repMap.put("repPduList", JSONUtil.toJsonStr(repPduList));

        return repMap;
    }

}
