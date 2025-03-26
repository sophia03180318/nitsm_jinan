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

import java.util.Arrays;
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

    List<TopoVertexAlarmStatusVo> topoCoreVertexStatus(@Param("nodeType") String nodeType, @Param("assetIds") List<String> assetIds, @Param("orgId") String orgId);

    List<TopoVertexAlarmStatusVo> topoNetWorkVertexStatus(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    List<TopoVertexAlarmLevelVo> selectPcTopoNodeAlarmLevelByAsset(@Param("nodeType") String nodeType, @Param("orgId") String orgId, @Param("mode1") Integer mode1);

    List<TopoVertexAlarmLevelVo> selectNetWorkTopoNodeAlarmLevelByAsset(@Param("nodeType") String nodeType, @Param("assetId") String assetId, @Param("orgId") String orgId);

    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByCabnet(@Param("nodeType") String nodeType, @Param("orgId") String orgId);
    List<TopoVertexAlarmLevelVo> selectNodeAlarmLevelByCabnet2(@Param("nodeType") String nodeType, @Param("orgId") String orgId,@Param("roomId") String roomId);

    @Delete("delete from TOPO_VERTEX where NODE_Type=#{nodeType} and ORG_ID=#{orgId}")
    Boolean deleteTopoVertex(@Param("nodeType") String nodeType, @Param("orgId") String orgId);

    @Delete("delete from TOPO_VERTEX where NODE_Type=#{nodeType} and CORE_ASSET_ID=#{assetId}")
    Boolean deleteNetWorkAssetTopoVertex(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    /**
     * 查询线下车站路由器
     *
     * @param reqID
     * @return
     */
    @Select("SELECT V.*, T.NAME AS ASSETNAME, T.ID AS ASSETID, T.ORG_ID AS ASSETORGID, ASSET_MODE AS ASSETMODE FROM ASSET T LEFT JOIN TOPO_VERTEX V ON  T.ID = V.ASSET_ID AND #{reqID}=V.ORG_ID WHERE T.ORG_ID IN (SELECT ID FROM SYS_ORG WHERE PID =#{reqID}) AND T.ASSET_MODE =42 AND T.SHOW_TOPO=1 AND T.IS_DEL=1")
    List<TopoVertexVo> selectLineRoutNetTopo(String reqID);

    /**
     * 查询线下车站交换机
     *
     * @param reqID
     * @return
     */
    @Select("SELECT V.*, T.NAME AS ASSETNAME, T.ID AS ASSETID, T.ORG_ID AS ASSETORGID, ASSET_MODE AS ASSETMODE FROM ASSET T LEFT JOIN TOPO_VERTEX V ON  T.ID = V.ASSET_ID AND #{reqID}=V.ORG_ID WHERE T.ORG_ID IN (SELECT ID FROM SYS_ORG WHERE PID =#{reqID}) AND T.ASSET_MODE =201 AND T.SHOW_TOPO=1 AND T.IS_DEL=1")
    List<TopoVertexVo> selectLineSwitchNetTopo(String reqID);

    /**
     * 查询线上中心核心网络设备
     *
     * @param reqID
     * @return
     */
    @Select("SELECT V.*,T.NAME AS ASSETNAME, T.ID AS ASSETID, T.ORG_ID AS ASSETORGID, ASSET_MODE AS ASSETMODE from (SELECT * FROM ASSET WHERE ORG_ID =(SELECT PID FROM SYS_ORG WHERE ID=#{reqID}) AND ASSET_MODE IN(42,201)  AND SHOW_CORE='SHOW_TOPO_@_SHOW' AND SHOW_TOPO=1 AND IS_DEL=1) t LEFT JOIN TOPO_VERTEX V ON  T.ID = V.ASSET_ID AND #{reqID}=V.ORG_ID ORDER BY V.NAME")
    List<TopoVertexVo> selectLineCenterTopo(String reqID);


    /**
     * 前端线路TOPO加载-车站路由器
     *
     * @param reqID
     * @return
     */
    @Select("select a.aLevel AS alarmLevel,v.*, t.name as assetName, t.id as assetId,t.SHOW_CORE as showCore, t.org_id as assetOrgId, t.asset_image as assetImage, t.watch,asset_mode as assetMode from topo_vertex v left join ( select ASSET.* from asset join SYS_org on asset.ORG_ID=SYS_ORG.id where SYS_ORG.TYPE=4 )t on t.id =v.asset_id left join (select ASSET_ID ,MIN(ALARM_LEVEL) as aLevel from ALARM_INFO where BLANK = 1 and (STATUS = 1 OR ALARM_STATE = 1)group by ASSET_ID) a on a.ASSET_ID=t.ID where v.org_id=#{reqID} and t.ASSET_MODE =42 and t.SHOW_TOPO=1 and t.IS_DEL=1 order by v.name")
    List<TopoVertexAlarmLevelVo> selectLineRoutNetTopoAlarmLevel(String reqID);

    /**
     * 前端线路TOPO加载-车站交换机
     *
     * @param reqID
     * @return
     */
    @Select("select a.aLevel AS alarmLevel,v.*, t.name as assetName, t.id as assetId,t.SHOW_CORE as showCore, t.org_id as assetOrgId, t.asset_image as assetImage, t.watch,asset_mode as assetMode from topo_vertex v left join ( select ASSET.* from asset join SYS_org on asset.ORG_ID=SYS_ORG.id where SYS_ORG.TYPE=4 )t on t.id =v.asset_id left join (select ASSET_ID ,MIN(ALARM_LEVEL) as aLevel from ALARM_INFO where BLANK = 1 and (STATUS = 1 OR ALARM_STATE = 1)group by ASSET_ID) a on a.ASSET_ID=t.ID where v.org_id=#{reqID} and t.ASSET_MODE =201 and t.SHOW_TOPO=1 and t.IS_DEL=1 order by v.name")
    List<TopoVertexAlarmLevelVo> selectLineSwitchNetTopoAlarmLevel(String reqID);

    /**
     * 前端线路TOPO加载中心网络设备
     *
     * @param reqID
     * @return
     */
    @Select("select a.aLevel AS alarmLevel,v.*, t.name as assetName, t.id as assetId,t.SHOW_CORE as showCore, t.org_id as assetOrgId, t.asset_image as assetImage, t.watch,asset_mode as assetMode from topo_vertex v left join ( select ASSET.* from asset join SYS_org on asset.ORG_ID=SYS_ORG.id where SYS_ORG.TYPE=2 )t on t.id =v.asset_id left join (select ASSET_ID ,MIN(ALARM_LEVEL) as aLevel from ALARM_INFO where BLANK = 1 and (STATUS = 1 OR ALARM_STATE = 1)group by ASSET_ID) a on a.ASSET_ID=t.ID where v.org_id=#{reqID} and t.ASSET_MODE in (42,201) and t.SHOW_TOPO=1 and t.IS_DEL=1 order by v.name")
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
    @Select("select v.ASSET_ID as assetId,v.NODE_ID as nodeId,a.STATUS as status from TOPO_VERTEX v LEFT JOIN topo_asset_port a ON v.ASSET_ID = a.ASSET_ID AND v.PORT_INDEX = a.PORT_INDEX where v.ORG_ID = #{orgId} and v.NODE_TYPE = #{nodeType} and a.STATUS = 2 and v.IS_PORT = 1")
    List<TopoPortStatus> findDownPort(@Param("orgId") String orgId, @Param("nodeType") String nodeType);

    List<AssetLinkAssetVo> findMaAssetAndPort();

    @Select("SELECT  t.*,v.* FROM (select  r.id as roomId, r.name roomName ,c.id as cabinetId ,c.name as cabinetName ,c.row_index,c.column_index  from cabinet c right join  room r on c.room_id=r.id  where r.org_id= #{orgId}) t left join TOPO_VERTEX v on v.asset_id=t.cabinetId order by row_index,column_index")
    List<CabinetTopoVo> selectNodeByCenterCabinetV2(String orgId);

    @Select("SELECT a.CABINET_ID, MIN(i.ALARM_LEVEL) alarmLevel " +
            "FROM ASSET_ATTACH a " +
            "LEFT JOIN ALARM_INFO i ON a.ASSET_ID = i.ASSET_ID " +
            "WHERE (i.STATUS = 1 OR  i.ALARM_STATE = 1) AND i.BLANK = 1 AND i.ALARM_LEVEL > 0 AND a.ORG_ID = #{orgId} AND a.CABINET_ID IS NOT NULL " +
            "GROUP BY a.CABINET_ID")
    List<CabinetTopoVo> selectAlarmByCenterCabinetV2(String orgId);


    @Select("select  r.id as roomId, r.name roomName ,c.id as cabinetId ,c.name as cabinetName ,c.row_index as rowIndex,c.column_index as columnIndex from cabinet c right join  room r on c.room_id=r.id  where r.org_id= #{orgId} and c.id is not null")
    List<CabinetTopoVo> selectCenterCabinetByOrgV2(String orgId);

    @Select("select a.id assetId, a.name assetName, a.ip assetIp, a.org_id orgId, a.service_type_id, b.alarm_status, b.host_type " +
            "from asset a left join broker_topo_business b on a.id = b.asset_id " +
            "where a.service_type_id is not null and a.org_id = #{orgId} and a.service_type_id = #{serviceTypeId}")
    List<BizTopoCenterVo> findTopoCenterQuery(String orgId, String serviceTypeId);

    List<TopoVertexVo> selectCabinetNodeV2(@Param("roomId") String roomId);

    List<TopoVertexVo> selectWanTopoNodeByAsset(@Param("nodeType") String nodeType, @Param("ids") List<String> ids);


    List<TopoVertexAlarmLevelVo> selectAllTopoNodeAlarmLevelByAsset(String category, String orgId);

    List<TopoVertexVo> selectAllTopoNodeByAsset(String category, String orgId);

    List<TopoVertexAlarmLevelVo> selectAllTopoNodeByLine(String category, String orgId);

    List<TopoVertexVo> selectAllTopoNodeByLine2(String category, String orgId);
}
