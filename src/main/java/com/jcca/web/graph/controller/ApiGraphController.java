package com.jcca.web.graph.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.vo.SysTopoGraph;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.graph.entity.TopoAssetGroup;
import com.jcca.web.graph.entity.TopoAssetMark;
import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.req.GraphQueryByNameReq;
import com.jcca.web.graph.service.*;
import com.jcca.web.graph.vo.CenterLineAlarmVo;
import com.jcca.web.graph.vo.TopoPortStatus;
import com.jcca.web.graph.vo.TopoVertexAlarmLevelVo;
import com.jcca.web.graph.vo.TopoVertexAlarmStatusVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ApiGraphController
 * @Description 画图工具前端展示
 * @Date 2020/4/27 14:35
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/graph")
@Slf4j
@Api(tags = "拓扑图相关接口")
public class ApiGraphController {

    private static final Byte CLUSTER = 2;
    /**
     * 网络拓扑图,机柜拓扑图,业务拓扑图,调度台拓扑
     */
    private static String[] topoType = {"net_topo", "cabinet_topo", "biz_topo", "pc_topo", "netWorkAsset_topo"};
    @Resource
    private AssetService assetService;
    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private TopoPointsService topoPointsService;
    @Resource
    private TopoEdgeService topoEdgeService;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private TopoAssetGroupService topoAssetGroupService;
    @Resource
    private TopoAssetMarkService topoAssetMarkService;
    @Resource
    private CollectRouteService collectRouteService;
    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private AlarmInfoService alarmInfoServ;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private CollectInterfacesService interfacesServ;
    @Resource
    private RoomService roomService;


