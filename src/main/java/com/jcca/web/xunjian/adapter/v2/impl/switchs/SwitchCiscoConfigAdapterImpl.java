package com.jcca.web.xunjian.adapter.v2.impl.switchs;


import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.utils.enums.ManufacturersEnum;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 备份获取思科日志配置
 */
@Service
@Slf4j
public class SwitchCiscoConfigAdapterImpl implements XunjianV2Adapter {
    /**
     * 含有|PWD|标识的表示需要进入特权模式，需要输入密码
     */
    private static final String command = "enTPWDTshow run";
    private static final String commandName = "备份cisco交换机配置";
    private static final Integer CISCO_MANUFACTURER_ID =4;

    @Override
    public String getCode() {
        return XunjianItemCode.CISCO_CONFIG;
    }
    @Override
    public Integer getMaxValue() {
        return null;
    }
    @Override
    public String getName(Asset asset) {
        if(!CISCO_MANUFACTURER_ID.equals(asset.getManufacturerId())){
            return "";
        }
        return commandName;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public XunjianDetailV2 xunJian(Asset asset, String xunjianRecordId, String orgMsg) {

        Date date = new Date();
        XunjianDetailV2 xunjianDetail = new XunjianDetailV2();
        if(orgMsg.length()>10){
            xunjianDetail.setNormalFlag(XunjianDetailV2.NORMAL_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.NORMAL_FLAG_STR);
        }else{
            xunjianDetail.setNormalFlag(XunjianDetailV2.UN_KNOW_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.UN_KNOW_FLAG_STR);
        }
        xunjianDetail.setXunjianRecordId(xunjianRecordId);
        xunjianDetail.setXunjianTargetItem(commandName);
        xunjianDetail.setCommand(command);

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
