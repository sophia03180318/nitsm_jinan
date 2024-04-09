package com.jcca.web.xunjian.adapter.v2.impl.linux;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * CPU使用率状态巡检
 */
@Service
@Slf4j
public class LinuxMemoryUsedAdapterImpl implements XunjianV2Adapter {

    private static final String command = "free -m";
    private static final String commandName = "查看物理内存使用情况";
    private static final Integer MAX_NUM = 90;

    @Resource
    private CollectMemoryService collectMemoryServ;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_MEMORY;
    }

    @Override
    public String getName(Asset asset) {
        return commandName;
    }

    @Override
    public Integer getMaxValue() {
        return MAX_NUM;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public XunjianDetailV2 xunJian(Asset asset, String xunjianRecordId,String orgData) {
        Date date = new Date();
        XunjianDetailV2 xunjianDetail = new XunjianDetailV2();
        xunjianDetail.setXunjianRecordId(xunjianRecordId);
        xunjianDetail.setXunjianTargetItem(commandName);
        xunjianDetail.setCommand(command);
        xunjianDetail.setNormalFlag(XunjianDetailV2.NORMAL_FLAG);
        xunjianDetail.setNormalFlagStr(XunjianDetailV2.NORMAL_FLAG_STR);
        xunjianDetail.setId(MyIdUtil.getId());
        xunjianDetail.setCreateTime(date);
        xunjianDetail.setModifyTime(date);
        xunjianDetail.setAssetId(asset.getId());
        xunjianDetail.setAssetMode(asset.getAssetMode());
        xunjianDetail.setAssetName(asset.getName());

        List<CollectMemory> realTimeData = collectMemoryServ.getRealTimeData(asset.getId());
        if(realTimeData.isEmpty()){
            xunjianDetail.setNormalFlag(XunjianDetailV2.UN_KNOW_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.UN_KNOW_FLAG_STR);
            xunjianDetail.setInputOrgStr("系统没有获取到内存信息，需要手动使用命令查看");
            return xunjianDetail;
        }
        StringBuilder orgStr = new StringBuilder("");
        for (CollectMemory realTimeDatum : realTimeData) {
            if(realTimeDatum.getMemUsedRate()>MAX_NUM){
                xunjianDetail.setInputErrorStr(String.format("内存使用率：%s %",realTimeDatum.getMemUsedRate()));
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
            }
            orgStr.append("内存计算后使用率：");
            orgStr.append(realTimeDatum.getMemUsedRate());
        }
        xunjianDetail.setInputOrgStr(orgStr.toString());

        return xunjianDetail;
    }
}
