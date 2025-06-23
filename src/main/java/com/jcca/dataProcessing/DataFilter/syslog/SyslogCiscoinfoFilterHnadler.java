package com.jcca.dataProcessing.DataFilter.syslog;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component("syslogCiscoinfoFilterHnadler")
public class SyslogCiscoinfoFilterHnadler extends IFilterHandler<SyslogEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(SyslogEventInfoEntity info) {

        EventInfo eventInfo = null;

        String patternstr = "%\\w*-" + info.getLevel() + "-\\w*:";
        Pattern patten = Pattern.compile(patternstr);
        // 现在创建 matcher 对象
        Matcher m = patten.matcher(info.getMessage());
        if (m.find()) {
            String alarmPatternString = m.group(0);
            eventInfo = new EventInfo();
            eventInfo.setMessageId(alarmPatternString.substring(0, alarmPatternString.length() - 1));
        }

        String patternTest = "%\\w*-" + info.getLevel() + "-\\w*:.*$";//cisco设备匹配

        Pattern patten1 = Pattern.compile(patternTest);

        Matcher m1 = patten1.matcher(info.getMessage());
        if (m1.find()) {
            String alarmPatternString = m1.group(0);
            eventInfo.setMessageId(alarmPatternString);

        }
        if (eventInfo == null) {
            return true;
        }
        //修改message，添加事件ID
        eventInfo.setMessage(info.getMessage() + "," + "当前事件ID：" + eventInfo.getMessageId() + "。");
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setCollectTime(new Date());
        changeInfo.setEventInfo(eventInfo);
        String redisKey = null;
        if (info.getAssetId() == null) {
            redisKey = info.getIp() + ":" + StatusInfoChangeTypeEnum.event_syslog.getCode();
        } else {
            redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.event_syslog.getCode();
        }
        String mapKey = eventInfo.getMessageId();
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);
        String eventRedisKey = StatusInfoChangeTypeEnum.event_syslog.getCode();
        //为了区别重复把时间添加上
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + new Date().getTime();
        Integer status = EventLevelEnum.ABNORMAL.getCode();

        AlarmTempReq tempReq = new AlarmTempReq();
        tempReq.setAssetIp(info.getAssetIp());
        tempReq.setOrgMsg(eventInfo.getMessage());

        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId());
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescLog(String.format(StatusInfoChangeTypeEnum.event_syslog.getDescr(), eventInfo.getMessage()));
            this.dispatureEvent(event);
        }
        return false;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
