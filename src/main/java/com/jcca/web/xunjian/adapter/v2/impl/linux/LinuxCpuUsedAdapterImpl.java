package com.jcca.web.xunjian.adapter.v2.impl.linux;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectNetworkCardService;
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
public class LinuxCpuUsedAdapterImpl implements XunjianV2Adapter {

    private static final String command = "vmstat 2 3";
    private static final String commandName = "查看CPU平均使用率";
    private static final Integer MAX_NUM = 80;

    @Resource
    private CollectCpuService cpuServ;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_CPU;
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

        List<CollectCpu> realTimeData = cpuServ.getRealTimeData(asset.getId());
        if(realTimeData.isEmpty()){
            xunjianDetail.setNormalFlag(XunjianDetailV2.UN_KNOW_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.UN_KNOW_FLAG_STR);
            xunjianDetail.setInputOrgStr("系统没有获取到CPU信息，需要手动使用命令查看");
            return xunjianDetail;
        }
        StringBuilder orgStr = new StringBuilder("");
        for (CollectCpu realTimeDatum : realTimeData) {
            if(realTimeDatum.getCpuUsedRate()>MAX_NUM){
                xunjianDetail.setInputErrorStr(String.format("CPU使用率：%s %",realTimeDatum.getCpuUsedRate()));
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
            }
            orgStr.append("CPU计算后使用率：");
            orgStr.append(realTimeDatum.getCpuUsedRate());
        }
        xunjianDetail.setInputOrgStr(orgStr.toString());

        return xunjianDetail;
    }
}
