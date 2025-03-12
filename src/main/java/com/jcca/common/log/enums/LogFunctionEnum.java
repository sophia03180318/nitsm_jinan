package com.jcca.common.log.enums;

import com.jcca.common.log.constant.LogFunctionConstant;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author HanHW
 * @description 功能枚举类
 * @className LogFunctionEnum
 * @date 2023/10/9 10:31
 * @since 2.1.0.0
 */
@Getter
public enum LogFunctionEnum {

    //===========================================登录退出============================================
    // 登录/退出 10
    LOGIN_WEB(LogFunctionConstant.LOGIN_WEB, "登录/退出", "前端登录"),
    LOGIN_ADMIN(LogFunctionConstant.LOGIN_ADMIN, "登录/退出", "后台登录"),
    REAL_TIME_MSG(LogFunctionConstant.REAL_TIME_MSG, "实时消息", ""),

    //===========================================系统管理============================================

    // 菜单管理 11
    MENU_MANAGE(LogFunctionConstant.MENU_MANAGE, "菜单管理", ""),

    // 角色管理 12
    ROLE_MANAGE(LogFunctionConstant.ROLE_MANAGE, "角色管理", ""),

    // 组织管理 13
    ORG_MANAGE(LogFunctionConstant.ORG_MANAGE, "组织管理", ""),
    CABINET_IMPORT(LogFunctionConstant.CABINET_IMPORT, "机柜导入", ""),

    // 用户管理 14
    USER_MANAGE(LogFunctionConstant.USER_MANAGE, "用户管理", ""),

    // 字典管理 15
    DICT_MANAGE(LogFunctionConstant.DICT_MANAGE, "字典管理", ""),

    // 行为日志 16
    ACTION_LOG(LogFunctionConstant.ACTION_LOG, "行为日志管理", ""),

    //===========================================系统配置============================================

    /**
     * 初始配置 17
     */
    DEFAULT_CONFIG(LogFunctionConstant.DEFAULT_CONFIG, "初始配置", ""),

    // 采集配置 18
    COLLECT_CONFIG(LogFunctionConstant.COLLECT_CONFIG, "采集配置", ""),

    // 车站配置 19
    STATION_CONFIG(LogFunctionConstant.STATION_CONFIG, "车站配置", ""),

    // 系统日志控制 20
    LOG_CONTROL(LogFunctionConstant.LOG_CONTROL, "系统日志控制", ""),

    // 数据库校验 21
    DB_CHECK(LogFunctionConstant.DB_CHECK, "数据库校验", ""),

    //===========================================模板配置============================================

    // 设备型号管理 22
    ASSET_TEMPLATE(LogFunctionConstant.ASSET_TEMPLATE, "设备型号管理", ""),
    ASSET_TEMPLATE_DOWNLOAD(LogFunctionConstant.DOWNLOAD_ERROR,"资产管理图片下载","文件下载失败IO异常"),

    // 面板模板配置 23
    PANEL_TEMPLATE(LogFunctionConstant.PANEL_TEMPLATE, "面板模板配置", ""),

    // 拓扑模板配置 24
    TOPO_TEMPLATE(LogFunctionConstant.TOPO_TEMPLATE, "拓扑模板配置", ""),

    // 业务进程管理 25
    PROCESS_TEMPLATE(LogFunctionConstant.PROCESS_TEMPLATE, "业务进程管理", ""),

    //===========================================业务配置管理============================================

    // 进程配置 26
    PROCESS_CONFIG(LogFunctionConstant.PROCESS_CONFIG, "进程配置", ""),

    // 设置面板配置 27
    ASSET_PANEL_CONFIG(LogFunctionConstant.ASSET_PANEL_CONFIG, "设备面板配置", ""),

    // 业务配置 28
    BIZ_CONFIG(LogFunctionConstant.BIZ_CONFIG, "业务配置", ""),

    // 拓扑图 29
    TOPO_CONFIG(LogFunctionConstant.TOPO_CONFIG, "拓扑图配置", ""),

    //===========================================告警管理============================================

    // 事件管理 30
    EVENT_MANAGE(LogFunctionConstant.EVENT_MANAGE, "事件管理", ""),

    // 告警规则管理 31
    ALARM_RULE_MANAGE(LogFunctionConstant.ALARM_RULE_MANAGE, "告警规则管理", ""),

    // 告警列表 统计 32
    ALARM_LIST(LogFunctionConstant.ALARM_LIST, "告警列表管理", ""),

