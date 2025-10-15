package com.jcca.web.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.common.entity.Device;
import com.jcca.web.common.entity.DhFlag;
import com.jcca.web.common.service.bean.DhNodeVo;

import java.util.List;

/**
 * @description: 设备
 * @author: sophia
 * @create: 2023/11/30 14:00
 **/
public interface DeviceService extends IService<Device> {

    /**
     * 判断设备是否有告警规则
     */
    DhFlag isDevice(String repoId, String deviceId);

     List<DhNodeVo> getDeviceTree();

     List<DhNodeVo> getDevices(String eventId);

    List<DhNodeVo> getProperty(String deviceId);
}