package com.jcca.web.graph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.graph.entity.TopoAssetMark;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-09-01 19:27:08
 **/
public interface TopoAssetMarkService extends IService<TopoAssetMark> {

    Boolean deleteAssetMark(String orgId, String nodeType);

    Boolean deleteNetWorkAssetMark(String assetId, String nodeType);

    List<TopoAssetMark> queryAssetMark(String orgId, String nodeType);

    List<TopoAssetMark> queryNetWorkAssetMark(String assetId, String nodeType);
}
