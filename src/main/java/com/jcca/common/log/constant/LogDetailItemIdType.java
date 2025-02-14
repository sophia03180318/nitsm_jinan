package com.jcca.common.log.constant;

/**
 * @author HanHW
 * @description 运维日志itemIdType类型
 * @className LogDetailItemIdType
 * @date 2024/4/10 13:33
 * @since 2.1.0.0
 */
public interface LogDetailItemIdType {

    String ASSET = "1"; // 资产
    String ALARM = "2"; // 告警
    String PROCESS = "3"; // 进程
    String DB = "4"; // 数据库
    String CONFIG = "5"; // 配置
    String DEFAULT_THRESHOLD = "6"; // 默认阈值
    String MQ = "7"; // MQ
}
