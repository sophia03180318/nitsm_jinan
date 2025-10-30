package com.jcca.dataProcessing.DataFilter.netInterface;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
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

/**
 * @description: 阶段阈值处理类  最高级别
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("interfaceLosePacketsInStageThreeFilterHandler")
public class InterfaceLosePacketsInStageThreeFilterHandler extends IFilterHandler<CollectInterfaceEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Resource
    private ThresholdManager thresholdManager;
    @Override
    public boolean handler(CollectInterfaceEntity info) {

        String mapkey = StatusInfoChangeTypeEnum.status_interface_losePacketInRate.getCode();
        ChangeInfo changeInfo = info.getMaps().get(mapkey);
        if (changeInfo == null || changeInfo.getValue() == null) {
            return true;
        }

        String eventRedisKey = StatusInfoChangeTypeEnum.event_port_inLose_sectionThree.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getPortName();

        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_port_inLose_sectionThree.getCode(),StatusInfoChangeTypeEnum.SECTION_THREE.getCode(),info.getPortName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_port_inLose_normal.getCode(), info.getAssetId(), "");
        if (threshold.threeLevelIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getThreeLevelValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
        }
        if (changeInfo.getIsChange() || thresholdFlag) {
            boolean compare = threshold.getThreeLevelValue() < (Double) changeInfo.getValue();
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status == EventLevelEnum.NORMAL.getCode() ? "" : "超过";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_port_inLose_sectionThree.getDescr(), info.getPortName(), changeInfo.getValue(), keyWord, threshold.getThreeLevelValue());

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setThresholdValue(threshold.getThreeLevelValue()+"%");
            tempReq.setCollectValue(changeInfo.getValue()+"%");
            tempReq.setFlag(info.getPortName());

            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_port_inLose_sectionThree.getCode(),StatusInfoChangeTypeEnum.SECTION_THREE_VAL.getCode(),info.getPortName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId(),info.getVersion());
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
    public boolean isNeedNextHandle(Boolean flag) {
        return true;
    }


}
