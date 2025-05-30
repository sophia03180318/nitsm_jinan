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
 * @description: 上下限阈值
 * @author: Lvyp
 * @create: 2023/11/02 20:13
 */
@Component("interfaceTxPowerStageFilterHandler")
public class InterfaceTxPowerStageFilterHandler extends IFilterHandler<CollectInterfaceEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectInterfaceEntity info) {

        String mapkey = StatusInfoChangeTypeEnum.status_interface_txPower.getCode();
        ChangeInfo changeInfo = info.getMaps().get(mapkey);
        if (changeInfo == null || changeInfo.getValue() == null) {
            return true;
        }

        String eventRedisKey = StatusInfoChangeTypeEnum.event_port_optical_out_section.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getPortName();

        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_port_optical_out_section.getCode(), StatusInfoChangeTypeEnum.SECTION.getCode(), info.getPortName());

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_port_optical_out_normal.getCode(), info.getAssetId(), info.getPortName());
        if (threshold.sectionValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        String sectionValue = threshold.getMaxValue() + "_" + threshold.getMinValue();
        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, sectionValue);
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, sectionValue, info);
        }

        if (changeInfo.getIsChange() || thresholdFlag) {
            boolean compare = threshold.getMaxValue() < (Double) changeInfo.getValue() || threshold.getMinValue() > (Double) changeInfo.getValue();
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "不在";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_port_optical_out_section.getDescr(), info.getPortName(), changeInfo.getValue(), keyWord, threshold.getMinValue(), threshold.getMaxValue());

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setThresholdValue(threshold.getMinValue()+"dbm - "+threshold.getMaxValue() +"dbm");
            tempReq.setCollectValue(changeInfo.getValue()+"dbm");
            tempReq.setFlag(info.getPortName());

            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_port_optical_out_section.getCode(), StatusInfoChangeTypeEnum.SECTION_VAL.getCode(), info.getPortName(), status, info, changeInfo);

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
