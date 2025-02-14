package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;

/**
 * @author HanHW
 * @description 运维日志记录
 * @className DevLogService
 * @date 2024/4/1 9:46
 * @since 2.1.0.0
 */
public interface DevLogService {

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    String getDevType();

    /**
     * 设置运维日志内容
     *
     * @param actionLog 日志
     * @param args      参数
     */
    void setDevLog(SysActionLog actionLog, Object[] args);
}
