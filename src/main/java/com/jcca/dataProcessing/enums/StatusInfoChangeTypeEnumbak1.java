package com.jcca.dataProcessing.enums;

import lombok.Getter;

/**
 * 枚举
 *
 * @author zhaozheng
 */
@Getter
public enum StatusInfoChangeTypeEnumbak1 {
    /**
     * 设备基础状态信息
     */
    status("status", "设备状态", ""),
    statusEvent("statusEvent", "事件监控状态", ""),
    //CPU相关基础信息
    status_CPUTop5("status:cpuTop5", "CPUTop5状态", ""),

    //管理口日志基础信息
    status_ipmi_log("status:ipmilog", "管理口日志", ""),
    //管理口硬件厂商
    status_ipmi_manu("status:manu", "硬件厂商", ""),

    status_MEMTop5("status:memTop5", "MEMTop5状态", ""),

    status_raid_log("status:raid_log", "存储日志", ""),


    //aix小机相关基础信息
    status_aix_io("status:aixIo", "小机io卡信息", ""),
    status_aix_type("adapterType", "io卡类型", ""),
    status_aix_state("adapterStat", "io状态", ""),
    status_aix_attention_state("attentionState", "链路状态", ""),
    status_aix_adapterSlot("adapterSlot", "IO口槽位", ""),
    status_aix_io_description("ioDescription", "IO描述信息", ""),
    status_aix_io_wwn("wwn", "光口wwn号", ""),
    status_memory("memory", "内存大小", ""),
    status_serial("serial", "设备列号", ""),
    status_systemVersion("systemVersion", "系统版本", ""),
    status_cpuMode("cpuMode", "CPU型号", ""),
    status_cpuNum("cpuNum", "CPU个数", ""),
    status_frequency("frequency", "CPU主频", ""),
    status_cpuCoreNum("cpuCoreNum", "CPU核心数", ""),
    status_powerNum("powerNum", "电源数量", ""),
    status_powerModel("powerModel", "电源型号", ""),
    status_diskNum("diskNum", "磁盘型号", ""),
    status_establishedNum("establishedNum", "设备连接数", ""),
    status_diskCapacityCount("diskCapacityCount", "磁盘个数", ""),
    status_totalCapacity("diskCapacityCount", "磁盘总容量", ""),


    //CTC业务状态事件
    event_CTC("event:event_CTC", "CTC业务状态", ""),
    event_CTC_link("event:event_CTC:link", "CTC业务连接事件", "CTC业务连接：%s 状态异常。"),
    event_CTC_version("event:event_CTC:version", "CTC业务版本事件", "软件：%s 变更为:%s。"),
    event_CTC_threshold("event:event_CTC:threshold", "CTC业务软件容量事件", "业务：%s 容量超过设定。"),
    event_CTC_runstate("event:event_CTC:runstate", "CTC业务运行状态事件", "业务：%s 运行状态异常。"),
    event_CTC_AB("event:event_CTC:AB", "CTC业务主备事件", "业务:%s 主备发生切换。"),

    //CTC业务状态事件对应的状态信息
    status_event_CTC("status:event_CTC", "CTC业务事件对应的状态信息", ""),
    status_event_softLinkState("status:event_CTC:softLinkState", "软件连接状态", ""),
    status_event_softThresholdState("status:event_CTC:softThresholdState", "软件容量状态", ""),

    status_softChannel("status:event_CTC:softChannel", "软件通道状态", ""),
    status_softVersion("status:event_CTC:softVersion", "软件版本状态", ""),
    status_soft("status:event_CTC:soft_status", "设备软件状态", ""),
    status_softMasterState("status:event_CTC:softMasterState", "软件主备状态", ""),


