package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.AssetMode;

import java.util.List;
import java.util.Map;

/**
 * @description: 资产类型
 * @author: sophia
 * @create: 2023/11/02 14:38
 **/

public interface AssetModeService extends IService<AssetMode> {
    /**
     * 获取型号键值对 <id,name>
     */

    Map<Integer, String> getModeMap();

    AssetMode getByCode(Integer code);

    /**
     * 由厂商找资产类型
     *
     * @param manufacturerId 厂商ID
     * @return AssetMode
     */
    List<AssetMode> getManufacturerMode(String manufacturerId);

    /**
     * 由型号找资产类型
     *
     * @param modelId 型号ID
     * @return AssetMode
     */
    AssetMode getModelMode(String modelId);
}