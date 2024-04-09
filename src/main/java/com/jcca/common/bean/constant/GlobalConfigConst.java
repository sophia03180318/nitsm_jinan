package com.jcca.common.bean.constant;

/**
 * @ClassName GlobalConfigConst
 * @Description 系统配置项常量key
 * @Author wone
 * @Date 2021/1/12 10:06
 * @Version ITSM2.0
 **/
public interface GlobalConfigConst {
    /**
     * syslog接收开关，0不接收，1接收
     */
    String SYSLOG_SWITCH = "config:syslog-switch";
    /**
     * snmp接收开关，0不接收，1接收
     */
    String SNMP_SWITCH = "config:snmp-switch";

    /**
     * 铁科时钟同步时间间隔记录
     */
    String TK_NET_TIME_SERVICE = "config:tk-net-time";
    String LAST_TK_NET_TIME_SERVICE = "LAST_TK_NET_TIME_SERVICE_";
}
