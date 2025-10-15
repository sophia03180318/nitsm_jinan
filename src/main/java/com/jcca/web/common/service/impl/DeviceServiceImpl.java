package com.jcca.web.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.common.dao.DeviceMapper;
import com.jcca.web.common.entity.Device;
import com.jcca.web.common.entity.DhFlag;
import com.jcca.web.common.service.DeviceService;
import com.jcca.web.common.service.bean.DhNodeVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
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

    @Override
    public DhFlag isDevice(String repoId, String deviceId) {
        return deviceMapper.isDevice(repoId, deviceId);
    }

    @Override
    public List<DhNodeVo> getDeviceTree() {
        return deviceMapper.getDeviceTree();

    }

    @Override
    public List<DhNodeVo> getDevices(String eventId) {
        return deviceMapper.getDevices(eventId);
    }


    @Override
    public List<DhNodeVo> getProperty(String deviceId) {
        return deviceMapper.getProperty(deviceId);
    }
}