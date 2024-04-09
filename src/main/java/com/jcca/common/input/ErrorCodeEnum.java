package com.jcca.common.input;

import lombok.Getter;

/**
 * 错误码
 */
@Getter
public enum ErrorCodeEnum {

    COMMON_ASSET_LOSE("资产丢失", "资产已经不存在，检查此设备是否已经被删除！"),

    COMMON_REPETITION("违反唯一性要求", "数据出现重复性数据，检查是否存在人工操作数据库的情况，如存在，删除重复的数据！如不存在可能是程序存在并发性问题，需要修改程序！"),

    COMMON_VERIFY_REPETITION("校验重复", "进行数据校验发现有重复数据。"),

    COMMON_KEY_TIMEOUT("凭证失效", "可以尝试重新操作！"),

    COMMON_EXPORT_ERROR("导出记录失败", "将文件流响应回去的时候发生了异常，可能是关闭浏览器导致的。"),

    COMMON_REDIS_ERROR("访问系统Redis出错", "检查redis网络连接是否正常；检查磁盘是否满了，导致redis 不能正常工作。"),

    COMMON_QUEUE_INTERRUPTED_EXCEPTION("线程中断", "线程发生中断，重启项目解决"),

    SNMP_INIT_PORT_ERROR("snmp初始化失败", "检查配置文件中配置项 project.syslog_snmp.host 对应的端口号是否被占用！"),

    SYSTEM_DB_LINK("数据库链接错误", "检查与数据库网络是否连通，检查数据库端口，用户名密码是否正确！"),

    JOB_UNHEALTHY_ASSET_01("config:unhealthyAsset 取值配置错误 ", "在系统5501》业务管理》系统配置中查看config:unhealthyAsset的配置value是否是纯数字若不是请改为纯数字！"),

    JOB_UNHEALTHY_ASSET_02("事件添加异常", "添加事件发生异常，请参考异常日志以及事件日志综合判断问题！"),

    ASSET_TELNET_ERROR("进行telnet失败", "一般可能是被测设备端口未打开，或者被测设备开启了防火墙。"),

    ASSET_TELNET_CLOSE_ERROR("telnet 关流失败", "telnetClient.disconnect 报错，具体参见异常描述"),

    WEB_ASSET_GET_HP_MANAGER_MSG_ERROR("获取惠普设备管理口信息失败", "在ITSM系统管理》监控管理中找到此设备，检查设备配置的管理口信息是否正确！"),

    WEB_ASSET_GET_MONITOR_STATUS("判定设备当前监控状态是否正确异常", "如果提示未知的设备类型需要检查字典CollectLogicServiceImpl.getCollectStatus()方法内补全类型判定！"),

    WEB_ASSET_COLLECT_TEST("设备采集测试", "采集测试失败，可以参考错误信息！"),

    WEB_ASSET_TEMP_SAVE_ERROR("保存设备模板异常", "设备保存或更新模板的时候与数据库交互失败。"),

    WEB_ASSET_DEL("删除设备异常", "检查数据库状态。如正常，参考异常信息。"),

    WEB_ASSET_EXCEL_DOWNLOAD("下载资产模板异常", "检查模板/templates/import/asset_import_template.xlsx是否存在"),

    WEB_ASSET_EXCEL_DOWNLOAD_01("下载资产模板异常", "检查模板精简版本是否存在"),

    WEB_ASSET_EXCEL_DOWNLOAD_02("下载资产模板异常", "检查模板完整版本是否存在"),

    WEB_ASSET_EXCEL_DOWNLOAD_03("下载资产模板异常", "检查模板默认版本是否存在"),

    WEB_ASSET_PMGRESS_BAR("资产导入进度条查询 缺少任务ID", "检查此sql 是否有结果输出：select id from ASSET_TASK where CREATE_TIME=(select max(CREATE_TIME) from ASSET_TASK)"),

    WEB_ASSET_STOP_IMPORT("终止资产导入异常", "具体请查看异常输出"),

    WEB_ASSET_STOP_IMPORT_02("资产导入因异步执行时传入任务ID为空,所以强行终止任务!", "重新尝试导入"),

    WEB_ASSET_IMPORT_ERROR("资产导入异常", "具体请查看异常输出"),

