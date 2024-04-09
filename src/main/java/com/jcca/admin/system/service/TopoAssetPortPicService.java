package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.TopoAssetPortPic;

import java.util.List;

/**
 * 面板信息
 * @author syt
 * @date 2021-07-15 18:14:39
 **/
public interface TopoAssetPortPicService extends IService<TopoAssetPortPic> {

    /**
     * 删除面板配置信息
     * @param assetId
     * @param pcbId
     * @return
     */
    Boolean deleteAssetPortPic(String assetId,String pcbId);

    /**
     * 查询面板配置信息
     * @param assetId
     * @param pcbId
     * @return
     */
    List<TopoAssetPortPic> queryAssetPortPic(String assetId,String pcbId);


}
