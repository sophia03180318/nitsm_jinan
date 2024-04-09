package com.jcca.web.graph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.graph.entity.TopoPoints;

import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:33
 */
public interface TopoPointsService extends IService<TopoPoints> {
    /**
     * 获取折线点
     *
     * @param nodeStyle
     * @param orgId
     * @return
     */
    List<TopoPoints> getTopoPoints(String nodeType, String orgId);


    /**
     * 查询网络设备
     *
     * @param nodeType
     * @param assetId
     * @return
     */
    List<TopoPoints> queryNetWorkPoints(String nodeType, String assetId);

    /**
     * 删除折线点
     *
     * @param nodeStyle
     * @param orgId
     * @return
     */
    Boolean deleteTopoPoints(String nodeStyle, String orgId);

    Boolean deleteNetWorkAssetTopoPoints(String nodeStyle, String assetId);

    /**
     * 保存折线点
     *
     * @param list
     * @return
     */
    Boolean saveTopoPoints(List<TopoPoints> list);
}
