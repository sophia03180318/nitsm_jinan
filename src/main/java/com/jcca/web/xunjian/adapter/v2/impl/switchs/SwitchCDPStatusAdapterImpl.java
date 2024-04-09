package com.jcca.web.xunjian.adapter.v2.impl.switchs;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 交换机巡检
 */
@Service
@Slf4j
public class SwitchCDPStatusAdapterImpl implements XunjianV2Adapter {

    private static final String command = "show cdp nei";
    private static final String commandName = "查看CDP邻居";


    @Override
    public String getCode() {
        return XunjianItemCode.SWITCH_CDP;
    }

    @Override
    public String getName(Asset asset) {
        return commandName;
    }
    @Override
    public Integer getMaxValue() {
        return null;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public XunjianDetailV2 xunJian(Asset asset, String xunjianRecordId,String orgMsg) {
        Date date = new Date();
        XunjianDetailV2 xunjianDetail = new XunjianDetailV2();
        xunjianDetail.setXunjianRecordId(xunjianRecordId);
        xunjianDetail.setXunjianTargetItem(commandName);
        xunjianDetail.setCommand(command);
        xunjianDetail.setNormalFlag(XunjianDetailV2.UN_KNOW_FLAG);
        xunjianDetail.setNormalFlagStr(XunjianDetailV2.UN_KNOW_FLAG_STR);
        xunjianDetail.setId(MyIdUtil.getId());
        xunjianDetail.setCreateTime(date);
        xunjianDetail.setModifyTime(date);
        xunjianDetail.setAssetId(asset.getId());
        xunjianDetail.setAssetMode(asset.getAssetMode());
        xunjianDetail.setAssetName(asset.getName());

        xunjianDetail.setInputOrgStr(orgMsg);
        return xunjianDetail;

    }
}
