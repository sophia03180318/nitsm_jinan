package com.jcca.dataProcessing.DataFilter.bhm.cpu;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectBhmCpuEntity;
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


@Component("bhmCpuStatusFilterHandler")
public class BhmCpuStatusFilterHandler extends IFilterHandler<CollectBhmCpuEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectBhmCpuEntity cpu) throws ResultException, Exception {
        if(Objects.isNull(cpu)){
            return true;
        }
        String flag = cpu.getName()+"_"+cpu.getId();
        ReadFishStatusEntity bhmStatus = cpu.getStatus();
        String redisKey = cpu.getAssetIp() + ":" + cpu.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_bhmCpu.getCode() + ":" + flag;

        //状态处理
        if(Objects.nonNull(bhmStatus) && StrUtil.isNotEmpty(bhmStatus.getHealth())){
            String cpuStatusKey = StatusInfoChangeTypeEnum.status_bhmCpuStatus.getCode();
            boolean statusChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, cpuStatusKey, bhmStatus.getHealth());
            if(statusChange){
                ChangeInfo statusChangeInfo = new ChangeInfo();
                statusChangeInfo.setRedisKey(redisKey);
                statusChangeInfo.setValue(bhmStatus.getHealth());
                statusChangeInfo.setCollectTime(new Date(cpu.getCollectTime()));
                statusChangeInfo.setMapKey(cpuStatusKey);

                cpu.getMaps().put(cpuStatusKey, statusChangeInfo);

                //事件上送
                String eventRedisKey = StatusInfoChangeTypeEnum.event_bhmCpu_state.getCode();
                String eventMapKey = cpu.getAssetIp() + "_" + cpu.getAssetId() + "_" + flag;
                List<String> normalStatusList = Arrays.asList("OK");
                Integer status = normalStatusList.contains(bhmStatus.getHealth().toUpperCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
                String statusInfoStr = "【启用状态："+bhmStatus.getState()+"健康状态："+bhmStatus.getHealth()+"】";

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_bhmCpu_state.getDescr(), cpu.getName(), statusInfoStr));
                alarmTempReq.setCollectValue(bhmStatus.getHealth());
                alarmTempReq.setFlag(cpu.getName());

                this.addEventStatus(StatusInfoChangeTypeEnum.event_bhmCpu_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), cpu.getName(), status, cpu, statusChangeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(cpu.getAssetId(), statusChangeInfo, eventRedisKey, eventMapKey, status, alarmTempReq,cpu.getInspectRecordId(),cpu.getVersion());
                if (event != null) {
                    //上送事件
                    statusChangeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_bhmCpu_state.getDescr(), cpu.getName(), statusInfoStr));
                    this.dispatureEvent(event);
                }
            }
        }


        if(StrUtil.isNotEmpty(cpu.getManufacturer())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuManufacturer.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getManufacturer());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getManufacturer());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }
        if(StrUtil.isNotEmpty(cpu.getModel())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuModel.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getModel());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getModel());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }
        if(StrUtil.isNotEmpty(cpu.getInstructionSet())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuInstructionSet.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getInstructionSet());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getInstructionSet());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }
        if(StrUtil.isNotEmpty(cpu.getProcessorArchitecture())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuProcessorArchitecture.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getProcessorArchitecture());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getProcessorArchitecture());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }
        if(Objects.nonNull(cpu.getTotalCores())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuTotalCores.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getTotalCores());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getTotalCores());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }
        if(Objects.nonNull(cpu.getTotalThreads())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuTotalThreads.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getTotalThreads());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getTotalThreads());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }
        if(Objects.nonNull(cpu.getMaxSpeedMHz())){
            String Key = StatusInfoChangeTypeEnum.status_bhmCpuMaxSpeedMHz.getCode();
            boolean infoChange = eventInfoChangeManagerService.infoIschange(cpu.getInspectRecordId(),redisKey, Key, cpu.getMaxSpeedMHz());

            if(infoChange){
                ChangeInfo cpuInfoChange = new ChangeInfo();
                cpuInfoChange.setRedisKey(redisKey);
                cpuInfoChange.setValue(cpu.getMaxSpeedMHz());
                cpuInfoChange.setCollectTime(new Date(cpu.getCollectTime()));
                cpuInfoChange.setMapKey(Key);

                cpu.getMaps().put(Key, cpuInfoChange);
            }
        }


        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
