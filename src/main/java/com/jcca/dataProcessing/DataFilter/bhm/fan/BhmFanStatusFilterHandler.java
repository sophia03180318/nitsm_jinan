package com.jcca.dataProcessing.DataFilter.bhm.fan;


import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectBhmFanEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 风扇状态
 */
@Component("bhmFanStatusFilterHandler")
public class BhmFanStatusFilterHandler  extends IFilterHandler<CollectBhmFanEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectBhmFanEntity fan) throws ResultException, Exception {
        if(Objects.isNull(fan)){
            return false;
        }

        String flag = fan.getName()+"_"+fan.getMemberId();
        ReadFishStatusEntity bhmStatus = fan.getStatus();
        String redisKey = fan.getAssetIp() + ":" + fan.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_fan.getCode() + ":" + flag;

        //状态处理
        if(Objects.nonNull(bhmStatus) && StrUtil.isNotEmpty(bhmStatus.getHealth())){
            String fanStatusKey = StatusInfoChangeTypeEnum.status_fanStatus.getCode();
            boolean statusChange = eventInfoChangeManagerService.infoIschange(fan.getInspectRecordId(),redisKey, fanStatusKey, bhmStatus.getHealth());
            if(statusChange){
                ChangeInfo statusChangeInfo = new ChangeInfo();
                statusChangeInfo.setRedisKey(redisKey);
                statusChangeInfo.setValue(bhmStatus.getHealth());
                statusChangeInfo.setCollectTime(new Date(fan.getCollectTime()));
                statusChangeInfo.setMapKey(fanStatusKey);

                fan.getMaps().put(fanStatusKey, statusChangeInfo);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_fan_state.getCode();
                String eventMapKey = fan.getAssetIp() + "_" + fan.getAssetId() + "_" + flag;
                List<String> normalStatusList = Arrays.asList("OK");

                Integer status = normalStatusList.contains(bhmStatus.getHealth().toUpperCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();

                String statusInfoStr = "【启用状态："+bhmStatus.getState()+"健康状态："+bhmStatus.getHealth()+"】";

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_fan_state.getDescr(), fan.getName(), statusInfoStr));
                alarmTempReq.setCollectValue(bhmStatus.getHealth());
                alarmTempReq.setFlag(fan.getName());

                this.addEventStatus(StatusInfoChangeTypeEnum.event_fan_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), fan.getName(), status, fan, statusChangeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(fan.getAssetId(), statusChangeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,fan.getInspectRecordId(),fan.getVersion());
                if (event != null) {
                    //上送风扇类的事件
                    statusChangeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_fan_state.getDescr(), fan.getName(), statusInfoStr));
                    this.dispatureEvent(event);
                }
            }
        }

        //保存转速信息
        if(Objects.nonNull(fan.getMaxReadingRange())){
            String fanMaxReadingKey = StatusInfoChangeTypeEnum.status_fanMaxReadingRange.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(fan.getInspectRecordId(),redisKey, fanMaxReadingKey, fan.getMaxReadingRange());

            if(infoChange){
                ChangeInfo maxReadingChangeInfo = new ChangeInfo();
                maxReadingChangeInfo.setRedisKey(redisKey);
                maxReadingChangeInfo.setValue(fan.getMaxReadingRange());
                maxReadingChangeInfo.setCollectTime(new Date(fan.getCollectTime()));
                maxReadingChangeInfo.setMapKey(fanMaxReadingKey);

                fan.getMaps().put(fanMaxReadingKey, maxReadingChangeInfo);
            }
        }
        //最小转速
        if(Objects.nonNull(fan.getMinReadingRange())){
            String fanMinReadingKey = StatusInfoChangeTypeEnum.status_fanMinReadingRange.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(fan.getInspectRecordId(),redisKey, fanMinReadingKey, fan.getMinReadingRange());

            if(infoChange){
                ChangeInfo maxReadingChangeInfo = new ChangeInfo();
                maxReadingChangeInfo.setRedisKey(redisKey);
                maxReadingChangeInfo.setValue(fan.getMinReadingRange());
                maxReadingChangeInfo.setCollectTime(new Date(fan.getCollectTime()));
                maxReadingChangeInfo.setMapKey(fanMinReadingKey);

                fan.getMaps().put(fanMinReadingKey, maxReadingChangeInfo);
            }
        }
        //当前转速
        if(Objects.nonNull(fan.getReading())){
            String fanReadingKey = StatusInfoChangeTypeEnum.status_fanvalue.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(fan.getInspectRecordId(),redisKey, fanReadingKey, fan.getMinReadingRange());

            if(infoChange){
                ChangeInfo maxReadingChangeInfo = new ChangeInfo();
                maxReadingChangeInfo.setRedisKey(redisKey);
                maxReadingChangeInfo.setValue(fan.getReading());
                maxReadingChangeInfo.setCollectTime(new Date(fan.getCollectTime()));
                maxReadingChangeInfo.setMapKey(fanReadingKey);

                fan.getMaps().put(fanReadingKey, maxReadingChangeInfo);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
