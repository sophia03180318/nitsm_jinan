package com.jcca.web.xunjian.adapter.v2.impl.linux;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.CollectDiskService;
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
 * 磁盘巡检
 */
@Service
@Slf4j
public class LinuxDiskAdapterImpl implements XunjianV2Adapter {

    private static final String command = "df -h";
    private static final String commandName = "文件系统";

    private static final Integer MAX_NUM = 80;

    @Resource
    private CollectDiskService diskService;

    @Override
    public String getCode() {
        return XunjianItemCode.LINUX_DISK;
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

        List<CollectDisk> realTimeData = diskService.getRealTimeData(asset.getId());
        if(realTimeData.isEmpty()){
            xunjianDetail.setNormalFlag(XunjianDetailV2.UN_KNOW_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.UN_KNOW_FLAG_STR);
            xunjianDetail.setInputOrgStr("系统没有获取到文件系统信息，需要手动使用命令查看");
            return xunjianDetail;
        }
        StringBuilder orgStr = new StringBuilder("");
        StringBuilder errorStr = new StringBuilder("");
        for (CollectDisk realTimeDatum : realTimeData) {
            String mountPoint = realTimeDatum.getMountPoint();
            double usedRate = realTimeDatum.getUsedRate();
            orgStr.append("文件系统路径：【");
            orgStr.append(mountPoint);
            orgStr.append("】使用率：");
            orgStr.append(usedRate);
            orgStr.append("\r\n");
            if(realTimeDatum.getUsedRate()>MAX_NUM){
                errorStr.append("文件系统路径：【");
                errorStr.append(mountPoint);
                errorStr.append("】使用率：");
                errorStr.append(usedRate);
            }
        }
        if(StrUtil.isNotEmpty(errorStr.toString())){
            xunjianDetail.setNormalFlag(XunjianDetailV2.EXCEPTION_FLAG);
            xunjianDetail.setNormalFlagStr(XunjianDetailV2.EXCEPTION_FLAG_STR);
            xunjianDetail.setInputErrorStr(errorStr.toString());
        }
        xunjianDetail.setInputOrgStr(orgStr.toString());

        return xunjianDetail;
    }
}
