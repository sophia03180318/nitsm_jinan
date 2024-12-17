package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.vo.AssetHealthDegreeModuleConf;
import com.jcca.web.asset.controller.bean.AssetQueryReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.asset.vo.AssetExportVo;
import com.jcca.web.asset.vo.LinkAssetExportVo;
import com.jcca.web.collect.controller.route.bean.ExportManualReq;
import com.jcca.web.graph.vo.GraphStatusVo;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.vo.AssetBaseInfoVo;
import com.jcca.web2.vo.AssetInfoBaseVo;
import com.jcca.web2.vo.AssetStatisticsVo;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-04-20 15:37:48
 **/
public interface AssetMapper extends BaseMapper<Asset> {


    /**
     * 厂商设备数量统计
     *
     * @param orgIds
     * @return
     */
    List<StatisticsAlarmVo> getManufacturerAsset(@Param("orgIds") List<String> orgIds);

    /**
     * 通过ip查询
     *
     * @param ip
     * @return
     */
    @Select("select * from (select * from asset a where (a.ip = #{ip} or a.ip2 = #{ip} or a.ipmi_ip = #{ip}) and a.is_del = 1 and a.ip is not null) where rownum=1")
    Asset selectByIp(@Param("ip") String ip);

    /**
     * 获取组织内网络设备状态
     *
     * @param orgId
     * @return
     */
    List<GraphStatusVo> findStatusByOrgId(String orgId);

    /**
     * 根据资产ID获取资产的归属  机柜 机房 组织
     *
     * @param id
     * @return
     */
    AssetBelong findAssetBelongById(String id);

    /**
     * 按条件导出设备信息
     *
     * @param assetReq
     * @return
     */
    List<AssetExportVo> findExportAsset(AssetQueryReq assetReq);

    /**
     * 按设备类型统计
     *
     * @param orgIds
     * @return
     */
    List<StatisticsAlarmVo> getModeAsset(@Param("orgIds") List<String> orgIds);

    /**
     * 按组织统计设备数量
     *
     * @return
     */
    List<StatisticsAlarmVo> getOrgAsset(@Param("orgIds") List<String> orgIds);

    /**
     * 获取满足大修提醒的所有设备
     *
     * @return
     */
    List<Asset> getOverhaulList(@Param("orgIds") List<String> orgIds);

    List<Asset> getCenterNetworklList();

    /**
     * 根据资产code获取资产
     */
    List<Asset> findGroupAssetByCode(@Param("assetCode") String assetCode);

    /**
     * 根据组织ID获取资产
     */
    List<Asset> findAssetByOrgId(String orgId);

    /**
     * 根据组织ID和监控状态获取资产
     */
    @Select("SELECT * FROM ASSET where ORG_ID=#{orgId} AND Watch=#{Watch} AND IS_DEL=1")
    List<Asset> findAssetByOrgIdAndWatch(String orgId, int Watch);


    @Select("SELECT A.*,O.title AS orgName FROM ASSET A JOIN SYS_ORG O ON A.ORG_ID=O.ID WHERE A.IS_DEL=1")
    List<Asset> getAllAsset();


    /**
     * 计算健康度
     *
     * @param conf
     */
    void calculateHealth(AssetHealthDegreeModuleConf conf);

    /**
     * 查询所有系统当前的健康状态
     *
     * @return
     */
    Double queryHealth();

    @Select("SELECT ID FROM ASSET where ASSET_SUPPLIER = 'JCCA' AND IS_DEL=1 ")
    List<String> selectJccaAsset();


    @Select("SELECT CONCAT(CONCAT(START_POSITION,'_'),END_POSITION) FROM ASSET_ATTACH WHERE ASSET_ID=#{assetId}")
    String getUIndex(String assetId);

    @Update("update asset set MONITOR=#{status} where id=#{id} ")
    void updateMonitorStatus(String id, Integer status);

    /**
     * 查询机柜内设备详情
     *
     * @param assetId
     * @return
     */
    CabinetAssetInfoVo selectCabinetAssetInfoV2(String assetId);


    /**
     * 查询设备基础信息
     *
     * @param assetId
     * @return
     */
    AssetInfoBaseVo queryBaseInfoV2(String assetId);

    @Select("SELECT * FROM ASSET WHERE IS_DEL = 1 AND WATCH = 1 AND ASSET_MODE = #{assetMode}")
    List<Asset> listByAssetMode(Integer assetMode);

    @Select("SELECT * FROM ASSET WHERE IS_DEL = 1 AND WATCH = 1")
    List<Asset> listAllV2();

    /**
     * 查询用户管理的设备ID
     *
     * @param username
     * @param watch
     * @return
     */
    List<String> listIdByUserNameV2(@Param("username") String username, @Param("watch") Integer watch);

    @Select("select * from asset where is_del = 1 and org_id = #{orgId}")
    List<Asset> findListByOrgId(String orgId);

    @Select("select * from asset where is_del = 1 and asset_mode = #{assetMode}")
    List<Asset> findListByAssetModeV2(Integer assetMode);

    List<AssetStatisticsVo> countModeV2(@Param("map") Map<String, Object> map);

    List<AssetStatisticsVo> countModelV2(@Param("map") Map<String, Object> map);

    List<AssetBaseInfoVo> getAssetIdListV2(ThresholdManage manage);

    List<LinkAssetExportVo> exportManualList(ExportManualReq req);
}
