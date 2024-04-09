package com.jcca.web.graph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.graph.entity.TopoAssetGroup;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-09-01 19:27:08
 **/
public interface TopoAssetGroupService extends IService<TopoAssetGroup> {

    Boolean deleteAssetGroup(String orgId, String nodeType);

    Boolean deleteNetWorkAssetGroup(String assetId, String nodeType);

    List<TopoAssetGroup> queryAssetGroup(String orgId, String nodeType);

    List<TopoAssetGroup> queryNetWorkAssetGroup(String assetId, String nodeType);

}
