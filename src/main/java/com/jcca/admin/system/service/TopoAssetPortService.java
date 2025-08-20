package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.web.graph.vo.TopoPortInfoVo;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-08-18 13:53:51
 **/
public interface TopoAssetPortService extends IService<TopoAssetPort> {

    /**
     * 查询指定资产物理端口列表(用这个)
     * <p>
     * sophia
     */
    List<AssetPortVo> selectPortListByAsset(String assetId);

    /**
     * 查询端口配置
     *
     * @param assetId
     * @param pcbId
     * @return
     */
    List<AssetPortVo> selectAssetPort(String assetId, String pcbId);

    List<AssetPortVo> selectAssetPortQuery(String assetId);

    //查询所有端口（包括虚拟口子）
    List<AssetPortVo> selectAssetAllPort(String assetId);

    List<AssetPortVo> selectAssetPortVlan(String assetId);

    /**
     * 删除设备端口配置
     *
     * @param assetId
     * @param pcbId
     * @return
     */
    Boolean deleteAssetPort(String assetId, String pcbId);

    TopoPortInfoVo selectPortIndex(String assetId, String portName);

    void updatePortStatus(String assetId, String portIndex, Integer status);

    /**
     * 查询端口下属的子虚拟端口
     *
     * @param portIndex
     * @return
     */
    List<AssetPortVo> querySonList(String portIndex, String assetId);

    /**
     * 查询Sonet类型端口下的子端口
     *
     * @param portIndex
     * @param assetId
     * @return
     */
    List<AssetPortVo> querySonetSonList(String portIndex, String assetId);

    /**
     * 查询资产有无配置拓扑图端口
     *
     * @param assetId   资产ID
     * @param portIndex 端口索引
     * @return
     */
    TopoAssetPort findAssetPort(String assetId, String portIndex);
}
