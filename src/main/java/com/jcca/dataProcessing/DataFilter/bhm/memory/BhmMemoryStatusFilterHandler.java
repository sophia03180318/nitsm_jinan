package com.jcca.dataProcessing.DataFilter.bhm.memory;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.ChangeInfo;

import com.jcca.dataProcessing.Entity.CollectBhmMemoryEntity;
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


@Component("bhmMemoryStatusFilterHandler")
public class BhmMemoryStatusFilterHandler extends IFilterHandler<List<CollectBhmMemoryEntity>> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(List<CollectBhmMemoryEntity> info) throws ResultException, Exception {
        if(Objects.isNull(info) || info.isEmpty()){
            return true;
        }
        for (CollectBhmMemoryEntity memory : info) {
            String flag = memory.getName()+"_"+memory.getId();
            ReadFishStatusEntity bhmStatus = memory.getStatus();
            String redisKey = memory.getAssetIp() + ":" + memory.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_bhmMemory.getCode() + ":" + flag;

            //状态处理
            if(Objects.nonNull(bhmStatus) && StrUtil.isNotEmpty(bhmStatus.getHealth())){
                String memoryStatusKey = StatusInfoChangeTypeEnum.status_bhmMemoryStatus.getCode();
                boolean statusChange = eventInfoChangeManagerService.infoIschange(memory.getInspectRecordId(),redisKey, memoryStatusKey, bhmStatus.getHealth());
                if(statusChange){
                    ChangeInfo statusChangeInfo = new ChangeInfo();
                    statusChangeInfo.setRedisKey(redisKey);
                    statusChangeInfo.setValue(bhmStatus.getHealth());
                    statusChangeInfo.setCollectTime(new Date(memory.getCollectTime()));
                    statusChangeInfo.setMapKey(memoryStatusKey);

                    memory.getMaps().put(memoryStatusKey, statusChangeInfo);

                    //事件上送
                    String eventRedisKey = StatusInfoChangeTypeEnum.event_bhmMemory_state.getCode();
                    String eventMapKey = memory.getAssetIp() + "_" + memory.getAssetId() + "_" + flag;
                    List<String> normalStatusList = Arrays.asList("OK");
                    Integer status = normalStatusList.contains(bhmStatus.getHealth().toUpperCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                    String statusInfoStr = "【启用状态："+bhmStatus.getState()+"健康状态："+bhmStatus.getHealth()+"】";

                    AlarmTempReq alarmTempReq = new AlarmTempReq();
                    alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_bhmMemory_state.getDescr(), memory.getName(), statusInfoStr));
                    alarmTempReq.setCollectValue(bhmStatus.getHealth());
                    alarmTempReq.setFlag(memory.getName());

                    this.addEventStatus(StatusInfoChangeTypeEnum.event_bhmMemory_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), memory.getName(), status, memory, statusChangeInfo);
                    IEvent event = eventInfoChangeManagerService.creatChangeEvent(memory.getAssetId(), statusChangeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,memory.getInspectRecordId());
                    if (event != null) {
                        //上送事件
                        statusChangeInfo.setIsEvent(true);
                        event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_bhmMemory_state.getDescr(), memory.getName(), statusInfoStr));
                        this.dispatureEvent(event);
                    }
                }
            }


            if(StrUtil.isNotEmpty(memory.getManufacturer())){
                String Key = StatusInfoChangeTypeEnum.status_bhmMemoryManufacturer.getCode();
                boolean infoChange = eventInfoChangeManagerService.infoIschange(memory.getInspectRecordId(),redisKey, Key, memory.getManufacturer());

                if(infoChange){
                    ChangeInfo cpuInfoChange = new ChangeInfo();
                    cpuInfoChange.setRedisKey(redisKey);
                    cpuInfoChange.setValue(memory.getManufacturer());
                    cpuInfoChange.setCollectTime(new Date(memory.getCollectTime()));
                    cpuInfoChange.setMapKey(Key);

                    memory.getMaps().put(Key, cpuInfoChange);
                }
            }

        }


        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