    @PostMapping("/showMsg")
    @ApiOperation(value = "获取拓扑图端口流量信息")
    public ResultVo showMsg(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String assetId = reqJson.getStr("assetId");
        String portIndex = reqJson.getStr("portIndex");

        JSONObject resp = new JSONObject();
        resp.put("portIndex",portIndex);
        resp.put("portInSpeed","暂未获取到数据");
        resp.put("portOutSpeed","暂未获取到数据");
        List<CollectInterfaces> collectInterface = interfacesServ.getRealTimeData(assetId);
        if(Objects.isNull(collectInterface)||collectInterface.isEmpty()){
            return ResultVoUtil.success(resp);
        }
        List<CollectInterfaces> collect = collectInterface.stream().filter(item -> item.getPortIndex().equals(portIndex)).collect(Collectors.toList());
        if(collect.isEmpty()){
            return ResultVoUtil.success(resp);
        }
        CollectInterfaces collectInterfaces = collect.get(0);
        Long portInSpeed = collectInterfaces.getPortInSpeed();
        Long portOutSpeed = collectInterfaces.getPortOutSpeed();
        if(Objects.nonNull(portInSpeed)){
            double v = new BigDecimal(portInSpeed).divide(new BigDecimal(8000), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            resp.put("portInSpeed",v+"Kbps");
        }
        if(Objects.nonNull(portOutSpeed)){
            double v = new BigDecimal(portOutSpeed).divide(new BigDecimal(8000), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            resp.put("portOutSpeed",v+"Kbps");
        }
        return ResultVoUtil.success(resp);
    }

    /**
     * 拓扑图
     *
     * @return
     */
    @PostMapping("/topo")
    @ApiOperation(value = "获取拓扑图")
    @RequiresPermissions("api:graph:topo")
    @ActionLog(name = "查看拓扑图", title = "首页", key = LogTypeConstant.QUERY)
    public ResultVo index(@RequestBody SysTopoGraph graph) {
        // 获取组织结构Id
        String orgId = graph.getOrgId();
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error("请选择组织");
        }
        Map<String, Object> map = new HashMap<>();
        Integer type = sysOrgService.getById(orgId).getType();

        if (OrgTypeEnum.GROUP.getCode() == type || OrgTypeEnum.PARENT.getCode() == type) {
            return ResultVoUtil.success(map);
        }

        // 获取分类
        String category = graph.getCategory();
        String assetId = graph.getAssetId();// 获取资产ID
        log.info("前端获取组织[{}]拓扑图[{}]", orgId, category);
        // 网络设备
        if (topoType[0].equals(category)) {

            List<TopoVertexAlarmLevelVo> list = null;
            // 如果是线
            if (type == OrgTypeEnum.LINE.getCode()) {
                list = topoVertexService.selectNodeAlarmLevelByAsset(category, orgId);
                List<TopoVertexAlarmLevelVo> list2 = topoVertexService.selectLineNetTopoAlarmLevel(orgId);
                list.addAll(list2);
            } else {
                int config = getConfig();
                switch (config) {
                    case 3:      //全部开启
                        list = topoVertexService.selectNodeAlarmLevelByAsset2(category, orgId);
                        break;
                    case 1:    //只开启中心
                        if (OrgTypeEnum.CENTER.getCode() == type) {
                            list = topoVertexService.selectNodeAlarmLevelByAsset2(category, orgId);

                        } else {
                            list = topoVertexService.selectNodeAlarmLevelByAsset(category, orgId);
                        }
                        break;
                    case 2:    //只开启车站
                        if (OrgTypeEnum.STATION.getCode() == type) {
                            list = topoVertexService.selectNodeAlarmLevelByAsset2(category, orgId);
                        } else {
                            list = topoVertexService.selectNodeAlarmLevelByAsset(category, orgId);
                        }
                        break;
                    default:
                        list = topoVertexService.selectNodeAlarmLevelByAsset(category, orgId);
                }
            }

            for (TopoVertexAlarmLevelVo topoVertexAlarmLevelVo : list) {
                if(CLUSTER.equals(topoVertexAlarmLevelVo.getABFlag())){
                    if(!topoVertexAlarmLevelVo.getAssetName().equals(topoVertexAlarmLevelVo.getName())){
                        topoVertexAlarmLevelVo.setAssetName(topoVertexAlarmLevelVo.getName());
                    }
                }
            }

            map.put("vertex", list);

        } else if (topoType[1].equals(category)) {
            // 机柜
            List<TopoVertexAlarmLevelVo> list = topoVertexService.selectNodeAlarmLevelByCabnet(category, orgId);
            map.put("vertex", list);
            map.put("room", roomService.listByOrgId(orgId)); // 适应一个组织下多个机房 20240829
        } else if (topoType[2].equals(category)) {
            // 业务设备

        } else if (topoType[3].equals(category)) {
            // 调度台设备
            List<TopoVertexAlarmLevelVo> list = topoVertexService.selectPcTopoNodeAlarmLevelByAsset(category, orgId);
            map.put("vertex", list);
        } else if (topoType[4].equals(category)) {
            List<TopoVertexAlarmLevelVo> list = topoVertexService.selectNetWorkTopoNodeAlarmLevelByAsset(category,
                    assetId, orgId);
            if (list == null || (list.size() == 1 && assetId.equals(list.get(0).getAssetId()))) {
                return ResultVoUtil.success("未查询到相关连线信息");
            }

            map.put("vertex", list);
            List<TopoEdge> topoEdgeList = topoEdgeService.queryNetWorkEdge(category, graph.getAssetId());
            if (topoEdgeList == null || topoEdgeList.size() == 0) {// 如果曾经已经配置过设备连线
                map.put("AssetEdge", collectRouteService.queryCollectRoute(graph.getAssetId()));
            }

        }
        if (topoType[4].equals(category)) {
            map.put("points", topoPointsService.queryNetWorkPoints(category, assetId));
        } else {
            map.put("points", topoPointsService.getTopoPoints(category, orgId));
        }
        if (topoType[4].equals(category)) {
            map.put("edge", topoEdgeService.queryNetWorkEdge(category, assetId));
        } else {
            map.put("edge", topoEdgeService.getTopoEdge(category, orgId));
        }
        List<TopoAssetGroup> list = null;
        if (topoType[4].equals(category)) {
            list = topoAssetGroupService.queryNetWorkAssetGroup(assetId, category);
        } else {
            list = topoAssetGroupService.queryAssetGroup(orgId, category);
        }

        for (TopoAssetGroup topoAssetGroup : list) {
            String str = new String(topoAssetGroup.getContent());
            topoAssetGroup.setContentStr(str);
        }
        // 拓扑图分组
        map.put("groups", list);
        List<TopoAssetMark> marks = null;
        if (topoType[4].equals(category)) {
            marks = topoAssetMarkService.queryNetWorkAssetMark(assetId, category);
        } else {
            marks = topoAssetMarkService.queryAssetMark(orgId, category);
        }

        for (TopoAssetMark topoAssetMark : marks) {

            String str = null;
            str = new String(topoAssetMark.getContent(), StandardCharsets.UTF_8);
            topoAssetMark.setContentStr(str);
        }
        // 编辑备注
        map.put("marks", marks);

        return ResultVoUtil.success(map);
    }

    /**
     * 拓扑图
     *
     * @return
     */
    @PostMapping("/topoVertexStatus")
    @ApiOperation(value = "获取拓扑图节点状态")
    public ResultVo topoVertexStatus(@RequestBody SysTopoGraph graph) {
        // 获取组织结构Id
        String orgId = graph.getOrgId();
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error("请选择组织");
        }
        QueryWrapper<TopoVertex> queryWrapper = new QueryWrapper<TopoVertex>();
        queryWrapper.eq("ORG_ID", orgId);
        queryWrapper.isNull("IS_PORT");
        // 查询节点设备下有无告警
        if (topoType[4].equals(graph.getCategory())) {
            queryWrapper.eq("NODE_TYPE", graph.getCategory());
            queryWrapper.eq("CORE_ASSET_ID", graph.getAssetId());
        } else {
            queryWrapper.eq("NODE_TYPE", "net_topo");
        }

        List<TopoVertexAlarmStatusVo> respList = new ArrayList<TopoVertexAlarmStatusVo>();

        // 查询组织下的节点
        List<TopoVertex> topoVertexs = topoVertexService.list(queryWrapper);
        for (TopoVertex topoVertex : topoVertexs) {
            String assetId = topoVertex.getAssetId();

            Integer maxLevel = alarmInfoServ.queryMaxLevel(assetId);

            TopoVertexAlarmStatusVo resp = new TopoVertexAlarmStatusVo();
            resp.setNodeId(topoVertex.getNodeId());
            resp.setAlarmLevel(maxLevel);
            respList.add(resp);
        }

        /**
         *
         * 网络设备 List<TopoVertexAlarmStatusVo> list =
         * topoVertexService.topoVertexStatus("net_topo", orgId);
         */

        return ResultVoUtil.success(respList);
    }


