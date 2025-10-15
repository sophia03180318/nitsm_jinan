package com.jcca.web.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.common.entity.Device;
import com.jcca.web.common.entity.DhFlag;
import com.jcca.web.common.service.bean.DhNodeVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/30 14:02
 **/
public interface DeviceMapper extends BaseMapper<Device> {

    DhFlag isDevice(@Param("eventId") String eventId, @Param("deviceId") String deviceId);

    List<DhNodeVo> getDeviceTree();

    List<DhNodeVo> getDevices(@Param("eventId") String eventId);

    List<DhNodeVo> getProperty(String deviceId);

    void savePub(String flag,String eventId);

}