package com.jcca.web.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.common.dao.DeviceMapper;
import com.jcca.web.common.entity.Device;
import com.jcca.web.common.service.DeviceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:01
 **/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
    @Resource
    private DeviceMapper deviceMapper;
}