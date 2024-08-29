package com.jcca.admin.biz.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.vo.AssetTargetVo;
import com.jcca.admin.biz.vo.SysTopoGraph;
import com.jcca.admin.biz.vo.TopoNodeGraph;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.controller.route.bean.AssetLinkConst;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectClusterService;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.graph.entity.TopoAssetGroup;
import com.jcca.web.graph.entity.TopoAssetMark;
import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.service.*;
import com.jcca.web.graph.vo.TopoVertexVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @ClassName MXGraphController
 * @Description 画图工具
 * @Date 2020/4/27 14:35
 * @Author hanwone
 */

@Controller
@RequestMapping("/system/graph")
@Slf4j
public class GraphController {

    private static final Byte CLUSTER = 2;

    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private TopoPointsService topoPointsService;
    @Resource
    private TopoEdgeService topoEdgeService;
    @Resource
    private TopoAssetPortService topoAssetPortService;
    @Resource
    private TopoAssetGroupService topoAssetGroupService;
    @Resource
    private TopoAssetMarkService topoAssetMarkService;
    @Resource
    private CollectInterfacesService intefacesServ;
    @Resource
    private CollectRouteService collectRouteService;
    @Resource
    private SysOrgService orgServ;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetLinkAssetService linkAssetService;

    @Resource
    private CollectClusterService collectClusterService;

    /**
     * 网络拓扑图,机柜拓扑图,业务拓扑图,调度台拓扑
     */
    private static String[] topoType = {"net_topo", "cabinet_topo", "biz_topo", "pc_topo", "netWorkAsset_topo"};

    /**
     * 拓扑图查询
     *
     * @param graph
     * @return
     */
    @PostMapping("/topoNode")
    @ApiOperation(value = "获取拓扑图")
    @ResponseBody
    public ResultVo topoNode(@RequestBody SysTopoGraph graph) throws UnsupportedEncodingException {
        // 获取组织结构Id
        String orgId = graph.getOrgId();
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error("请选择组织");
        }
        // 获取分类
        String category = graph.getCategory();
        if (log.isInfoEnabled()) {
            log.info("前端获取组织[{}]拓扑图[{}]", orgId, category);
        }
        Map<String, Object> map = new HashMap<>();
        // 网络设备 网络设备界面
        if (topoType[0].equals(category)) {
            SysOrg org = orgServ.getById(orgId);

            List<TopoVertexVo> list;
            if (OrgTypeEnum.LINE.getCode() == org.getType()) {
                list = topoVertexService.selectLineNetTopo(orgId);
            } else {
                int config = this.getConfig();
                switch (config) {
                    case 3:
                        //全部开启
                        list = topoVertexService.selectNodeByAsset2(category, orgId);
                        break;
                    case 1:
                        //只开启中心
                        if (OrgTypeEnum.CENTER.getCode() == org.getType()) {
                            list = topoVertexService.selectNodeByAsset2(category, orgId);

                        } else {
                            list = topoVertexService.selectNodeByAsset(category, orgId);
                        }
                        break;
                    case 2:
                        //只开启车站
                        if (OrgTypeEnum.STATION.getCode() == org.getType()) {
                            list = topoVertexService.selectNodeByAsset2(category, orgId);
                        } else {
                            list = topoVertexService.selectNodeByAsset(category, orgId);
                        }
                        break;
                    default:
                        list = topoVertexService.selectNodeByAsset(category, orgId);
                }
            }
            List<TopoVertexVo> addList = new ArrayList<>();
            List<TopoVertexVo> removeList = new ArrayList<>();
            for (TopoVertexVo topoVertexVo : list) {
                if (CLUSTER.equals(topoVertexVo.getABFlag())) {
                    //集群 需要根据集群信息在查一遍
                    List<TopoVertexVo> clusterTopo = collectClusterService.selectNodeById(topoVertexVo.getAssetId());
                    if (!clusterTopo.isEmpty()) {
                        addList.addAll(clusterTopo);
                    }
                    removeList.add(topoVertexVo);
                }
            }
            if (!addList.isEmpty()) {
                list.addAll(addList);
            }
            if (!removeList.isEmpty()) {
                list.removeAll(removeList);
            }

            map.put("vertex", list);

        }
        // 机柜
        if (topoType[1].equals(category)) {
            List<TopoVertexVo> list = topoVertexService.selectNodeByCabnet(category, orgId);
            map.put("vertex", list);
        }
        // 业务设备
        if (topoType[2].equals(category)) {
        }
        // 调度台设备
        if (topoType[3].equals(category)) {
            List<TopoVertexVo> list = topoVertexService.selectPcTopoNodeByAsset(category, orgId);
            map.put("vertex", list);
        }
        //网络设备资产连线拓扑
        if (topoType[4].equals(category)) {
            List<TopoVertexVo> list = topoVertexService.selectNetworkAssetTopoNodeByAsset(category, graph.getAssetId());

            map.put("vertex", list);
            List<TopoEdge> topoEdgeList = topoEdgeService.queryNetWorkEdge(category, graph.getAssetId());
            //如果曾经已经配置过设备连线
            if (topoEdgeList == null || topoEdgeList.size() == 0) {
                map.put("AssetEdge", collectRouteService.queryCollectRoute(graph.getAssetId()));
            }
            map.put("edge", topoEdgeList);
            map.put("points", topoPointsService.queryNetWorkPoints(category, graph.getAssetId()));
        } else {
            map.put("edge", topoEdgeService.getTopoEdge(category, orgId));
            map.put("points", topoPointsService.getTopoPoints(category, orgId));
        }

