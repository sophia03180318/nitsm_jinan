package com.jcca.dataProcessing;

import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;

/**
 * @ClassName SyslogHandler
 * @Description syslog告警处理器
 * @Date 2020/7/9 10:39
 * @Author hanwone
 */

@Component
@Slf4j
public class SyslogReceiver implements SyslogServerSessionEventHandlerIF {
    @Resource
    private DataProcessManager dataProcessManager;


    @Override
    public Object sessionOpened(SyslogServerIF syslogServerIF, SocketAddress socketAddress) {
        return null;
    }

    /**
     * severity严重级别
     * 0 Emergency: system is unusable 1 Alert: action must be taken immediately 2
     * Critical: critical conditions 3 Error: error conditions 4 Warning: warning
     * conditions 5 Notice: normal but significant condition 6 Informational:
     * informational messages 7 Debug: debug-level messages
     */
    /**
     * facility含义
     * 0内核消息 1用户级消息 2邮件系统 3系统守护程序 4安全/授权消息 5 Syslogd内部生成的消息 6行式打印机子系统
     * 7网络新闻子系统 8 UUCP子系统 9时钟守护程序 10安全/授权消息 11 FTP守护程序 12 NTP子系统 13日志审核 14日志警报
     * 15时钟守护程序 16本地使用0（本地0） 17本地使用1（本地1） 18本地使用2（本地2） 19本地使用3（本地3） 20本地使用4（本地4）
     * 21本地使用5（本地5） 22本地使用6（本地6） 23本地使用7（本地7）
     * <p>
     * <p>
     * Priority(优先级) = facility * 8 + severity 值
     *
     * @param session
     * @param syslogThread
     * @param socketAddress
     * @param event
     */
    @Override
    public void event(Object session, SyslogServerIF syslogThread, SocketAddress socketAddress,
                      SyslogServerEventIF event) {
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

        try {
            if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_ORG_SYSLOG)) {
                log.info("系统收到最原始的事件" + JSONUtil.toJsonStr(event));
            }
            byte[] raw = event.getRaw();
            String message = new String(raw, "gbk");
            int level = event.getLevel();

            message = message;
            event.setMessage(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        String ip = socketAddress.toString();
        ip = ip.split("/")[1].split(":")[0];


        IAdapter adapter = dataProcessManager.getAdapter(CollectConst.SYSLOG);
        SyslogEventInfoEntity syslogEventInfoEntity = new SyslogEventInfoEntity();

        syslogEventInfoEntity.setIp(ip);
        syslogEventInfoEntity.setMessage(event.getMessage());
        syslogEventInfoEntity.setLevel(event.getLevel());
        adapter.dispose(syslogEventInfoEntity);
        //获取当前处理数量
        adapter.dataProcess();
    }

    @Override
    public void exception(Object o, SyslogServerIF syslogServerIF, SocketAddress socketAddress, Exception e) {

    }

    @Override
    public void sessionClosed(Object o, SyslogServerIF syslogServerIF, SocketAddress socketAddress, boolean b) {

    }

    @Override
    public void initialize(SyslogServerIF syslogServerIF) {

    }

    @Override
    public void destroy(SyslogServerIF syslogServerIF) {

    }
}
