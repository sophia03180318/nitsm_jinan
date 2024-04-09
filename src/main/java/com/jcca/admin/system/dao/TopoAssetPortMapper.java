package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.vo.AssetPortVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-08-18 13:53:51
 **/
public interface TopoAssetPortMapper extends BaseMapper<TopoAssetPort> {

    /**
     * 查询设备端口配置
     * @param assetId
     * @return
     */
    List<AssetPortVo> selectAssetPort(@Param("assetId") String assetId,@Param("pcbId") String pcbId,@Param("portLinkType")String portLinkType);

    /**
     * 查询设备端口配置
     * @param assetId
     * @param pcbId
     * @return
     */
    List<AssetPortVo> selectAssetPort2(@Param("assetId") String assetId,@Param("pcbId") String pcbId);

    List<AssetPortVo> selectAssetPortVlan(String assetId);

    List<AssetPortVo> selectAssetPortQuery(String assetId);

    List<AssetPortVo> selectAssetAllPort(String assetId);

    List<AssetPortVo> selectSonList(String portIndex, String assetId);

    List<AssetPortVo> querySonetSonList(String portIndex, String assetId);

    List<AssetPortVo> selectAssetPortQuery2(String assetId);

    List<AssetPortVo> selectPort2(String assetId);

    List<AssetPortVo> selectPort(String assetId);

    @Select("select * from topo_asset_port where asset_id = #{assetId} and port_index = #{portIndex}")
    List<TopoAssetPort> findAssetPort(String assetId, String portIndex);
}