        // 拓扑图分组
        List<TopoAssetGroup> list = null;
        if (topoType[4].equals(category)) {
            list = topoAssetGroupService.queryNetWorkAssetGroup(graph.getAssetId(), category);
        } else {
            list = topoAssetGroupService.queryAssetGroup(orgId, category);
        }

        for (TopoAssetGroup topoAssetGroup : list) {
            String str = new String(topoAssetGroup.getContent());
            topoAssetGroup.setContentStr(str);
        }
        map.put("groups", list);
        // 拓扑图编辑备注
        List<TopoAssetMark> marks = null;
        if (topoType[4].equals(category)) {
            marks = topoAssetMarkService.queryNetWorkAssetMark(graph.getAssetId(), category);
        } else {
            marks = topoAssetMarkService.queryAssetMark(orgId, category);
        }
        for (TopoAssetMark topoAssetMark : marks) {
            String str = new String(topoAssetMark.getContent(), StandardCharsets.UTF_8);
            topoAssetMark.setContentStr(str);
        }
        map.put("marks", marks);
        return ResultVoUtil.success(map);
    }

    @PostMapping("/saveTopoNode")
    @ApiOperation(value = "保存拓扑图")
    @ResponseBody
    @ActionLog(name = "保存拓扑图", title = "拓扑图", key = LogTypeConstant.MODIFY)
    public ResultVo saveTopoNode(@RequestBody TopoNodeGraph topoNodeGraph) {
        List<TopoEdge> edges = topoNodeGraph.getEdges();
        for (TopoEdge edge : edges) {
            try {
                if (!StrUtil.isEmpty(edge.getPortMode()) && edge.getPortMode().getBytes(StandardCharsets.UTF_8).length > 17) {
                    return ResultVoUtil.error("标注的字符超长,最长16位英文");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        topoVertexService.deleteNodes(topoNodeGraph.getCategory(), topoNodeGraph.getOrgId());
        topoPointsService.deleteTopoPoints(topoNodeGraph.getCategory(), topoNodeGraph.getOrgId());
        topoEdgeService.deleteTopoEdge(topoNodeGraph.getCategory(), topoNodeGraph.getOrgId());
        topoAssetGroupService.deleteAssetGroup(topoNodeGraph.getOrgId(), topoNodeGraph.getCategory());
        topoAssetMarkService.deleteAssetMark(topoNodeGraph.getOrgId(), topoNodeGraph.getCategory());
        topoVertexService.saveNodes(topoNodeGraph.getNodes());
        topoPointsService.saveTopoPoints(topoNodeGraph.getPoints());
        topoEdgeService.saveTopoEdge(topoNodeGraph.getEdges());
        List<String> groups = topoNodeGraph.getGroups();
        if (groups != null && groups.size() > 0) {
            for (String content : groups) {
                TopoAssetGroup topoAssetGroup = new TopoAssetGroup();
                topoAssetGroup.setOrgId(topoNodeGraph.getOrgId());
                topoAssetGroup.setNodeType(topoNodeGraph.getCategory());
                topoAssetGroup.setContent(content.getBytes());
                topoAssetGroupService.saveOrUpdate(topoAssetGroup);
            }
        }
        List<String> marks = topoNodeGraph.getMarks();
        if (marks != null && marks.size() > 0) {
            for (String content : marks) {
                TopoAssetMark topoAssetMark = new TopoAssetMark();
                topoAssetMark.setOrgId(topoNodeGraph.getOrgId());
                topoAssetMark.setNodeType(topoNodeGraph.getCategory());
                topoAssetMark.setContent(content.getBytes(StandardCharsets.UTF_8));
                topoAssetMarkService.saveOrUpdate(topoAssetMark);
            }
        }

        // 保存对端连接信息
        try {
            Executor executor = (Executor) SpringContextUtil.getBean("transferDataExecutor");
            executor.execute(() -> {
                try {
                    linkAssetService.saveLinkAsset(AssetLinkConst.TOPO_SET); // 拓扑图配置的
                } catch (Exception e) {
                    log.error("通过网络拓扑图保存设备连接信息异常1", e);
                }
            });
        } catch (Exception e) {
            log.error("通过网络拓扑图保存设备连接信息异常2", e);
        }

        return ResultVoUtil.success("保存成功！");
    }

    @PostMapping("/saveNetWorkTopoNode")
    @ApiOperation(value = "保存网络资产拓扑图")
    @ResponseBody
    public ResultVo saveNetWorkTopoNode(@RequestBody TopoNodeGraph topoNodeGraph) throws UnsupportedEncodingException {
        // 删除网络设备节点
        topoVertexService.deleteNetWorkAssetNodes(topoNodeGraph.getCategory(), topoNodeGraph.getAssetId());
        // 删除网络设备端口
        topoPointsService.deleteNetWorkAssetTopoPoints(topoNodeGraph.getCategory(), topoNodeGraph.getAssetId());
        // 删除网络设备边界
        topoEdgeService.deleteNetWorkAssetTopoEdge(topoNodeGraph.getCategory(), topoNodeGraph.getAssetId());
        // 删除网络资产分组
        topoAssetGroupService.deleteNetWorkAssetGroup(topoNodeGraph.getAssetId(), topoNodeGraph.getCategory());
        // 删除网络设备备注
        topoAssetMarkService.deleteNetWorkAssetMark(topoNodeGraph.getAssetId(), topoNodeGraph.getCategory());
        topoVertexService.saveNodes(topoNodeGraph.getNodes());
        topoPointsService.saveTopoPoints(topoNodeGraph.getPoints());
        topoEdgeService.saveTopoEdge(topoNodeGraph.getEdges());
        List<String> groups = topoNodeGraph.getGroups();
        if (groups != null && groups.size() > 0) {
            for (String content : groups) {
                TopoAssetGroup topoAssetGroup = new TopoAssetGroup();
                topoAssetGroup.setOrgId(topoNodeGraph.getOrgId());
                topoAssetGroup.setNodeType(topoNodeGraph.getCategory());
                topoAssetGroup.setAssetId(topoNodeGraph.getAssetId());
                topoAssetGroup.setContent(content.getBytes());
                topoAssetGroupService.saveOrUpdate(topoAssetGroup);
            }
        }
        List<String> marks = topoNodeGraph.getMarks();
        if (marks != null && marks.size() > 0) {
            for (String content : marks) {
                TopoAssetMark topoAssetMark = new TopoAssetMark();
                topoAssetMark.setOrgId(topoNodeGraph.getOrgId());
                topoAssetMark.setNodeType(topoNodeGraph.getCategory());
                topoAssetMark.setAssetId(topoNodeGraph.getAssetId());
                topoAssetMark.setContent(content.getBytes(StandardCharsets.UTF_8));
                topoAssetMarkService.saveOrUpdate(topoAssetMark);
            }
        }

        return ResultVoUtil.success("保存成功！");
    }

    @PostMapping("/queryAssetTarget")
    @ApiOperation(value = "查询对端链接")
    @ResponseBody
    public ResultVo queryAssetTarget(@RequestBody String assetId) {
        List<AssetTargetVo> list = topoVertexService.queryTarget(assetId);
        return ResultVoUtil.success(list);

    }

    @PostMapping("/queryPortIndex")
    @ApiOperation(value = "查询设备端口信息")
    @ResponseBody
    public ResultVo queryPortIndex(@RequestBody String assetId) {
        List<AssetPortVo> list = topoAssetPortService.selectAssetPortQuery(assetId);
        if (list.isEmpty()) {
            list = getCachePortData(assetId);
        }
        return ResultVoUtil.success(list);
    }

    /**
     * 主要是查询对端连接信息表中有没有对端的连接信息
     * @param assetTargetVo
     * @return
     */
    @PostMapping("/queryPortIScorrect")
    @ApiOperation(value = "查询端口连线的正确性")
    @ResponseBody
    public ResultVo queryPortIScorrect(@RequestBody AssetTargetVo assetTargetVo) {
        List<AssetTargetVo> list = topoVertexService.queryPortIScorrect(assetTargetVo);
        if (list != null && list.size() > 0) {
            return ResultVoUtil.success(list.get(0));
        }
        return ResultVoUtil.success(list);
    }


    /**
     * 获取缓存中的端口数据 防止未上采集数据导致端口面板不显示
     *
     * @param assetId
     */
    private List<AssetPortVo> getCachePortData(String assetId) {
        List<AssetPortVo> portList = new ArrayList<AssetPortVo>();
        List<CollectInterfaces> realTimeData = intefacesServ.getRealTimeData(assetId);
        Asset asset = assetService.getById(assetId);
        for (CollectInterfaces item : realTimeData) {
            List<Integer> portType = null;
            if (AssetModeConst.B24.equals(asset.getAssetImage())) {
                portType = Arrays.asList(6, 18, 22, 23, 39, 56);
            } else {
                portType = Collections.singletonList(88);
            }
            if (!portType.contains(item.getPortType())) {
                continue;
            }
            if (!assetService.isStationAsset(assetId) && !AssetModeConst.B24.equals(asset.getAssetImage()) && 1 != item.getPortLinkType()) {
                continue;
            }
            QueryWrapper<TopoAssetPort> queryWrapper = new QueryWrapper<TopoAssetPort>();
            queryWrapper.eq("ASSET_ID", assetId);
            queryWrapper.eq("PORT_INDEX", item.getPortIndex());
            TopoAssetPort topoPort = topoAssetPortService.getOne(queryWrapper);

            AssetPortVo vo = new AssetPortVo();
            if (Objects.nonNull(topoPort)) {
                vo.setPointX(topoPort.getPointX());
                vo.setPointY(topoPort.getPointY());
                vo.setNodeId(topoPort.getNodeId());
                vo.setParentId(topoPort.getParentId());
                vo.setStatus(topoPort.getStatus());
                vo.setUpdateDate(topoPort.getUpdateDate());
            }
            vo.setAssetId(item.getAssetId());
            vo.setPortIndex(item.getPortIndex());
            vo.setPortName(item.getPortName());
            portList.add(vo);
        }

        return portList;
    }

    private int getConfig() {
        QueryWrapper<SysModuleConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("NAME", "config:topoType");
        List<SysModuleConfig> list = configService.list(queryWrapper);
        try {
            return Integer.parseInt(list.get(0).getValue());
        } catch (Exception e) {
            return 0;
        }
    }

}
