package com.jcca.web.xunjian.adapter.v2.impl.aix;


import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectMemory;
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
 * aix 磁盘使用率巡检
 */
@Slf4j
@Service
public class AixSwatchMemoryAdapterImpl implements XunjianV2Adapter{

    private static final String command = "lsps -s";
    private static final String commandName = "查看交换区使用情况";
    private static final Integer maxThreshold = 80;

    @Resource
    private CollectMemoryService collectMemoryServ;

    @Override
    public String getCode() {
        return XunjianItemCode.AIX_SWATCH_MEMORY;
    }

    @Override
    public String getName(Asset asset) {
        return commandName;
    }

    @Override
    public Integer getMaxValue() {
        return maxThreshold;
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
        for (CollectMemory memory : realTimeData) {
            Double swapUsedRate = memory.getSwapUsedRate();
            xunjianDetail.setNormalFlagStr("Pct Used(Total):"+swapUsedRate+"%");
            if(swapUsedRate>maxThreshold){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
            }
        }

        xunjianDetail.setInputOrgStr(JSONUtil.toJsonStr(realTimeData));

        return xunjianDetail;
    }
}