    /**
     * 拓扑图端口状态
     *
     * @return
     */
    @PostMapping("/portStatus")
    @ApiOperation(value = "拓扑图端口状态")
    public ResultVo portStatus(@RequestBody SysTopoGraph graph) {
        String orgId = graph.getOrgId();
        if (StrUtil.isEmpty(orgId)) {
            return ResultVoUtil.error("缺少参数");
        }

        List<TopoPortStatus> list = null;
        if (topoType[4].equals(graph.getCategory())) {
            list = topoVertexService.findNetWorkDownPortStatus(graph.getAssetId());
        } else {
            list = topoVertexService.findDownPort(orgId);
        }

        /**
         * 赵政原来的逻辑 Map<String, Object> paraMap = new HashMap<>(); paraMap.put("orgId",
         * orgId); paraMap.put("status", 2);
         *
         * List<TopoPortStatus> respList = topoVertexService.findPortStatus(paraMap);
         */

        return ResultVoUtil.success(list);
    }

    /**
     * 拓扑图设备搜索
     *
     * @return
     */
    @PostMapping("/search")
    @ApiOperation(value = "拓扑图设备搜索")
    @RequiresPermissions("api:graph:search")
    @ActionLog(name = "搜索拓扑图设备", title = "首页", key = LogTypeConstant.QUERY)
    public ResultVo search(@RequestBody GraphQueryByNameReq req) {
        if (StrUtil.isEmpty(req.getAssetName())) {
            return ResultVoUtil.error("请输入查询关键字");
        }
        List<String> nodeIdList = new ArrayList<>();

        // 按名字查---机柜 调度台 业务设备 网络设备
        QueryWrapper<TopoVertex> nameQuery = Wrappers.query();
        nameQuery.like("NAME", req.getAssetName());
        nameQuery.eq("NODE_TYPE", req.getCategory());
        if (Objects.nonNull(req.getOrgId())) {
            nameQuery.eq("ORG_ID", req.getOrgId());
        }
        List<TopoVertex> topoVertexList = topoVertexService.list(nameQuery);
        if (CollUtil.isNotEmpty(topoVertexList)) {
            List<String> list = topoVertexList.stream().map(TopoVertex::getNodeId).collect(Collectors.toList());
            nodeIdList.addAll(list);
        }
//        if (CollectionUtil.isNotEmpty(nodeIdList)) {
//            return ResultVoUtil.success(new HashSet<>(nodeIdList));
//        }

        // 按IP查--调度台
        QueryWrapper<Asset> assetQuery = Wrappers.query();
        assetQuery.eq("ip", req.getAssetName());
        assetQuery.eq("is_del", StatusConst.OK);
        Asset asset = assetService.getOne(assetQuery);
        if (Objects.nonNull(asset)) {
            nameQuery = Wrappers.query();
            nameQuery.eq("ASSET_ID", asset.getId());
            nameQuery.eq("NODE_TYPE", req.getCategory());
            if (Objects.nonNull(req.getOrgId())) {
                nameQuery.eq("ORG_ID", req.getOrgId());
            }
            topoVertexList = topoVertexService.list(nameQuery);
            if (CollUtil.isNotEmpty(topoVertexList)) {
                List<String> list = topoVertexList.stream().map(TopoVertex::getNodeId).collect(Collectors.toList());
                nodeIdList.addAll(list);
            }
//            if (CollectionUtil.isNotEmpty(nodeIdList)) {
//                return ResultVoUtil.success(new HashSet<>(nodeIdList));
//            }
        }

        // 按名字查--服务器
        List<String> assetIds = new ArrayList<>();
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        assetQuery = Wrappers.query();
        assetQuery.ne("DESK", AssetModeConst.TERMINAL);
        assetQuery.like("NAME", req.getAssetName());
        if (Objects.nonNull(req.getOrgId())) {
            assetQuery.eq("ORG_ID", req.getOrgId());
        } else {
            assetQuery.in("ORG_ID", orgIds);
        }
        List<Asset> assetList = assetService.list(assetQuery);
        if (CollUtil.isNotEmpty(assetList)) {
            assetIds.addAll(assetList.stream().map(Asset::getId).collect(Collectors.toList()));
        }

        // 按IP查--服务器
        assetQuery = Wrappers.query();
        if (Objects.nonNull(req.getOrgId())) {
            assetQuery.eq("ORG_ID", req.getOrgId());
        } else {
            assetQuery.in("ORG_ID", orgIds);
        }
        assetQuery.like("IP", req.getAssetName());
        assetQuery.eq("IS_DEL", StatusEnum.OK.getCode());
        assetQuery.ne("DESK", AssetModeConst.TERMINAL);
        assetList = assetService.list(assetQuery);
        if (CollUtil.isNotEmpty(assetList)) {
            assetIds.addAll(assetList.stream().map(Asset::getId).collect(Collectors.toList()));
        }
        if (CollUtil.isNotEmpty(assetIds)) {
            QueryWrapper<AssetAttach> query = Wrappers.query();
            query.in("ASSET_ID", new HashSet<>(assetIds));
            query.isNotNull("CABINET_ID");
            List<AssetAttach> assetAttachList = assetAttachService.list(query);
            if (CollUtil.isNotEmpty(assetAttachList)) {
                List<String> cabinetIds = assetAttachList.stream().map(AssetAttach::getCabinetId)
                        .collect(Collectors.toList());
                QueryWrapper<TopoVertex> assetIdQuery = Wrappers.query();
                assetIdQuery.in("ASSET_ID", cabinetIds);
                assetIdQuery.eq("NODE_TYPE", req.getCategory());
                topoVertexList = topoVertexService.list(assetIdQuery);
            }
        }

        if (CollUtil.isNotEmpty(topoVertexList)) {
            List<String> list = topoVertexList.stream().map(TopoVertex::getNodeId).collect(Collectors.toList());
            nodeIdList.addAll(list);
        }

        if (CollectionUtil.isEmpty(nodeIdList)) {
            return ResultVoUtil.warning("未找到相应设备");
        }

        return ResultVoUtil.success(new HashSet<>(nodeIdList));
    }

