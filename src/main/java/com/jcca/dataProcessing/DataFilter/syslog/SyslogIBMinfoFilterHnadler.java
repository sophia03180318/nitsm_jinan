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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhaozheng
 * @description TODO
 * @className SyslogIBMinfoFilterHnadler
 * @date 2023/11/28 11:59
 * @since 2.1.0.0
 * <p>
 * 0       Emergency: system is unusable
 * 1       Alert: action must be taken immediately
 * 2       Critical: critical conditions
 * 3       Error: error conditions
 * 4       Warning: warning conditions
 * 5       Notice: normal but significant condition
 * 6       Informational: informational messages
 * 7       Debug: debug-level messages
 * facility含义
 * 0内核消息
 * 1用户级消息
 * 2邮件系统
 * 3系统守护程序
 * 4安全/授权消息
 * 5 Syslogd内部生成的消息
 * 6行式打印机子系统
 * 7网络新闻子系统
 * 8 UUCP子系统
 * 9时钟守护程序
 * 10安全/授权消息
 * 11 FTP守护程序
 * 12 NTP子系统
 * 13日志审核
 * 14日志警报
 * 15时钟守护程序
 * 16本地使用0（本地0）
 * 17本地使用1（本地1）
 * 18本地使用2（本地2）
 * 19本地使用3（本地3）
 * 20本地使用4（本地4）
 * 21本地使用5（本地5）
 * 22本地使用6（本地6）
 * 23本地使用7（本地7）
 */

/**
 *           0       Emergency: system is unusable
 *           1       Alert: action must be taken immediately
 *           2       Critical: critical conditions
 *           3       Error: error conditions
 *           4       Warning: warning conditions
 *           5       Notice: normal but significant condition
 *           6       Informational: informational messages
 *           7       Debug: debug-level messages
 */
/**facility含义
 * 0内核消息
 * 1用户级消息
 * 2邮件系统
 * 3系统守护程序
 * 4安全/授权消息
 * 5 Syslogd内部生成的消息
 * 6行式打印机子系统
 * 7网络新闻子系统
 * 8 UUCP子系统
 * 9时钟守护程序
 * 10安全/授权消息
 * 11 FTP守护程序
 * 12 NTP子系统
 * 13日志审核
 * 14日志警报
 * 15时钟守护程序
 * 16本地使用0（本地0）
 * 17本地使用1（本地1）
 * 18本地使用2（本地2）
 * 19本地使用3（本地3）
 * 20本地使用4（本地4）
 * 21本地使用5（本地5）
 * 22本地使用6（本地6）
 * 23本地使用7（本地7）
 */

/**
 * {"charSet":"UTF-8","level":0,"host":"192.168.53.125","rawLength":587,"isHostStrippedFromMessage":false,
 * "message":"\tServer MTM: 7915I01\n\n\n\tAlert Text: Remote Login Successful.
 * Login ID: USERID from webguis at IP address 192.168.20.191.\n\tType of Alert:
 * System - Remote Login\n\n\tSeverity: 4\n\tDate(m/d/y):
 * 07/07/2000\n\tTime(h:m:s): 23:44:03\n\n\tContact: \n\n\tLocation: \n\tIMM
 * Text ID: UnknownIMM\n\tIMM Serial Number: 06MPBD9\n\tIMM UUID:
 * BBD386F6B4CE11E1A21C6CAE8B1C6DA2\n\tEvent ID: 4000000e00000000\n\tServiceable
 * Event Indicator: Not Serviceable\n\tFRU list: Not available\n\tRoom ID: Not
 * available\n\tRack ID: Not available\n\tLowest U-position: 0\n\tBlade Bay: Not
 * available\n\tTest Alert: no\n\tAuxiliary Data: Not available","facility":0}
 */
@Slf4j
@Component("syslogIBMinfoFilterHnadler")
public class SyslogIBMinfoFilterHnadler extends IFilterHandler<SyslogEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(SyslogEventInfoEntity info) {
        String contentStr = info.getMessage();
        String patternstr = "Alert Text:.*";
        String typeofAlert = "Type of Alert:.*";
        String eventId = "Event ID:.*";
        String severity = "Severity:.*";
        Pattern patten = Pattern.compile(patternstr);
        Pattern patten2 = Pattern.compile(typeofAlert);
        Pattern patten3 = Pattern.compile(eventId);
        Pattern patten4 = Pattern.compile(severity);

        EventInfo eventInfo = null;
        // 现在创建 matcher 对象
        Matcher m = patten.matcher(contentStr);
        Matcher m2 = patten2.matcher(contentStr);
        Matcher m3 = patten3.matcher(contentStr);
        Matcher m4 = patten4.matcher(contentStr);
        if (m.find()) {
            //告警内容
            eventInfo = new EventInfo();
            String alarmPatternString = m.group(0);
            String text = alarmPatternString.substring(11, alarmPatternString.length() - 1);
            eventInfo.setMessage(text);
        }
        if(m2.find()) {
            //告警类型
            String alertType = m2.group(0).split(":")[1];
            eventInfo.setEventType(alertType.trim());
        }
        if (m3.find()) {
            //事件ID
            String eId = m3.group(0).split(":")[1];
            eventInfo.setMessageId(eId.trim());
        }
        if (m4.find()) {
            //原始告警级别
            String level = m4.group(0).split(":")[1];
            eventInfo.setLevel(Integer.parseInt(level.trim()));
        }

        if(Objects.isNull(eventInfo)) {
            return true;
        }
        //修改message，添加事件ID
        eventInfo.setMessage(info.getMessage() + "," + "当前事件ID：" + eventInfo.getMessageId() + "。");
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setCollectTime(new Date());
        String redisKey = null;

        if (info.getAssetId() == null) {
            redisKey = info.getIp() + ":" + StatusInfoChangeTypeEnum.event_syslog.getCode();
        } else {
            redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.event_syslog.getCode();
        }

        String mapKey = eventInfo.getMessageId();
        changeInfo.setEventInfo(eventInfo);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);
        String eventRedisKey = StatusInfoChangeTypeEnum.event_syslog.getCode();
        //为了区别重复把时间添加上
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + System.currentTimeMillis();
        Integer status = EventLevelEnum.ABNORMAL.getCode();

        AlarmTempReq tempReq = new AlarmTempReq();
        tempReq.setAssetIp(info.getAssetIp());
        tempReq.setOrgMsg(eventInfo.getMessage());

        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq);
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
