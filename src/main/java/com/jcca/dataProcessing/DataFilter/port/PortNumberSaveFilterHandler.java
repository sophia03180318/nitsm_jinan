package com.jcca.dataProcessing.DataFilter.port;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectPortUsedNumberEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectPort;
import com.jcca.web.collect.service.CollectPortService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 端口使用数量过滤处理类
 * @className PortNumberFilterHandler
 * @date 2023/10/27 9:56
 * @since 2.1.0.0
 */
@Component("portNumberSaveFilterHandler")
public class PortNumberSaveFilterHandler extends IFilterHandler<CollectPortUsedNumberEntity> {

    @Resource
    private CollectPortService collectPortServ;

    @Override
    public boolean handler(CollectPortUsedNumberEntity info) {
        List<CollectPort> portList = new ArrayList<CollectPort>();
        List<String> tcpPortList = info.getTcpPortList();
        List<String> udpPortList = info.getUdpPortList();
        Date date = new Date();
        date.setTime(info.getCollectTime());
        for (String port : tcpPortList) {
            CollectPort portItem = new CollectPort();
            portItem.setId(MyIdUtil.getId());
            portItem.setType("TCP");
            portItem.setPortUsedRate(info.getUsedRate());
            portItem.setPortNum(port);
            portItem.setAssetId(info.getAssetId());
            portItem.setCollectTime(date);
            portItem.setCreateTime(new Date());
            portItem.setUsedCount(info.getUsedCount());
            portList.add(portItem);
        }
        for (String port : udpPortList) {
            CollectPort portItem = new CollectPort();
            portItem.setId(MyIdUtil.getId());
            portItem.setType("UDP");
            portItem.setPortNum(port);
            portItem.setAssetId(info.getAssetId());
            portItem.setCollectTime(date);
            portItem.setPortUsedRate(info.getUsedRate());
            portItem.setCreateTime(new Date());
            portItem.setUsedCount(info.getUsedCount());
            portList.add(portItem);
        }

        if (!portList.isEmpty()) {
            collectPortServ.updatePortList(portList, info.getAssetId());
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}
