package com.jcca.dataProcessing.DataFilter.casco;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
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
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO casco软件连接过滤处理类
 * @className CascoLinkFitlerHandler
 * @date 2023/10/27 11:24
 * @since 2.1.0.0
 */
@Slf4j
@Component("cascoLinkFitlerHandler")
public class CascoLinkFitlerHandler extends IFilterHandler<ItsmQueueEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(ItsmQueueEntity info) {

        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "casco软件连接过滤处理类", info);

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_softLinkState.getCode();
        String mapKey = info.getEntityId() + "_" + info.getAbFlag() + "_" + info.getAttrGroupId() + "_" + info.getAttrIndex();

        if (StrUtil.isEmpty(info.getLinkStatus())) {
            return false;
        }
        Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(info.getInspectRecordId(), redisKey, mapKey, info.getLinkStatus());

        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getLinkStatus());
        changeInfo.setCollectTime(new Date());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        info.getMaps().put(mapKey, changeInfo);

        if (Objects.isNull(flag)) {
            return true;
        }
        //将所有状态全部推给事件，应为正常与异常切换的特别快，存在数据不统一的问题
        // if (flag) {
        String eventRedisKey = StatusInfoChangeTypeEnum.event_CTC_link.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getEntityId() + "_" + info.getAbFlag() + "_" + info.getAttrGroupId() + "_" + info.getAttrIndex();

        Integer status = info.getLinkStatus().toLowerCase().equals("up") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();

        //添加状态监控（设备监控的事件信息是否正常）
        this.addEventStatus(StatusInfoChangeTypeEnum.event_CTC_link.getCode(), StatusInfoChangeTypeEnum.LINK_STATUS.getCode(), info.getEntityId() + "_" + info.getAbFlag(), status, info, changeInfo);
        String str = status.equals(EventLevelEnum.ABNORMAL.getCode()) ? "异常，" : "恢复，";
        str = str + "实体号:" + info.getEntityId() + "，属性索引：" + info.getAttrIndex();
        if (!StringUtils.isEmpty(info.getProcessName())) {
            str = str + "，属性名称：" + info.getProcessName();
        }

        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_CTC_link.getDescr(), info.getCascoSoftName(), str));
        alarmTempReq.setCollectValue(info.getLinkStatus());
        alarmTempReq.setFlag(mapKey);
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq, info.getInspectRecordId());
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_CTC_link.getDescr(), info.getAssetIp(), str));
            this.dispatureEvent(event);
        }
        //  }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
