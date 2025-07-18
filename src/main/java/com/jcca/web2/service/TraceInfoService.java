package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.exception.ResultException;
import com.jcca.web2.entity.TraceInfo;


/**
 * 追踪管理
 */
public interface TraceInfoService extends IService<TraceInfo> {


    /**
     * 转为追踪
     * @param info
     * @throws ResultException
     */
    void alarmTransform(TraceInfo info) throws ResultException;

    /**
     * 处理追踪
     * @param info
     */
    void dispose(TraceInfo info);
}