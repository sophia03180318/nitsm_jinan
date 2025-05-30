package com.jcca.dataProcessing.DataFilter.memory;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectMemoryEntity;
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
 * @description TODO 内存普通阈值信息过滤处理类
 * @className MemoryFilterHandler
 * @date 2023/10/27 9:44
 * @since 2.1.0.0
 */
@Component("memoryFilterHandler")
public class MemoryFilterHandler extends IFilterHandler<CollectMemoryEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectMemoryEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "内存普通阈值信息过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_memoryState.getCode();

        if (info.getMemRate() == null) {
            BigDecimal usedRate = new BigDecimal(info.getMemUsed() * 100).divide(new BigDecimal(info.getMemTotal()), 2, BigDecimal.ROUND_HALF_UP);
            info.setMemRate(usedRate.doubleValue());
        }
        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getMemRate());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getMemRate());
        changeInfo.setIsChange(flag);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_memory_normal.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_memory_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL.getCode(),null);

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_memory_normal.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }


        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            ChangeInfo changeThresholdInfo = new ChangeInfo();
            changeThresholdInfo.setValue(threshold.getBaseValue());
            changeThresholdInfo.setRedisKey(redisThresholdKey);
            changeThresholdInfo.setCollectTime(new Date(info.getCollectTime()));
            changeThresholdInfo.setMapKey(thresholdMapKey);
            info.getMaps().put(thresholdMapKey, changeThresholdInfo);
        }


        if (changeInfo.getIsChange() || thresholdFlag) {
            Boolean compare = AppMathUtil.compare(info.getMemRate() + "", threshold.getBaseValue() + "");
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord=status==EventLevelEnum.NORMAL.getCode()?"":"超过";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_memory_normal.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue());

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setThresholdValue(threshold.getBaseValue()+"%");
            tempReq.setCollectValue(info.getMemRate()+"%");
            tempReq.setFlag("内存");


            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_memory_normal.getCode(),StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(),null, status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(descStr);
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
