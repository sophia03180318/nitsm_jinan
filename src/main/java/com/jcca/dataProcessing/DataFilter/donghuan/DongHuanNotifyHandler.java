package com.jcca.dataProcessing.DataFilter.donghuan;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.DongHuanEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO casco软件连接过滤处理类
 * @className CascoLinkFitlerHandler
 * @date 2023/10/27 11:24
 * @since 2.1.0.0
 */
@Component("dongHuanNotifyHandler")
public class DongHuanNotifyHandler extends IFilterHandler<DongHuanEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(DongHuanEntity info) {
        info.setCollectTime(info.getCreateTime().getTime());
        String eventRedisKey = StatusInfoChangeTypeEnum.event_environment_notify.getCode();
        String eventMapKey = info.getFlag();

        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setFlag(info.getFlag());
        alarmTempReq.setOrgMsg(info.getOriginalMsg());
        alarmTempReq.setAssetIp(info.getAssetIp());
        alarmTempReq.setAssetName(info.getAssetName());

        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), new ChangeInfo(), eventRedisKey, eventMapKey, -1,alarmTempReq,info.getInspectRecordId());
        if (event != null) {
            //被事件信息截取
            event.setDescLog(info.getOriginalMsg());
            event.setCollectTime(info.getCreateTime());
            this.dispatureEvent(event);
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
