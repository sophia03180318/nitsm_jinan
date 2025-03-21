package com.jcca.dataProcessing.DataFilter.disk;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectDiskEntity;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 磁盘阈值过滤处理类 普通阈值
 * @className DiskFilterHandler
 * @date 2023/10/27 9:31
 * @since 2.1.0.0
 */
@Component("diskFilterHandler")
public class DiskFilterHandler extends IFilterHandler<CollectDiskEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectDiskEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "磁盘普通阈值过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_disk.getCode();
        String mapKey = info.getName();
        if (info.getUsedRate() == null) {
            BigDecimal usedRate = new BigDecimal(info.getUsed() * 100).divide(new BigDecimal(info.getTotal()), 2, BigDecimal.ROUND_HALF_UP);
            info.setUsedRate(usedRate.doubleValue());
        }

        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getUsedRate());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getUsedRate());
        changeInfo.setIsChange(flag);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_disk_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_disk_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL.getCode(),info.getName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_disk_normal.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            ChangeInfo changeThresholdInfo = new ChangeInfo();
            changeThresholdInfo.setValue(threshold.getBaseValue());
            changeThresholdInfo.setRedisKey(redisThresholdKey);
            changeThresholdInfo.setCollectTime(new Date(info.getCollectTime()));
            changeThresholdInfo.setMapKey(thresholdMapKey);
            info.getMaps().put(thresholdMapKey, changeThresholdInfo);
        }

        //这里有大问题，磁盘有多个，当磁盘采集阈值不变，设定阈值改变得时候  第一个磁盘会进入这个里面，后面的都没办法进入了，造成后面的无法恢复,所以改变了 threshold 的MAP KEY
        if (changeInfo.getIsChange() || thresholdFlag) {
            Boolean compare = AppMathUtil.compare(info.getUsedRate() + "", threshold.getBaseValue() + "");
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_disk_normal.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"%");
            alarmTempReq.setThresholdValue(threshold.getBaseValue()+"%");
            alarmTempReq.setFlag(info.getName());
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_disk_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(),info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_disk_normal.getDescr(), info.getName(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));

                this.dispatureEvent(event);
            }
        }
        return true;


    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}
