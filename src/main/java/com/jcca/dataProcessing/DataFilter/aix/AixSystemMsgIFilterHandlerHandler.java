package com.jcca.dataProcessing.DataFilter.aix;


import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectAixSystemFattenEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * IFilterHandler处理流程作为数据处理类
 * IFilterHandler-------》IFilterHandler-------》IFilterHandler-------》支持组合式处理。
 * handler返回true，则到下一个IFilterHandler处理，返回false则无需下一个IFilterHandler进行处理
 *   如果有事件变动 ，使用this.dispatureEvent(event)将事件抛出，有其他监听器进行处理
 */

/**
 * @author Zhaozheng
 * @description TODO 小型机基础信息过滤处理类
 * @className AixSystemMsgIFilterHandlerHandler
 * @date 2023/10/27 9:21
 * @since 2.1.0.0
 */
@Slf4j
@Component("aixSystemMsgIFilterHandlerHandler")
public class AixSystemMsgIFilterHandlerHandler extends IFilterHandler<CollectAixSystemFattenEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectAixSystemFattenEntity info) {

        String redisKey= info.getAssetIp()+":"+info.getAssetId()+":"+StatusInfoChangeTypeEnum.status.getCode();
        String mapKey1=StatusInfoChangeTypeEnum.status_serial.getCode();
        String mapKey2=StatusInfoChangeTypeEnum.status_powerModel.getCode();
        String mapKey3=StatusInfoChangeTypeEnum.status_frequency.getCode();
        String mapKey4=StatusInfoChangeTypeEnum.status_cpuCoreNum.getCode();
        String mapKey5=StatusInfoChangeTypeEnum.status_powerNum.getCode();
        String mapKey6=StatusInfoChangeTypeEnum.status_diskNum.getCode();
        String mapKey7=StatusInfoChangeTypeEnum.status_cpuNum.getCode();
        String mapKey8=StatusInfoChangeTypeEnum.status_cpuMode.getCode();
        String mapKey9=StatusInfoChangeTypeEnum.status_systemVersion.getCode();
        String mapKey10=StatusInfoChangeTypeEnum.status_memory.getCode();
        String mapKey11=StatusInfoChangeTypeEnum.status_diskCapacityCount.getCode();
        String mapKey12=StatusInfoChangeTypeEnum.status_totalCapacity.getCode();

        boolean flag1= eventInfoChangeManagerService.infoIschange(redisKey, mapKey1,info.getSerial());
        boolean flag2= eventInfoChangeManagerService.infoIschange(redisKey, mapKey2,info.getPowerModel());
        boolean flag3= eventInfoChangeManagerService.infoIschange(redisKey, mapKey3,info.getFrequency());
        boolean flag4= eventInfoChangeManagerService.infoIschange(redisKey, mapKey4,info.getCpuCoreNum());
        boolean flag5= eventInfoChangeManagerService.infoIschange(redisKey, mapKey5,info.getPowerNum());
        boolean flag6= eventInfoChangeManagerService.infoIschange(redisKey, mapKey6,info.getDiskNum());
        boolean flag7= eventInfoChangeManagerService.infoIschange(redisKey, mapKey7,info.getCpuNum());
        boolean flag8= eventInfoChangeManagerService.infoIschange(redisKey, mapKey8,info.getCpuMode());
        boolean flag9= eventInfoChangeManagerService.infoIschange(redisKey, mapKey9,info.getSystemVersion());
        boolean flag10= eventInfoChangeManagerService.infoIschange(redisKey, mapKey10,info.getMemory());
        boolean flag11= eventInfoChangeManagerService.infoIschange(redisKey, mapKey11,info.getDiskCapacityCount());
        boolean flag12= eventInfoChangeManagerService.infoIschange(redisKey, mapKey12,info.getDiskCapacity());


        if(flag1){
            ChangeInfo changeInfo= this.createChangeInfo(info.getSerial(),redisKey,mapKey1);
            info.getMaps().put(mapKey1,changeInfo);
        }
        if(flag2){
            ChangeInfo changeInfo= this.createChangeInfo(info.getPowerModel(),redisKey,mapKey2);
            info.getMaps().put(mapKey2,changeInfo);
        }

        if(flag3){
            ChangeInfo changeInfo= this.createChangeInfo(info.getFrequency(),redisKey,mapKey3);
            info.getMaps().put(mapKey3,changeInfo);
        }
        if(flag4){
            ChangeInfo changeInfo= this.createChangeInfo(info.getCpuCoreNum(),redisKey,mapKey4);
            info.getMaps().put(mapKey4,changeInfo);
        }

        if(flag5){
            ChangeInfo changeInfo= this.createChangeInfo(info.getPowerNum(),redisKey,mapKey5);
            info.getMaps().put(mapKey5,changeInfo);
        }

        if(flag6){
            ChangeInfo changeInfo= this.createChangeInfo(info.getDiskNum(),redisKey,mapKey6);
            info.getMaps().put(mapKey6,changeInfo);
        }

        if(flag7){
            ChangeInfo changeInfo= this.createChangeInfo(info.getCpuNum(),redisKey,mapKey7);
            info.getMaps().put(mapKey7,changeInfo);
        }

        if(flag8){
            ChangeInfo changeInfo= this.createChangeInfo(info.getCpuMode(),redisKey,mapKey8);
            info.getMaps().put(mapKey8,changeInfo);
        }

        if(flag9){
            ChangeInfo changeInfo= this.createChangeInfo(info.getSystemVersion(),redisKey,mapKey9);
            info.getMaps().put(mapKey9,changeInfo);
        }

        if(flag10){
            ChangeInfo changeInfo= this.createChangeInfo(info.getMemory(),redisKey,mapKey10);
            info.getMaps().put(mapKey10,changeInfo);
        }

        if(flag11){
            ChangeInfo changeInfo= this.createChangeInfo(info.getDiskCapacityCount(),redisKey,mapKey11);
            info.getMaps().put(mapKey11,changeInfo);
        }

        if(flag12){
            ChangeInfo changeInfo= this.createChangeInfo(info.getDiskCapacity(),redisKey,mapKey12);
            info.getMaps().put(mapKey12,changeInfo);
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

    private ChangeInfo  createChangeInfo(Object value,String redisKey, String mapKey){
        ChangeInfo changeInfo=new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }
}
