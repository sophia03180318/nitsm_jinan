package com.jcca.web.xunjian.adapter.v2.impl.aix;

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
 * AIX 处理器
 */
@Slf4j
@Service
public class AixProcessorAdapterImpl implements XunjianV2Adapter {

    private static final String command = "lsdev -Cc processor";
    private static final String commandName = "小机处理器状态";

    @Resource
    private OutService outServ;

    @Override
    public String getCode() {
        return XunjianItemCode.AIX_PROCESSOR;
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

        try {
            String sshResult = outServ.getSSHResult(asset, command);
            //分析结果
            String[] split = sshResult.split("\\n+");
            StringBuilder errorStr = new StringBuilder("");
            for (String line : split) {
                String lineUpper = line.toUpperCase();
                if(lineUpper.contains("AVAILABLE")){
                    continue ;
                }
                errorStr.append(line);
                errorStr.append("\\n");
            }
            if(StrUtil.isNotEmpty(errorStr.toString())){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                xunjianDetail.setInputErrorStr(errorStr.toString());
            }
            xunjianDetail.setInputOrgStr(sshResult);
            //保存结果
            return xunjianDetail;
        } catch (Exception e) {
            if(LogInputUtils.inputError(ServerTypeEnum.WEB_XUNJIAN)){
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_COLLECT_ERROR,asset.getIp(),e.getMessage()),e);
            }
        }
        return null;
    }
}
