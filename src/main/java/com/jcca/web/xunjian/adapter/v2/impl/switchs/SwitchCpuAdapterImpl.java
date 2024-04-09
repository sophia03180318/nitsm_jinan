package com.jcca.web.xunjian.adapter.v2.impl.switchs;

import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 交换机巡检
 */
@Service
@Slf4j
public class SwitchCpuAdapterImpl implements XunjianV2Adapter {

    private static final String command = "mib采集";
    private static final String commandName = "CPU使用率";

    private static final int max = 80;

    @Resource
    private CollectCpuService collectCpuServ;

    @Override
    public Integer getMaxValue() {
        return max;
    }

    @Override
    public String getCode() {
        return XunjianItemCode.SWITCH_CPU;
    }

    @Override
    public String getName(Asset asset) {
        return commandName;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public XunjianDetailV2 xunJian(Asset asset, String xunjianRecordId,String orgMsgStr) {
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

        try {
            List<CollectCpu> realTimeData = collectCpuServ.getRealTimeData(asset.getId());
            StringBuilder orgMsg = new StringBuilder("");
            for (CollectCpu cpuItem : realTimeData) {
                orgMsg.append("Cpu使用率：");
                orgMsg.append(cpuItem.getCpuUsedRate());
                if(cpuItem.getCpuUsedRate()>max){
                    xunjianDetail.setInputOrgStr("cpu使用率"+cpuItem.getCpuFlg()+"大于"+max);
                    xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                    xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                }
            }
            xunjianDetail.setInputOrgStr(orgMsg.toString());
            return xunjianDetail;
        } catch (Exception e) {
            if(LogInputUtils.inputError(ServerTypeEnum.WEB_XUNJIAN)){
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_COLLECT_ERROR,asset.getIp(),e.getMessage()),e);
            }
        }


        return null;
    }
}
