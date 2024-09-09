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
import java.math.BigDecimal;
import java.util.Date;

/**
 * linux IO 采集
 */
@Service
@Slf4j
public class LinuxIoAdapterImpl implements XunjianV2Adapter {

    private static final String command = "iostat 2 3";
    private static final String commandName = "I/O";
    private static final Integer maxThreshold = 80;

    @Resource
    private OutService outServ;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_IO;
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
    public XunjianDetailV2 xunJian(Asset asset, String xunjianRecordId, String orgData) {
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

            boolean flag = false;
            BigDecimal user = new BigDecimal(0);
            BigDecimal sys = new BigDecimal(0);
            BigDecimal count = new BigDecimal(0);

            String[] lineArray = sshResult.split("\\n+");
            for (String line : lineArray) {
                String lineTrim = line.trim().toUpperCase();
                if (flag) {
                    //计算和
                    String[] cellArray = lineTrim.split("\\s+");
                    user = user.add(new BigDecimal(cellArray[0].trim()));
                    sys = sys.add(new BigDecimal(cellArray[2].trim()));
                    count = count.add(new BigDecimal(1));
                }
                flag = lineTrim.startsWith("AVG");
            }
            if (count.intValue() != 0) {
                BigDecimal userAvg = user.divide(count, 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal sysAvg = sys.divide(count, 2, BigDecimal.ROUND_HALF_UP);
                if (user.doubleValue() > maxThreshold || sys.doubleValue() > maxThreshold) {
                    //异常
                    String errorStr = String.format("【User】: %s,【system】: %s", userAvg.doubleValue(), sysAvg.doubleValue());
                    xunjianDetail.setInputErrorStr(errorStr);
                    xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                    xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
                }
            }

            xunjianDetail.setInputOrgStr(sshResult);
            return xunjianDetail;
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_XUNJIAN)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_XUNJIAN, ErrorCodeEnum.WEB_XUNJIAN_COLLECT_ERROR, asset.getIp(), e.getMessage()), e);
            }
        }

        return null;
    }
}
