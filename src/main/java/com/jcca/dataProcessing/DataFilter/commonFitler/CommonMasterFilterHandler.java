package com.jcca.dataProcessing.DataFilter.commonFitler;

import cn.hutool.core.util.StrUtil;
import com.jcca.component.casco.enums.HostRunStatusEnum;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO casco主备状态过滤处理类
 * @className CascoMasterFilterHandler
 * @date 2023/10/27 9:24
 * @since 2.1.0.0
 */
@Slf4j
@Component("commonMasterFilterHandler")
public class CommonMasterFilterHandler extends IFilterHandler<ItsmQueueEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_softMasterState.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_softMasterState.getCode() + "_" + info.getEntityId() + "_" + info.getAbFlag();

        if (HostRunStatusEnum.STOP.name().equals(info.getHostType())) {
            log.info("业务主备消息：" + redisKey + mapKey + "[收到消息]：设备:" + info.getAssetIp() + " entityId:" + info.getEntityId() + " 主备状态：" + info.getHostType() + "主备旧状态" + info.getOldHostType() + "---已经被过滤");
            return false;
        }

        Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(info.getInspectRecordId(),redisKey, mapKey, info.getHostType());
        if (flag == null || flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getHostType());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);

            if (StrUtil.isEmpty(info.getHostType())) {
                return true;
            }

            if (flag != null && flag) {
                //如果缓存不为空，说明flag产生了变化
                String eventRedisKey = StatusInfoChangeTypeEnum.event_CTC_AB.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getEntityId() + "_" + info.getAbFlag() + "_" + changeInfo.getCollectTime();
                String str = HostRunStatusEnum.getName(info.getHostType());
                String strOld = HostRunStatusEnum.getName(info.getOldHostType());
                String format = String.format(StatusInfoChangeTypeEnum.event_CTC_AB.getDescr(), info.getEntityId(), info.getCascoSoftName(), strOld, str);

                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(format);
                alarmTempReq.setCollectValue(info.getAbFlag().toString());
                alarmTempReq.setFlag(info.getEntityId() + "_" + info.getAbFlag());
                this.addEventStatus(StatusInfoChangeTypeEnum.event_CTC_AB.getCode(), StatusInfoChangeTypeEnum.MASTER_CHANGE.getCode(), "", EventLevelEnum.ABNORMAL.getCode(), info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(), alarmTempReq);
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(format);
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
