package com.jcca.dataProcessing.enums;

import lombok.Getter;

/**
 * 枚举
 *
 * @author zhaozheng
 */
@Getter
public enum StatusInfoChangeTypeEnum {
    /**
     * 设备基础状态信息
     */

    /**
     * 设备类型
     * 183:服务器,
     * 1831:终端,
     * 201:交换机,
     * 42:路由器,
     * 1833:工控机,
     * 1832:小型机,
     * 1834:自律机,
     * 318:磁盘阵列,
     * 7001:存储设备,
     * 7002:安全边界,
     * 7003:网闸,
     * 7004:kvm,
     * 7005:PDU,
     * 7006:配线架,
     * 7007:光纤架,
     * 7008:授时仪,
     * 7009:双机切换单元,
     * 7010:防病毒服务器,
     * 7011:分析单元,
     * 7012:检测单元,
     * 7013:DDF,
     * 7014:KVM切换器,
     * 7015:空调,
     * 7016:UPS电源
     */
    status("status", "设备状态", "", "", ""),
    statusEvent("statusEvent", "事件监控状态", "", "", ""),
    statusEventValue("statusEventValue", "事件监控状态对应值", "", "", ""),
    //CPU相关基础信息
    status_CPUTop5("status:cpuTop5", "CPUTop5状态", "", "", ""),

    //管理口日志基础信息
    status_ipmi_log("status:ipmilog", "管理口日志", "", "", ""),
    //管理口硬件厂商
    status_ipmi_manu("status:manu", "硬件厂商", "", "", ""),
    status_ipmi_manuName("manuName", "硬件厂商名称", "", "", ""),

    status_MEMTop5("status:memTop5", "MEMTop5状态", "", "", ""),

    status_raid_log("status:raid_log", "存储日志", "", "", ""),


    //aix小机相关基础信息
    status_aix_io("status:aixIo", "小机io卡信息", "", "", ""),
    status_aix_type("adapterType", "io卡类型", "", "", ""),
    status_aix_state("adapterStat", "io状态", "", "", ""),
    status_aix_attention_state("attentionState", "链路状态", "", "", ""),
    status_aix_adapterSlot("adapterSlot", "IO口槽位", "", "", ""),
    status_aix_io_description("ioDescription", "IO描述信息", "", "", ""),
    status_aix_io_wwn("wwn", "光口wwn号", "", "", ""),
    status_memory("memory", "内存大小", "", "", ""),
    status_serial("serial", "设备列号", "", "", ""),
    status_systemVersion("systemVersion", "系统版本", "", "", ""),
    status_cpuMode("cpuMode", "CPU型号", "", "", ""),
    status_cpuNum("cpuNum", "CPU个数", "", "", ""),
    status_frequency("frequency", "CPU主频", "", "", ""),
    status_cpuCoreNum("cpuCoreNum", "CPU核心数", "", "", ""),
    status_powerNum("powerNum", "电源数量", "", "", ""),
    status_powerModel("powerModel", "电源型号", "", "", ""),
    status_diskNum("diskNum", "磁盘型号", "", "", ""),
    status_establishedNum("establishedNum", "设备连接数", "", "", ""),
    status_diskCapacityCount("diskCapacityCount", "磁盘个数", "", "", ""),
    status_totalCapacity("diskCapacityCount", "磁盘总容量", "", "", ""),


    //CTC业务状态事件
    event_CTC("event:event_CTC", "CTC业务状态", "", "", ""),
    event_CTC_link("event:event_CTC:link", "CTC业务连接事件", "业务告警：软件[%s]连接状态%s", "", ""),
    event_CTC_version("event:event_CTC:version", "CTC业务版本事件", "业务告警：软件[%s]版本由 %s 变更为：%s。", "", ""),
    event_CTC_threshold("event:event_CTC:threshold", "CTC业务软件容量事件", "业务告警：软件容量%s，%s阈值容量%s。", "", ""),
    event_CTC_runstate("event:event_CTC:runstate", "CTC业务运行状态事件", "业务告警：软件 %s 运行状态%s。", "", ""),
    //event_CTC_AB("event:event_CTC:AB", "CTC业务主备事件", "业务告警：业务:%s 主备发生切换,设备从%s变为%s。", "", ""),
    //业务系统那只有新值没有旧值
    event_CTC_AB("event:event_CTC:AB", "CTC业务主备事件", "业务告警：实体号为 %s 的软件[%s]主备发生切换，由%s切换为%s。", "", ""),

    //CTC业务状态事件对应的状态信息
    status_softLinkState("status:softLinkState", "软件连接状态", "", "", ""),
    status_softThresholdState("status:softThresholdState", "软件容量状态", "", "", ""),

    status_softChannel("status:softChannel", "软件通道状态", "", "", ""),
    status_softVersion("status:softVersion", "软件版本状态", "", "", ""),
    status_soft("status:soft_status", "设备软件状态", "", "", ""),
    status_softMasterState("status:softMasterState", "软件主备状态", "", "", ""),


    //时钟同步状态事件
    event_time("event:event_time", "时钟事件", "时钟服务器状态异常", "时钟事件", "183_1831_1832_1833_1834"),
    event_time_state("event:event_time:state", "时间偏差事件", getPubThresholdMsg("", "时间偏差", "秒"), "", ""),
    event_run_time_state("event:event_time:run_state", "运行时长事件", getPubThresholdMsg("", "运行时长", "天"), "运行时长", ""),
    event_clock_state("event:event_time:clock_state", "时间服务器状态事件", "时钟同步服务状态%s", "", ""),
    event_run_restart("event:event_time:run_restart", "设备事件", "检测到设备可能存在重启情况,请检查设备运行状态是否正常。", "", ""),
    //时钟徒步事件对应的状态信息
    status_time_deviation("time_deviation", "时间偏差", "", "", ""),
    status_run_time("run_time", "运行时间", "", "", ""),

