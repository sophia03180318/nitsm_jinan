package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.AssetManufacturer;

import java.util.Map;

/**
 * @description: 资产厂商
 * @author: sophia
 * @create: 2023/11/02 14:38
 **/

public interface AssetManufacturerService extends IService<AssetManufacturer> {

    Map<Long, String> getManufacturerMap();

    Long getMaxId();
}