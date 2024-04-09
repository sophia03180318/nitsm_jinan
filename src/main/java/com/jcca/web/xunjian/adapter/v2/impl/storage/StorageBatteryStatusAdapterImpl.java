package com.jcca.web.xunjian.adapter.v2.impl.storage;

import cn.hutool.core.util.StrUtil;
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
 * V系列存储
 */
@Service
@Slf4j
public class StorageBatteryStatusAdapterImpl implements XunjianV2Adapter {

    private static final String command = "lsenclosurebattery";
    private static final String commandName = "查看电池状态";

    @Resource
    private OutService outServ;

    @Override
    public String getCode() {
        return XunjianItemCode.STORAGE_BATTERY_STATUS;
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
            String[] lineArray = sshResult.trim().split("\\n+");

            StringBuilder errorStr = new StringBuilder();
            for (int i = 1;i<lineArray.length;i++){
                String line = lineArray[i];
                String[] cellArray = line.split("\\s+");
                String status = cellArray[2].toLowerCase();
                if(!"online".equals(status)){
                    errorStr.append(line);
                    errorStr.append("\\n");
                }
            }

            if(StrUtil.isNotEmpty(errorStr.toString())){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                xunjianDetail.setInputErrorStr(errorStr.toString());
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
