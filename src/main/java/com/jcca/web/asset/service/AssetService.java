package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.controller.bean.AssetCollectReq;
import com.jcca.web.asset.controller.bean.AssetNetReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetTelnet;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.asset.utils.bean.CabinetUsed;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.asset.vo.AssetPingVo;
import com.jcca.web.asset.vo.AssetTelnetVo;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.vo.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-04-20 15:37:48
 **/
public interface AssetService extends IService<Asset> {


    /**
     * 资产类型 厂商统计
     *
     * @return
     */
    List<StatisticsAlarmVo> getManufacturerAsset();

    /**
     * 添加资产/api/asset/list
     *
     * @param addReq
     * @throws Exception
     */
    void createAsset(Asset addReq) throws AddAssetException;

    /**
     * 修改资产
     *
     * @param addReq
     * @throws Exception
     */
    void updateAsset(Asset addReq) throws AddAssetException;

    /**
     * 保存导入的资产
     *
     * @param assetVo
     * @throws Exception
     */
    void importSave(Asset assetVo) throws Exception;

    /**
     * 通过ip获取资产
     *
     * @param ip
     * @return
     */
    Asset findOneByIp(String ip);

    /**
     * 依次扫描IP、IP2、管理口IP
     *
     * @param ip
     * @return
     */
    Asset getOneByAllIp(String ip);

    /**
     * 查询资产信息
     *
     * @param ip
     * @return
     */
    AssetMsgVo findMsgByIp(String ip);

    /**
     * 查询资产信息
     *
     * @param id
     * @return
     */
    AssetMsgVo findMsgById(String id);

    AssetMsgVo getAssetMsgVo(Asset asset);


    /**
     * 删除资产
     *
     * @param ids
     * @return
     * @throws Exception
     */
    int deleteAsset(String ids) throws Exception;

    /**
     * 根据资产ID获取资产的归属 机柜 机房 组织
     *
     * @param id
     * @return
     */
    AssetBelong findAssetBelongById(String id);

    /**
     * 按类型统计设备
     *
     * @return
     */
    List<StatisticsAlarmVo> getModeAsset();

    /**
     * 按组织统计设备数量
     *
     * @return
     */
    List<StatisticsAlarmVo> getOrgAsset();

    /**
     * 获取满足大修提醒的所有设备
     *
     * @return
     */
    List<Asset> getOverhaulList();

    /**
     * 机柜中可用U位
     *
     * @return
     */
    List<CabinetUsed> freePosition(String cabinetId);

    /**
     * 监控保存资产中监控资产校验
     *
     * @return
     */
    Object verifyIp(AssetCollectReq collectReq) throws AddAssetException;

    /**
     * 资产IP地址ping测试 syt
     */
    ResultVo<?> assetPing(String id);

    /**
     * telnet测试主机端口是否启用 syt
     */
    ResultVo<?> assetTelnet(AssetNetReq telnetReq);

    /**
     * 查询资产
     *
     * @param assetCode
     * @return
     */
    List<Asset> listByAssetCode(String assetCode);

    List<Asset> getCenterNetworklList();

    /**
     * 判断资产是否是车站资产
     *
     * @param assetId
     * @return
     */
    Boolean isStationAsset(String assetId);

    /**
     * 根据资产code获取资产
     *
     * @param assetCode
     * @return
     */
    List<Asset> findGroupAssetByCode(String assetCode);

    List<Asset> ListByOrgIds(List<String> orgIds);

    /**
     * 查询平均健康值
     *
     * @return
     */
    Double queryHealth();

    /**
     * 查询指定系统类型的资产状态正常的资产
     *
     * @param asList
     * @return
     */
    List<Asset> listByCollectionType(List<Integer> asList);

    /**
     * 查询服务器最后一次采集时间
     *
     * @return
     */
    Date queryServerLastTimeDate(String assetId);

    /**
     * 查询网络设备最后一次采集时间
     *
     * @param assetId
     * @return
     */
    Date queryNetLastTimeDate(String assetId);

    /**
     * 查询所有JCCA厂商的设备ID
     *
     * @return
     */
    List<String> listJccaId();

    String getUIndex(String assetId);

    /**
     * 更新监控状态
     */
    void updateMonitorStatus(Asset asset, Integer status);

