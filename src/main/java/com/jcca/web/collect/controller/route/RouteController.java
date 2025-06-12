package com.jcca.web.collect.controller.route;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.controller.GraphController;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppPattenUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.poi.xssf.usermodel.XSSFWorkbook;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.utils.NullFieldException;
import com.jcca.web.asset.vo.AssetManualVo;
import com.jcca.web.asset.vo.LinkAssetExportVo;
import com.jcca.web.collect.controller.route.bean.AssetLinkConst;
import com.jcca.web.collect.controller.route.bean.ExportManualReq;
import com.jcca.web.collect.controller.route.bean.RouteMsg;
import com.jcca.web.collect.controller.route.bean.UpdateRouteReq;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.collect.service.bean.TopoRouteReturnVo;
import com.jcca.web.collect.service.bean.TopoRouteTarget;
import com.jcca.web.collect.service.bean.TopoRouteVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "端口对端设备查询")
@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Resource
    private CollectRouteService routeServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectInterfacesService interfaceServ;
    @Resource
    private CollectNetworkCardService netWorkServ;
    @Resource
    private AssetLinkAssetService linkAssetService;

    @SuppressWarnings("rawtypes")
    @GetMapping("/refresh")
    ResultVo refresh(String assetId) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("IS_DEL", 1);
        queryWrapper.in("ASSET_MODE", Arrays.asList(42, 201));
        if (StrUtil.isNotEmpty(assetId)) {
            queryWrapper.eq("ID", assetId);
        }
        List<Asset> list = assetServ.list(queryWrapper);

        for (Asset asset : list) {
            routeServ.collectRoute(asset);
        }
        return ResultVoUtil.success("OK");
    }

    /**
     * 获取网络设备对端设备信息
     *
     * @param localassetId
     * @param portIndexRank
     * @return
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/netGetAsset")
    @ResponseBody
    ResultVo netGetAsset(String localassetId, Integer portIndexRank) {
        if (Objects.isNull(portIndexRank) || StrUtil.isEmpty(localassetId)) {
            return ResultVoUtil.error("资产ID和端口序号不可传空");
        }
        Asset localAsset = assetServ.getById(localassetId);
        if (Objects.isNull(localAsset)) {
            return ResultVoUtil.error("资产不存在");
        }

        List<RouteMsg> respAssetList = findLinkAssetList(localassetId, portIndexRank, localAsset);

        return ResultVoUtil.success(respAssetList);
    }

    /**
     * 获取主机设备对端端口信息
     *
     * @param localassetId
     * @return
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/hostGetAsset")
    @RequiresPermissions({"api:route:hostGetAsset"})
    @ResponseBody
    ResultVo hostGetAsset(String localassetId) {
        if (StrUtil.isEmpty(localassetId)) {
            return ResultVoUtil.error("资产ID不可传空");
        }
        Asset localAsset = assetServ.getById(localassetId);
        if (Objects.isNull(localAsset)) {
            return ResultVoUtil.error("资产不存在");
        }
        // 查询所有有IP的网卡
        List<CollectNetworkCard> cardList = netWorkServ.getRealTimeData(localassetId);
        List<RouteMsg> routeList = new ArrayList<RouteMsg>();
        for (CollectNetworkCard card : cardList) {
            String macAddr = card.getMacAddress();
            if (StrUtil.isEmpty(macAddr)) {
                continue;
            }
            RouteMsg routeMsg = routeServ.queryRouteMsg(card.getId(), card.getMacAddress());
            routeList.add(routeMsg);
        }

        return ResultVoUtil.success(routeList);
    }

    /**
     * 更新TOPO发现记录
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    @PostMapping("/updateRoute")
    @RequiresPermissions({"api:route:queryRouteList"})
    @ResponseBody
    ResultVo updateRoute(@RequestBody UpdateRouteReq req) {
        CollectRoute route = routeServ.getById(req.getId());
        if (Objects.isNull(route)) {
            return ResultVoUtil.error("该路由信息已不存在，请刷新页面重新操作");
        }

        route.setAtName(req.getAtAssetName());
        route.setRemark(req.getRemark());
        route.setAtPortName(req.getAtPortName());

        routeServ.updateById(route);

        return ResultVoUtil.success("处理成功");
    }

    /**
     * 查询对端设备列表
     *
     * @param assetId
     * @return
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/queryRouteList/{assetId}")
    @RequiresPermissions({"api:route:queryRouteList"})
    @ResponseBody
    ResultVo queryRouteList(@PathVariable("assetId") String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            return ResultVoUtil.error("请传入网络设备资产ID");
        }
        ArrayList<RouteMsg> arrayList = new ArrayList<RouteMsg>();
        Asset localAsset = assetServ.getById(assetId);

        List<Integer> netAssetMode = Arrays.asList(42, 201);
        if (Objects.isNull(localAsset) || !netAssetMode.contains(localAsset.getAssetMode())) {
            log.error("查询对端设备列表-资产ID对应资产不存在或者非网络设备--返回空列表：参数：{}", JSONUtil.toJsonStr(assetId));
            return ResultVoUtil.success(arrayList);
        }
        List<TopoRouteVo> list = routeServ.queryTopoRoute(assetId);
        List<TopoRouteReturnVo> topoRouteReturnVos = new ArrayList<>();
        TopoRouteReturnVo topoRouteVoLast = null;
        for (TopoRouteVo topoRouteVo : list) {
            if (topoRouteVoLast != null && topoRouteVoLast.getPortIndexRank().equals(topoRouteVo.getPortIndexRank())) {
                if (topoRouteVo.getAtNetAddress() != null && !"".equals(topoRouteVo.getAtNetAddress())) {
                    TopoRouteTarget topoRouteTarget = new TopoRouteTarget();
                    topoRouteTarget.setAtNetAddress(topoRouteVo.getAtNetAddress());
                    topoRouteTarget.setAtPhysAddress(topoRouteVo.getAtPhysAddress());
                    topoRouteTarget.setAtAssetId(topoRouteVo.getAtAssetId());
                    topoRouteTarget.setAtName(topoRouteVo.getAtName());
                    topoRouteTarget.setAtPortName(topoRouteVo.getAtPortName());
                    topoRouteTarget.setAtPortIndexName(topoRouteVo.getAtPortIndexName());
                    topoRouteTarget.setAtType(topoRouteVo.getAtType());
                    if (topoRouteVoLast.getTargetList() == null) {
                        List<TopoRouteTarget> targetList = new ArrayList<>();
                        targetList.add(topoRouteTarget);
                        topoRouteVoLast.setTargetList(targetList);
                    } else {
                        topoRouteVoLast.getTargetList().add(topoRouteTarget);
                    }
                }
            } else {
                TopoRouteReturnVo topoRouteReturnVo = new TopoRouteReturnVo();
                topoRouteReturnVo.setId(topoRouteVo.getId());
                topoRouteReturnVo.setStatus(topoRouteVo.getStatus());
                topoRouteReturnVo.setAssetId(topoRouteVo.getAssetId());
                topoRouteReturnVo.setPortIndexRank(topoRouteVo.getPortIndexRank());
                topoRouteReturnVo.setPortSendNum(topoRouteVo.getPortSendNum());
                topoRouteReturnVo.setPortReceiveNum(topoRouteVo.getPortReceiveNum());
                topoRouteReturnVo.setPortOrgName(topoRouteVo.getPortOrgName());
                topoRouteReturnVo.setPortName(topoRouteVo.getPortName());
                topoRouteReturnVo.setPortIp(topoRouteVo.getPortIp());
                topoRouteReturnVo.setPortMacAddress(topoRouteVo.getPortMacAddress());
                topoRouteReturnVo.setPortIndexName(topoRouteVo.getPortIndexName());
                topoRouteReturnVo.setRemark(topoRouteVo.getRemark());
                if (topoRouteVo.getAtNetAddress() != null && !"".equals(topoRouteVo.getAtNetAddress())) {
                    TopoRouteTarget topoRouteTarget = new TopoRouteTarget();
                    topoRouteTarget.setAtNetAddress(topoRouteVo.getAtNetAddress());
                    topoRouteTarget.setAtPhysAddress(topoRouteVo.getAtPhysAddress());
                    topoRouteTarget.setAtAssetId(topoRouteVo.getAtAssetId());
                    topoRouteTarget.setAtName(topoRouteVo.getAtName());
                    topoRouteTarget.setAtPortName(topoRouteVo.getAtPortName());
                    topoRouteTarget.setAtPortIndexName(topoRouteVo.getAtPortIndexName());
                    topoRouteTarget.setAtType(topoRouteVo.getAtType());
                    if (topoRouteReturnVo.getTargetList() == null) {
                        List<TopoRouteTarget> targetList = new ArrayList<>();
                        targetList.add(topoRouteTarget);
                        topoRouteReturnVo.setTargetList(targetList);
                    } else {
                        topoRouteReturnVo.getTargetList().add(topoRouteTarget);
                    }
                }
                topoRouteVoLast = topoRouteReturnVo;
                topoRouteReturnVos.add(topoRouteReturnVo);
            }
        }

        return ResultVoUtil.success(topoRouteReturnVos);
    }

    /**
     * 通过本地设备信息查询对端设备列表
     *
     * @param localAssetId
     * @param portIndexRank
     * @param localAsset
     * @return
     */
    private List<RouteMsg> findLinkAssetList(String localAssetId, Integer portIndexRank, Asset localAsset) {
        // 最后一次采集的信息
        List<CollectInterfaces> realTimeData = interfaceServ.getRealTimeData(localAssetId);
        if (Objects.isNull(realTimeData)) {
            realTimeData = new ArrayList<CollectInterfaces>();
        }
        List<CollectInterfaces> interfaces = realTimeData.stream().filter(item -> portIndexRank.equals(item.getPortIndexRank())).collect(Collectors.toList());
        if (interfaces.isEmpty()) {
            return new ArrayList<RouteMsg>();
        }
        CollectInterfaces collectInterfaces = interfaces.get(0);

        AssetLinkAsset linkAssetByAsset = linkAssetService.findLinkAssetByAsset(localAssetId, collectInterfaces.getPortIndex());
        if (Objects.isNull(linkAssetByAsset)) {
            return new ArrayList<RouteMsg>();
        }
        RouteMsg msg = new RouteMsg();
        msg.setLocalIp(linkAssetByAsset.getPortIp());
        msg.setRemoteIp(linkAssetByAsset.getLinkAssetIp());
        msg.setRemoteName(linkAssetByAsset.getLinkAssetName());

        Asset remoteAsset = new Asset();
        remoteAsset.setIp(linkAssetByAsset.getLinkAssetIp());
        remoteAsset.setName(linkAssetByAsset.getLinkAssetName());

        msg.setRemoteAsset(remoteAsset);
        return Arrays.asList(msg);
    }


    @GetMapping("/linkAsset")
    @ResponseBody
    @ApiOperation(value = "查询对端设备信息")
    public ResultVo linkAsset(String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            return ResultVoUtil.warning("缺少查询参数");
        }

        QueryWrapper<AssetLinkAsset> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        query.isNotNull("PORT_INDEX_RANK");
        query.orderByAsc("TO_NUMBER(PORT_INDEX_RANK)");
        List<AssetLinkAsset> list = linkAssetService.list(query);
        if (CollectionUtil.isEmpty(list)) {
            linkAssetService.saveThisAsset(assetId);
            list = linkAssetService.list(query);
        }

        List<CollectInterfaces> realTimeData = interfaceServ.getRealTimeData(assetId);

        for (AssetLinkAsset assetLinkAsset : list) {
            String portIndex = assetLinkAsset.getPortIndex();
            List<CollectInterfaces> collect = realTimeData.stream().filter(item -> portIndex.equals(item.getPortIndex())).collect(Collectors.toList());
            if(!collect.isEmpty()){
                Boolean nowIsUp = InterfaceStatus.isUp(collect.get(0).getStatus());
                assetLinkAsset.setPortStatus(nowIsUp?"1":"2");
            }
        }
        return ResultVoUtil.success(list);
    }


    @GetMapping("/queryPortIndex")
    @ResponseBody
    @ApiOperation(value = "查询设备端口信息")
    public ResultVo queryPortIndex(String assetId) {
        GraphController bean = SpringContextUtil.getBean(GraphController.class);
        return bean.queryPortIndex(assetId);
    }

    /**
     * 导出对端设备模板
     *
     * @return
     */
    @GetMapping("/exportManualExecl")
    @ApiOperation(value = "导出对端设备模板")
    public void exportManualExecl(HttpServletResponse response) {

        try {
            ClassPathResource resource = new ClassPathResource("templates/system/export/manual.xlsx");
            InputStream is = resource.getInputStream();
            SXSSFWorkbook sheets = new SXSSFWorkbook(new XSSFWorkbook(is));
            DispatchRecordExcelUtil.responseBody(sheets, response, "对端信息导入");
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }


    /**
     * 导出对端设备信息
     *
     * @return
     */
    @PostMapping("/exportManual")
    @ApiOperation(value = "导出对端设备信息")
    public void exportManual(HttpServletResponse response, @RequestBody ExportManualReq req) {

        List<LinkAssetExportVo> exportList = linkAssetService.exportManualList(req);
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("assetIp", "资产IP");
        writer.addHeaderAlias("assetName", "资产名称");
        writer.addHeaderAlias("portIndex", "端口名称");
        writer.addHeaderAlias("assetModeStr", "资产类型");
        writer.addHeaderAlias("linkAssetIp", "对端资产IP");
        writer.addHeaderAlias("linkAssetName", "对端资产名称");
        writer.addHeaderAlias("linkPort", "对端端口");
        writer.addHeaderAlias("remark", "备注信息");

        LinkAssetExportVo vo = new LinkAssetExportVo();
        vo.setAssetIp("assetIp");
        vo.setPortIndex("portName");
        vo.setLinkAssetIp("linkAssetIp");
        vo.setLinkAssetName("linkAssetName");
        vo.setLinkPort("linkPort");
        vo.setRemark("remark");

        exportList.add(0, vo);

        writer.write(exportList, true);

        String fileName = "资产连接信息-" + DateUtil.formatDate(new Date());
        try {
            String utf8FileName = URLEncoder.encode(fileName, "utf8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename= " + utf8FileName + ".xlsx");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, "导出对端设备信息", e);
        }
    }


    /**
     * 自动导入对端设备信息
     *
     * @return
     */
    @PostMapping("/importManual")
    @ApiOperation(value = "表格导入对端设备信息")
    @ActionLog(name = "表格导入对端设备信息", title = "资产详情", key = LogTypeConstant.ADD)
    public ResultVo importManual(@RequestParam("file") MultipartFile file) {
        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }

            InputStream in = file.getInputStream();
            ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(in);
            //判断模板是否含有必需字段
            List<Object> titleList = reader.readRow(1);
            ArrayList<String> list = new ArrayList<>();
            list.add("assetIp");
            list.add("portName");
            list.add("linkAssetIp");
            list.add("linkAssetName");
            list.add("linkPort");
            if (!titleList.containsAll(list)) {
                throw new NullFieldException("模板不符合规范 请重新下载对端设备模板");
            }
            //导入设备
            List<AssetManualVo> dataList = reader.read(1, 2, AssetManualVo.class);
            if (dataList.isEmpty()) {
                throw new NullFieldException("模板不可为空,请填写数据");
            }
            //返回验证失败的记录
            List<AssetManualVo> repositories = importManualList(dataList);

            if (repositories.isEmpty()) {
                return ResultVoUtil.success("全部导入成功", null);
            }
            return ResultVoUtil.success(dataList.size() + "条数据，共" + repositories.size() + "条失败 请查看详细日志", repositories);
        } catch (NullFieldException e) {
            return ResultVoUtil.error(e.toString());
        } catch (IOException e) {
            return ResultVoUtil.error("IO连接发生错误" + e.toString());
        } catch (Exception e) {
            return ResultVoUtil.error("导入失败" + e.toString());
        }
    }

    private List<AssetManualVo> importManualList(List<AssetManualVo> dataList) {
        List<AssetManualVo> arrayList = new ArrayList<>();
        for (AssetManualVo assetManualVo : dataList) {
            String assetId = "";
            if (StrUtil.isNotEmpty(assetManualVo.getAssetIp())) {
                if (!AppPattenUtils.isIp(assetManualVo.getAssetIp()) || assetManualVo.getAssetIp().length() > 16) {
                    assetManualVo.setLog("资产IP格式不正确");
                    arrayList.add(assetManualVo);
                    continue;
                }

                if (StrUtil.isNotEmpty(assetManualVo.getLinkAssetIp())) {
                    assetManualVo.setLinkAssetIp(assetManualVo.getLinkAssetIp().trim());
                    if (!AppPattenUtils.isIp(assetManualVo.getLinkAssetIp()) || assetManualVo.getLinkAssetIp().length() > 16) {
                        assetManualVo.setLog("对端IP格式不正确");
                        arrayList.add(assetManualVo);
                        continue;
                    }
                }

                if (StrUtil.isNotEmpty(assetManualVo.getLinkAssetName())) {
                    assetManualVo.setLinkAssetName(assetManualVo.getLinkAssetName().trim());
                    if (assetManualVo.getLinkAssetName().length() > 40) {
                        assetManualVo.setLog("对端资产名称过长");
                        arrayList.add(assetManualVo);
                        continue;
                    }
                }
                if (StrUtil.isNotEmpty(assetManualVo.getLinkPort())) {
                    assetManualVo.setLinkPort(assetManualVo.getLinkPort().trim());
                    if (assetManualVo.getLinkPort().length() > 30) {
                        assetManualVo.setLog("对端端口名称过长");
                        arrayList.add(assetManualVo);
                        continue;
                    }
                }
                assetManualVo.setAssetIp(assetManualVo.getAssetIp().trim());
                Asset asset = assetServ.findOneByIp(assetManualVo.getAssetIp().trim());
                if (ObjectUtil.isNull(asset)) {
                    assetManualVo.setLog("找不到 [" + assetManualVo.getAssetIp() + "] 对应的设备");
                    arrayList.add(assetManualVo);
                    continue;
                }
                assetId = asset.getId();
            } else {
                assetManualVo.setLog("资产IP不可为空");
                arrayList.add(assetManualVo);
                continue;
            }

            if (StrUtil.isNotEmpty(assetManualVo.getPortName())) {
                assetManualVo.setPortName(assetManualVo.getPortName().trim());
                AssetLinkAsset linkAssetByAsset = linkAssetService.findLinkAssetByAsset(assetId, assetManualVo.getPortName());
                if (ObjectUtil.isNull(linkAssetByAsset)) {
                    assetManualVo.setLog("找不到对应端口 [" + assetManualVo.getPortName() + "]");
                    arrayList.add(assetManualVo);
                    continue;
                }
                assetManualVo.setId(linkAssetByAsset.getId());
            } else {
                assetManualVo.setLog("端口名称不可为空");
                arrayList.add(assetManualVo);
                continue;
            }

            ResultVo resultVo = saveManual(assetManualVo);
            if (resultVo.getCode() != ResultVoUtil.SAVE_SUCCESS.getCode()) {
                assetManualVo.setLog(resultVo.getMsg());
                arrayList.add(assetManualVo);
                continue;
            }
        }
        return arrayList;


    }


    /**
     * 手动配置对端设备信息
     *
     * @return
     */
    @PostMapping("/saveManual")
    @ApiOperation(value = "手动配置对端设备信息")
    @ActionLog(name = "手动修改对端设备信息", title = "资产详情", key = LogTypeConstant.ADD)
    public ResultVo saveManual(@RequestBody AssetManualVo vo) {

        String linkAssetIp = vo.getLinkAssetIp();
        String linkPort = vo.getLinkPort();
        String linkAssetName = vo.getLinkAssetName();
        String id = vo.getId();
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.warning("缺少参数");
        }

        if (StrUtil.isNotEmpty(linkAssetIp)) {
            if (!AppPattenUtils.isIp(linkAssetIp)) {
                return ResultVoUtil.warning("对端IP格式不正确");
            }
        }

        AssetLinkAsset one = linkAssetService.getById(id);
        if (Objects.isNull(one)) {
            return ResultVoUtil.warning("数据已被删除，请刷新页面后重新操作");
        }
        one.setMaOrAt(AssetLinkConst.MANUAL);
        one.setLinkAssetIp(linkAssetIp);
        one.setLinkPort(linkPort);
        one.setLinkAssetName(linkAssetName);

        if (StrUtil.isNotEmpty(linkAssetIp)) {
            Asset asset = assetServ.getOneByAllIp(linkAssetIp);
            //如果是主机应该从网卡中找
            if (Objects.nonNull(asset)) {
                one.setLinkAssetId(asset.getId());
                if (StrUtil.isEmpty(linkAssetName)) {
                    one.setLinkAssetName(asset.getName());
                }
            } else {
                CollectNetworkCard netCard = netWorkServ.getOneByIp(linkAssetIp);
                if (Objects.nonNull(netCard)) {
                    one.setLinkAssetId(netCard.getAssetId());
                    if (StrUtil.isEmpty(linkAssetName)) {
                        one.setLinkAssetName(netCard.getName());
                    }
                }
            }
        }

        linkAssetService.updateById(one);

        return ResultVoUtil.SAVE_SUCCESS;
    }


}
