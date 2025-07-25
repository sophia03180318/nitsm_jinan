package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.AssetManufacturer;

import java.util.List;
import java.util.Map;

/**
 * @description: 资产厂商
 * @author: sophia
 * @create: 2023/11/02 14:38
 **/

public interface AssetManufacturerService extends IService<AssetManufacturer> {

    Map<Long, String> getManufacturerMap();

    Long getMaxId();

    /**
     * 判定是不是指定的厂商
     * @param manufacturerList 自己指定的关键字列表
     * @param manufacturerId 待判定的厂商ID
     * @return
     */
    boolean isManufacturer(List<String> manufacturerList, Integer manufacturerId);
}