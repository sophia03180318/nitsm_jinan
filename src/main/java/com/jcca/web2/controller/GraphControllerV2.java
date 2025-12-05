package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.collect.controller.route.bean.AssetLinkConst;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.graph.entity.TopoAssetGroup;
import com.jcca.web.graph.entity.TopoAssetMark;
import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.service.*;
import com.jcca.web.graph.vo.TopoVertexAlarmLevelVo;
import com.jcca.web.graph.vo.TopoVertexVo;
import com.jcca.web2.entity.BusinessServiceType;
import com.jcca.web2.entity.FileRelate;
import com.jcca.web2.entity.TopoTag;
import com.jcca.web2.enums.TopoCategoryEnum;
import com.jcca.web2.service.BusinessServiceTypeService;
import com.jcca.web2.service.FileRelateService;
import com.jcca.web2.service.TopoTagService;
import com.jcca.web2.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @description: 拓扑图V2版本
 * @author: sophia
 * @create: 2023/10/20 09:45
 **/

@RestController
@RequestMapping("/api/v2/graph")
@Api(tags = "拓扑图V2版本")
public class GraphControllerV2 {

    private static final Byte CLUSTER = 2;
    @Value("${project.upload.file-path}")
    private String path;

    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private TopoPointsService topoPointsService;
    @Resource
    private TopoEdgeService topoEdgeService;
    @Resource
    private TopoAssetGroupService topoAssetGroupService;
    @Resource
    private TopoAssetMarkService topoAssetMarkService;
    @Resource
    private TopoAssetPortService topoAssetPortService;
    @Resource
    private CollectInterfacesService intefacesServ;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private TopoTagService topoTagService;
    @Resource
    private AssetService assetService;
    @Resource
    private BusinessServiceTypeService serviceTypeService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetLinkAssetService linkAssetService;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private CollectNetworkCardService networkCardService;
    @Resource
    private FileRelateService fileRelateService;
    @Resource
    private SysModuleConfigService configServ;


    /**
     * 配置该组织下应有的TOPO标签
     */
    @PostMapping("/setTopoTagList")
    @RequiresPermissions("api:v2:graph:setTopoTagList")
    @ApiOperation(value = "设置组织下拓扑页签")
    @ActionLog(name = "查看组织页签", title = "拓扑图页签", key = LogTypeConstant.QUERY)
    public ResultVo<Object> setTopoTagList(@RequestBody TopoTag topoTag) {
        if (StringUtils.isEmpty(topoTag.getOrgId())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }

        String orgId = topoTag.getOrgId();
        SysOrg one = orgService.getById(orgId);
        if (Objects.isNull(one)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织不存在");
        }

        String category = topoTag.getCategory();
        if (!TopoCategoryEnum.containsType(one.getType(), category)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "该组织拓扑图类型不正确");
        }