    status_run_time_restart("run_time_restart", "重启时间时间", "", "", ""),

    status_system_time("system_time", "系统时间", "", "", ""),
    status_time_server("status:time_server", "时钟服务器时钟状态", "", "", ""),
    status_time_server_monitor("status:time_server_monitor", "时钟服务器时钟状态监控", "", "", ""),
    status_time_server_status("time_serve_status", "时钟服务器时钟状态", "", "", ""),
    status_time_server_location("time_serve_location", "时钟服务器软件路径", "", "", ""),
    status_time_server_message("time_serve_status", "时钟服务器日志消息", "", "", ""),


    //自律机集群状态
    event_clusterState("event:clusterStateEvent", "自律机状态事件", "自律机集群主机%s状态异常。", "", ""),
    event_clusterABState("event:clusterStateEvent:clusterABState", "自律机集群AB机状态事件", "自律机集群主机%s状态异常。", "", ""),
    event_clusterMasterState("event:clusterStateEvent:clusterMasterState", "自律机集群主备机切换事件", "自律机集群主机切换为%s。", "", ""),
    //自律机集群事件对应的状态信息
    status_clusterABState("clusterABState", "自律机集群AB机状态", "", "", ""),
    status_clusterMasterState("clusterMasterState", "自律机集群主备机切换", "", "", ""),
    status_clusterState("clusterState", "自律机状态", "", "", ""),


    //通信质量监督状态事件
    event_linkQuality("event:event_linkQuality", "通信质量监督状态", "", "", ""),
    event_linkQuality_state("event:event_linkQuality:state", "通信质量监督事件", "业务告警：检测到通信质量监督状态异常。", "", ""),
    //通信质量监督事件对应的状态信息
    status_congxing("status:congxingState", "通信质量监督软件变化", "", "", ""),


