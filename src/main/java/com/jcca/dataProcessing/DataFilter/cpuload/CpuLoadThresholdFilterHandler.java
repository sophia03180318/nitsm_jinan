package com.jcca.dataProcessing.DataFilter.cpuload;

import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectCpuLoadBean;
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
import java.util.Date;

/**
 * @author: hhw
 * @description: CpuLoadThresholdFilterHandler 主要是用来处理应用服务器CPU负载阈值
 * @date: 2025-07-18  09:14
 * @since: 2.1.8.0
 */
@Component("cpuLoadThresholdFilterHandler")
public class CpuLoadThresholdFilterHandler extends IFilterHandler<CollectCpuLoadBean> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    /**
     * 处理方法
     *
     * @param info
     * @return 返回值true表示可以进入下一层处理，放回false
     * 表示整个处理结束，不会进入下层处理，不会保存缓存
     */
    @Override
    public boolean handler(CollectCpuLoadBean info) throws ResultException, Exception {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.event_cpuLoad.getCode();
        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(), redisKey, mapKey, info.getCpuLoadFifteen());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getCpuLoadFifteen());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        changeInfo.setIsChange(flag);
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_cpuLoad_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_cpuLoad_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL.getCode(), "");

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_cpuLoad_normal.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey, info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(), redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, threshold.getBaseValue(), info);
        }

        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (flag || thresholdFlag) {
            Boolean compare = AppMathUtil.compare(info.getCpuLoadFifteen(), threshold.getBaseValue() + "");
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_cpuLoad_normal.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(info.getCpuLoadFifteen());
            alarmTempReq.setThresholdValue(threshold.getBaseValue() + "");
            alarmTempReq.setFlag("cpuLoad");
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_cpuLoad_normal.getCode(), StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(), "", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq, info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_cpuLoad_normal.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
                changeInfo.setIsEvent(true);
                this.dispatureEvent(event);
            }
        }

        return true;
    }

    /**
     * 直接控制下层处理
     *
     * @param flag
     * @return 返回true则需要下层处理，返回false不需要下层处理，并且不会保存缓存
     */
    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
