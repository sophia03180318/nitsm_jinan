package com.jcca.web.xunjian.adapter.v2.impl.optical;


import cn.hutool.core.util.StrUtil;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.AppPattenUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.service.OutService;
import com.jcca.web.xunjian.adapter.v2.XunjianItemCode;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Adapter;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 光交巡检
 */
@Service
@Slf4j
public class OpticalStatusAdapterImpl implements XunjianV2Adapter {

    private static final String command = "switchshow";
    private static final String commandName = "查看交换机整体状态";

    @Resource
    private OutService outService;


    @Override
    public String getCode() {
        return XunjianItemCode.OPTICAL_STATUS;
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
            String[] lineArray = orgMsg.split("\\n+");
            StringBuilder errorStr = new StringBuilder("");
            for (String line : lineArray) {
                if(StrUtil.isEmpty(line)||line.contains(">")){
                    //结尾的输出符 跳过
                    continue;
                }
                String[] cellList = line.trim().split("\\s+");
                String cellOne = cellList[0];
                if(AppPattenUtils.isNumber(cellOne)){
                    if(!line.toLowerCase().contains("online")){
                        errorStr.append("【");
                        errorStr.append(line);
                        errorStr.append("】\r\n");
                    }
                }
            }

            if(StrUtil.isNotEmpty(errorStr.toString())){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                xunjianDetail.setInputErrorStr(errorStr.toString());
            }


            xunjianDetail.setInputOrgStr(orgMsg);
            return xunjianDetail;
        } catch (Exception e) {
            if(LogInputUtils.inputError(ServerTypeEnum.WEB_XUNJIAN)){
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_COLLECT_ERROR,asset.getIp(),e.getMessage()),e);
            }
        }


        return null;
    }
}
