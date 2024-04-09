package com.jcca.component.thresholds.impl;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectPortUsedNumber;
import com.jcca.web.collect.entity.CollectPort;
import com.jcca.web.collect.service.CollectPortService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * 处理端口占用
 */
@Slf4j
@Component
public class DisposePortNumberAdapterImpl  implements CollectAdapter {

    private static final String SERVER_MAX_PORT = "65535";

    @Resource
    private CollectPortService collectPortServ;
    @Resource
    private EventLogicService eventLogicServ;


    @Override
    public void dispose(JSONArray data) {
        List<CollectPortUsedNumber> portNumbers = JSONUtil.toList(data, CollectPortUsedNumber.class);
        if(portNumbers.isEmpty()){
            log.error("【未获取到任何端口占用数据】：" + data.toString());
            return;
        }
        CollectPortUsedNumber collectPortUsedNumber = portNumbers.get(0);
        Double usedRate = collectPortUsedNumber.getUsedRate();
        TreeSet<String> tcpPortList = collectPortUsedNumber.getTcpPortList();
        TreeSet<String> udpPortList = collectPortUsedNumber.getUdpPortList();
        Date date = new Date();
        date.setTime(collectPortUsedNumber.getCollectTime());

        List<CollectPort> portList = new ArrayList<CollectPort>();

        for (String port : tcpPortList) {
            CollectPort portItem = new CollectPort();
            portItem.setId(MyIdUtil.getId());
            portItem.setType("TCP");
            portItem.setPortUsedRate(usedRate);
            portItem.setPortNum(port);
            portItem.setAssetId(collectPortUsedNumber.getAssetId());
            portItem.setCollectTime(date);
            portItem.setCreateTime(new Date());
            portItem.setUsedCount(collectPortUsedNumber.getUsedCount());
            portList.add(portItem);
        }

        for (String port : udpPortList) {
            CollectPort portItem = new CollectPort();
            portItem.setId(MyIdUtil.getId());
            portItem.setType("UDP");
            portItem.setPortNum(port);
            portItem.setAssetId(collectPortUsedNumber.getAssetId());
            portItem.setCollectTime(date);
            portItem.setPortUsedRate(collectPortUsedNumber.getUsedRate());
            portItem.setCreateTime(new Date());
            portItem.setUsedCount(collectPortUsedNumber.getUsedCount());
            portList.add(portItem);
        }

        //判定65535端口是否被占用，占用上报告警，未占用上报正常

        try {
            CreateEventReq createEventReq = new CreateEventReq();
            createEventReq.setBaseValue("");
            createEventReq.setBaseValue("");
            createEventReq.setFlag(SERVER_MAX_PORT);
            createEventReq.setCreateTime(date);
            createEventReq.setUniqueCode(EventUniqueCode.SERVER_PORT_USED);
            createEventReq.setAssetId(collectPortUsedNumber.getAssetId());
            createEventReq.setGroupFlag(EventUniqueCode.SERVER_PORT_USED);
            if(tcpPortList.contains(SERVER_MAX_PORT)){
                createEventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                createEventReq.setOriginalMsg(String.format("系统发现，端口%s已被占用！",SERVER_MAX_PORT));
                createEventReq.setRepoMsg(String.format("系统发现，端口%s已被占用！",SERVER_MAX_PORT));
            }else{
                createEventReq.setOriginalMsg(String.format("端口%s 未被占用！",SERVER_MAX_PORT));
                createEventReq.setRepoMsg(String.format("端口%s 未被占用！",SERVER_MAX_PORT));
                createEventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            }
            eventLogicServ.addEvent(createEventReq);

        } catch (Exception e) {
            log.error("处理端口占用事件异常：{}", e.getMessage(), e);
        }


        collectPortServ.updatePortList(portList,collectPortUsedNumber.getAssetId());
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.PORT_NUMBER;
    }
}
