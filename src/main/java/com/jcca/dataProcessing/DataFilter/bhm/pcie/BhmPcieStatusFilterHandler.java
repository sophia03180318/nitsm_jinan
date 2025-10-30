package com.jcca.dataProcessing.DataFilter.bhm.pcie;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectBhmMemoryEntity;
import com.jcca.dataProcessing.Entity.CollectBhmPcieEntity;
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


@Component("bhmPcieStatusFilterHandler")
public class BhmPcieStatusFilterHandler extends IFilterHandler<CollectBhmPcieEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectBhmPcieEntity pcie) throws ResultException, Exception {
        if(Objects.isNull(pcie)){
            return true;
        }

        String flag = pcie.getName()+"_"+pcie.getId();
        ReadFishStatusEntity bhmStatus = pcie.getStatus();
        String redisKey = pcie.getAssetIp() + ":" + pcie.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_bhmPcie.getCode() + ":" + flag;

        //状态处理
        if(Objects.nonNull(bhmStatus) && StrUtil.isNotEmpty(bhmStatus.getHealth())){
            String statusKey = StatusInfoChangeTypeEnum.status_bhmPcieStatus.getCode();
            boolean statusChange = eventInfoChangeManagerService.infoIschange(pcie.getInspectRecordId(),redisKey, statusKey, bhmStatus.getHealth());
            if(statusChange){
                ChangeInfo statusChangeInfo = new ChangeInfo();
                statusChangeInfo.setRedisKey(redisKey);
                statusChangeInfo.setValue(bhmStatus.getHealth());
                statusChangeInfo.setCollectTime(new Date(pcie.getCollectTime()));
                statusChangeInfo.setMapKey(statusKey);

                pcie.getMaps().put(statusKey, statusChangeInfo);

                //事件上送
                String eventRedisKey = StatusInfoChangeTypeEnum.event_bhmPcie_state.getCode();
                String eventMapKey = pcie.getAssetIp() + "_" + pcie.getAssetId() + "_" + flag;
                List<String> normalStatusList = Arrays.asList("OK");
                Integer status = normalStatusList.contains(bhmStatus.getHealth().toUpperCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                String statusInfoStr = "【启用状态："+bhmStatus.getState()+"健康状态："+bhmStatus.getHealth()+"】";

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_bhmPcie_state.getDescr(), pcie.getName(), statusInfoStr));
                alarmTempReq.setCollectValue(bhmStatus.getHealth());
                alarmTempReq.setFlag(pcie.getName());

                this.addEventStatus(StatusInfoChangeTypeEnum.event_bhmPcie_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), pcie.getName(), status, pcie, statusChangeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(pcie.getAssetId(), statusChangeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,pcie.getInspectRecordId(),pcie.getVersion());
                if (event != null) {
                    //上送事件
                    statusChangeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_bhmPcie_state.getDescr(), pcie.getName(), statusInfoStr));
                    this.dispatureEvent(event);
                }
            }
        }


        if(StrUtil.isNotEmpty(pcie.getManufacturer())){
            String Key = StatusInfoChangeTypeEnum.status_bhmPcieManufacturer.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(pcie.getInspectRecordId(),redisKey, Key, pcie.getManufacturer());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(pcie.getManufacturer());
                cpuInfoChange.setCollectTime(new Date(pcie.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                pcie.getMaps().put(Key, cpuInfoChange);
            }
        }




        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
