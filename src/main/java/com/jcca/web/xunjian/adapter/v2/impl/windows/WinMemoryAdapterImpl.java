package com.jcca.web.xunjian.adapter.v2.impl.windows;

import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
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


@Slf4j
@Service
public class WinMemoryAdapterImpl implements XunjianV2Adapter {

    private static final String command = "mib采集";
    private static final String commandName = "内存";
    private static final int max = 80;

    @Resource
    private CollectMemoryService memoryService;

    @Override
    public String getCode() {
        return XunjianItemCode.WIN_MEMORY;
    }

    @Override
    public String getName(Asset asset) {
        return commandName;
    }

    @Override
    public Integer getMaxValue() {
        return max;
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
            List<CollectMemory> realTimeData = memoryService.getRealTimeData(asset.getId());
            StringBuilder orgMsg = new StringBuilder("");
            for (CollectMemory memoryItem : realTimeData) {
                orgMsg.append("内存使用率：");
                orgMsg.append(memoryItem.getMemUsedRate());
                if(memoryItem.getMemUsedRate()>max){
                    xunjianDetail.setInputOrgStr("内存使用率"+memoryItem.getMemUsedRate()+"大于"+max);
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
