package com.jcca.dataProcessing.DataFilter.commonFitler;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @description: 保存中心设备的时间
 * @author: Lvyp
 * @create: 2023/11/30 16:41
 */
@Component("commonSystemRestartFilterHandler")
public class CommonSystemRestartFilterHandler extends IFilterHandler<CommonEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Resource
    private AssetService assetService;

    @Override
    public boolean handler(CommonEntity info) {

        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_run_time_restart.getCode());
        if (ObjectUtil.isNotNull(changeInfo)) {
            //查询缓存中的历史运行时长
            Object state = eventInfoChangeManagerService.getStateValue(changeInfo.getRedisKey(), changeInfo.getMapKey());
            if (state == null) {
                return true;
            }
            Long timeduration = Long.parseLong(state.toString());
            Long newTimeduration = (Long) changeInfo.getValue();
            //重启事件  运行时长小于半小时
            String eventRedisKey = StatusInfoChangeTypeEnum.event_run_restart.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(StatusInfoChangeTypeEnum.event_run_restart.getDescr());
            alarmTempReq.setCollectValue(newTimeduration+"秒");
            if (newTimeduration < timeduration && newTimeduration < 1800) {
                Asset asset = assetService.getById(info.getAssetId());
                //windows snmp运行时长会归0
                if (Objects.nonNull(asset) && Objects.nonNull(asset.getCollectionType()) && asset.getCollectionType() == 1) {
                    if (timeduration <= 42940800) {
                        //添加状态监控（设备监控的事件信息是否正常）
                        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(),alarmTempReq);
                        if (event != null) {
                            //被事件信息截取
                            changeInfo.setIsEvent(true);
                            event.setDescStr(StatusInfoChangeTypeEnum.event_run_restart.getDescr());
                            this.dispatureEvent(event);
                        }
                    }
                } else {
                    IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.ABNORMAL.getCode(),alarmTempReq);
                    if (event != null) {
                        //被事件信息截取
                        changeInfo.setIsEvent(true);
                        event.setDescStr(StatusInfoChangeTypeEnum.event_run_restart.getDescr());
                        this.dispatureEvent(event);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
