package com.jcca.dataProcessing.DataFilter.bhm.storage;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.*;
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


@Component("bhmDiskStatusFilterHandler")
public class BhmDiskStatusFilterHandler extends IFilterHandler<CollectBhmStorageEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectBhmStorageEntity storage) throws ResultException, Exception {
        if(Objects.isNull(storage) ){
            return true;
        }

        List<ReadFishDiskEntity> diskInfos = storage.getDiskInfos();
        if(Objects.isNull(diskInfos) || diskInfos.isEmpty()){
            return true;
        }
        for (ReadFishDiskEntity disk : diskInfos) {
            String flag = disk.getName()+"_"+disk.getId();
            ReadFishStatusEntity bhmStatus = disk.getStatus();
            String redisKey = storage.getAssetIp() + ":" + storage.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_bhmDisk.getCode() + ":" + flag;

            //状态处理
            if(Objects.nonNull(bhmStatus) && StrUtil.isNotEmpty(bhmStatus.getHealth())){
                String statusKey = StatusInfoChangeTypeEnum.status_bhmDiskStatus.getCode();
                boolean statusChange = eventInfoChangeManagerService.infoIschange(storage.getInspectRecordId(),redisKey, statusKey, bhmStatus.getHealth());
                if(statusChange){
                    ChangeInfo statusChangeInfo = new ChangeInfo();
                    statusChangeInfo.setRedisKey(redisKey);
                    statusChangeInfo.setValue(bhmStatus.getHealth());
                    statusChangeInfo.setCollectTime(new Date(storage.getCollectTime()));
                    statusChangeInfo.setMapKey(statusKey);

                    storage.getMaps().put(statusKey, statusChangeInfo);

                    //事件上送
                    String eventRedisKey = StatusInfoChangeTypeEnum.event_bhmDisk.getCode();
                    String eventMapKey = storage.getAssetIp() + "_" + storage.getAssetId() + "_" + flag;
                    List<String> normalStatusList = Arrays.asList("OK");
                    Integer status = normalStatusList.contains(bhmStatus.getHealth().toUpperCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                    String statusInfoStr = "【启用状态："+bhmStatus.getState()+"健康状态："+bhmStatus.getHealth()+"】";

                    AlarmTempReq alarmTempReq = new AlarmTempReq();
                    alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_bhmDisk_state.getDescr(), disk.getName(), statusInfoStr));
                    alarmTempReq.setCollectValue(bhmStatus.getHealth());
                    alarmTempReq.setFlag(disk.getName());

                    this.addEventStatus(StatusInfoChangeTypeEnum.event_bhmDisk_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), disk.getName(), status, storage, statusChangeInfo);
                    IEvent event = eventInfoChangeManagerService.creatChangeEvent(storage.getAssetId(), statusChangeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,storage.getInspectRecordId(),"");
                    if (event != null) {
                        //上送事件
                        statusChangeInfo.setIsEvent(true);
                        event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_bhmDisk_state.getDescr(), disk.getName(), statusInfoStr));
                        this.dispatureEvent(event);
                    }
                }
            }


            if(StrUtil.isNotEmpty(disk.getManufacturer())){
                String Key = StatusInfoChangeTypeEnum.status_bhmDiskManufacturer.getCode();
                boolean infoChange = eventInfoChangeManagerService.infoIschange(storage.getInspectRecordId(),redisKey, Key, disk.getManufacturer());

                if(infoChange){
                    ChangeInfo cpuInfoChange = new ChangeInfo();
                    cpuInfoChange.setRedisKey(redisKey);
                    cpuInfoChange.setValue(disk.getManufacturer());
                    cpuInfoChange.setCollectTime(new Date(storage.getCollectTime()));
                    cpuInfoChange.setMapKey(Key);

                    storage.getMaps().put(Key, cpuInfoChange);
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