    //时钟同步状态事件
    event_time("event:event_time", "时钟事件", "时钟服务器状态异常"),
    event_time_state("event:event_time:state", "时间偏差事件", getPubThresholdMsg("", "时间偏差", "秒")),
    event_run_time_state("event:event_time:run_state", "运行时长事件", getPubThresholdMsg("", "运行时长", "天")),
    event_clock_state("event:event_time:clock_state", "时间服务器状态事件", "时钟同步状态异常！"),
    //时钟徒步事件对应的状态信息
    status_event_time("status:event_time", "时钟事件对应的状态信息", ""),
    status_time_deviation("time_deviation", "时间偏差", ""),
    status_run_time("run_time", "运行时间", ""),
    status_system_time("system_time", "系统时间", ""),
    status_time_server("status:event_time:time_server", "时钟服务器时钟状态", ""),
    status_time_server_monitor("status:event_time:time_server_monitor", "时钟服务器时钟状态监控", ""),
    status_time_server_status("time_serve_status", "时钟服务器时钟状态", ""),
    status_time_server_location("time_serve_location", "时钟服务器软件路径", ""),
    status_time_server_message("time_serve_status", "时钟服务器日志消息", ""),


    //自律机集群状态
    event_clusterState("event:clusterStateEvent", "自律机状态事件", "自律机集群主机%s状态异常。"),
    event_clusterABState("event:clusterStateEvent:clusterABState", "自律机集群AB机状态事件", "自律机集群主机%s状态异常。"),
    event_clusterMasterState("event:clusterStateEvent:clusterMasterState", "自律机集群主备机切换事件", "自律机集群主机切换为%s。"),
    //自律机集群事件对应的状态信息
    status_event_clusterState("status:clusterStateEvent", "自律机状态事件", "自律机集群主机%s状态异常。"),
    status_clusterABState("clusterABState", "自律机集群AB机状态", ""),
    status_clusterMasterState("clusterMasterState", "自律机集群主备机切换", ""),
    status_clusterState("clusterState", "自律机状态", ""),


    //通信质量监督状态事件
    event_linkQuality("event:event_linkQuality", "通信质量监督状态", ""),
    event_linkQuality_state("event:event_linkQuality:state", "通信质量监督事件", "检测到通信质量监督状态异常。"),
    //通信质量监督事件对应的状态信息
    status_event_linkQuality("status:event_linkQuality", "通信质量监督事件对应的状态信息", ""),
    status_congxing("status:event_linkQuality:CongxingState", "通信质量监督软件变化", ""),


