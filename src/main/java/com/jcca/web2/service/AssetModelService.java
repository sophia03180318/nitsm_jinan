package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.AssetModel;

import java.util.List;
import java.util.Map;

/**
 * @description: 资产型号
 * @author: sophia
 * @create: 2023/11/02 14:38
 **/

public interface AssetModelService extends IService<AssetModel> {

    Map<String, String> getModelMapByModeId(String modeId);

    /**
     * @description: 获取厂商型号列表
     * @author: HanHW
     * @date: 2023/12/4 11:18
     * @param: [manufacturerId]
     * @return: java.util.List<com.jcca.web2.entity.AssetModel>
     **/
    List<AssetModel> getByManufacturerId(String manufacturerId);

}