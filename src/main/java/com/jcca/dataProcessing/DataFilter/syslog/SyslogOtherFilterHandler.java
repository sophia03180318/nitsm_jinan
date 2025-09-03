package com.jcca.dataProcessing.DataFilter.syslog;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.EventInfo;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
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
 * 未命中 IBM和CISCO规则的syslog
 * 都会默认的收入系统，并给出默认的类型
 * 此类型的syslog并不会上送告警。需要给一个
 * 具体的告警级别之后才能上送告警
 * 此类型必须放到最后
 */
@Slf4j
@Component("syslogOtherFilterHandler")
public class SyslogOtherFilterHandler extends IFilterHandler<SyslogEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(SyslogEventInfoEntity info) {
        StatusInfoChangeTypeEnum redisKeyStatus = StatusInfoChangeTypeEnum.event_syslog;
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + redisKeyStatus.getCode();
        //为了区别重复把时间添加上
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + new Date().getTime();

        String contentStr = info.getMessage();

        EventInfo eventInfo = new EventInfo();
        eventInfo.setMessage(contentStr);
        eventInfo.setMessageId(MyIdUtil.getId());
        eventInfo.setLevel(info.getLevel());

        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setCollectTime(new Date());
        changeInfo.setEventInfo(eventInfo);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(eventMapKey);

        Integer status = EventLevelEnum.ABNORMAL.getCode();

        AlarmTempReq tempReq = new AlarmTempReq();
        tempReq.setAssetIp(info.getAssetIp());
        tempReq.setOrgMsg(info.getMessage());

        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, StatusInfoChangeTypeEnum.event_syslog.getCode(), eventMapKey, status,tempReq,info.getInspectRecordId());
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescLog(String.format(redisKeyStatus.getDescr(), contentStr));
            //无法恢复事件
            this.dispatureEvent(event);
            return false;
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
