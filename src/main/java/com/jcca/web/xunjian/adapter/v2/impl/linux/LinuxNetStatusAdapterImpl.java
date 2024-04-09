package com.jcca.web.xunjian.adapter.v2.impl.linux;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;
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
 * 网卡状态巡检
 */
@Service
@Slf4j
public class LinuxNetStatusAdapterImpl implements XunjianV2Adapter {

    private static final String command = "ifconfig -a";
    private static final String commandName = "网卡状态";

    @Resource
    private CollectNetworkCardService netService;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_NET_STATUS;
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

        List<CollectNetworkCard> realTimeData = netService.getRealTimeData(asset.getId());
        if(realTimeData.isEmpty()){
            xunjianDetail.setNormalFlag(XunjianDetailV2.UN_KNOW_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.UN_KNOW_FLAG_STR);
            xunjianDetail.setInputOrgStr("系统没有获取到网卡信息，需要手动使用命令查看");
            return xunjianDetail;
        }
        StringBuilder orgStr = new StringBuilder("");
        for (CollectNetworkCard realTimeDatum : realTimeData) {
            orgStr.append("网卡：").append(realTimeDatum.getName());
            if(CollectNetCardStatus.DOWN.equals(realTimeDatum.getStatus())){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                orgStr.append("状态:DOWN \n\r ");
            }else{
                orgStr.append("状态:UP \n\r ");
            }
        }

        xunjianDetail.setInputErrorStr(orgStr.toString());
        xunjianDetail.setInputOrgStr(orgStr.toString());

        return xunjianDetail;
    }
}