    /**
     * @description: 被监控设备总数
     * @author: HanHW
     * @date: 2023/10/25 15:20
     * @param: []
     * @return: java.lang.Integer
     **/
    Integer getAssetCountV2();

    /**
     * @description: 按组织类型统计被监控设备数量
     * @author: HanHW
     * @date: 2023/10/25 15:48
     * @param: [OrgTypeConst]
     * @return: java.lang.Integer
     **/
    Integer getAssetCountByOrgTypeV2(Byte orgType);


    /**
     * 查询设备在机柜中的详情信息
     *
     * @param assetId
     * @return
     */
    CabinetAssetInfoVo findCabinetAssetInfoV2(String assetId);

    /**
     * 查询用户所管理的下属设备ID列表
     *
     * @param username
     * @param watch    0查询不监控的  1 查询监控的  1查询所有的
     * @return
     */
    List<String> listIdByUserNameV2(String username, Integer watch);

    /**
     * @description: 查询所有监控设备
     * @author: HanHW
     * @date: 2023/11/16 13:20
     * @param: []
     * @return: java.util.List<com.jcca.web.asset.entity.Asset>
     **/
    List<Asset> listAllV2();

    /**
     * @description: 添加或更新资产
     * @author: HanHW
     * @date: 2023/12/5 17:31
     * @param: [asset]
     * @return: void
     **/
    void saveAssetV2(Asset asset) throws Exception;

    /**
     * @description: 资产变动通知其它应用
     * @author: HanHW
     * @date: 2023/12/6 9:21
     * @param: [asset, state]
     * @return: void
     * <p>
     * OutConst 0新增，1删除，2修改
     **/
    void notifySubjectV2(Asset asset, Integer state) throws AddAssetException;

    /**
     * @description: 查询全部大修改资产
     * @author: HanHW
     * @date: 2023/12/7 18:33
     * @param: []
     * @return: java.util.Map<java.lang.Object, java.lang.Object>
     **/
    List<Asset> getOverhaulListV2();

    /**
     * 查询设备的基础信息
     *
     * @param assetId
     * @return
     * @author: Lvyp
     */
    AssetInfoBaseVo queryBaseInfoV2(String assetId);

    /**
     * 查询设备的时间线
     *
     * @param assetId
     * @return
     */
    List<AssetLifeLineVo> getLifeLineV2(String assetId);

    /**
     * 查询组织下所有设备
     *
     * @param orgId 组织ID
     * @return
     */
    List<Asset> findListByOrgIdV2(String orgId);

    /**
     * 按类型查询所有设备
     *
     * @param assetMode 设备类型
     * @return
     */
    List<Asset> findListByAssetModeV2(Integer assetMode);

    /**
     * 资产按类型统计
     *
     * @param map
     * @return AssetStatisticsVo
     */
    List<AssetStatisticsVo> countModeV2(Map<String, Object> map);

    /**
     * 资产按型号统计
     *
     * @param map
     * @return AssetStatisticsVo
     */
    List<AssetStatisticsVo> countModelV2(Map<String, Object> map);

    /**
     * ping测试
     *
     * @param ids
     * @return
     */
    List<AssetPingVo> assetPingV2(List<String> ids);

    /**
     * telnet测试
     *
     * @param req
     * @return
     */
    List<AssetTelnetVo> assetTelnetV2(List<AssetTelnet> req);

    /**
     * 查询性能数据V2
     *
     * @param assetId
     * @return
     * @author Lvyp
     */
    AssetPerformanceDataVo queryPerformanceDataV2(String assetId);

    /**
     * 查询服务器相关组件信息
     *
     * @param assetId
     * @return
     */
    ServerModuleVo serverModuleQueryV2(String assetId);

    /**
     * 阈值管理 按条件查询资产ID
     *
     * @param manage
     * @return
     */
    List<AssetBaseInfoVo> getAssetIdListV2(ThresholdManage manage);

    /**
     * 查询设备基础需要展示的列表
     * @param assetId
     * @return
     */
    List<AssetStatusItmVo>  queryBaseStatusItmV2(String assetId);

    /**
     * 查询基础信息列表
     * @param assetId
     * @param code
     * @return
     */
    AssetStatusDetailVo getAssetStatusDetailV2(String assetId, String code);

    /**
     * 存储动环设备
     * */
    void saveDevice(List<Asset> assets);
}