    //自定义事件信息  category分类
    //CPU事件
    event("event", "设备事件", "", "", "183_1831_1832_1833_1834_201_42"),
    event_CPU("event:event_CPU", "cpu状态", "", "CPU巡检", "183_1831_1832_1833_1834_201_42"),
    event_cpu_state("event:event_CPU:status", "cpu状态", "%s状态%s", "CPU状态", ""),
    event_CPU_normal("event:event_CPU:normal", "cpu普通阈值事件", getPubThresholdMsg("", "CPU使用率", "%%"), "CPU阈值状态", ""),
    event_CPU_section("event:event_CPU:section", "cpu区间阈值事件", getSectionThresholdMsg("", "CPU使用率", "%%"), "CPU区间阈值状态", ""),
    event_CPU_sectionOne("event:event_CPU:sectionOne", "cpu一阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "一阶", "%%"), "CPU一阶状态", ""),
    event_CPU_sectionTwo("event:event_CPU:sectionTwo", "cpu二阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "二阶", "%%"), "CPU二阶状态", ""),
    event_CPU_sectionThree("event:event_CPU:sectionThree", "cpu三阶阈值事件", getLevelThresholdMsg("", "CPU使用率", "三阶", "%%"), "CPU三阶状态", ""),
    //CPU事件对应状态
    status_CPU("status:CPU", "CPU状态", "", "", ""),
    status_CPUState("CPUState", "CPU状态", "", "", ""),
    //CPU管理口获取的数据
    status_cpu_value("cpuValue", "cpu值", "", "", ""),
    status_cpu_status("cpuStatus", "cpu状态", "", "", ""),
    status_cpu_core("cpuCore", "cpu核数", "", "", ""),
    status_cpu_voltage("cpuVoltage", "cpu电压", "", "", ""),


    //数据库状态事件
    event_db("event:event_db", "数据库状态", "", "数据库巡检", "263"),
    event_db_connect("event:event_db:connect", "数据库连接事件", "检测到数据库连接%s！", "数据库状态", ""),
    //数据库事件对应的状态信息
    status_db("status:db", "数据库状态", "", "", ""),
    status_db_state("db_state", "数据库状态", "", "", ""),
    status_dbVersion("dbVersion", "数据库版本", "", "", ""),
    status_dbSysUpTime("dbSysUpTime", "启动时间", "", "", ""),
    status_dbCacheLibrary("dbCacheLibrary", "缓存命中率", "", "", ""),
    status_dbMemTotal("dbMemTotal", "数据库的运存总量", "", "", ""),
    status_dbDiskTotal("dbDiskTotal", " 硬盘总量", "", "", ""),
    status_dbCache("dbCache", "缓存大小", "", "", ""),
    status_dbBusynessRate("dbBusynessRate", "繁忙率", "", "", ""),
    status_dbSessionSize("dbSessionSize", "数据库的session数", "", "", ""),
    status_dbSessionUsedRate("dbSessionUsedRate", "数据库使用率", "", "", ""),
    status_dbCachePoolSize("dbCachePoolSize", "缓存池大小", "", "", ""),
    status_dbCachePoolhit("dbCachePoolhit", "缓存池命中率", "", "", ""),
    status_canUseLockSize("canUseLockSize", "可用锁数量", "", "", ""),
    status_dbLockUsedRate("dbLockUsedRate", "锁使用率", "", "", ""),
    status_dbLockWaitRate("dbLockWaitRate", "据库锁的等待率", "", "", ""),
    status_javaPoolSize("javaPoolSize", "java池大小", "", "", ""),
    status_redoLogBuffer("redoLogBuffer", "大池大小", "", "", ""),
    status_dbConnection("dbConnection", "数据库连接数", "", "", ""),
    status_dbActive("dbActive", "数据库活动连接数", "", "", ""),
    status_dbLanguage("dbLanguage", "语言环境", "", "", ""),
    status_alertPath("alertPath", "告警文件地址", "", "", ""),

    //表空间事件
    event_db_tableSpace("event:tableSpace", "数据库表空间使用率事件", getPubThresholdMsg("表空间：%s", "使用率", "%%"), "数据库表空间巡检", "263"),
    event_db_tableSpace_normal("event:tableSpace:normal", "数据库表空间使用率事件", getPubThresholdMsg("表空间：%s", "使用率", "%%"), "表空间使用率状态", ""),
    event_tableSpace_sectionOne("event:tableSpace:sectionOne", "数据库表空间一阶阈值事件", getLevelThresholdMsg("表空间：%s", "使用率", "一阶", "%%"), "表空间一阶状态", ""),
    event_tableSpace_sectionTwo("event:tableSpace:sectionTwo", "数据库表空间二阶阈值事件", getLevelThresholdMsg("表空间：%s", "使用率", "二阶", "%%"), "表空间二阶状态", ""),
    event_tableSpace_sectionThree("event:tableSpace:sectionThree", "数据库表空间三阶阈值事件", getLevelThresholdMsg("表空间：%s", "使用率", "三阶", "%%"), "表空间三阶状态", ""),

    //表空间事件对应的状态信息
    status_tablespace("status:tablespace", "使用率", "", "", ""),
    status_tablespace_usedRate("usedRate", "使用率", "", "", ""),
    status_tablespace_totalSize("totalSize", "表空间总大小", "", "", ""),
    status_tablespace_freeSize("freeSize", "空闲大小", "", "", ""),
    status_tablespace_usedSize("usedSize", "使用大小", "", "", ""),


    //磁盘事件
    event_disk("event:event_disk", "磁盘状态", "", "磁盘巡检", "183_1831_1832_1833_1834"),
    event_disk_normal("event:event_disk:normal", "磁盘普通阈值事件", getPubThresholdMsg("磁盘：%s ", "磁盘使用率", "%%"), "磁盘阈值状态", ""),
    event_disk_section("event:event_disk:section", "磁盘区间阈值事件", getSectionThresholdMsg("磁盘: %s ", "磁盘使用率", "%%"), "磁盘区间阈值状态", ""),
    event_mdisk_sectionOne("event:event_disk:sectionOne", "磁盘一阶阈值事件", getLevelThresholdMsg("磁盘: %s ", "磁盘使用率", "一阶", "%%"), "磁盘一阶状态", ""),
    event_disk_sectionTwo("event:event_disk:sectionTwo", "磁盘二阶阈值事件", getLevelThresholdMsg("磁盘: %s ", "磁盘使用率", "二阶", "%%"), "磁盘二阶状态", ""),
    event_disk_sectionThree("event:event_disk:sectionThree", "磁盘三阶阈值事件", getLevelThresholdMsg("磁盘: %s ", "磁盘使用率", "三阶", "%%"), "磁盘三阶状态", ""),
    //磁盘事件对应的状态信息
    status_disk("status:disk", "磁盘状态", "", "", ""),


    //风扇事件
    event_fan("event:event_fan", "风扇状态", "", "风扇巡检", "183_201_42"),
    event_fan_state("event:event_fan:state", "风扇状态事件", "风扇%s状态%s", "风扇状态", ""),
    //风扇事件对应的状态信息
    status_fan("status:fan", "", "", "", ""),
    status_fanvalue("fanValue", "风扇转速", "", "", ""),
    status_fanStatus("fanStatus", "风扇状态", "", "", ""),


    //指示灯事件
    event_led("event:event_led", "指示灯状态", "", "指示灯巡检", "183_201_42"),
    event_led_state("event:event_led:state", "指示灯状态事件", "指示灯%s状态%s", "指示灯状态", ""),
    //指示灯事件对应的状态信息
    status_event_ipmi_led("status:led", "硬件状态灯", "", "", ""),
    status_led_state("led_state", "指示灯状态", "", "", ""),


    //电源事件
    event_power("event:event_power", "电源状态", "", "电源巡检", "183_201_42"),
    event_power_state("event:event_power:state", "电源状态事件", "电源%s状态%s", "电源状态", ""),
    //电源事件对应的状态信息
    status_power("status:power", "电源状态", "", "", ""),
    status_power_state("power_state", "电源状态", "", "", ""),

    //IPMI事件
    event_ipmi("event:ipmi", "管理口状态", "", "", ""),
    event_ipmi_ping("event:ipmi:ping", "管理口Ping事件", "", "", ""),

    //系统未确认告警
    event_unconfirmed("event:unconfirmed", "存在未处理告警", "", "", ""),
    event_unconfirmed_2("event:unconfirmed:2", "存在未处理二级告警事件", "", "", ""),
    event_unconfirmed_3("event:unconfirmed:3", "存在未处理三级告警事件", "", "", ""),

    //采集器节点状态
    event_jcca("event:jcca", "运维平台状态", "", "", ""),
    event_jcca_center("event:jcca:center", "中心采集器事件", "", "", ""),
    event_jcca_station("event:jcca:station", "车站采集器事件", "", "", ""),


    //温度事件
    event_temp("event:event_temp", "温度状态", "", "温度巡检", "183_201_42"),
    event_temp_state("event:event_temp:state", "温度状态", "温度传感%s状态%s", "温度状态", ""),
    event_temp_state_normal("event:event_temp:normal", "温度传感器普通阈值事件", getPubThresholdMsg("温度传感器%s", "温度", "℃"), "温度传感器阈值状态", ""),
    event_temp_state_sectionOne("event:event_temp:sectionOne", "温度传感器一阶阈值事件", getLevelThresholdMsg("温度传感器%s", "温度", "一阶", "℃"), "温度一阶状态", ""),
    event_temp_state_sectionTwo("event:event_temp:sectionTwo", "温度传感器二阶阈值事件", getLevelThresholdMsg("温度传感器%s", "温度", "二阶", "℃"), "温度二阶状态", ""),
    event_temp_state_sectionThree("event:event_temp:sectionThree", "温度传感器三阶阈值事件", getLevelThresholdMsg("温度传感器%s", "温度", "三阶", "℃"), "温度三阶状态", ""),
    //温度事件对应的状态信息
    status_temp("status:temp", "温度", "", "", ""),
    status_tempValue("tempValue", "温度值", "", "", ""),
    status_tempStatus("tempStatus", "温度状态", "", "", ""),


    //内存事件
    event_memory("event:event_memory", "内存状态", "", "内存状态巡检", "183_1831_1832_1833_1834_201_42"),
    event_memory_normal("event:event_memory:normal", "内存普通阈值事件", getPubThresholdMsg("", "内存使用率", "%%"), "内存阈值状态", ""),
    event_memory_section("event:event_memory:section", "内存区间阈值事件", getSectionThresholdMsg("", "内存使用率", "%%"), "内存区间阈值状态", ""),
    event_memory_sectionOne("event:event_memory:sectionOne", "内存一阶阈值事件", getLevelThresholdMsg("", "内存使用率", "一阶", "%%"), "内存一阶状态", ""),
    event_memory_sectionTwo("event:event_memory:sectionTwo", "内存二阶阈值事件", getLevelThresholdMsg("", "内存使用率", "二阶", "%%"), "内存二阶状态", ""),
    event_memory_sectionThree("event:event_memory:sectionThree", "内存三阶阈值事件", getLevelThresholdMsg("", "内存使用率", "三阶", "%%"), "内存三阶状态", ""),
    //内存事件对应的状态信息
    status_memoryState("memoryState", "内存状态", "", "", ""),
    status_memory_total("memoryTotal", "内存总量", "", "", ""),
    status_memory_used("memoryUsed", "内存使用量", "", "", ""),
    status_switch_memory_total("switchMemoryTotal", "交换内存总量", "", "", ""),
    status_switch_memory_used("switchMemoryUsed", "交换内存使用量", "", "", ""),
    status_switch_memory_used_rate("switchMemoryUsedRate", "交换内存使用率", "", "", ""),


    //网卡状态事件
    event_net("event:event_net", "网卡状态", "", "网卡状态巡检", "183_1831_1832_1833_1834"),
    event_net_flow("event:event_net:flow", "网卡流量阈值事件", getPubThresholdMsg("网卡：%s", "网卡流量", "Kbps"), "网卡流量状态", ""),
    event_net_state("event:event_net:state", "网卡状态事件", "网卡%s状态变化。", "网卡状态", ""),
    //网卡事件对应的状态数据
    status_net("status:net", "网卡状态", "", "", ""),
    status_net_status("netStatus", "网卡状态", "", "", ""),
    status_net_portIn("portIn", "网卡流入量", "", "", ""),
    status_net_portOut("portOut", "网卡流出量", "", "", ""),
    status_net_portInSpeed("portInSpeed", "网卡流入速率", "", "", ""),
    status_net_portOutSpeed("portOutSpeed", "网卡流出速率", "", "", ""),
    status_net_macAddress("macAddress", "网卡mac地址", "", "", ""),
    status_net_ip("ip", "网卡ip地址", "", "", ""),

    //端口状态事件
    event_port("event:event_port", "网络设备端口状态", "", "端口巡检", "201_42"),
    event_port_state("event:event_port:state", "端口状态事件", "端口%s状态发生变化(%s)。", "端口状态", ""),
    //端口流入事件
    event_port_in_normal("event:event_port:in_normal", "端口流入普通阈值事件", getPubThresholdMsg("端口%s", "端口流入率", "%%"), "端口流入状态", ""),
    event_port_in_section("event:event_port:in_section", "端口流入区间阈值事件", getSectionThresholdMsg("端口%s", "端口流入", "Bps"), "区间流入阈值状态", ""),
    event_port_in_sectionOne("event:event_port:in_sectionOne", "端口流入一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入率", "一阶", "%%"), "端口流入一阶状态", ""),
    event_port_in_sectionTwo("event:event_port:in_sectionTwo", "端口流入二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入率", "二阶", "%%"), "端口流入二阶状态", ""),
    event_port_in_sectionThree("event:event_port:in_sectionThree", "端口流入三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入率", "三阶", "%%"), "端口流入三阶状态", ""),
    //端口流出事件
    event_port_out_normal("event:event_port:out_normal", "端口流出普通阈值事件", getPubThresholdMsg("端口%s", "端口流出率", "%%"), "端口流出阈值状态", ""),
    event_port_out_section("event:event_port:out_section", "端口流出区间阈值事件", getSectionThresholdMsg("端口%s", "端口流出", "Bps"), "端口流出区间状态", ""),
    event_port_out_sectionOne("event:event_port:out_sectionOne", "端口流出一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出率", "一阶", "%%"), "端口流出一阶状态", ""),
    event_port_out_sectionTwo("event:event_port:out_sectionTwo", "端口流出二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出率", "二阶", "%%"), "端口流出二阶状态", ""),
    event_port_out_sectionThree("event:event_port:out_sectionThree", "端口流出三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出率", "三阶", "%%"), "端口流出三阶状态", ""),
    //端口流入丢包事件
    event_port_inLose_normal("event:event_port:inLose_normal", "端口流入丢包普通阈值事件", getPubThresholdMsg("端口%s", "端口流入丢包率", "%%"), "端口流入丢包阈值状态", ""),
    event_port_inLose_section("event:event_port:inLose_section", "端口流入丢包区间阈值事件", getSectionThresholdMsg("端口%s", "端口流入丢包率", "%%"), "端口流入丢包区间阈值状态", ""),
    event_port_inLose_sectionOne("event:event_port:inLose_sectionOne", "端口流入丢包一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入丢包率", "一阶", "%%"), "端口流入丢包一阶状态", ""),
    event_port_inLose_sectionTwo("event:event_port:inLose_sectionTwo", "端口流入丢包二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入丢包率", "二阶", "%%"), "端口流入丢包二阶状态", ""),
    event_port_inLose_sectionThree("event:event_port:inLose_sectionThree", "端口流入丢包三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入丢包率", "三阶", "%%"), "端口流入丢包三阶状态", ""),
    //端口流出丢包事件
    event_port_outLose_normal("event:event_port:outLose_normal", "端口流出丢包普通阈值事件", getPubThresholdMsg("端口%s", "端口流出丢包率", "%%"), "端口流出丢包阈值状态", ""),
    event_port_outLose_section("event:event_port:outLose_section", "端口流出丢包区间阈值事件", getSectionThresholdMsg("端口%s", "端口流出丢包率", "%%"), "端口流出丢包区间状态", ""),
    event_port_outLose_sectionOne("event:event_port:outLose_sectionOne", "端口流出丢包一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出丢包率", "一阶", "%%"), "端口流出丢包一阶状态", ""),
    event_port_outLose_sectionTwo("event:event_port:outLose_sectionTwo", "端口流出丢包二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出丢包率", "二阶", "%%"), "端口流出丢包二阶状态", ""),
    event_port_outLose_sectionThree("event:event_port:outLose_sectionThree", "端口流出丢包三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出丢包率", "三阶", "%%"), "端口丢包三阶状态", ""),
    //端口流入误码事件
    event_port_inError_normal("event:event_port:inError_normal", "端口流入误码普通阈值事件", getPubThresholdMsg("端口%s", "端口流入误码率", "%%"), "端口流入误码阈值状态", ""),
    event_port_inError_section("event:event_port:inError_section", "端口流入误码区间阈值事件", getSectionThresholdMsg("端口%s", "端口流入误码率", "%%"), "端口流入误码区间状态", ""),
    event_port_inError_sectionOne("event:event_port:inError_sectionOne", "端口流入误码一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入误码率", "一阶", "%%"), "端口流入误码一阶状态", ""),
    event_port_inError_sectionTwo("event:event_port:inError_sectionTwo", "端口流入误码二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入误码率", "二阶", "%%"), "端口流入误码二阶状态", ""),
    event_port_inError_sectionThree("event:event_port:inError_sectionThree", "端口流入误码三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流入误码率", "三阶", "%%"), "端口流入误码三阶状态", ""),

    //端口流出误码事件
    event_port_outError_normal("event:event_port:outError_normal", "端口流出误码普通阈值事件", getPubThresholdMsg("端口%s", "端口流出误码率", "%%"), "端口流出误码阈值状态", ""),
    event_port_outError_section("event:event_port:outError_section", "端口流出误码区间阈值事件", getSectionThresholdMsg("端口%s", "端口流出误码率", "%%"), "端口流出误码区间状态", ""),
    event_port_outError_sectionOne("event:event_port:outError_sectionOne", "端口流出误码一阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出误码率", "一阶", "%%"), "端口流出误码一阶状态", ""),
    event_port_outError_sectionTwo("event:event_port:outError_sectionTwo", "端口流出误码二阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出误码率", "二阶", "%%"), "端口流出误码二阶状态", ""),
    event_port_outError_sectionThree("event:event_port:outError_sectionThree", "端口流出误码三阶阈值事件", getLevelThresholdMsg("端口%s", "端口流出误码率", "三阶", "%%"), "端口流出误码三阶状态", ""),
    //端口光口状态事件
    event_port_optical_state("event:event_port:optical_state", "光口状态事件", "光口%s状态%s", "", ""),
    event_port_optical_in_normal("event:event_port:optical_in_normal", "光功率接收普通阈值事件", getPubThresholdMsg("光口%s", "光功率接收功率", "dbm"), "", ""),
    event_port_optical_in_section("event:event_port:optical_in_section", "光功率接收区间阈值事件", getSectionThresholdMsg("端口%s", "光功率接收功率", "dbm"), "", ""),
    event_port_optical_in_sectionOne("event:event_port:optical_in_sectionOne", "光功率接收一阶阈值事件", getLevelThresholdMsg("端口%s", "光功率接收功率", "一阶", "dbm"), "", ""),
    event_port_optical_in_sectionTwo("event:event_port:optical_in_sectionTwo", "光功率接收二阶阈值事件", getLevelThresholdMsg("端口%s", "光功率接收功率", "二阶", "dbm"), "", ""),
    event_port_optical_in_sectionThree("event:event_port:optical_in_sectionThree", "光功率接收三阶阈值事件", getLevelThresholdMsg("端口%s", "光功率接收功率", "三阶", "dbm"), "", ""),


    event_port_optical_out_normal("event:event_port:optical_out_normal", "光功率发送普通阈值事件", getPubThresholdMsg("光口%s", "光功率发送功率", "dbm"), "", ""),
    event_port_optical_out_section("event:event_port:optical_out_section", "光功率发送区间阈值事件", getSectionThresholdMsg("端口%s", "光功率发送功率", "dbm"), "", ""),
    event_port_optical_out_sectionOne("event:event_port:optical_out_sectionOne", "光功率发送一阶阈值事件", getLevelThresholdMsg("端口%s", "光功率发送功率", "一阶", "dbm"), "", ""),
    event_port_optical_out_sectionTwo("event:event_port:optical_out_sectionTwo", "光功率发送二阶阈值事件", getLevelThresholdMsg("端口%s", "光功率发送功率", "二阶", "dbm"), "", ""),
    event_port_optical_out_sectionThree("event:event_port:optical_out_sectionThree", "光功率发送三阶阈值事件", getLevelThresholdMsg("端口%s", "光功率发送功率", "三阶", "dbm"), "", ""),

    //端口事件对应的状态数据
    status_interface("status:interface", "端口up/dwon状态", "", "", ""),
    status_interface_up_down("interface_up_down", "端口up/dwon状态", "", "", ""),
    status_interface_portOut("interface_portOut", "端口流出量状态", "", "", ""),
    status_interface_portOutRate("interface_portOutRate", "端口流出率状态", "", "", ""),

    status_interface_portIn("interface_portIn", "端口流入量状态", "", "", ""),
    status_interface_portInRate("interface_portInRate", "端口流入率状态", "", "", ""),
    status_interface_discardPacketsIn("interface_discardPacketsIn", "端口流入丢包数", "", "", ""),
    status_interface_discardPacketsOut("interface_discardPacketsOut", "端口流出丢包数", "", "", ""),
    status_interface_losePacketInRate("interface_losepacketInRate", "端口流入丢包率状态", "", "", ""),
    status_interface_losePacketOutRate("interface_losepacketOutRate", "端口流出丢包率状态", "", "", ""),
    status_interface_errorCodeIn("interface_errorCodeIn", "端口流入误码数", "", "", ""),

    status_interface_errorCodeInRate("interface_errorCodeInRate", "端口流入误码率状态", "", "", ""),
    status_interface_errorCodeOut("interface_errorCodeOut", "端口流出误码状态", "", "", ""),
    status_interface_errorCodeOutRate("interface_errorCodeOutRate", "端口流出误码率", "", "", ""),
    status_interface_portAlias("interface_portAlias", "端口别名状态", "", "", ""),
    status_interface_portType("interface_portType", "端口类型状态", "", "", ""),
    status_interface_portIndex("interface_portIndex", "端口索引状态", "", "", ""),
    status_interface_portIndexRank("interface_portIndexRank", "端口排序状态", "", "", ""),
    status_interface_portLinkType("interface_portLinkType", "端口连接类型状态", "", "", ""),
    status_interface_linkIp("interface_linkIp", "端口对应的IP地址状态", "", "", ""),
    status_interface_linkMask("interface_linkMask", "端口对应的mask地址状态", "", "", ""),
    status_interface_linkPhyAddress("interface_linkPhyAddress", "端口物理地址状态", "", "", ""),
    status_interface_noUnicastPacketsIn("interface_noUnicastPacketsIn", "端口非单播流入量", "", "", ""),
    status_interface_noUnicastPacketsOut("interface_noUnicastPacketsOut", "端口非单播流出量状态", "", "", ""),
    status_interface_unicastPacketsIn("interface_unicastPacketsIn", "端口单播流入量", "", "", ""),
    status_interface_unicastPacketsOut("interface_unicastPacketsOut", "端口单播流出量状态", "", "", ""),
    status_interface_portSpeed("interface_portSpeed", "端口最大速率状态", "", "", ""),
    status_interface_portInSpeed("interface_portInSpeed", "端口接收速率状态", "", "", ""),
    status_interface_portInSpeedBps("interface_portInSpeedBps", "端口状态流入速率Bps", "", "", ""),


    status_interface_portOutSpeed("interface_portOutSpeed", "端口发送速率状态", "", "", ""),
    status_interface_portOutSpeedBps("interface_portOutSpeedBps", "端口发送速率Bps", "", "", ""),
    status_interface_crcErrors("interface_crcErrors", "端口CRC校验错误数状态", "", "", ""),
    status_interface_txPower("interface_txPower", "端口tx传输功率状态", "", "", ""),
    status_interface_rxPower("interface_rxPower", "端口rx传输功率状态", "", "", ""),
    //光纤交换机状态
    status_interface_optical_status("interface_optical_status", "光交光口状态", "", "", ""),
    status_interface_optical_bandwith("bandwith", "光交光口边界宽度", "", "", ""),


    //ping状态
    event_ping("event:event_ping", "ping状态", "", "ping状态巡检", "183_1831_1832_1833_1834_201_42"),
    event_ping_no_group("event:event_ping:no_group", "单机ping事件", "设备Ping状态变化。", "单设备ping状态", ""),
    event_ping_group_all("event:event_ping:group_all", "组ping全部掉线事件", "设备组内所有设备Ping状态变化。", "组设备ping部分丢失状态", ""),
    event_ping_group_other("event:event_ping:group_other", "组ping部分掉线事件", "设备组内部分设备Ping状态变化。", "组设备ping全部丢失状态", ""),
    //ping事件对应的状态数据
    group_single_status_ping("group_single_ping", "组ping中单个设备状态", "", "", ""),
    group_all_status_ping("group_all_ping", "组ping中全部设备状态", "", "", ""),
    group_other_status_ping("group_other_ping", "组ping中其他设备状态", "", "", ""),
    status_ping("status_ping", "ping状态", "", "", ""),


    //系统端口占用事件
    event_system_port("event:event_system_port", "系统端口占用状态", "", "系统端口状态", "183_1831_1832_1833_1834"),
    event_system_udpPort_status("event:event_system_port:udpState", "系统UDP端口65535占用状态事件", "UDP端口65535占用状态,设备当前最大占用UDP端口%s。", "UDP端口65535占用状态", ""),
    event_system_tcpPort_status("event:event_system_port:tcpState", "系统tcp端口65535占用状态事件", "TCP端口65535占用状态,设备当前最大占用TCP端口%s。", "TCP端口65535占用状态", ""),
    //系统端口占用事件对应的状态数据
    status_tcp_portNumber("port_tcpNumber", "tcp端口占用个数", "", "", ""),
    status_tcp_max_port("port_maxTcpPort", "tcp最大端口占用", "", "", ""),
    status_udp_portNumber("port_udpNumber", "udp端口占用个数", "", "", ""),
    status_udp_max_port("port_maxUdpPort", "udp最大端口占用", "", "", ""),


    //进程状态事件
    event_process("event:event_process", "进程状态", "", "进程巡检", "183_1831_1832_1833_1834"),
    event_process_cpu("event:event_process:cpu", "进程CPU阈值事件", getPubThresholdMsg("进程:%s,进程号:%s,", "进程CPU使用率", "%%"), "进程CPU状态", ""),
    event_process_memory("event:event_process:memory", "进程内存阈值事件", getPubThresholdMsg("进程:%s,进程号:%s,", "进程内存使用率", "%%"), "进程内存状态", ""),
    event_process_other_down("event:event_process:other_down", "进程部分掉线事件", "集群进程%s，当前设备进程丢失,当前设备进程号:%s", "进程组部分丢失状态", ""),
    event_process_all_down("event:event_process:all_down", "进程全部掉线事件", "集群进程%s,组内设备进程全部丢失,当前设备进程号：%s", "进程组全部丢失状态", ""),
    event_process_status("event:event_process:status", "进程状态事件", "进程%s丢失,当前进程号：%s,", "单进程状态", ""),
    event_process_once("event:event_process:once", "双机单活进程事件", "进程 %s，已切换到 %s 上运行", "双机单活进程状态", ""),
    //进程事件对应的状态数据
    status_process("status:process", "进程信息", "", "", ""),
    status_process_cpu("process_cpu", "进程CPU阈值状态", "", "", ""),
    status_process_mem("process_mem", "进程内存阈值状态", "", "", ""),
    status_process_id("process_id", "进程id", "", "", ""),
    status_process_status("process_status", "进程状态", "", "", ""),


    //存储状态事件
    event_storage("event:event_storage", "存储状态", "", "存储巡检", "318"),
    event_storage_controller("event:event_storage:controller", "存储控制器事件", "存储控制器%s异常。", "存储控制器状态", ""),
    event_storage_capacity("event:event_storage:capacity", "存储容量事件", "存储容量超过设定值。", "存储容量状态", ""),
    event_storage_group("event:event_storage:group", "存储group事件", "存储组%s状态%s。", "存储group状态", ""),
    event_storage_Mdisk("event:event_storage:Mdisk", "存储Mdisk事件", "存储磁盘%s状态%s。", "存储磁盘状态", ""),
    event_storage_driver("event:event_storage:driver", "存储driver事件", "存储driver:%s状态%s。", "存储driver状态", ""),
    event_storage_logic_driver("event:event_storage:logicDriver", "存储logicDriver事件", "存储logicDriver:%s状态%s。", "存储逻辑分区状态", ""),
    event_storage_Vdisk("event:event_storage:Vdisk", "存储Vdisk事件", "存储Vdisk:%s状态%s。", "存储vdisk状态", ""),
    event_storage_link("event:event_storage:link", "存储连接事件", "存储连接状态%s。", "存储连接状态", ""),
    //存储事件对应的状态信息
    status_raidDS_controller_status("controller_status", "存储控制器状态", "", "", ""),
    status_raidDS_controller("status:raid_controller", "存储控制器", "", "", ""),
    status_raid_diskType("raid_diskType", "存储磁盘类型", "", "", ""),
    status_raid_totalCapacity("raid_totalCapacity", "磁盘阵列总容量", "", "", ""),
    status_raid_usedCapacity("raid_usedCapacity", "磁盘阵列已使用容量", "", "", ""),
    status_raid_freeCapacity("raid_freeCapacity", "磁盘阵列未使用容量", "", "", ""),
    status_raid_diskCapacityCount("raid_diskCapacityCount", "磁盘容量个数", "", "", ""),
    status_raid_mdisk("status:raid_mdisk", "存储物理磁盘", "", "", ""),
    status_raid_vdisk("status:raid_vdisk", "存储虚拟磁盘", "", "", ""),
    status_raid_group("status:raid_group", "存储磁盘组", "", "", ""),
    status_raid_drive("status:raid_driver", "存储分区", "", "", ""),


    status_raidDS_logicDrives("status:raid_logicDrives", "存储逻辑分区", "", "", ""),
    status_raidDS_Drives("status:raid_Drives", "存储物理分区", "", "", ""),

    status_raidDS_drive("status:raid_drives", "存储分区", "", "", ""),

    status_raid_name("raid_name", "存储磁盘名称", "", "", ""),

    status_raid_realid("raid_realid", "存储磁盘ID", "", "", ""),

    status_raid_status("raid_status", "存储磁盘状态", "", "", ""),
    status_raid_driver_status("raid_driver_status", "存储driver状态", "", "", ""),
    status_raidDS_mdisk_status("mdisk_status", "存储物理磁盘状态", "", "", ""),
    status_raidDS_drivers_status("drivers_status", "存储drivers状态", "", "", ""),
    status_raid_grpid("raid_grpid", "存储组ID", "", "", ""),
    status_raid_raidLevel("raid_raidLevel", "存储raid等级", "", "", ""),
    status_raid_capacity("raid_capacity", "存储容量", "", "", ""),
    status_raid_parentOrgName("raid_parentOrgName", "存储组织名称", "", "", ""),
    status_raid_parentOrgId("raid_parentOrgId", "存储组织ID", "", "", ""),
    status_raid_xindx("raid_xindx", "存储x位置", "", "", ""),
    status_raid_yindx("raid_yindx", "存储y位置", "", "", ""),
    status_raid_capacityStr("raid_capacityStr", "存储容量名称", "", "", ""),
    status_raid_usedCapacityStr("raid_usedCapacityStr", "存储使用容量名称", "", "", ""),
    status_raid_freeCapacityStr("raid_usedCapacityStr", "存储未使用容量名称", "", "", ""),


    //snnmp事件信息
    event_log("event:log", "事件信息", "系统收到SNMP消息：%s", "", ""),
    event_snmp("event:log:snmp", "snmp事件信息", "系统收到SNMP消息：%s", "", ""),
    event_syslog("event:log:syslog", "syslog事件信息", "系统收到syslog消息：%s", "", ""),
    event_aix_log("event:log:aixlog", "小机log事件信息", "", "", ""),
    event_raid_log("event:log:raidlog", "存储log事件信息", "", "", ""),

    event_log_jcca_0("event:log:levelZero", "Emergency事件信息", "系统收到 Emergency 级别消息：%s", "", ""),
    event_log_jcca_1("event:log:levelOne", "Alert事件信息", "系统收到 Alert 级别消息：%s", "", ""),
    event_log_jcca_2("event:log:levelTwo", "Critical事件信息", "系统收到 Critical 级别消息：%s", "", ""),
    event_log_jcca_3("event:log:levelThree", "ERROR事件信息", "系统收到 ERROR 级别消息：%s", "", ""),


    //网络安全事件
    event_xdhy("event:event_xdhy", "网络安全状态", "", "", ""),
    event_xdhy_state("event:event_xdhy:state", "网络安全事件", "业务告警：检测网络安全状态异常。", "", ""),
    //网络安全事件对应的状态数据
    status_xdhy("status:xdhy", "软件安全", "", "", ""),
    status_softSecureLinkState("softSecureLinkState", "软件安全连接状态", "", "", ""),

    //MQ事件信息
    event_MQ("event:mq", "MQ状态", "", "中间件巡检", "264"),
    event_mq_queue("event:mq:queue", "MQ队列事件信息", "MQ队列状态：%s", "中间件队列状态", ""),
    event_mq_state("event:mq:status", "MQ状态事件", "MQ状态：%s", "中间件状态", ""),
    status_mq_queue("status:queque", "队列状态", "", "", ""),
    status_mq("mqstate", "mq状态", "", "", ""),


    //动环事件 environment
    event_environment("event:event_environment", "动环状态", "检测到动环状态异常", "", ""),
    event_environment_notify("event:event_environment:notify", "动环通知", "接收到动环通知消息", "", ""),

    //对应的值
    NORMAL("NORMAL", "设定普通阈值", "", "", ""),
    SECTION("SECTION", "设定上下限阈值", "", "", ""),
    SECTION_ONE("SECTION_ONE", "设定一阶阈值", "", "", ""),
    SECTION_TWO("SECTION_TWO", "设定二阶阈值", "", "", ""),
    SECTION_THREE("SECTION_THREE", "设定三阶阈值", "", "", ""),
    CPU("CPU", "设定CPU使用率", "", "", ""),
    MEM("MEM", "设定内存使用率", "", "", ""),


    NORMAL_VAL("NORMAL_VAL", "采集值", "", "", ""),
    SECTION_VAL("SECTION_VAL", "上下限采集值", "", "", ""),
    SECTION_ONE_VAL("SECTION_ONE_VAL", "采集值", "", "", ""),
    SECTION_TWO_VAL("SECTION_TWO_VAL", "采集值", "", "", ""),
    SECTION_THREE_VAL("SECTION_THREE_VAL", "采集值", "", "", ""),
    CPU_VAL("CPU_VAL", "采集值", "", "", ""),
    MEM_VAL("MEM_VAL", "采集值", "", "", ""),


    STATUS("STATUS", "状态", "", "", ""),
    RUN_STATUS("RUN_STATUS", "运行状态", "", "", ""),
    LINK_STATUS("LINK_STATUS", "业务连接状态", "", "", ""),
    THRESHOLD_STATUS("THRESHOLD_STATUS", "业务软件容量", "", "", ""),
    //自律机
    ZLJ_STATUS("ZLJ_STATUS", "", "", "", ""),
    DB_CONN("DB_CONN", "数据库连接状态", "", "", ""),
    TCP_STATUS("TCP_STATUS", "TCP端口占用状态", "", "", ""),
    UDP_STATUS("UDP_STATUS", "UDP端口占用状态", "", "", ""),
    MASTER_CHANGE("MASTER_CHANGE", "主备切换", "", "", ""),
    ;


    private String code;
    private String name;
    private String descr;
    private String xuanjianName;
    private String assetMode;

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
        return title + "当前" + msg + ": %s" + unit + ",%s阈值范围：%s" + unit + "~%s" + unit + "。";
    }

    StatusInfoChangeTypeEnum(String code, String name, String descr, String xunjianName, String assetMode) {
        this.code = code;
        this.name = name;
        this.descr = descr;
        this.xuanjianName = xunjianName;
        this.assetMode = assetMode;
    }

    public static String getName(String code) {
        StatusInfoChangeTypeEnum[] values = StatusInfoChangeTypeEnum.values();
        for (StatusInfoChangeTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return code;
    }

    public static String getXunjianName(String code) {
        StatusInfoChangeTypeEnum[] values = StatusInfoChangeTypeEnum.values();
        for (StatusInfoChangeTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.xuanjianName;
            }
        }
        return code;
    }

    public static String getDescr(String code) {
        StatusInfoChangeTypeEnum[] values = StatusInfoChangeTypeEnum.values();
        for (StatusInfoChangeTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.descr;
            }
        }
        return code;
    }
}