        try {
            topoTagService.saveOrUpdateTag(topoTag);
        } catch (Exception e) {
            return ResultVoUtil.error(e.getMessage());
        }
        return ResultVoUtil.success("处理成功");
    }

    @GetMapping("/del/{id}")
    @RequiresPermissions("api:v2:graph:del")
    @ApiOperation(value = "删除组织下拓扑页签")
    @ActionLog(name = "删除页签", title = "拓扑图页签", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> delTag(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "数据ID不能为空");
        }
        topoTagService.removeById(id);

        return ResultVoUtil.success();
    }


    /**
     * 获取该组织下拥有的TOPO标签
     * 有则显示 无则新建
     */
    @GetMapping("/getTopoTagList/{orgId}")
    @ApiOperation(value = "获取组织下拓扑页签")
    public ResultVo<Object> getTopoTagList(@PathVariable String orgId) {
        if (StringUtils.isEmpty(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        if (Objects.isNull(org)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND.getCode(), "无此组织：" + orgId);
        }

        QueryWrapper<TopoTag> query = Wrappers.query();
        query.eq("ORG_ID", orgId);
        query.orderByAsc("TAG_SORT");
        List<TopoTag> topoTags = topoTagService.list(query);
        if (!topoTags.isEmpty()) {
            return ResultVoUtil.success(topoTags);
        }

        Integer type = org.getType();
        if (OrgTypeConst.CENTER == type) {
            this.addTopo(TopoCategoryEnum.NET_TOPO.tagName, TopoCategoryEnum.NET_TOPO.category, orgId, topoTags);
            this.addTopo(TopoCategoryEnum.CABINET_TOPO.tagName, TopoCategoryEnum.CABINET_TOPO.category, orgId, topoTags);
            this.addTopo(TopoCategoryEnum.PC_TOPO.tagName, TopoCategoryEnum.PC_TOPO.category, orgId, topoTags);
            this.addTopo(TopoCategoryEnum.BIZ_TOPO.tagName, TopoCategoryEnum.BIZ_TOPO.category, orgId, topoTags);
            this.addTopo(TopoCategoryEnum.WAN_TOPO.tagName, TopoCategoryEnum.WAN_TOPO.category, orgId, topoTags);
        }
        if (OrgTypeConst.LINE == type) {
            this.addTopo(TopoCategoryEnum.NET_TOPO.tagName, TopoCategoryEnum.NET_TOPO.category, orgId, topoTags);
            this.addTopo(TopoCategoryEnum.BIZ_TOPO.tagName, TopoCategoryEnum.BIZ_TOPO.category, orgId, topoTags);
//            this.addTopo(TopoCategoryEnum.WAN_TOPO.tagName, TopoCategoryEnum.WAN_TOPO.category, orgId, topoTags);
        }
        if (OrgTypeConst.STATION == type) {
            this.addTopo(TopoCategoryEnum.NET_TOPO.tagName, TopoCategoryEnum.NET_TOPO.category, orgId, topoTags);
            this.addTopo(TopoCategoryEnum.CABINET_TOPO.tagName, TopoCategoryEnum.CABINET_TOPO.category, orgId, topoTags);
        }

        return ResultVoUtil.success(topoTags);
    }

    private void addTopo(String name, String category, String orgId, List<TopoTag> topoTags) {
        TopoTag topoTag = new TopoTag();
        topoTag.setId(MyIdUtil.getId());
        topoTag.setCategory(category);
        topoTag.setName(name);
        topoTag.setOrgId(orgId);
        topoTag.setRemark("自动生成");
        topoTagService.save(topoTag);
        topoTags.add(topoTag);
    }


    /**
     * 获取中心组织下的业务页签
     */
    @GetMapping("/topoBizNode/{orgId}")
    @ApiOperation(value = "获取业务拓扑图标签")
    public ResultVo<Object> topoBizTag(@PathVariable String orgId) {
        // 获取组织结构Id
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        if (OrgTypeEnum.CENTER.getCode() != org.getType()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织类型不是中心");
        }
        QueryWrapper<Asset> query = Wrappers.query();
        query.eq("IS_DEL", StatusConst.OK);
        query.eq("ORG_ID", orgId);
        query.isNotNull("SERVICE_TYPE_ID");
        Map<String, List<Asset>> collect = assetService.list(query).stream().collect(Collectors.groupingBy(Asset::getServiceTypeId));

        Set<String> typeIds = collect.keySet();
        if (typeIds.isEmpty()) {
            return ResultVoUtil.success();
        }
        QueryWrapper<BusinessServiceType> qw = new QueryWrapper<>();
        qw.in("ID", typeIds);
        qw.orderByAsc("SORT");
        List<BusinessServiceType> list = serviceTypeService.list(qw);
        return ResultVoUtil.success(list);
    }


    /**
     * 获取指定组织下的网络拓扑图
     */
    @GetMapping("/topoNetNode/{orgId}")
    @ApiOperation(value = "获取网络拓扑图")
    public ResultVo<Object> topoNetNode(@PathVariable String orgId) {
        // 获取组织结构Id
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        Integer type = org.getType();
        Map<String, Object> map = new HashMap<>();
        if (OrgTypeEnum.GROUP.getCode() == type || OrgTypeEnum.PARENT.getCode() == type) {
            return ResultVoUtil.success(map);
        }
        SysConfig sysConfig = configServ.getSysConfig();
        Integer showJcca = "no".equals(sysConfig.getShowJcca()) ? 2 : 1;

        String category = TopoCategoryEnum.NET_TOPO.category;
        List<TopoVertexAlarmLevelVo> list;

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

        for (TopoVertexAlarmLevelVo vo : list) {
            FileRelate one = fileRelateService.getByItemId(vo.getAssetId());
            if (Objects.nonNull(one)) {
                vo.setViewUrl(one.getViewUrl());
            }
            if (showJcca == 2) {
                if (ObjectUtil.isNotNull(vo.getAssetId())) {
                    Asset asset = assetService.getById(vo.getAssetId());
                    if (ObjectUtil.isNotNull(asset) && "JCCA".equals(asset.getAssetSupplier())) {
                        vo.setAlarmLevel(0);
                    }
                }
            }
        }

        map.put("vertex", list);
        //网络设备资产连线拓扑
        map.put("edge", topoEdgeService.getTopoEdge(category, orgId));
        map.put("points", topoPointsService.getTopoPoints(category, orgId));

        List<TopoAssetGroup> groupList = topoAssetGroupService.queryAssetGroup(orgId, category);
        for (TopoAssetGroup topoAssetGroup : groupList) {
            String str = new String(topoAssetGroup.getContent());
            topoAssetGroup.setContentStr(str);
        }
        // 拓扑图分组
        map.put("groups", groupList);

        // 拓扑图编辑备注
        List<TopoAssetMark> marks = topoAssetMarkService.queryAssetMark(orgId, category);
        for (TopoAssetMark topoAssetMark : marks) {
            String str = new String(topoAssetMark.getContent(), StandardCharsets.UTF_8);
            topoAssetMark.setContentStr(str);
        }
        map.put("marks", marks);
        return ResultVoUtil.success(map);
    }


    /**
     * 获取指定组织下的网络拓扑图
     */
    @GetMapping("/allTopoNode/{orgId}")
    @ApiOperation(value = "获取全局拓扑图")
    public ResultVo<Object> allTopoNode(@PathVariable String orgId) {
        // 获取组织结构Id
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        Integer type = org.getType();
        Map<String, Object> map = new HashMap<>();
        if (OrgTypeEnum.GROUP.getCode() == type || OrgTypeEnum.PARENT.getCode() == type) {
            return ResultVoUtil.success(map);
        }

        SysConfig sysConfig = configServ.getSysConfig();
        Integer showJcca = "no".equals(sysConfig.getShowJcca()) ? 2 : 1;
        String category = TopoCategoryEnum.ALL_TOPO.category;


        // 全局拓扑
        List<TopoVertexAlarmLevelVo> topoVertexAlarmLevelVos = topoVertexService.selectAllTopoNodeAlarmLevelByAsset(category, orgId);
        if (showJcca == 2) {
            for (TopoVertexAlarmLevelVo vo : topoVertexAlarmLevelVos) {
                if (ObjectUtil.isNotNull(vo.getAssetId())) {
                    Asset asset = assetService.getById(vo.getAssetId());
                    if (ObjectUtil.isNotNull(asset) && "JCCA".equals(asset.getAssetSupplier())) {
                        vo.setAlarmLevel(0);
                    }
                }
            }
        }
        map.put("vertex", topoVertexAlarmLevelVos);

        //网络设备资产连线拓扑
        map.put("points", topoPointsService.getTopoPoints(category, orgId));
        map.put("edge", topoEdgeService.getTopoEdge(category, orgId));


        List<TopoAssetGroup> groupList = topoAssetGroupService.queryAssetGroup(orgId, category);
        for (TopoAssetGroup topoAssetGroup : groupList) {
            String str = new String(topoAssetGroup.getContent());
            topoAssetGroup.setContentStr(str);
        }
        // 拓扑图分组
        map.put("groups", groupList);

        // 拓扑图编辑备注
        List<TopoAssetMark> marks = topoAssetMarkService.queryAssetMark(orgId, category);
        for (TopoAssetMark topoAssetMark : marks) {
            String str = null;
            try {
                str = new String(topoAssetMark.getContent(), "UTF-8");
                topoAssetMark.setContentStr(str);
            } catch (Exception e) {
            }
        }
        map.put("marks", marks);
        return ResultVoUtil.success(map);
    }


    /**
     * 获取指定中心组织下的机柜的拓扑图
     */
    @GetMapping("/topoCenterCabinetNode/{orgId}")
    @ApiOperation(value = "获取中心机柜拓扑图")
    public ResultVo<Object> topoCabinetNode(@PathVariable String orgId) {
        // 获取组织结构Id
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        if (OrgTypeEnum.CENTER.getCode() != org.getType()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织类型不是中心");
        }
        List<RoomTopoVo> list = topoVertexService.selectNodeByCenterCabinetV2(orgId);
        return ResultVoUtil.success(list);
    }

    /**
     * 获取指定车站组织下的机柜的拓扑图
     */
    @GetMapping("/topoStationCabinetNode/{orgId}")
    @ApiOperation(value = "获取车站机柜拓扑图")
    public ResultVo<Object> topoStationCabinetNode(@PathVariable String orgId) {
        // 获取组织结构Id
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        if (OrgTypeEnum.STATION.getCode() != org.getType()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织类型不是车站");
        }
        List<CabinetTopoDetailVo> list = topoVertexService.selectNodeByStationCabinetV2(orgId);
        return ResultVoUtil.success(list);
    }


    /**
     * 获取指定组织下调度台的拓扑图
     */
    @GetMapping("/topoPcNode/{orgId}")
    @ApiOperation(value = "获取指定组织下调度台的拓扑图")
    public ResultVo<Object> topoPcNode(@PathVariable String orgId) {
        // 获取组织结构Id
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织ID不能为空");
        }
        SysConfig sysConfig = configServ.getSysConfig();
        Integer showJcca = "no".equals(sysConfig.getShowJcca()) ? 2 : 1;

        String category = "pc_topo";
        Map<String, Object> map = new HashMap<>();
        List<TopoVertexAlarmLevelVo> list = topoVertexService.selectPcTopoNodeAlarmLevelByAsset(category, orgId);

        if (CollectionUtils.isEmpty(list)) {
            List<TopoVertexVo> topoVertexVos = topoVertexService.selectPcTopoNodeByAsset(category, orgId);
            if (showJcca == 2) {
                for (TopoVertexVo topoVertexAlarmLevelVo : topoVertexVos) {
                    if (ObjectUtil.isNotNull(topoVertexAlarmLevelVo.getAssetId())) {
                        Asset asset = assetService.getById(topoVertexAlarmLevelVo.getAssetId());
                        if (ObjectUtil.isNotNull(asset) && "JCCA".equals(asset.getAssetSupplier())) {
                            topoVertexAlarmLevelVo.setAlarmLevel(0);
                        }
                    }
                }
            }
            map.put("vertex", topoVertexVos);
        } else {
            if (showJcca == 2) {
                for (TopoVertexAlarmLevelVo topoVertexAlarmLevelVo : list) {
                    if (ObjectUtil.isNotNull(topoVertexAlarmLevelVo.getAssetId())) {
                        Asset asset = assetService.getById(topoVertexAlarmLevelVo.getAssetId());
                        if (ObjectUtil.isNotNull(asset) && "JCCA".equals(asset.getAssetSupplier())) {
                            topoVertexAlarmLevelVo.setAlarmLevel(0);
                        }
                    }
                }
            }
            map.put("vertex", list);
        }

        // 拓扑图分组
        List<TopoAssetGroup> groupList = topoAssetGroupService.queryAssetGroup(orgId, category);
        for (TopoAssetGroup topoAssetGroup : groupList) {
            String str = new String(topoAssetGroup.getContent());
            topoAssetGroup.setContentStr(str);
        }
        map.put("groups", groupList);
        // 拓扑图编辑备注
        List<TopoAssetMark> marks = topoAssetMarkService.queryAssetMark(orgId, category);
        for (TopoAssetMark topoAssetMark : marks) {
            String str = new String(topoAssetMark.getContent(), StandardCharsets.UTF_8);
            topoAssetMark.setContentStr(str);
        }
        map.put("marks", marks);
        return ResultVoUtil.success(map);
    }

    @GetMapping("/topoCenterQuery")
    @ApiOperation(value = "获取指定组织下的业务拓扑图")
    public ResultVo<Object> topoCenterQuery(String orgId, String serviceTypeId) {

        if (StringUtils.isEmpty(orgId) || StringUtils.isEmpty(serviceTypeId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        List<BizTopoCenterVo> bizVoList = topoVertexService.findTopoCenterQuery(orgId, serviceTypeId);

        return ResultVoUtil.success(bizVoList);
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

    /**
     * 根据资产名称或者资产IP 搜索包含的机柜
     */
    @PostMapping("/topoCabinetByAssetStr")
    @ApiOperation(value = "根据获取车站机柜列表")
    public ResultVo<Object> topoCabinetByAssetStr(@RequestBody AssetStrVo assetStrVo) {
        if (ObjectUtil.isNull(assetStrVo.getRoomId()) || assetStrVo.getRoomId().isEmpty()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "机房ID不可为空");
        }
        if (ObjectUtil.isNull(assetStrVo.getKeyword()) || assetStrVo.getKeyword().isEmpty()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), " 查询关键字不可为空");
        }
        List<String> cabinetIdList = cabinetService.topoCabinetByAssetStr(assetStrVo.getRoomId(), assetStrVo.getKeyword());
        return ResultVoUtil.success(cabinetIdList);
    }


    @PostMapping("/queryPortIndex")
    @ApiOperation(value = "查询设备端口信息")
    @ResponseBody
    public ResultVo queryPortIndex(@RequestBody String assetId) {
        List<AssetPortVo> list = topoAssetPortService.selectAssetPortQuery(assetId);
        if (list.isEmpty()) {
            list = this.getCachePortData(assetId);
        }
        return ResultVoUtil.success(list);
    }

    /**
     * 获取缓存中的端口数据 防止未上采集数据导致端口面板不显示
     *
     * @param assetId
     */
    private List<AssetPortVo> getCachePortData(String assetId) {
        List<AssetPortVo> portList = new ArrayList<>();
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

    @PostMapping("/saveTopoNode")
    @ApiOperation(value = "保存拓扑图V2")
    @ResponseBody
    @ActionLog(name = "保存拓扑图V2", title = "拓扑图", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> saveTopoNode(@RequestBody TopoNodeGraph topoNodeGraph) {
        List<TopoEdge> edges = topoNodeGraph.getEdges();
        for (TopoEdge edge : edges) {
            try {
                if (!StrUtil.isEmpty(edge.getPortMode()) && edge.getPortMode().getBytes(StandardCharsets.UTF_8).length > 17) {
                    return ResultVoUtil.error("标注的字符超长,最长16位英文");
                }
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.NET_TOPO, "保存拓扑图异常", e);
            }
        }

        topoVertexService.deleteNodes(topoNodeGraph.getCategory(), topoNodeGraph.getOrgId());
        topoPointsService.deleteTopoPoints(topoNodeGraph.getCategory(), topoNodeGraph.getOrgId());
        topoEdgeService.deleteTopoEdge(topoNodeGraph.getCategory(), topoNodeGraph.getOrgId());
        topoAssetGroupService.deleteAssetGroup(topoNodeGraph.getOrgId(), topoNodeGraph.getCategory());
        topoAssetMarkService.deleteAssetMark(topoNodeGraph.getOrgId(), topoNodeGraph.getCategory());

        List<TopoVertex> nodes = topoNodeGraph.getNodes();
        if (TopoCategoryEnum.CABINET_TOPO.category.equals(topoNodeGraph.getCategory())) {
            for (TopoVertex node : nodes) {
                node.setOrgId(topoNodeGraph.getOrgId()); // 机柜拓扑使用机房ID做为组织ID
            }
        }
        topoVertexService.saveNodes(nodes);
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
                    AppLogUtils.buildLogError(LogFunctionEnum.ASSET_LINK_ASSET, "通过网络拓扑图保存设备连接信息异常1", e);
                }
            });
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_LINK_ASSET, "通过网络拓扑图保存设备连接信息异常2", e);
        }

        return ResultVoUtil.success("保存成功！");
    }

    @PostMapping("/topoNode")
    @ApiOperation(value = "获取拓扑图V2")
    @ResponseBody
    public ResultVo topoNode(@RequestBody SysTopoGraph graph) {
        // 获取组织结构Id
        String orgId = graph.getOrgId();
        if (Objects.isNull(orgId)) {
            return ResultVoUtil.error("请选择组织");
        }

        SysConfig sysConfig = configServ.getSysConfig();
        Integer showJcca = "no".equals(sysConfig.getShowJcca()) ? 2 : 1;

        // 获取分类
        String category = graph.getCategory();
        Map<String, Object> map = new HashMap<>();
        // 网络设备 网络设备界面
        if (TopoCategoryEnum.NET_TOPO.category.equals(category)) {
            SysOrg org = orgService.getById(orgId);

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

            map.put("vertex", list);

        }
        // 机柜
        if (TopoCategoryEnum.CABINET_TOPO.category.equals(category)) {
            String roomId = graph.getRoomId();
            //  List<TopoVertexVo> list = topoVertexService.selectCabinetNodeV2(roomId);
            List<TopoVertexAlarmLevelVo> list = topoVertexService.selectNodeAlarmLevelByCabnet2(category, roomId, roomId, showJcca);
            map.put("vertex", list);
        }
        // 调度台设备
        if (TopoCategoryEnum.PC_TOPO.category.equals(category)) {
            List<TopoVertexVo> list = topoVertexService.selectPcTopoNodeByAsset(category, orgId);
            map.put("vertex", list);
        }
        // 广域网拓扑
        if (TopoCategoryEnum.WAN_TOPO.category.equals(category)) {
            this.setWanMap(category, orgId, map);
        }

        map.put("edge", topoEdgeService.getTopoEdge(category, orgId));
        map.put("points", topoPointsService.getTopoPoints(category, orgId));

        // 拓扑图分组
        List<TopoAssetGroup> list = topoAssetGroupService.queryAssetGroup(orgId, category);

        for (TopoAssetGroup topoAssetGroup : list) {
            String str = new String(topoAssetGroup.getContent());
            topoAssetGroup.setContentStr(str);
        }
        map.put("groups", list);
        // 拓扑图编辑备注
        List<TopoAssetMark> marks;
        if (TopoCategoryEnum.CABINET_TOPO.category.equals(category)) {
            marks = topoAssetMarkService.queryAssetMark(graph.getRoomId(), category);
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

    private void setWanMap(String category, String orgId, Map<String, Object> map) {
        SysOrg org = orgService.getById(orgId);
        if (org.getType() != OrgTypeConst.CENTER) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "请选择中心组织");
        }
        List<SysOrg> stations = orgService.getListByOrgType(OrgTypeConst.STATION);
        if (stations.isEmpty()) {
            map.put("vertex", new ArrayList<>());
            return;
        }
        List<String> orgIds = stations.stream().map(SysOrg::getId).collect(Collectors.toList());
        if (orgIds.isEmpty()) {
            orgIds.add("x");
        }
        QueryWrapper<Asset> query = Wrappers.query();
        query.eq("DESK", AssetModeConst.ROUTER);
        query.eq("SHOW_TOPO", 1);
        query.eq("IS_DEL", 1);
        query.in("ORG_ID", orgIds);
        List<Asset> assetList = assetService.list(query);
        query = Wrappers.query();
        query.eq("DESK", AssetModeConst.ROUTER);
        query.eq("SHOW_TOPO", 1);
        query.eq("IS_DEL", 1);
        query.eq("SHOW_CORE", "SHOW_TOPO_@_SHOW");
        query.eq("ORG_ID", orgId);
        assetList.addAll(assetService.list(query));
        if (assetList.isEmpty()) {
            map.put("vertex", new ArrayList<>());
            return;
        }

        List<String> ids = assetList.stream().map(Asset::getId).collect(Collectors.toList());
        List<TopoVertexVo> list = topoVertexService.selectWanTopoNodeByAsset(category, ids);
        map.put("vertex", list);
    }


    @GetMapping("/getNetWorkAsset/{orgId}")
    @ApiOperation(value = "获取网络设备")
    @ResponseBody
    public ResultVo getNetWorkAsset(@PathVariable String orgId) {
        QueryWrapper<Asset> qw = new QueryWrapper<>();
        qw.eq("IS_DEL", 1);
        qw.eq("ORG_ID", orgId);
        List<Asset> list = assetService.list(qw);
        ArrayList<TopoAsset> topoAssets = new ArrayList<>();
        for (Asset asset : list) {
            TopoAsset topoAsset = new TopoAsset();
            topoAsset.setAssetId(asset.getId());
            topoAsset.setAssetName(asset.getName());
            topoAsset.setAssetMode(asset.getAssetMode());
            if (asset.getAssetMode() == 42 || asset.getAssetMode() == 201) {
                List<CollectInterfaces> realTimeData = intefacesServ.getRealTimeData(asset.getId());
                if (!realTimeData.isEmpty()) {
                    topoAsset.setPortNameList(realTimeData);
                }
            }
            topoAssets.add(topoAsset);
        }
        return ResultVoUtil.success(topoAssets);
    }


    @GetMapping("/getStationNetTopo/{orgId}")
    @ApiOperation(value = "获取车站网络topo")
    @ResponseBody
    public ResultVo getNetWorkTopoFile(@PathVariable String orgId) {
        String filePath = path + "/NetTopo/";
        Reader reader = null;
        try {
            File file = new File(filePath + orgId);
            if (!file.exists()) {
                return ResultVoUtil.success("");
            }
            reader = new InputStreamReader(new FileInputStream(file));
            char[] tempchars = new char[50];
            int charread = 0;
            StringBuilder builder = new StringBuilder();
            while ((charread = reader.read(tempchars)) != -1) {
                if ((charread == tempchars.length)) {
                    builder.append(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        builder.append(tempchars[i]);
                    }
                }
            }
            reader.close();
            String content = builder.toString();
            return ResultVoUtil.success(content);
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(reader)) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
            return ResultVoUtil.error("");
        }
    }


    @PostMapping("/saveStationNetTopo")
    @ApiOperation(value = "保存网络资产拓扑文件")
    @ResponseBody
    public ResultVo saveNetWorkTopoFile(@RequestBody NetTopoVo netTopoVo) {
        String filePath = path + "/NetTopo/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath + netTopoVo.getOrgId());
            fileWriter.write(netTopoVo.getMsg());
            fileWriter.close();
            return ResultVoUtil.success("保存成功");
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(fileWriter)) {
                try {
                    fileWriter.close();
                } catch (IOException ex) {
                }
            }
            return ResultVoUtil.error(e.toString());
        }
    }


    @GetMapping("/querySystemAlarmInfo/{orgId}")
    @ApiOperation(value = "获取组织下资产告警")
    @ResponseBody
    public ResultVo querySystemAlarmInfo(@PathVariable String orgId) {
        return ResultVoUtil.success(alarmInfoService.getAssetAlarmByOrg(orgId));
    }

    @GetMapping("/initAssetPort/{orgId}")
    @ApiOperation(value = "获取车站所有端口/网卡状态")
    @ResponseBody
    public ResultVo initAssetPort(@PathVariable String orgId) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ORG_ID", orgId);
        queryWrapper.eq("IS_DEL", 1);
        List<Asset> assets = assetService.list(queryWrapper);
        ArrayList<TopoPortVo> topoPortVos = new ArrayList<>();
        for (Asset asset : assets) {
            String id = asset.getId();
            if (AssetModeConst.ROUTER.equals(asset.getAssetMode()) || AssetModeConst.SWITCH.equals(asset.getAssetMode())) {
                intefacesServ.getRealTimeData(id).stream().forEach(i -> {
                    TopoPortVo topoPortVo = new TopoPortVo();
                    topoPortVo.setAssetId(id);
                    topoPortVo.setName(i.getPortName());
                    topoPortVo.setStatus(i.getStatus().intValue());
                    topoPortVos.add(topoPortVo);
                });
            } else {
                networkCardService.getRealTimeData(id).stream().forEach(n -> {
                    TopoPortVo topoPortVo = new TopoPortVo();
                    topoPortVo.setAssetId(id);
                    topoPortVo.setName(n.getName());
                    topoPortVo.setStatus(n.getStatus().intValue());
                    topoPortVos.add(topoPortVo);
                });
            }
        }
        return ResultVoUtil.success(topoPortVos);
    }

}
