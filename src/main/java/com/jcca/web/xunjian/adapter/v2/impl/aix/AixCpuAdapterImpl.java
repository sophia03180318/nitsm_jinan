package com.jcca.web.xunjian.adapter.v2.impl.aix;

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
 * AIX CPU巡检
 */
@Slf4j
@Service
public class AixCpuAdapterImpl implements XunjianV2Adapter {

    private static final String command = "vmstat 5 3";
    private static final String commandName = "查看CPU 平均使用率";
    private static final Integer cpuMax = 80;

    @Resource
    private OutService outService;

    @Override
    public String getCode() {
        return XunjianItemCode.AIX_CPU;
    }

    @Override
    public String getName(Asset asset) {
        return commandName;
    }

    @Override
    public Integer getMaxValue() {
        return cpuMax;
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
            StringBuilder orgStr = new StringBuilder("");
            StringBuilder msgStr = new StringBuilder("");

            String sshResult = outService.getSSHResult(asset, command);
            //分析结果
            String[] split = sshResult.split("\\n+");

            boolean startFlg = false;
            BigDecimal usr = new BigDecimal(0);
            BigDecimal sys = new BigDecimal(0);
            BigDecimal idle = new BigDecimal(0);

            for (String line : split) {
                String lineTrim = line.trim();
                String lineUpper = lineTrim.toUpperCase();
                if (lineUpper.startsWith("R")) {
                    startFlg = true;
                    continue;
                }
                if(!startFlg){
                    continue;
                }
                //计算平均值
                String[] cellArray = lineTrim.split("\\s+");
                orgStr.append("【");
                orgStr.append("usr:");
                orgStr.append(cellArray[13]);
                orgStr.append("%,sys:");
                orgStr.append(cellArray[13]);
                orgStr.append("%,idle");
                orgStr.append(cellArray[13]);
                orgStr.append("%】");
                usr = usr.add(new BigDecimal(cellArray[13]));
                sys = sys.add(new BigDecimal(cellArray[14]));
                idle = idle.add(new BigDecimal(cellArray[15]));
            }
            BigDecimal usrAvg = usr.divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal sysAvg = usr.divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal idleAvg = usr.divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);

            msgStr.append("usr:");
            msgStr.append(usrAvg.doubleValue());
            msgStr.append("%,sys:");
            msgStr.append(sysAvg.doubleValue());
            msgStr.append("%,idle");
            msgStr.append(idleAvg.doubleValue());
            msgStr.append("%");


            if(usrAvg.doubleValue()>cpuMax.doubleValue()||sysAvg.doubleValue()>cpuMax.doubleValue()){
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
                xunjianDetail.setNormalFlagStr(msgStr.toString());
                xunjianDetail.setInputErrorStr(msgStr.toString());
            }else{
                xunjianDetail.setNormalFlagStr(msgStr.toString());
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