    //自定义事件信息  category分类
    //CPU事件
    event("event", "设备事件", ""),
    event_CPU("event:event_CPU", "cpu状态", ""),
    event_CPU_normal("event:event_CPU:normal", "cpu普通阈值事件", getPubThresholdMsg("", "CPU使用率", "%%")),
    event_CPU_section("event:event_CPU:section", "cpu区间阈值事件", getSectionThresholdMsg("", "CPU使用率", "%%")),
    event_CPU_sectionOne("event:event_CPU:sectionOne", "cpu一阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "一阶", "%%")),
    event_CPU_sectionTwo("event:event_CPU:sectionTwo", "cpu二阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "二阶", "%%")),
    event_CPU_sectionThree("event:event_CPU:sectionThree", "cpu三阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "三阶", "%%")),
    //CPU事件对应状态
    status_event_CPU("status:event_CPU", "cpu事件对应状态", ""),
    status_CPUState("CPUState", "CPU状态", ""),
    //CPU管理口获取的数据
    status_ipmi_cpu("status:event_CPU:cpu", "管理口cpu状态", ""),
    status_cpu_value("cpuValue", "cpu值", ""),
    status_cpu_status("cpuStatus", "cpu状态", ""),
    status_cpu_core("cpuCore", "cpu核数", ""),
    status_cpu_voltage("cpuVoltage", "cpu电压", ""),


    //数据库状态事件
    event_db("event:event_db", "数据库状态", ""),
    event_db_connect("event:event_db:connect", "数据库连接事件", "检测到数据库连接异常！"),
    //数据库事件对应的状态信息
    status_event_db("status:event_db", "数据库事件对应的状态信息", ""),
    status_db_state("db_state", "数据库状态", ""),
    status_dbVersion("dbVersion", "数据库版本", ""),
    status_dbSysUpTime("dbSysUpTime", "启动时间", ""),
    status_dbCacheLibrary("dbCacheLibrary", "缓存命中率", ""),
    status_dbMemTotal("dbMemTotal", "数据库的运存总量", ""),
    status_dbDiskTotal("dbDiskTotal", " 硬盘总量", ""),
    status_dbCache("dbCache", "缓存大小", ""),
    status_dbBusynessRate("dbBusynessRate", "繁忙率", ""),
    status_dbSessionSize("dbSessionSize", "数据库的session数", ""),
    status_dbSessionUsedRate("dbSessionUsedRate", "数据库使用率", ""),
    status_dbCachePoolSize("dbCachePoolSize", "缓存池大小", ""),
    status_dbCachePoolhit("dbCachePoolhit", "缓存池命中率", ""),
    status_canUseLockSize("canUseLockSize", "可用锁数量", ""),
    status_dbLockUsedRate("dbLockUsedRate", "锁使用率", ""),
    status_dbLockWaitRate("dbLockWaitRate", "据库锁的等待率", ""),
    status_javaPoolSize("javaPoolSize", "java池大小", ""),
    status_redoLogBuffer("redoLogBuffer", "大池大小", ""),
    status_dbConnection("dbConnection", "数据库连接数", ""),
    status_dbActive("dbActive", "数据库活动连接数", ""),
    status_dbLanguage("dbLanguage", "语言环境", ""),
    status_alertPath("alertPath", "告警文件地址", ""),

    //表空间事件
    event_db_tableSpace("event:tableSpace", "数据库表空间使用率事件", getPubThresholdMsg("表空间：%s", "使用率", "%%")),
    event_tableSpace_sectionOne("event:tableSpace:tableSpace_sectionOne", "数据库表空间一阶阈值事件", getLevelThresholdMsg("表空间：%s", "使用率", "一阶", "%%")),
    event_tableSpace_sectionTwo("event:tableSpace:tableSpace_sectionTwo", "数据库表空间二阶阈值事件", getLevelThresholdMsg("表空间：%s", "使用率", "二阶", "%%")),
    event_tableSpace_sectionThree("event:tableSpace:tableSpace_sectionThree", "数据库表空间三阶阈值事件", getLevelThresholdMsg("表空间：%s", "使用率", "三阶", "%%")),

    //表空间事件对应的状态信息
    status_event_tablespace_status("status:event_tablespace", "表空间状态", ""),
    status_tablespace_usedRate("usedRate", "使用率", ""),
    status_tablespace_totalSize("totalSize", "表空间总大小", ""),
    status_tablespace_freeSize("freeSize", "空闲大小", ""),
    status_tablespace_usedSize("usedSize", "使用大小", ""),


    //磁盘事件
    event_disk("event:event_disk", "磁盘状态", ""),
    event_disk_normal("event:event_disk:normal", "磁盘普通阈值事件", getPubThresholdMsg("磁盘：%s ", "磁盘使用率", "%%")),
    event_disk_section("event:event_disk:section", "磁盘区间阈值事件", getSectionThresholdMsg("磁盘: %s ", "磁盘使用率", "%%")),
    event_mdisk_sectionOne("event:event_disk:sectionOne", "磁盘一阶阈值事件", getLevelThresholdMsg("", "磁盘使用率", "一阶", "%%")),
    event_disk_sectionTwo("event:event_disk:sectionTwo", "磁盘二阶阈值事件", getLevelThresholdMsg("", "磁盘使用率", "二阶", "%%")),
    event_disk_sectionThree("event:event_disk:sectionThree", "磁盘三阶阈值事件", getLevelThresholdMsg("", "磁盘使用率", "三阶", "%%")),
    //磁盘事件对应的状态信息
    status_event_diskState("status:event_disk:diskState", "磁盘事件对应的状态信息", ""),


    //风扇事件
    event_fan("event:event_fan", "风扇状态", ""),
    event_fan_state("event:event_fan:state", "风扇状态事件", "风扇%s状态异常"),
    event_fan_unknown("event:event_fan:unknown", "风扇未知状态事件", "风扇%s状态未知"),
    //风扇事件对应的状态信息
    status_event_fan("status:event_fan", "风扇事件对应的状态数据", ""),
    status_fan("status:event_fan:fan", "", ""),
    status_fanvalue("fanValue", "风扇转速", ""),
    status_fanStatus("fanStatus", "风扇状态", ""),


    //指示灯事件
    event_led("event:event_led", "指示灯状态", ""),
    event_led_state("event:event_led:state", "指示灯状态事件", "指示灯%s状态异常"),
    //指示灯事件对应的状态信息
    status_event_ipmi_led("status:event_led:led", "硬件状态灯", ""),


    //电源事件
    event_power("event:event_power", "电源状态", ""),
    event_power_state("event:event_power:state", "电源状态事件", "电源%s状态异常"),
    //电源事件对应的状态信息
    status_event_power("status:event_power", "电源状态", ""),


    //温度事件
    event_temp("event:event_temp", "温度状态", ""),
    event_temp_state("event:event_temp:state", "温度状态", "温度传感%s状态异常"),
    event_temp_state_normal("event:event_temp:normal", "温度传感器普通阈值事件", getPubThresholdMsg("温度传感器%s", "温度", "℃")),
    event_temp_state_sectionOne("event:event_temp:sectionOne", "温度传感器一阶阈值事件", getLevelThresholdMsg("温度传感器%s", "温度", "一阶", "℃")),
    event_temp_state_sectionTwo("event:event_temp:sectionTwo", "温度传感器二阶阈值事件", getLevelThresholdMsg("温度传感器%s", "温度", "二阶", "℃")),
    event_temp_state_sectionThree("event:event_temp:sectionThree", "温度传感器三阶阈值事件", getLevelThresholdMsg("温度传感器%s", "温度", "三阶", "℃")),
    //温度事件对应的状态信息
    status_event_temp("status:event_temp", "温度事件对应的状态信息", ""),
    status_tempValue("tempStatus", "温度值", ""),
    status_tempStatus("tempStatus", "温度状态", ""),


    //内存事件
    event_memory("event:event_memory", "内存状态", ""),
    event_memory_normal("event:event_memory:normal", "内存普通阈值事件", getPubThresholdMsg("", "内存使用率", "%%")),
    event_memory_section("event:event_memory:section", "内存区间阈值事件", getSectionThresholdMsg("", "内存使用率", "%%")),
    event_memory_sectionOne("event:event_memory:sectionOne", "内存一阶阈值事件", getLevelThresholdMsg("", "内存使用率", "一阶", "%%")),
    event_memory_sectionTwo("event:event_memory:sectionTwo", "内存二阶阈值事件", getLevelThresholdMsg("", "内存使用率", "二阶", "%%")),
    event_memory_sectionThree("event:event_memory:sectionThree", "内存三阶阈值事件", getLevelThresholdMsg("", "内存使用率", "三阶", "%%")),
    //内存事件对应的状态信息
    status_event_memory("status:event_memory", "设备状态", ""),
    status_memoryState("memoryState", "内存状态", ""),


    //网卡状态事件
    event_net("event:event_net", "网卡状态", ""),
    event_net_flow("event:event_net:flow", "网卡流量阈值事件", getPubThresholdMsg("网卡：%s", "网卡流量", "Kbps")),
    event_net_state("event:event_net:state", "网卡状态事件", "网卡%s状态异常。"),
    //网卡事件对应的状态数据
    status_event_net("status:event_net", "网卡事件对应的状态数据", ""),
    status_net_status("netStatus", "网卡状态", ""),
    status_net_portIn("portIn", "网卡流入量", ""),
    status_net_portOut("portOut", "网卡流出量", ""),
    status_net_portInSpeed("portInSpeed", "网卡流入速率", ""),
    status_net_portOutSpeed("portOutSpeed", "网卡流出速率", ""),
    status_net_macAddress("macAddress", "网卡mac地址", ""),

    //端口状态事件
    event_port("event:event_port", "网络设备端口状态", ""),
    event_port_state("event:event_port:state", "端口状态事件", "端口%s 已断开连接"),
    //端口流入事件
    event_port_in_normal("event:event_port:in_normal", "端口流入普通阈值事件", getPubThresholdMsg("端口%s", "端口流入率", "%%")),
    event_port_in_section("event:event_port:in_section", "端口流入区间阈值事件", getSectionThresholdMsg("端口%s", "端口流入率", "%%")),
    event_port_in_sectionOne("event:event_port:in_sectionOne", "端口流入一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入率", "一阶", "%%")),
    event_port_in_sectionTwo("event:event_port:in_sectionTwo", "端口流入二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入率", "二阶", "%%")),
    event_port_in_sectionThree("event:event_port:in_sectionThree", "端口流入三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入率", "三阶", "%%")),
    //端口流出事件
    event_port_out_normal("event:event_port:out_normal", "端口流出普通阈值事件", getPubThresholdMsg("端口%s", "端口流出率", "%%")),
    event_port_out_section("event:event_port:out_section", "端口流出区间阈值事件", getSectionThresholdMsg("端口%s", "端口流出率", "%%")),
    event_port_out_sectionOne("event:event_port:out_sectionOne", "端口流出一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出率", "一阶", "%%")),
    event_port_out_sectionTwo("event:event_port:out_sectionTwo", "端口流出二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出率", "二阶", "%%")),
    event_port_out_sectionThree("event:event_port:out_sectionThree", "端口流出三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出率", "三阶", "%%")),
    //端口流入丢包事件
    event_port_inLose_normal("event:event_port:inLose_normal", "端口流入丢包普通阈值事件", getPubThresholdMsg("端口%s", "端口流入丢包率", "%%")),
    event_port_inLose_section("event:event_port:inLose_section", "端口流入丢包区间阈值事件", getSectionThresholdMsg("端口%s", "端口流入丢包率", "%%")),
    event_port_inLose_sectionOne("event:event_port:inLose_sectionOne", "端口流入丢包一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入丢包率", "一阶", "%%")),
    event_port_inLose_sectionTwo("event:event_port:inLose_sectionTwo", "端口流入丢包二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入丢包率", "二阶", "%%")),
    event_port_inLose_sectionThree("event:event_port:inLose_sectionThree", "端口流入丢包三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入丢包率", "三阶", "%%")),
    //端口流出丢包事件
    event_port_outLose_normal("event:event_port:outLose_normal", "端口流出丢包普通阈值事件", getPubThresholdMsg("端口%s", "端口流出丢包率", "%%")),
    event_port_outLose_section("event:event_port:outLose_section", "端口流出丢包区间阈值事件", getSectionThresholdMsg("端口%s", "端口流出丢包率", "%%")),
    event_port_outLose_sectionOne("event:event_port:outLose_sectionOne", "端口流出丢包一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出丢包率", "一阶", "%%")),
    event_port_outLose_sectionTwo("event:event_port:outLose_sectionTwo", "端口流出丢包二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出丢包率", "二阶", "%%")),
    event_port_outLose_sectionThree("event:event_port:outLose_sectionThree", "端口流出丢包三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出丢包率", "三阶", "%%")),
    //端口流入误码事件
    event_port_inError_normal("event:event_port:inError_normal", "端口流入误码普通阈值事件", getPubThresholdMsg("端口%s", "端口流入误码率", "%%")),
    event_port_inError_section("event:event_port:inError_section", "端口流入误码区间阈值事件", getSectionThresholdMsg("端口%s", "端口流入误码率", "%%")),
    event_port_inError_sectionOne("event:event_port:inError_sectionOne", "端口流入误码一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入误码率", "一阶", "%%")),
    event_port_inError_sectionTwo("event:event_port:inError_sectionTwo", "端口流入误码二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入误码率", "二阶", "%%")),
    event_port_inError_sectionThree("event:event_port:inError_sectionThree", "端口流入误码三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入误码率", "三阶", "%%")),

    //端口流出误码事件
    event_port_outError_normal("event:event_port:outError_normal", "端口流出误码普通阈值事件", getPubThresholdMsg("端口%s", "端口流出误码率", "%%")),
    event_port_outError_section("event:event_port:outError_section", "端口流出误码区间阈值事件", getSectionThresholdMsg("端口%s", "端口流出误码率", "%%")),
    event_port_outError_sectionOne("event:event_port:outError_sectionOne", "端口流出误码一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出误码率", "一阶", "%%")),
    event_port_outError_sectionTwo("event:event_port:outError_sectionTwo", "端口流出误码二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出误码率", "二阶", "%%")),
    event_port_outError_sectionThree("event:event_port:outError_sectionThree", "端口流出误码三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出误码率", "三阶", "%%")),
    //端口光口状态事件
    event_port_optical_state("event:event_port:optical_state", "光口状态事件", "光口%s状态异常"),
    event_port_optical_in_normal("event:event_port:optical_in_normal", "光功率接收普通阈值事件", getPubThresholdMsg("光口%s", "光功率接收率", "%%")),
    event_port_optical_in_section("event:event_port:optical_in_section", "光功率接收区间阈值事件", getSectionThresholdMsg("端口%s", "光功率接收率", "%%")),
    event_port_optical_out_normal("event:event_port:optical_out_normal", "光功率发送普通阈值事件", getPubThresholdMsg("光口%s", "光功率发送率", "%%")),
    event_port_optical_out_section("event:event_port:optical_out_section", "光功率发送区间阈值事件", getSectionThresholdMsg("端口%s", "光功率发送率", "%%")),
    //端口事件对应的状态数据
    status_event_interface("status:event:interface", "端口事件对应的状态数据", ""),

    status_interface_up_down("interface_up_down", "端口up/dwon状态", ""),
    status_interface_portOut("interface_portOut", "端口流出量状态", ""),
    status_interface_portOutRate("interface_portOutRate", "端口流出率状态", ""),
    status_interface_portIn("interface_portIn", "端口流入量状态", ""),
    status_interface_portInRate("interface_portInRate", "端口流入率状态", ""),
    status_interface_discardPacketsIn("interface_discardPacketsIn", "端口流入丢包状态", ""),
    status_interface_losePacketInRate("interface_losepacketInRate", "端口流入丢包率状态", ""),
    status_interface_discardPacketsOut("interface_discardPacketsOut", "端口流出丢包状态", ""),
    status_interface_losePacketOutRate("interface_losepacketOutRate", "端口流出丢包率状态", ""),
    status_interface_errorCodeIn("interface_errorCodeIn", "端口流入误码状态", ""),

    status_interface_errorCodeInRate("interface_errorCodeInRate", "端口流入误码率状态", ""),
    status_interface_errorCodeOut("interface_errorCodeOut", "端口流出误码状态", ""),
    status_interface_errorCodeOutRate("interface_errorCodeOutRate", "端口流出误码率状态", ""),
    status_interface_portAlias("interface_portAlias", "端口别名状态", ""),
    status_interface_portType("interface_portType", "端口类型状态", ""),
    status_interface_portIndex("interface_portIndex", "端口索引状态", ""),
    status_interface_portIndexRank("interface_portIndexRank", "端口排序状态", ""),
    status_interface_portLinkType("interface_portLinkType", "端口连接类型状态", ""),
    status_interface_linkIp("interface_linkIp", "端口对应的IP地址状态", ""),
    status_interface_linkMask("interface_linkMask", "端口对应的mask地址状态", ""),
    status_interface_linkPhyAddress("interface_linkPhyAddress", "端口物理地址状态", ""),
    status_interface_noUnicastPacketsIn("interface_noUnicastPacketsIn", "端口非单播流入量状态", ""),
    status_interface_noUnicastPacketsOut("interface_noUnicastPacketsOut", "端口非单播流出量状态", ""),
    status_interface_unicastPacketsIn("interface_unicastPacketsIn", "端口单播流入量状态", ""),
    status_interface_unicastPacketsOut("interface_unicastPacketsOut", "端口单播流出量状态", ""),
    status_interface_portSpeed("interface_portSpeed", "端口最大速率状态", ""),
    status_interface_portInSpeed("interface_portInSpeed", "端口接收速率状态", ""),
    status_interface_portOutSpeed("interface_portOutSpeed", "端口发送速率状态", ""),
    status_interface_crcErrors("interface_crcErrors", "端口CRC校验错误数状态", ""),
    status_interface_txPower("interface_txPower", "端口传输功率状态", ""),
    status_interface_rxPower("interface_rxPower", "端口传输功率状态", ""),
    //光纤交换机状态
    status_interface_optical_status("interface_optical_status", "光交光口状态", ""),
    status_interface_optical_bandwith("bandwith", "光交光口边界宽度", ""),
    status_interface_txpower("txpower", "tx功率", ""),
    status_interface_rxpower("rxpower", "rx功率", ""),


    //ping状态
    event_ping("event:event_ping", "ping状态", ""),
    event_ping_no_group("event:event_ping:no_group", "单机ping事件", "Ping设备失败，设备网络异常"),
    event_ping_group_all("event:event_ping:group_all", "组ping全部掉线事件", "设备组内所有设备Ping失败"),
    event_ping_group_other("event:event_ping:group_other", "组ping部分掉线事件", "设备组内部分设备Ping检测失败"),
    //ping事件对应的状态数据
    status_event_ping("status:event_ping", "ping状态", ""),
    group_status_ping("group_status_ping", "组ping状态", ""),
    status_ping("status_ping", "ping状态", ""),


    //系统端口占用事件
    event_system_port("event:event_system_port", "系统端口占用状态", ""),
    event_system_udpPort_status("event:event_system_port:udpState", "系统UDP端口占用状态事件", "设备UDP最后一个端口被占用。"),
    event_system_tcpPort_status("event:event_system_port:tcpState", "系统tcp端口占用状态事件", "设备TCP最后一个端口被占用"),
    //系统端口占用事件对应的状态数据
    event_status_system_port("status:event_system_port", "系统端口占用事件对应的状态数据", ""),
    status_tcp_portNumber("port_tcpNumber", "tcp端口占用个数", ""),
    status_tcp_max_port("port_maxTcpPort", "tcp最大端口占用", ""),
    status_udp_portNumber("port_udpNumber", "udp端口占用个数", ""),
    status_udp_max_port("port_maxUdpPort", "udp最大端口占用", ""),


    //进程状态事件
    event_process("event:event_process", "进程状态", ""),
    event_process_cpu("event:event_process:cpu", "进程CPU阈值事件", getPubThresholdMsg("进程:%s", "进程CPU使用率", "%%")),
    event_process_memory("event:event_process:memory", "进程内存阈值事件", getPubThresholdMsg("进程:%s", "进程内存使用率", "%%")),
    event_process_other_down("event:event_process:other_down", "进程部分掉线事件", "集群进程%s部分设备进程丢失。"),
    event_process_all_down("event:event_process:all_down", "进程全部掉线事件", "集群进程%s全部设备进程丢失。"),
    event_process_status("event:event_process:status", "进程状态事件", "进程%s丢失。"),
    //进程事件对应的状态数据
    status_event_process("status:event_process", "进程事件对应的状态数据", ""),
    status_process_cpu("status:event_process:process_cpu", "进程CPU阈值状态", ""),
    status_process_mem("status:event_process:process_mem", "进程内存阈值状态", ""),
    status_process_status("status:event_process:process_status", "进程状态", ""),


    //存储状态事件
    event_storage("event:event_storage", "存储状态", ""),
    event_storage_controller("event:event_storage:controller", "存储控制器事件", "存储控制器%s异常。"),
    event_storage_capacity("event:event_storage:capacity", "存储容量事件", "存储容量超过设定值。"),
    event_storage_group("event:event_storage:group", "存储group事件", "存储组%s异常。"),
    event_storage_Mdisk("event:event_storage:Mdisk", "存储Mdisk事件", "存储磁盘%s异常。"),
    event_storage_driver("event:event_storage:driver", "存储driver事件", "存储driver:%s 异常。"),
    event_storage_Vdisk("event:event_storage:Vdisk", "存储Vdisk事件", "存储Vdisk:%s 异常。"),
    event_storage_link("event:event_storage:link", "存储连接事件", "存储连击发生异常。"),
    //存储事件对应的状态信息
    status_event_storage("status:event_storage", "存储状态", ""),
    status_raidDS_controller_status("controller_status", "存储控制器状态", ""),
    status_raidDS_controller("status:event_storage:raid_controller", "存储控制器", ""),
    status_raid_diskType("raid_diskType", "存储磁盘类型", ""),
    status_raid_totalCapacity("raid_totalCapacity", "磁盘阵列总容量", ""),
    status_raid_usedCapacity("raid_usedCapacity", "磁盘阵列已使用容量", ""),
    status_raid_freeCapacity("raid_freeCapacity", "磁盘阵列未使用容量", ""),
    status_raid_diskCapacityCount("raid_diskCapacityCount", "磁盘容量个数", ""),
    status_raid_mdisk("status:raid_mdisk", "存储物理磁盘", ""),
    status_raid_vdisk("status:raid_vdisk", "存储虚拟磁盘", ""),
    status_raid_group("status:raid_group", "存储磁盘组", ""),
    status_raid_drive("status:raid_driver", "存储分区", ""),


    status_raidDS_logicDrives("status:event_storage:raid_logicDrives", "存储逻辑分区", ""),
    status_raidDS_Drives("status:event_storage:raid_Drives", "存储物理分区", ""),

    status_raidDS_drive("status:event_storage:raid_drives", "存储分区", ""),

    status_raid_name("raid_name", "存储磁盘名称", ""),

    status_raid_realid("raid_realid", "存储磁盘ID", ""),

    status_raid_status("raid_status", "存储磁盘状态", ""),
    status_raid_driver_status("raid_driver_status", "存储driver状态", ""),
    status_raidDS_mdisk_status("mdisk_status", "存储物理磁盘状态", ""),
    status_raidDS_drivers_status("drivers_status", "存储drivers状态", ""),
    status_raid_grpid("raid_grpid", "存储组ID", ""),
    status_raid_raidLevel("raid_raidLevel", "存储raid等级", ""),
    status_raid_capacity("raid_capacity", "存储容量", ""),
    status_raid_parentOrgName("raid_parentOrgName", "存储组织名称", ""),
    status_raid_parentOrgId("raid_parentOrgId", "存储组织ID", ""),
    status_raid_xindx("raid_xindx", "存储x位置", ""),
    status_raid_yindx("raid_yindx", "存储y位置", ""),
    status_raid_capacityStr("raid_capacityStr", "存储容量名称", ""),
    status_raid_usedCapacityStr("raid_usedCapacityStr", "存储使用容量名称", ""),
    status_raid_freeCapacityStr("raid_usedCapacityStr", "存储未使用容量名称", ""),


    //snnmp事件信息
    event_log("event:log", "事件信息", "系统收到SNMP消息：%s"),
    event_snmp("event:log:snmp", "snmp事件信息", "系统收到SNMP消息：%s"),
    event_syslog("event:log:syslog", "syslog事件信息", "系统收到syslog消息：%s"),
    event_aix_log("event:log:aixlog", "小机log事件信息", ""),
    event_raid_log("event:log:raidlog", "存储log事件信息", ""),

    //网络安全事件
    event_xdhy("event:event_xdhy", "网络安全状态", ""),
    event_xdhy_state("event:event_xdhy:state", "网络安全事件", "检测网络安全状态异常。"),
    //网络安全事件对应的状态数据
    status_xdhy("status:event_xdhy:xdhyState", "网络安全软件变化", ""),
    status_softSecureLinkState("softSecureLinkState", "软件安全连接状态", ""),

    //MQ事件信息
    mq_status("event:mq:status", "MQ状态事件信息", "MQ状态：%s"),
    mq_queue("event:mq:queue", "MQ队列事件信息", "MQ队列状态：%s"),


    //动环事件 environment
    event_environment("event:event_environment", "动环状态", "检测到动环状态异常");


    private String code;
    private String name;
    private String descr;

    /**
     * 拼装普通阈值
     *
     * @param title
     * @param msg
     * @return
     */
    private static String getPubThresholdMsg(String title, String msg, String unit) {
        return title + "当前" + msg + "为: %s" + unit + ",%s阈值:%s" + unit + "。";
    }

    /**
     * 拼装级别阈值
     *
     * @param title
     * @param msg
     * @return
     */
    private static String getLevelThresholdMsg(String title, String msg, String level, String unit) {
        return title + "当前" + msg + "为: %s" + unit + ",%s" + level + "阈值:%s" + unit + "。";
    }

    /**
     * 拼装区间阈值
     *
     * @param title
     * @param msg
     * @return
     */
    private static String getSectionThresholdMsg(String title, String msg, String unit) {
        return title + "当前" + msg + ": %s" + unit + ",%s阈值范围：%s%%~%s" + unit + "。";
    }

    StatusInfoChangeTypeEnumbak1(String code, String name, String descr) {
        this.code = code;
        this.name = name;
        this.descr = descr;
    }

    public static String getName(String code) {
        StatusInfoChangeTypeEnumbak1[] values = StatusInfoChangeTypeEnumbak1.values();
        for (StatusInfoChangeTypeEnumbak1 value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "UNKNOWN";
    }

    public static String getDescr(String code) {
        StatusInfoChangeTypeEnumbak1[] values = StatusInfoChangeTypeEnumbak1.values();
        for (StatusInfoChangeTypeEnumbak1 value : values) {
            if (value.code.equals(code)) {
                return value.descr;
            }
        }
        return "UNKNOWN";
    }
}