    // 告警弹框 33
    ALARM_POP(LogFunctionConstant.ALARM_POP, "告警弹框", ""),

    // 告警处理 34
    ALARM_HANDLE(LogFunctionConstant.ALARM_HANDLE, "告警处理", ""),


    //===========================================详情展示============================================

    // 机柜详情 35
    CABINET_DETAIL(LogFunctionConstant.CABINET_DETAIL, "机柜详情", ""),

    // 设备详情 36
    ASSET_DETAIL(LogFunctionConstant.ASSET_DETAIL, "设备详情", ""),

    // LINUX设备详情 37
    LINUX_DETAIL(LogFunctionConstant.LINUX_DETAIL, "LINUX设备详情", ""),

    // 网络设备详情 38
    NETWORK_DETAIL(LogFunctionConstant.NETWORK_DETAIL, "网络设备详情", ""),
    ASSET_LINK_ASSET(LogFunctionConstant.ASSET_LINK_ASSET, "网络设备对端信息", ""),

    // 数据库详情 39
    DB_DETAIL(LogFunctionConstant.DB_DETAIL, "数据库详情", ""),

    // 软件状态详情 40
    SOFTWARE_DETAIL(LogFunctionConstant.SOFTWARE_DETAIL, "软件状态详情", ""),

    // 中间件详情 41
    MQ_DETAIL(LogFunctionConstant.MQ_DETAIL, "中间件详情", ""),

    // 存储详情 42
    DS_DETAIL(LogFunctionConstant.DS_DETAIL, "存储设备详情", ""),

    // 管理口详情 43
    IPMI_DETAIL(LogFunctionConstant.IPMI_DETAIL, "管理口详情", ""),

    //===========================================资产生命周期============================================

    // 资产管理 44
    ASSET_MANAGE(LogFunctionConstant.ASSET_MANAGE, "资产管理", ""),
    ASSET_MANAGE_OPTICAL(LogFunctionConstant.ASSET_MANAGE_OPTICAL, "光功率模板文件", ""),
    ASSET_CHANGE(LogFunctionConstant.ASSET_CHANGE, "资产变动", ""),

    // 维护手册 45,
    HAND_BOOK(LogFunctionConstant.HAND_BOOK, "维护手册", ""),

    // 资产档案 46
    ASSET_PROFILE(LogFunctionConstant.ASSET_PROFILE, "资产档案", ""),

    // 阈值管理 47
    THRESHOLD_MANAGE(LogFunctionConstant.THRESHOLD_MANAGE, "阈值管理", ""),

    // 故障记录 48
    BROKEN_RECORD(LogFunctionConstant.BROKEN_RECORD, "故障记录", ""),

    // 维护计划 49
    CONSTRUCTION_RECORD(LogFunctionConstant.CONSTRUCTION_RECORD, "维护计划", ""),

    // IP管理 50
    IP_MANAGE(LogFunctionConstant.IP_MANAGE, "IP管理", ""),

    //===========================================拓扑管理============================================

    // 网络拓扑 51
    NET_TOPO(LogFunctionConstant.NET_TOPO, "网络拓扑", ""),

    // 中心机柜拓扑 52
    CENTER_CABINET_TOPO(LogFunctionConstant.CENTER_CABINET_TOPO, "中心机柜拓扑", ""),

    // 车站机柜拓扑 53
    STATION_CABINET_TOPO(LogFunctionConstant.STATION_CABINET_TOPO, "车站机柜拓扑", ""),

    // 线路拓扑 54
    LINE_TOPO(LogFunctionConstant.LINE_TOPO, "线路拓扑", ""),

    // 中心业务拓扑 55
    CENTER_BIZ_TOPO(LogFunctionConstant.CENTER_BIZ_TOPO, "中心业务拓扑", ""),

    // 线路业务拓扑 56
    LINE_BIZ_TOPO(LogFunctionConstant.LINE_BIZ_TOPO, "线路业务拓扑", ""),

    // 广域网拓扑 57
    WAN_TOPO(LogFunctionConstant.WAN_TOPO, "广域网拓扑", ""),

    //===========================================其它============================================

    // 中间件监控 58
    MIDDLE_MONITOR(LogFunctionConstant.MIDDLE_MONITOR, "中间件监控", ""),

    // 统计大屏 59
    STATISTICS(LogFunctionConstant.STATISTICS, "统计大屏", ""),

    // 巡检管理 60
    XUNJIAN_MANAGE(LogFunctionConstant.XUNJIAN_MANAGE, "巡检管理", ""),

