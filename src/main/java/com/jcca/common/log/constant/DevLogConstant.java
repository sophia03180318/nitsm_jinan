package com.jcca.common.log.constant;

/**
 * @author HanHW
 * @description 运维日志记录分类
 * @className DevLogConstant
 * @date 2024/4/1 9:15
 * @since 2.1.0.0
 */
public interface DevLogConstant {

    // 告警相关
    String ALARM_CONFIRM_SINGLE = "ALARM_CONFIRM_SINGLE"; // 单条确认
    String ALARM_CONFIRM_CONDITION = "ALARM_CONFIRM_CONDITION"; // 条件确认
    String ALARM_CONFIRM_BATCH = "ALARM_CONFIRM_BATCH"; // 批量确认
    String ALARM_DEL_SINGLE = "ALARM_DEL_SINGLE"; // 单条删除
    String ALARM_CLEAR_CONDITION = "ALARM_CLEAR_CONDITION"; // 批量清除

    String ALARM_ROUND_TIME = "ALARM_ROUND_TIME"; // 轮询次数

    // 阈值相关
    String THRESHOLD_DEFAULT = "THRESHOLD_DEFAULT"; // 默认阈值
    String THRESHOLD_SINGLE = "THRESHOLD_SINGLE"; // 单个阈值

    String THRESHOLD_TABLESPACE = "THRESHOLD_TABLESPACE"; // 表空间阈值
    String THRESHOLD_MQ = "THRESHOLD_MQ"; // 队列深度阈值
    String THRESHOLD_PROCESS = "THRESHOLD_PROCESS"; // 进程阈值

    // 进程配置
    String PROCESS_CONFIG = "PROCESS_CONFIG"; // 进程配置
    String PROCESS_DEL = "PROCESS_DEL"; // 进程删除
    String PROCESS_MODE = "PROCESS_MODE"; // 进程模式

    // 资产相关
    String ASSET_MODIFY = "ASSET_MODIFY"; // 资产新增或修改
    String ASSET_DEL = "ASSET_DEL"; // 资产删除

    // 告警黑名单相关
    String ALARM_BLACKLIST_ADD = "ALARM_BLACKLIST_ADD";
    String ALARM_BLACKLIST_DEL = "ALARM_BLACKLIST_DEL";
    String ALARM_BLACKLIST_UPDATE = "ALARM_BLACKLIST_UPDATE";
}
