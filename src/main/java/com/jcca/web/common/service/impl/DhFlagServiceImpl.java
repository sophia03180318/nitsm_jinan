package com.jcca.web.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.common.dao.DeviceMapper;
import com.jcca.web.common.dao.DhFlagMapper;
import com.jcca.web.common.entity.DhFlag;
import com.jcca.web.common.service.DhFlagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: sophia
 * @create: 2025/10/13 10:26
 **/
@Service
public class DhFlagServiceImpl extends ServiceImpl<DhFlagMapper, DhFlag> implements DhFlagService {
    @Resource
    private DeviceMapper deviceMapper;

    @Override
    public void savePub(String flag,String eventId) {
        deviceMapper.savePub(flag,eventId);
    }
}