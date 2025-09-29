package com.jcca.web.graph.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.biz.vo.AssetTargetVo;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.vo.TopoPortStatus;
import com.jcca.web.graph.vo.TopoVertexAlarmLevelVo;
import com.jcca.web.graph.vo.TopoVertexAlarmStatusVo;
import com.jcca.web.graph.vo.TopoVertexVo;
import com.jcca.web2.vo.BizTopoCenterVo;
import com.jcca.web2.vo.CabinetTopoVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:45
 */
public interface TopoVertexMapper extends BaseMapper<TopoVertex> {

    List<TopoVertexVo> selectNodeByAsset(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("assetModeList") List<Integer> assetModeList, @Param("name") String name);

    List<TopoVertexVo> selectNodeByAsset2(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("assetModeList") List<Integer> assetModeList, @Param("name") String name);

    List<TopoVertexVo> selectNodeByCabnet(@Param("nodeStyle") String nodeStyle, @Param("orgId") String orgId);

    List<TopoVertexVo> selectPcTopoNodeByAsset(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("mode1") Integer mode1, @Param("name") String name);

    List<TopoVertexVo> selectNetworkAssetTopoNodeByAsset(@Param("assetId") String assetId);

    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByAsset(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("mode1") Integer mode1, @Param("mode2") Integer mode2);

    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByAsset2(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("mode1") Integer mode1, @Param("mode2") Integer mode2);

    List<TopoVertexAlarmStatusVo> topoVertexStatus(@Param("nodeType") String nodeType, @Param("orgIds") List<String> orgIds);

    List<TopoVertexAlarmStatusVo> topoNetWorkVertexStatus(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    List<TopoVertexAlarmLevelVo> selectPcTopoNodeAlarmLevelByAsset(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("mode1") Integer mode1);

    List<TopoVertexAlarmLevelVo> selectNetWorkTopoNodeAlarmLevelByAsset(@Param("nodeType") String nodeType, @Param("assetId") String assetId, @Param("orgId") String orgId);

    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByCabnet(@Param("nodeType") String nodeType, @Param("orgId") String orgId);

    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByCabnet2(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("roomId") String roomId, @Param("showJcca") Integer showJcca);

    Boolean deleteTopoVertex(@Param("nodeType") String nodeType, @Param("orgId") String orgId);

    Boolean deleteNetWorkAssetTopoVertex(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    /**
     * 查询线下车站路由器
     *
     * @param reqID
     * @return
     */
    List<TopoVertexVo> selectLineRoutNetTopo(String reqID);

    /**
     * 查询线下车站交换机
     *
     * @param reqID
     * @return
     */
    List<TopoVertexVo> selectLineSwitchNetTopo(String reqID);

    /**
     * 查询线上中心核心网络设备
     *
     * @param reqID
     * @return
     */
    List<TopoVertexVo> selectLineCenterTopo(String reqID);


    /**
     * 前端线路TOPO加载-车站路由器
     *
     * @param reqID
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectLineRoutNetTopoAlarmLevel(String reqID);

    /**
     * 前端线路TOPO加载-车站交换机
     *
     * @param reqID
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectLineSwitchNetTopoAlarmLevel(String reqID);

    /**
     * 前端线路TOPO加载中心网络设备
     *
     * @param reqID
     * @return
     */
    List<TopoVertexAlarmLevelVo> selectLineCenterTopoAlarmLevel(String reqID);

    List<TopoPortStatus> findNetWorkPortStatus(@Param("assetId") String assetId);

    List<TopoPortStatus> findNetWorkDownPort(@Param("assetId") String assetId);

    List<AssetTargetVo> queryTarget(String assetId);

    List<AssetTargetVo> queryTargetAll(String assetId);


    List<AssetTargetVo> queryPortIScorrect(String local, String target, String localId, String targetId);

    List<TopoPortStatus> findPortStatus(@Param("params") Map<String, Object> paraMap);

    /**
     * 查询异常的端口
     *
     * @param orgId
     * @return
     */
    List<TopoPortStatus> findDownPort(@Param("orgId") String orgId, @Param("nodeType") String nodeType);

    List<AssetLinkAssetVo> findMaAssetAndPort();

    List<CabinetTopoVo> selectNodeByCenterCabinetV2(String orgId);

    List<CabinetTopoVo> selectAlarmByCenterCabinetV2(String orgId);


    List<CabinetTopoVo> selectCenterCabinetByOrgV2(String orgId);

    List<BizTopoCenterVo> findTopoCenterQuery(String orgId, String serviceTypeId);

    List<TopoVertexVo> selectCabinetNodeV2(@Param("roomId") String roomId);

    List<TopoVertexVo> selectWanTopoNodeByAsset(@Param("nodeType") String nodeType, @Param("ids") List<String> ids);


    List<TopoVertexAlarmLevelVo> selectAllTopoNodeAlarmLevelByAsset(String category, String orgId);

    List<TopoVertexVo> selectAllTopoNodeByAsset(String category, String orgId);

    List<TopoVertexAlarmLevelVo> selectAllTopoNodeByLine(String category, String orgId);

    List<TopoVertexVo> selectAllTopoNodeByLine2(String category, String orgId);
}