    /**
     * 判断节点能否下钻
     *
     * @return
     */
    @PostMapping("/down")
    @ApiOperation(value = "是否可以下钻")
    public ResultVo isDown(@RequestBody SysTopoGraph graph) {
        // 获取分类
        String category = graph.getCategory();
        // 获取资产ID
        String assetId = graph.getAssetId();
        // 获取组织结构Id
        String orgId = graph.getOrgId();
        if (StrUtil.isEmpty(orgId)) {
            return ResultVoUtil.success("组织不可为空");
        }
        if (StrUtil.isEmpty(assetId)) {
            return ResultVoUtil.success("资产ID不可为空");
        }
        List<TopoVertexAlarmLevelVo> list = topoVertexService.selectNetWorkTopoNodeAlarmLevelByAsset(category,
                assetId, orgId);
        if (CollUtil.isNotEmpty(list)) {
            return ResultVoUtil.success(true);
        }
        return ResultVoUtil.success(false);
    }

    /**
     * 查找中心或站线有无告警数据
     *
     * @return 告警数据
     */
    @PostMapping("/centerLineAlarm")
    @ApiOperation(value = "站线图告警")
    public ResultVo<List<CenterLineAlarmVo>> centerLineAlarm() {

        List<CenterLineAlarmVo> voList = new ArrayList<>();
        // 用户管理的所有组织ID
        List<String> subjectOrgIds = ShiroUtil.getSubjectOrgIds();
        // 查找组织类型为中心和线路的组织
        List<SysOrg> centerList = sysOrgService.getListByOrgType(OrgTypeConst.CENTER);
        List<SysOrg> lineList = sysOrgService.getListByOrgType(OrgTypeConst.LINE);
        List<String> centerOrgIds = centerList.stream().map(SysOrg::getId).collect(Collectors.toList());
        List<String> lineOrgIds = lineList.stream().map(SysOrg::getId).collect(Collectors.toList());

        // 查找中心最高告警级别,适用于只有一个中心
        Collection<String> centerIds = CollectionUtil.intersection(subjectOrgIds, centerOrgIds);
        if (CollectionUtil.isNotEmpty(centerIds)) {
            for (SysOrg org : centerList) {
                CenterLineAlarmVo vo = new CenterLineAlarmVo();
                vo.setOrgId(org.getId());
                vo.setOrgName(org.getTitle());
                vo.setOrgType(org.getType());
                vo.setAlarmLevel(alarmInfoServ.getMaxLevelByOrgId(org.getId()));
                voList.add(vo);
            }
        }

        // 查找线路下车站最高告警级别,适用于只有一条线路
        Collection<String> lineIds = CollectionUtil.intersection(subjectOrgIds, lineOrgIds);
        if (CollectionUtil.isNotEmpty(lineIds)) {
            for (SysOrg org : lineList) {
                Set<SysOrg> childrenSet = sysOrgService.getChildrenById(org.getId());
                Iterator<SysOrg> it = childrenSet.iterator();
                List<String> stationIds = new ArrayList<>();
                while (it.hasNext()) {
                    stationIds.add(it.next().getId());
                }
                List<String> stationList = (List<String>) CollectionUtil.intersection(subjectOrgIds, stationIds);

                if (CollectionUtil.isEmpty(stationList)) {
                    stationList.add("X");
                }
                CenterLineAlarmVo vo = new CenterLineAlarmVo();
                vo.setOrgId(org.getId());
                vo.setOrgName(org.getTitle());
                vo.setOrgType(org.getType());
                vo.setAlarmLevel(alarmInfoServ.getMaxLevelByOrgIds(stationList));

                voList.add(vo);
            }
        }
        return ResultVoUtil.success(voList);
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
