package com.jcca.web.xunjian.adapter.v2.impl.linux;

import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * CPU使用率状态巡检
 */
@Service
@Slf4j
public class LinuxSwapUsedAdapterImpl implements XunjianV2Adapter {

    private static final String command = "vmstat 5 3";
    private static final String commandName = "查看交换区使用情况";
    private static final Integer MAX_NUM = 0;

    @Resource
    private OutService outServ;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_SWAP;
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

        try {
            String sshResult = outServ.getSSHResult(asset, command);
            String[] lineArray = sshResult.trim().split("\\n+");
            boolean flag = false;

            for (String line : lineArray) {
                if(line.toUpperCase().startsWith("R")){
                    flag = true;
                }
                if(flag){
                    String[] split = line.trim().split("\\s+");
                    int si = Integer.valueOf(split[6]);
                    if(si>MAX_NUM){
                        xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                        xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                        xunjianDetail.setInputErrorStr(si+"");
                    }
                }
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
