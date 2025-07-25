package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.InspectRecordShareMapper;
import com.jcca.web2.entity.InspectRecordShare;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.InspectRecordShareService;
import com.jcca.web2.vo.InspectShareVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: hhw
 * @description: InspectRecordShareImpl 主要是用来
 * @date: 2025-07-17  13:27
 * @since: 2.1.8.0
 */
@Service
public class InspectRecordShareImpl extends ServiceImpl<InspectRecordShareMapper, InspectRecordShare> implements InspectRecordShareService {

    @Resource
    private InspectRecordShareMapper inspectRecordShareMapper;

    /**
     * 根据用户名查询分享的记录
     *
     * @param username 用户名
     * @return 分享的记录列表
     */
    @Override
    public List<XunjianSchedule> findByViewer(String username) {
        return inspectRecordShareMapper.findByViewer(username);
    }

    /**
     * 根据任务ID和用户名查询分享的记录
     *
     * @param jobId    任务ID
     * @param username 用户名
     * @return 分享的记录列表
     */
    @Override
    public List<InspectShareVo> findShareRecord(String jobId, String username) {
        return inspectRecordShareMapper.findShareRecord(jobId, username);
    }
}
