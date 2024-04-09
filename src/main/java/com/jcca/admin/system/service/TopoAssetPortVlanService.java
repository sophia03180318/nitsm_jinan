package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.TopoAssetPortVlan;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-08-19 18:14:39
 **/
public interface TopoAssetPortVlanService extends IService<TopoAssetPortVlan> {

    /**
     * 设备vlan配置
     * @param assetId
     * @param pcbId
     * @return
     */
    Boolean deleteAssetPortVlan(String assetId,String pcbId);

    /**
     * 查询配置信息
     * @param assetId
     * @return
     */
    List<TopoAssetPortVlan> queryAssetPortVlan(String assetId,String pcbId);


}
