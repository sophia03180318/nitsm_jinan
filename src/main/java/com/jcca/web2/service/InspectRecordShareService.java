package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.InspectRecordShare;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.vo.InspectShareVo;

import java.util.List;

/**
 * @author: hhw
 * @description: InspectRecordShareService 主要是用来
 * @date: 2025-07-17  13:26
 * @since: 2.1.8.0
 */
public interface InspectRecordShareService extends IService<InspectRecordShare> {

    /**
     * 根据用户名查询分享的记录
     *
     * @param username 用户名
     * @return 分享的记录列表
     */
    List<XunjianSchedule> findByViewer(String username);

    /**
     * 根据任务ID和用户名查询分享的记录
     *
     * @param jobId    任务ID
     * @param username 用户名
     * @return 分享的记录列表
     */
    List<InspectShareVo> findShareRecord(String jobId, String username);
}
