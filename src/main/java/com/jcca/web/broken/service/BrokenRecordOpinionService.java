package com.jcca.web.broken.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.broken.entity.BrokenRecordOpinion;

/**
 * @ClassName BrokenRecordOpinionService
 * @Description 故障记录处理意见
 * @Date 2020/6/28 17:44
 * @Author hanwone
 */
public interface BrokenRecordOpinionService extends IService<BrokenRecordOpinion> {

    /**
     * 获取告警的处理方案
     *
     * @param id
     * @return
     */
    String getMsgByAlarmInfoId(String id);
}
