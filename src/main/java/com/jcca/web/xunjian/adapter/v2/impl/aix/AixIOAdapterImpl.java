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
 * AIX IO 数据采集
 */
@Service
@Slf4j
public class AixIOAdapterImpl implements XunjianV2Adapter {

    private static final String command = "iostat 1 5";
    private static final String commandName = "I/O";
    private static final double maxThreshold = 80;

    @Resource
    private OutService outService;

    @Override
    public String getCode() {
        return XunjianItemCode.AIX_IO;
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
        xunjianDetail.setId(MyIdUtil.getId());
        xunjianDetail.setCreateTime(date);
        xunjianDetail.setModifyTime(date);
        xunjianDetail.setAssetId(asset.getId());
        xunjianDetail.setAssetMode(asset.getAssetMode());
        xunjianDetail.setAssetName(asset.getName());

        try {
            String sshResult = outService.getSSHResult(asset, command);
            //分析结果
            boolean flag  = false;
            StringBuilder inputOutStr = new StringBuilder("");
            BigDecimal user = new BigDecimal(0);
            BigDecimal sys = new BigDecimal(0);
            BigDecimal idle = new BigDecimal(0);
            BigDecimal ioWait = new BigDecimal(0);
            BigDecimal count = new BigDecimal(0);

            String[] lineArray = sshResult.split("\\n+");
            for (String line : lineArray) {
                String lineTrim = line.trim().toUpperCase();
                if(flag){
                    //计算和
                    String[] cellArray = lineTrim.split("\\s+");
                    inputOutStr.append(String.format("【avg-cpu user: %s,sys: %s,idle: %s,iowait: %s 】",cellArray[2],cellArray[3],cellArray[4],cellArray[5]));
                    user =user.add(new BigDecimal(cellArray[2].trim()));
                    sys = sys.add(new BigDecimal(cellArray[3].trim()));
                    idle = idle.add(new BigDecimal(cellArray[4].trim()));
                    ioWait = ioWait.add(new BigDecimal(cellArray[5].trim()));
                    count = count.add(new BigDecimal(1));
                }
                if(lineTrim.startsWith("TTY")){
                    flag = true;
                }else{
                    flag = false;
                }
            }

            BigDecimal userAvg = user.divide(count, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal sysAvg = sys.divide(count, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal idleAvg = user.divide(count, 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal ioWaitAvg = user.divide(count, 3, BigDecimal.ROUND_HALF_UP);

            xunjianDetail.setNormalFlagStr("%Usr:"+userAvg+" % %Sys:"+sysAvg+" % %Idle:"+idleAvg+" %,ioWait:"+ioWaitAvg+" %");

            StringBuilder error = new StringBuilder("");
            if(userAvg.doubleValue()>maxThreshold){
                error.append("usr采集到");
                error.append(userAvg);
                error.append("% 超过80%");
            }
            if(sysAvg.doubleValue()>maxThreshold){
                error.append("sys采集到");
                error.append(sysAvg);
                error.append("% 超过80%");
            }
            if(idleAvg.doubleValue()>maxThreshold){
                error.append("idle采集到");
                error.append(idleAvg);
                error.append("% 超过80%");
            }
            if(ioWaitAvg.doubleValue()>maxThreshold){
                error.append("iowait采集到");
                error.append(ioWaitAvg);
                error.append("% 超过80%");
            }
            if(!error.toString().isEmpty()){
                xunjianDetail.setInputErrorStr(error.toString());
                xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
            }else{
                xunjianDetail.setNormalFlag(XunjianDetailV2.NORMAL_FLAG);
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