    WEB_CABINET_ERROR_01("获取机柜简要详情及机柜内告警信息失败", "前端接口/api/cabinet/general/{id} 传入的机柜ID不存在"),

    WEB_DS_MANAGER_GET_DETAIL("获取DS设备详情异常", "获取DS设备详情失败……"),

    WEB_DS_MANAGER_LOG_DOWNLOAD("下载DS日志文件异常", "下载DS设备日志异常，查看具体异常信息"),

    WEB_OPTICAL_TYPE_NUMBER("计算光功率表格序号不是数字格式", "检查模板文件填入的内容 首行是否是数字！如果不是数字请改为数字类型！"),

    WEB_OPTICAL_IP_FORMAT("计算光功率表格填写IP格式错误", "检查模板中填写的IP格式是否正确，如果不正确请改正！"),

    WEB_OPTICAL_EXPORT_ERROR("导出光功率表单异常", "参考异常内容解决！ApiOpticalExportController.downloadExcel()"),

    WEB_PROCESS_GET_REMOTE_ERROR("获取设备上的进程列表失败", "1、检查采集器到此设备的网络是否正常；2、从采集器上用命令是否可以正常采集到此设备的进程；3、检查ITSM到采集器的网络是否正常！"),

    WEB_PROCESS_SEND_PROCESS_CHANGE("向采集器推送进程变动失败", "1、检查ITSM和采集器之间的网路是否畅通；2、检查采集器是否开启了防火墙等安全措施导致端口不可用；3、检查采集器应用是否存活"),

    WEB_PROCESS_CREATE_ERROR("保存进程配置失败", "根据异常提示解决"),

    WEB_XUNJIAN_COLLECT_ERROR("巡检采集失败","可能是巡检设备网络不通，或者用户名密码错误导致。"),

    WEB_XUNJIAN_TELNET_ERROR("TELNET请求失败","请根据异常信息分析，有可能是命令输出过大，读取时间超过3分钟导致"),

    ALARM_WEB_SOCKET_ERROR("WebSocketServer 向前端推送消息异常", "根据异常信息进行解决。如果是因为用户退出登录或关闭浏览器导致的异常无需处理……"),

    QUEUE_PUSH_WEB_ERROR("向前端推送数据异常", "有可能是数据格式非JSON格式或前端界面关闭导致的。"),

    QUEUE_PUSH_WEB_ERROR_02("向前端推送数据异常", "根据异常信息排查……"),

    QUEUE_CASCO_ERROR("casco队列处理告警的时候发生了异常", "执行brokerAlarmHandler.alarmHandle方法时发生了异常，请参照异常详细信息"),

    QUEUE_CASCO_ERROR_01("casco队列处理告警的时候发生了异常", "1、检查队列数据是否是json格式！"),

    QUEUE_CASCO_ERROR_02("casco队列处理告警的时候发生了异常", "根据异常提示信息排查！"),

    QUEUE_BROKER_ERROR("处理业务队列数据的时候发生了异常", "根据异常提示信息排查！"),

    QUEUE_ALARM_EXE_ERROR("处理事件告警异常", "请参考异常信息解决……"),

    QUEUE_EVENT_ADD_ERROR("处理新增事件错误", "执行addEventQueue方法时报错！参考具体异常信息"),

    QUEUE_EVENT_ADD_ERROR_02("处理新增事件告警错误", "参考具体异常信息"),

    SYSTEM_COLLECTOR_PING("设置ping隧道失败", "对业务功能无影响"),

    SYSTEM_IMPORT_CABINET("机柜导入失败", "请查看具体异常信息"),
    SYSTEM_MONITOR_REDIS("本地监控获取redis信息异常", "请查看具体异常信息"),
    SYSTEM_ORG_ADD("添加/编辑组织异常", "请查看具体异常信息"),

    SYSTEM_STATION_ADD("更新车站异常", "请查看具体异常信息"),
    SYSTEM_STATION_DELETE("删除JAR包重新上传异常", "网络问题导致删除JAR失败，请排查网络问题后重试");
    private String descStr;
    private String operationStr;

    ErrorCodeEnum(String descStr, String operationStr) {
        this.descStr = descStr;
        this.operationStr = operationStr;
    }
}
