package com.jcca.common.log.constant;

/**
 * @author HanHW
 * @description 系统功能常量，由四位数字组成，前两位表示功能，后两位表示操作，用于按功能项日志输出控制
 * @className LogFunctionConstant
 * @date 2023/10/10 13:46
 * @since 2.1.0.0
 */
public interface LogFunctionConstant {
    //===========================================登录退出============================================
    // 登录/退出 10
    String LOGIN_WEB = "1001";
    String LOGIN_ADMIN = "1002";
    String REAL_TIME_MSG = "1003";

    //===========================================系统管理============================================

    // 菜单管理 11
    String MENU_MANAGE = "1101";

    // 角色管理 12
    String ROLE_MANAGE = "1201";

    // 组织管理 13
    String ORG_MANAGE = "1301";

    // 用户管理 14
    String USER_MANAGE = "1401";

    // 字典管理 15
    String DICT_MANAGE = "1501";

    // 行为日志 16
    String ACTION_LOG = "1601";

    //===========================================系统配置============================================

    // 初始配置 17
    String DEFAULT_CONFIG = "1701";

    // 采集配置 18
    String COLLECT_CONFIG = "1801";

    // 车站配置 19
    String STATION_CONFIG = "1901";

    // 系统日志控制 20
    String LOG_CONTROL = "2001";

    // 数据库校验 21
    String DB_CHECK = "2101";

    //===========================================模板配置============================================

    // 设备型号管理 22
    String ASSET_TEMPLATE = "2201";
    //文件下载异常
    String DOWNLOAD_ERROR = "2202";


    // 面板模板配置 23
    String PANEL_TEMPLATE = "2301";

    // 拓扑模板配置 24
    String TOPO_TEMPLATE = "2401";

    // 业务进程管理 25
    String PROCESS_TEMPLATE = "2501";

    //===========================================业务配置管理============================================

    // 进程配置 26
    String PROCESS_CONFIG = "2601";

    // 设置面板配置 27
    String ASSET_PANEL_CONFIG = "2701";

    // 业务配置 28
    String BIZ_CONFIG = "2801";

    // 拓扑图 29
    String TOPO_CONFIG = "2901";

    //===========================================告警管理============================================

    // 事件管理 30
    String EVENT_MANAGE = "3001";

    // 告警规则管理 31
    String ALARM_RULE_MANAGE = "3101";

    // 告警列表 统计 32
    String ALARM_LIST = "3201";

    // 告警弹框 33
    String ALARM_POP = "3301";

    // 告警处理 34
    String ALARM_HANDLE = "3401";


    //===========================================详情展示============================================

    // 机柜详情 35
    String CABINET_DETAIL = "3501";

    // 设备详情 36
    String ASSET_DETAIL = "3601";

    // LINUX设备详情 37
    String LINUX_DETAIL = "3701";

    // 网络设备详情 38
    String NETWORK_DETAIL = "3801";
    String ASSET_LINK_ASSET = "3802";

    // 数据库详情 39
    String DB_DETAIL = "3901";

    // 软件状态详情 40
    String SOFTWARE_DETAIL = "4001";

    // 中间件详情 41
    String MQ_DETAIL = "4101";

    // 存储详情 42
    String DS_DETAIL = "4201";

    // 管理口详情 43
    String IPMI_DETAIL = "4301";

    //===========================================资产生命周期============================================

    // 资产管理 44
    String ASSET_MANAGE = "4401";
    String ASSET_MANAGE_OPTICAL = "4402"; // 光功率上传下载
    String ASSET_CHANGE = "4403"; // 资产变动

    // 维护手册 45,
    String HAND_BOOK = "4501";

    // 资产档案 46
    String ASSET_PROFILE = "4601";

    // 阈值管理 47
    String THRESHOLD_MANAGE = "4701";

    // 故障记录 48
    String BROKEN_RECORD = "4801";

    // 维护计划 49
    String CONSTRUCTION_RECORD = "4901";

    // IP管理 50
    String IP_MANAGE = "5001";

    //===========================================拓扑管理============================================

    // 网络拓扑 51
    String NET_TOPO = "5101";

    // 中心机柜拓扑 52
    String CENTER_CABINET_TOPO = "5201";

    // 车站机柜拓扑 53
    String STATION_CABINET_TOPO = "5301";

    // 线路拓扑 54
    String LINE_TOPO = "5401";

    // 中心业务拓扑 55
    String CENTER_BIZ_TOPO = "5501";

    // 线路业务拓扑 56
    String LINE_BIZ_TOPO = "5601";

    // 广域网拓扑 57
    String WAN_TOPO = "5701";

    //===========================================其它============================================

    // 中间件监控 58
    String MIDDLE_MONITOR = "5801";

    // 统计大屏 59
    String STATISTICS = "5901";

    // 巡检管理 60
    String XUNJIAN_MANAGE = "6001";

    //===========================================纯后台业务============================================

    // 定时任务 61
    String CRON_DATA = "6101";
    String CRON_COLLECT_STATUS = "6102";
    String CRON_DATA_STATISTICS = "6103";
    String CRON_DATA_STATION_PING = "6104";
    String CRON_DATA_STATION_UPDATE = "6105";
    String CRON_DATA_MANAGER_PORT = "6106";
    String CRON_NETTIMESERVICE = "6107";

    // 数据采集 62
    String DATA_PROCESS = "6201";

    // 数据分发 63
    String COLLECT_DATA_PARSER = "6301";
    String COLLECTOR_TO_ITSM = "6302";
    String ITSM_TO_COLLECTOR = "6303";
    String SYSLOG = "6304";

    // 自定义异常拦截 64
    String SELF_EXCEPTION = "6401";
    String SELF_EXCEPTION_RESULT = "6402";
    String SELF_EXCEPTION_BIND = "6403";
    String SELF_EXCEPTION_RUNTIME = "6404";
    String SELF_EXCEPTION_UNAUTH = "6405";
    String SELF_EXCEPTION_NOSESSION = "6406";
    String SELF_EXCEPTION_TIMEOUT = "6407";
    String SELF_EXCEPTION_PARAM = "6408";

    // 3D机房
    String THREE_D_CRON = "6501";
    //===========================================对外接口============================================
    String OUT_API = "6601";


}
