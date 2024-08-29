package com.jcca.web.graph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.biz.vo.AssetTargetVo;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.vo.TopoPortStatus;
import com.jcca.web.graph.vo.TopoVertexAlarmLevelVo;
import com.jcca.web.graph.vo.TopoVertexAlarmStatusVo;
import com.jcca.web.graph.vo.TopoVertexVo;
import com.jcca.web2.vo.BizTopoCenterVo;
import com.jcca.web2.vo.CabinetTopoDetailVo;
import com.jcca.web2.vo.RoomTopoVo;

import java.util.List;
import java.util.Map;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:34
 */
public interface TopoVertexService extends IService<TopoVertex> {


    /**
     * 获取设备拓扑节点位置
     *
     * @param nodeType
     * @param orgId
     * @return
     */
    List<TopoVertexVo> selectNodeByAsset(String nodeType, String orgId);


    /**
     *返回设备包括183的主机设备
     * */
    List<TopoVertexVo> selectNodeByAsset2(String nodeType, String orgId);

    /**
     * 名称查询节点
     *
     * @param nodeType
     * @param orgId
     * @param name
     * @return
     */
    List<TopoVertexVo> selectNodeByAssetName(String nodeType, String orgId, String name);

    /**
     * 获取调度台拓扑节点位置
     *
     * @param nodeType
     * @param orgId
     * @return
     */

    List<TopoVertexVo> selectPcTopoNodeByAsset(String nodeType, String orgId);


    /**
     * 获取网络设备连接硬件设备信息
     *
     * @param nodeType
     * @param
     * @return
     */

    List<TopoVertexVo> selectNetworkAssetTopoNodeByAsset(String nodeType, String assetId);

    /**
     * 名称查询
     *
     * @param nodeType
     * @param orgId
     * @param name
     * @return
     */
    List<TopoVertexVo> selectPcTopoNodeByAssetName(String nodeType, String orgId, String name);

    /**
     * 获取节点的告警状态
     *
     * @param nodeType
     * @param orgId
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByAsset(String nodeType, String orgId);

    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByAsset2(String nodeType, String orgId);

    List<TopoVertexAlarmStatusVo> topoVertexStatus(String nodeType, String orgId);

    List<TopoVertexAlarmStatusVo> topoNetWorkVertexStatus(String nodeType, String assetId);


    /**
     * 获取机柜拓扑节点位置
     *
     * @param orgId
     * @return
     */
    List<TopoVertexVo> selectNodeByCabnet(String nodeType, String orgId, String roomId);


    /**
     * V2版本获取中心机柜拓扑节点位置
     *
     * @param orgId
     * @return
     */
    List<RoomTopoVo> selectNodeByCenterCabinetV2(String orgId);


    /**
     * V2版本获取车站机柜拓扑节点位置
     *
     * @param orgId
     * @return
     */
    List<CabinetTopoDetailVo> selectNodeByStationCabinetV2(String orgId);

    /**
     * 获取机柜告警状态
     *
     * @param nodeType
     * @param orgId
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByCabnet(String nodeType, String orgId);


    /**
     * 获取调度台信息
     *
     * @param nodeType
     * @param orgId
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectPcTopoNodeAlarmLevelByAsset(String nodeType, String orgId);


    List<TopoVertexAlarmLevelVo> selectNetWorkTopoNodeAlarmLevelByAsset(String nodeType, String assetId, String orgId);

    /**
     * 删除TopoVertex
     *
     * @param nodeType
     * @param orgId
     * @return
     */
    Boolean deleteNodes(String nodeType, String orgId);

    Boolean deleteNetWorkAssetNodes(String nodeType, String assetId);

    /**
     * 保存TopoVertex
     *
     * @param list
     * @return
     */
    Boolean saveNodes(List<TopoVertex> list);

    /**
     * 查询网络拓扑端口状态
     *
     * @param paraMap
     * @return
     */
    List<TopoPortStatus> findPortStatus(Map<String, Object> paraMap);

    List<TopoPortStatus> findNetWorkPortStatus(String assetId);

    /**
     * 查询线路下的网络拓扑
     *
     * @param id 组织id
     * @return
     */
    List<TopoVertexVo> selectLineNetTopo(String id);

    /**
     * 查询线路下的网络拓扑及对应报警信息
     *
     * @param id
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectLineNetTopoAlarmLevel(String id);

    /**
     * 查询存在告警的端口节点信息
     *
     * @param orgId
     * @return
     */
    List<TopoPortStatus> findDownPort(String orgId);


    List<AssetTargetVo> queryTarget(String assetId);


    List<AssetTargetVo> queryPortIScorrect(AssetTargetVo assetTargetVo);

    List<TopoPortStatus> findNetWorkDownPortStatus(String assetId);

    /**
     * 查找拓扑图配置的设备连接信息
     *
     * @return
     */
    List<AssetLinkAssetVo> findMaAssetAndPort();

    /**
     * 查询中心业务拓扑图
     *
     * @param orgId         组织ID
     * @param serviceTypeId 业务类型ID
     * @return BizTopoCenterVo
     */
    List<BizTopoCenterVo> findTopoCenterQuery(String orgId, String serviceTypeId);
}
