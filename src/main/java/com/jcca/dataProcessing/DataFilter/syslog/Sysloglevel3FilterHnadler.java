package com.jcca.dataProcessing.DataFilter.syslog;

import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.ChangeInfo;
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
@Component("sysloglevel3FilterHnadler")
public class Sysloglevel3FilterHnadler extends IFilterHandler<SyslogEventInfoEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(SyslogEventInfoEntity info) {
        StatusInfoChangeTypeEnum redisKeyStatus = null;
        //只取0-3级的告警信息
        if (info.getLevel() == 0) {
            redisKeyStatus = StatusInfoChangeTypeEnum.event_log_jcca_0;
        } else if (info.getLevel() == 1) {
            redisKeyStatus = StatusInfoChangeTypeEnum.event_log_jcca_1;
        } else if (info.getLevel() == 2) {
            redisKeyStatus = StatusInfoChangeTypeEnum.event_log_jcca_2;
        } else if (info.getLevel() == 3) {
            redisKeyStatus = StatusInfoChangeTypeEnum.event_log_jcca_3;
        } else {
            return true;
        }
        String contentStr = info.getMessage();
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setCollectTime(new Date());
        String redisKey = null;
        if (info.getAssetId() == null) {
            throw new ResultException(ResultEnum.dataProcess_syslog_interrupter.getCode(), "syslog事件主动终止处理:" + info.getMessage());
        } else {
            redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + redisKeyStatus.getCode();
        }

        String eventRedisKey = redisKeyStatus.getCode();
        //为了区别重复把时间添加上
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + new Date().getTime();
        Integer status = EventLevelEnum.ABNORMAL.getCode();

        AlarmTempReq tempReq = new AlarmTempReq();
        tempReq.setAssetIp(info.getAssetIp());
        tempReq.setOrgMsg(String.format(redisKeyStatus.getDescr(), contentStr));

        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq,info.getInspectRecordId(),info.getVersion());
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
