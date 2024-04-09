package com.jcca.web.xunjian.adapter.v2.impl.linux;

import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.service.OutService;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * linux IO 采集
 */
@Service
@Slf4j
public class LinuxLoadAdapterImpl implements XunjianV2Adapter {

    private static final String command = "cat /proc/loadavg";
    private static final String commandName = "查看设备负载情况";
    private static final Integer max = 5;

    @Resource
    private OutService outServ;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_LOAD;
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

        try {
            String sshResult = outServ.getSSHResult(asset, command);
            String[] split = sshResult.trim().split("\\s+");
            Double aDouble = Double.valueOf(split[0]);
            if(aDouble>max){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                xunjianDetail.setInputErrorStr(sshResult);
            }
            xunjianDetail.setInputOrgStr(sshResult);
            return xunjianDetail;
        } catch (Exception e) {
            if(LogInputUtils.inputError(ServerTypeEnum.WEB_XUNJIAN)){
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_COLLECT_ERROR,asset.getIp(),e.getMessage()),e);
            }
        }
        return null;
    }
}
