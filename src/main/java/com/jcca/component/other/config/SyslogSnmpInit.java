package com.jcca.component.other.config;

import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName SyslogSnmpBoot
 * @Description syslog和snmp告警启动器
 * @Date 2020/7/9 10:24
 * @Author hanwone
 */
//@Component
@Slf4j
public class SyslogSnmpInit {

    /**
     * syslog和snmp接收地址
     */
    @Value("${project.syslog_snmp.host}")
    private String host;

    @Resource
    private SyslogHandler syslogHandler;
    @Resource
    private SnmpHandler snmpHandler;

    @PostConstruct
    public void boot() {
        this.bootSyslog();
        this.bootSnmp();
    }

    private void bootSnmp() {
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "启动SNMP监听成功"));
        }

        try {
            String[] hosts = host.split(",");
            snmpHandler.init(hosts[0]);
        } catch (IOException e) {
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_INIT)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_INIT, ErrorCodeEnum.SNMP_INIT_PORT_ERROR, "", e.getMessage()), e);
            }
        }
    }

    private void bootSyslog() {
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "启动syslog监听");
        }
        String[] hosts = host.split(",");
        for (String h : hosts) {
            SyslogServerIF syslogServer = SyslogServer.getInstance(SyslogConstants.UDP);
            SyslogServer.getThreadedInstance(SyslogConstants.UDP);

            SyslogServerConfigIF syslogServerConfig = syslogServer.getConfig();
            syslogServerConfig.setHost(h);
            syslogServerConfig.setPort(SyslogConstants.SYSLOG_PORT_DEFAULT);
            syslogServerConfig.addEventHandler(syslogHandler);
        }

    }
}
