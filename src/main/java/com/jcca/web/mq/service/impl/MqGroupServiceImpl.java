package com.jcca.web.mq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.mq.dao.MqGroupMapper;
import com.jcca.web.mq.entity.MqGroup;
import com.jcca.web.mq.service.MqGroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:49 2021/11/25
 * @ Description:
 */
@Service
public class MqGroupServiceImpl extends ServiceImpl<MqGroupMapper, MqGroup> implements MqGroupService {
    @Resource
    MqGroupMapper mqGroupMapper;

    @Override
    public List<MqGroup> selectByName(String name, String connectId) {//是否有重名
        return mqGroupMapper.selectByName(name, connectId);
    }

    @Override
    public List<MqGroup> selectByConnectId(String connectId) {
        return  mqGroupMapper.selectByConnectId(connectId);
    }


}