    //===========================================纯后台业务============================================

    // 定时任务 61
    CRON_DATA(LogFunctionConstant.CRON_DATA, "定时任务", ""),
    CRON_COLLECT_STATUS(LogFunctionConstant.CRON_COLLECT_STATUS, "定时任务", "定时更新采集节点状态"),
    CRON_DATA_STATISTICS(LogFunctionConstant.CRON_DATA_STATISTICS, "定时任务", "定时统计数据"),
    CRON_DATA_STATION_PING(LogFunctionConstant.CRON_DATA_STATION_PING, "定时任务", "定时向车站推送ITSM接收到的车站PING结果"),
    CRON_DATA_STATION_UPDATE(LogFunctionConstant.CRON_DATA_STATION_UPDATE, "定时任务", "车站自动升级"),
    CRON_DATA_MANAGER_PORT(LogFunctionConstant.CRON_DATA_MANAGER_PORT, "定时任务", "车站自动升级"),
    CRON_NETTIMESERVICE(LogFunctionConstant.CRON_NETTIMESERVICE, "定时任务", "铁科时钟同步"),

    //性能数据处理62
    DATA_PROCESS(LogFunctionConstant.DATA_PROCESS, "数据处理", "性能处理数量"),
    DATA_PROCESS_CHANGE(LogFunctionConstant.DATA_PROCESS_CHANGE, "数据处理", "进程切换"),

    // 数据分发 63
    COLLECT_DATA_PARSER(LogFunctionConstant.COLLECT_DATA_PARSER, "数据分发", ""),
    COLLECTOR_TO_ITSM(LogFunctionConstant.COLLECTOR_TO_ITSM, "采集器请求ITSM", ""),
    ITSM_TO_COLLECTOR(LogFunctionConstant.ITSM_TO_COLLECTOR, "ITSM请求采集器", ""),
    SYSLOG(LogFunctionConstant.ITSM_TO_COLLECTOR, "SYSLOG消息", ""),

    // 异常拦截 64
    SELF_EXCEPTION(LogFunctionConstant.SELF_EXCEPTION, "异常拦截", "未知异常"),
    SELF_EXCEPTION_RESULT(LogFunctionConstant.SELF_EXCEPTION_RESULT, "异常拦截", "返回结果异常"),
    SELF_EXCEPTION_BIND(LogFunctionConstant.SELF_EXCEPTION_BIND, "异常拦截", "数据绑定异常"),
    SELF_EXCEPTION_RUNTIME(LogFunctionConstant.SELF_EXCEPTION_RUNTIME, "异常拦截", "运行时异常"),
    SELF_EXCEPTION_UNAUTH(LogFunctionConstant.SELF_EXCEPTION_UNAUTH, "异常拦截", "权限相关异常"),
    SELF_EXCEPTION_NOSESSION(LogFunctionConstant.SELF_EXCEPTION_NOSESSION, "异常拦截", "登录相关异常"),
    SELF_EXCEPTION_TIMEOUT(LogFunctionConstant.SELF_EXCEPTION_TIMEOUT, "异常拦截", "超时异常"),
    SELF_EXCEPTION_PARAM(LogFunctionConstant.SELF_EXCEPTION_PARAM, "异常拦截", "参数异常"),

    //3D机房 65
    THREE_D_CRON(LogFunctionConstant.THREE_D_CRON, "3D机房推送现存告警", "3D机房推送现存告警"),

    // 对外接口 66
    OUT_API(LogFunctionConstant.OUT_API, "对外接口", "");

    // 由四位数字组成，前两位表示功能，后两位表示操作，用于日志输出控制
    private String code;
    // 功能名
    private String function;
    // 具体操作
    private String action;

    LogFunctionEnum(String code, String function, String action) {
        this.function = function;
        this.action = action;
        this.code = code;
    }

    public static LogFunctionEnum getByCode(String code) {
        if (StringUtils.isEmpty(code)) return null;
        LogFunctionEnum[] values = LogFunctionEnum.values();
        for (LogFunctionEnum item : values) {
            if (Objects.equals(code, item.getCode())) {
                return item;
            }
        }
        return null;
    }

    public static LogFunctionEnum getByFunctionCode(String functionCode) {
        if (StringUtils.isEmpty(functionCode)) return null;
        String code = functionCode + "01";
        LogFunctionEnum[] values = LogFunctionEnum.values();
        for (LogFunctionEnum item : values) {
            if (Objects.equals(code, item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
