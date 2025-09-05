package com.jcca.dataProcessing.DataFilter.ping;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.other.bean.PingAssetStatus;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 组模式的单个设备ping
 * @className PingNoGroupFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pingNoGroupFilterHandler")
public class PingNoGroupFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "非组ping模式过滤", info.getAssetIp());
        String content = info.getContent();
        if (StrUtil.isEmpty(content) || !JSONUtil.isJsonArray(content)) {
            //异常数据
            return true;
        }
        List<PingAssetStatus> pingAssetStatusList = JSONUtil.toList(JSONUtil.parseArray(content),
                PingAssetStatus.class);
        if (Objects.nonNull(pingAssetStatusList) && pingAssetStatusList.size() > 1) {
            //组
            return true;
        }
        if (Objects.nonNull(pingAssetStatusList) && pingAssetStatusList.size() == 1) {
            PingAssetStatus pingAssetStatus = pingAssetStatusList.get(0);
            info.setAssetIp(pingAssetStatus.getAsset().getIp());
            info.setFlag(pingAssetStatus.getCurrStatus());
        }

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_ping.getCode();

        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getFlag());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getFlag());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);

            Integer status = info.getFlag() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String eventRedisKey = StatusInfoChangeTypeEnum.event_ping_no_group.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_ping_no_group.getDescr()));
            alarmTempReq.setCollectValue(changeInfo.getValue().toString());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_ping_no_group.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_ping_no_group.getDescr()));
                this.dispatureEvent(event);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
