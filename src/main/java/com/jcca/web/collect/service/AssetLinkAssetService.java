package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.vo.LinkAssetExportVo;
import com.jcca.web.collect.controller.route.bean.ExportManualReq;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.common.service.bean.ThreeDLinkReq;

import java.util.List;

/**
 * @author hanhw
 * @description 对端设备连接信息
 * @className AssetLinkAssetService
 * @date 2023/4/27 9:38
 * @since 2.0.3.0
 */
public interface AssetLinkAssetService extends IService<AssetLinkAsset> {


    /**
     * 保存对端设备信息
     * 1拓扑图配置的，2自动发现的
     */
    void saveLinkAsset(String atOrMa);

    /**
     * 网络设备查找对端的主机或网络设备信息
     * @param assetId
     * @param portIndex
     * @return
     */
    AssetLinkAsset findLinkAssetByAsset(String assetId, String portIndex);

    /**
     * 主机设备查找对端的网络设备
     * @param assetId
     * @param linkAssetIp
     * @return
     */
    AssetLinkAsset findAssetByLinkAsset(String assetId, String linkAssetIp);

    /**
     * 格式化文本
     * @param assetId
     * @param atIp
     * @return
     */
    String formatMsg(String assetId, String atIp);

    /**
     * 保存当前设备对端信息
     *
     * @param assetId
     */
    void saveThisAsset(String assetId);


    /**
     * 查询3D机房的所有链路信息
     */
    List<ThreeDLinkReq> getThreeDLink(String roomId1, String roomId2);

    List<LinkAssetExportVo> exportManualList(ExportManualReq req);
}
