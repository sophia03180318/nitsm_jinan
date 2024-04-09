package com.jcca.web.xunjian.adapter.v2.impl.storage;

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
public class StorageStatusAdapterImpl  implements XunjianV2Adapter {

    private static final String command = "lsenclosure";
    private static final String commandName = "查看磁盘阵列整体状态";

    @Resource
    private OutService outServ;

    @Override
    public String getCode() {
        return XunjianItemCode.STORAGE_STATUS;
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
            String cell = lineArray[1].trim();
            String[] cellArray = cell.split("\\s+");
            String status = cellArray[1].toLowerCase();

            if(!"online".equals(status)){
                xunjianDetail.setNormalFlag(XunjianDetailV2.NORMAL_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.NORMAL_FLAG_STR);
                xunjianDetail.setInputErrorStr(status);
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
