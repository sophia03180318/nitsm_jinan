package com.jcca.web.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.SysModuleConfigConst;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.*;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAppServer;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetAppServerService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetOutVo;
import com.jcca.web.asset.vo.AssetProcessVo;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.controller.bean.StationTopoBody;
import com.jcca.web.common.controller.req.AssetOutReq;
import com.jcca.web.common.controller.req.ProcessOutReq;
import com.jcca.web.common.dao.OutMapper;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.common.service.bean.TestPerformanceTarget;
import com.jcca.web.common.vo.AssetCollectTestVo;
import com.jcca.web.common.vo.AssetTestResult;
import com.jcca.web.common.vo.ProcessOnChangeVo;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.graph.entity.*;
import com.jcca.web.graph.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @ClassName OutServiceImpl
 * @Description ITSM调用外部应用接口
 * @Date 2020/7/31 11:31
 * @Author hanwone
 */
@Service
public class OutServiceImpl implements OutService {

    public static String ERROR_FLAG = "COLLECT_ERROR";


    @Resource
    private OutMapper outMapper;
    @Resource
    private StationService stationService;
    @Resource
    private ManageDbService manageDbService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetService assetService;
    @Resource(name = "ThresholdProcess")
    private ThresholdProcess thresholdProcessService;
    @Resource
    private CollectAgent collectAgent;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private TopoAssetGroupService topoAssetGroupServ;
    @Resource
    private TopoAssetMarkService topoAssetMarkServ;
    @Resource
    private TopoEdgeService topoEdgeServ;
    @Resource
    private TopoPointsService topoPointsServ;
    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private AssetAppServerService appServerService;


    @Override
    public ResultVo queryStationTopo(String stationIp) throws UnsupportedEncodingException {
        List<Station> stationList = stationService.findByIp(stationIp);
        if (stationList.isEmpty()) {
            return ResultVoUtil.error("车站IP不存在");
        }
        Station station = stationList.get(0);

        StationTopoBody body = new StationTopoBody();

        QueryWrapper<TopoAssetGroup> groupQuery = new QueryWrapper<>();
        groupQuery.eq("ORG_ID", station.getOrgId());

        QueryWrapper<TopoAssetMark> markQuery = new QueryWrapper<>();
        markQuery.eq("ORG_ID", station.getOrgId());

        QueryWrapper<TopoEdge> edgQuery = new QueryWrapper<>();
        edgQuery.eq("ORG_ID", station.getOrgId());

        QueryWrapper<TopoPoints> pointQuery = new QueryWrapper<>();
        pointQuery.eq("ORG_ID", station.getOrgId());

        QueryWrapper<TopoVertex> vertexQuery = new QueryWrapper<>();
        vertexQuery.eq("ORG_ID", station.getOrgId());

        List<TopoAssetGroup> groupList = topoAssetGroupServ.list(groupQuery);
        List<TopoAssetMark> markList = topoAssetMarkServ.list(markQuery);
        for (TopoAssetMark topoAssetMark : markList) {
            if (Objects.nonNull(topoAssetMark.getContent())) {
                topoAssetMark.setContentStr(new String(topoAssetMark.getContent(), StandardCharsets.UTF_8));
                topoAssetMark.setContent("".getBytes());
            }
        }
        for (TopoAssetGroup topoAssetGroup : groupList) {
            if (Objects.nonNull(topoAssetGroup.getContent())) {
                topoAssetGroup.setContentStr(new String(topoAssetGroup.getContent(), StandardCharsets.UTF_8));
                topoAssetGroup.setContent("".getBytes());
            }
        }
        List<TopoEdge> edgList = topoEdgeServ.list(edgQuery);
        List<TopoPoints> pointList = topoPointsServ.list(pointQuery);
        List<TopoVertex> vertexList = topoVertexService.list(vertexQuery);
        body.setGroupList(groupList);
        body.setEdgeList(edgList);
        body.setMarkList(markList);
        body.setPointsList(pointList);
        body.setVertexList(vertexList);

        return ResultVoUtil.success(body);
    }

    /**
     * 采集指标变动
     */
    @Override
    public void targetOnChange() {
        try {
            String resp = collectAgent.sendPostToCenter(OutConst.NOTIFY_ONCHANGE_TARGET, "", 120 * 1000);
            AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECT_CONFIG, resp, "指标变动通知中心采集器");
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_CONFIG, "指标变动通知中心采集器异常", e);
        }
    }

    @Override
    public ResultVo testPerformanceTarget(Asset asset, PerformanceTarget performanceTarget) {
        TestPerformanceTarget target = new TestPerformanceTarget();
        target.setAsset(asset);
        target.setTargetList(Arrays.asList(performanceTarget));
        try {
            String resp = collectAgent.sendPostToCenter(OutConst.TEST_TARGET, JSONUtil.toJsonStr(target), 180 * 1000);
            JSONObject respJson = JSONUtil.parseObj(resp);
            RestBean restBean = JSONUtil.toBean(respJson, RestBean.class);
            if(RestBean.SUCCESS.equals(restBean.getCode())){
                return ResultVoUtil.success(restBean.getBody());
            }
            return ResultVoUtil.error(restBean.getMsg());
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.COLLECT_CONFIG, "验证指标中心采集器异常", e);
        }

        return ResultVoUtil.error("指标验证失败-网络异常");
    }


    /**
     * 外部接口获取资产列表
     *
     * @param req
     * @return
     */
    @Override
    public List<AssetOutVo> findForOutRequest(AssetOutReq req) {

        List<AssetOutVo> assetOutVoList = outMapper.findForOutRequest(req);

        // 查询已配置的车站
        List<String> orgIdList = assetOutVoList.stream().map(AssetOutVo::getOrgId).collect(Collectors.toList());
        // 去重
        List<String> orgIds = orgIdList.stream().distinct().collect(Collectors.toList());
        orgIds.add("x");
        QueryWrapper<Station> query = Wrappers.query();
        query.in("org_id", orgIds);
        List<Station> stationList = stationService.list(query);
        if (CollUtil.isNotEmpty(stationList)) {
            for (Station station : stationList) {
                String orgId = station.getOrgId();
                for (AssetOutVo outVo : assetOutVoList) {
                    if (orgId.equals(outVo.getOrgId())) {
                        outVo.setStationIp(station.getIp());
                        outVo.setStationPort(station.getPort());
                    }
                }
            }
        }

        // 判断assetId 查询数据库
        String reqAssetId = req.getAssetId();
        if (StrUtil.isNotEmpty(reqAssetId)) {
            if (CollUtil.isNotEmpty(assetOutVoList)) {
                return assetOutVoList;
            }
            ManageDb manageDb = manageDbService.getById(reqAssetId);
            if (Objects.nonNull(manageDb)) {
                Asset asset = assetService.getById(manageDb.getAssetId());
                AssetOutVo outVo = this.setVo(manageDb, BeanUtil.copyProperties(asset, AssetOutVo.class));
                assetOutVoList.add(outVo);
                return assetOutVoList;
            }
        }

        // 当为中心时，查询中心的数据库
        List<AssetOutVo> resultList = new ArrayList<>();
        List<ManageDb> dbList = manageDbService.list();
        if (StrUtil.isEmpty(reqAssetId) && req.getType() == OrgTypeConst.CENTER) {
            for (ManageDb db : dbList) {
                String assetId = db.getAssetId();
                for (AssetOutVo vo : assetOutVoList) {
                    if (assetId.equals(vo.getId())) {
                        resultList.add(this.setVo(db, vo));
                        break;
                    }
                }
            }
        }

        // 当车站设备采集配置为无车站采集器模式时,中心采集器获取中心资产时,要将车站的设备追加上去
        String configValue = configService.getStationCollectorMode();
        if (StrUtil.isNotEmpty(configValue) && configValue.equals(SysModuleConfigConst.NoStationCollectMode)) {
            if (StrUtil.isEmpty(reqAssetId) && req.getType() == OrgTypeConst.CENTER) {
                AssetOutReq stationReq = new AssetOutReq();
                stationReq.setType(Convert.toInt(OrgTypeConst.STATION));
                List<AssetOutVo> stationAssetOutVoList = outMapper.findForOutRequest(stationReq);
                resultList.addAll(stationAssetOutVoList);

                for (ManageDb db : dbList) {
                    String assetId = db.getAssetId();
                    for (AssetOutVo vo : stationAssetOutVoList) {
                        if (assetId.equals(vo.getId())) {
                            resultList.add(this.setVo(db, vo));
                            break;
                        }
                    }
                }
            }
        }
        resultList.addAll(assetOutVoList);

        for (AssetOutVo assetOutVo : resultList) {
            if (StrUtil.isEmpty(assetOutVo.getOsPassword())) {
                assetOutVo.setOsPassword(EncryptUtil.aesEncryptHex("123"));
            }
            if (assetOutVo.getServiceType() != null && assetOutVo.getServiceType() == 1) {
                List<AssetAppServer> list = appServerService.findByAssetIdNullLink(assetOutVo.getId());
                Set<Integer> collect = list.stream().map(AssetAppServer::getServerPort).collect(Collectors.toSet());
                assetOutVo.setSsPortSet(collect);
            }
        }

        return resultList;
    }

    private AssetOutVo setVo(ManageDb db, AssetOutVo vo) {
        AssetOutVo outVo = new AssetOutVo();
        outVo.setId(db.getId());
        outVo.setName(db.getDbName());
        outVo.setIp(vo.getIp());
        outVo.setPort(db.getPort());
        outVo.setOsUser(db.getUsername());
        outVo.setOsPassword(db.getPassword());
        outVo.setAssetMode(AssetModeConst.DB);

        outVo.setManufacturerId(db.getManufacturerId());
        outVo.setCollectionType(db.getDbProtocol());
        outVo.setAssetImage(db.getAssetImage());

        outVo.setOrgId(vo.getOrgId());
        outVo.setAssetCode(vo.getAssetCode());
        return outVo;
    }

    /**
     * 资产变动通知外部应用
     *
     * @param optFlag 0:增,1:删,2:改
     * @param asset   变动的资产
     */
    @Override
    public void notifyOnChange(Integer optFlag, Asset asset) {
        if (StrUtil.isEmpty(asset.getOsPassword())) {
            asset.setOsPassword(EncryptUtil.aesEncryptHex("123"));
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, "资产变动通知外部应用参数：" + optFlag, JSONUtil.toJsonStr(asset));
        //判定是车站还是中心
        SysOrg org = orgService.getById(asset.getOrgId());
        if (Objects.isNull(org)) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_CHANGE, "资产变动通知外部应用失败：资产缺少组织ID！", JSONUtil.toJsonStr(asset));
            return;
        }
        AssetOutVo assetOutVo = BeanUtil.copyProperties(asset, AssetOutVo.class);
        assetOutVo.setABFlag(asset.getABFlag());
        if (asset.getServiceType() != null && asset.getServiceType() == 1) {
            List<AssetAppServer> list = appServerService.findByAssetIdNullLink(asset.getId());
            Set<Integer> collect = list.stream().map(AssetAppServer::getServerPort).collect(Collectors.toSet());
            assetOutVo.setSsPortSet(collect);
        }

        JSONObject reqBody = new JSONObject();
        reqBody.put("optFlag", optFlag);
        reqBody.put("asset", JSONUtil.parseObj(assetOutVo));
        if (org.getType() == OrgTypeConst.CENTER) {
            //中心设备
            try {
                String resp = collectAgent.sendPostToCenter(OutConst.NOTIFY_ONCHANGE_ASSET, reqBody.toString(), 120 * 1000);
                AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, "资产变动通知中心采集器返回消息", resp);
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ASSET_CHANGE, "资产变动通知中心采集器异常", e);
            }
        } else if (org.getType() == OrgTypeConst.STATION) {
            //车站设备
            String resp = stationService.sendPostToStation(OutConst.NOTIFY_ONCHANGE_ASSET, asset.getOrgId(), reqBody.toString());
            AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, "资产变动通知车站采集器返回消息", resp);
        }
    }

    /**
     * 进程变动通知
     *
     * @throws Exception
     */
    @Override
    public void processOnChange(ProcessOnChangeVo changeVo) throws Exception {
        String validateReq = ValidatorUtils.validateReq(changeVo);
        if (StrUtil.isNotEmpty(validateReq)) {
            AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, "进程变动通知采集器失败", validateReq);
            throw new Exception(validateReq);
        }
        Asset asset = assetService.getById(changeVo.getAssetId());
        SysOrg org = orgService.getById(asset.getOrgId());
        if (Objects.isNull(org)) {
            AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, "进程变动通知采集器失败:资产缺少组织ID", changeVo);
            return;
        }

        String resp = "";
        if (org.getType() == OrgTypeConst.CENTER) {
            //中心设备
            try {
                resp = collectAgent.sendPostToCenter(OutConst.NOTIFY_ONCHANGE_PROCESS, JSONUtil.parseObj(changeVo).toString(), 20 * 1000);
                AppLogUtils.buildLogInfo(LogFunctionEnum.PROCESS_CONFIG, "进程变动通知中心采集器返回消息", resp);
            } catch (CollectAgencyException e) {
                throw new Exception("进程变动通知中心采集器失败:" + e.toString());
            }
        } else if (org.getType() == OrgTypeConst.STATION) {
            //车站设备
            resp = stationService.sendPostToStation(OutConst.NOTIFY_ONCHANGE_PROCESS, asset.getOrgId(), JSONUtil.parseObj(changeVo).toString());
            AppLogUtils.buildLogInfo(LogFunctionEnum.PROCESS_CONFIG, "进程变动通知车站采集器返回消息", resp);
        }

        if (!JSONUtil.isJson(resp)) {
            throw new Exception("进程变动通知外部应用失败：" + resp);
        }
        JSONObject bodyJson = JSONUtil.parseObj(resp);
        String code = bodyJson.getStr("code");
        String msg = bodyJson.getStr("msg");

        if ("1".equals(code)) {
            throw new Exception("通知采集器进程变动失败code:" + code + ";msg:" + msg);
        }

    }

    /**
     * 测试资产采集指标
     *
     * @return
     */
    @Override
    public AssetCollectTestVo collectTest(AssetOutVo assetOutVo) {
        if (StrUtil.isEmpty(assetOutVo.getOsPassword())) {
            assetOutVo.setOsPassword(EncryptUtil.aesEncryptHex("123"));
        }

        SysOrg org = orgService.getById(assetOutVo.getOrgId());
        if (Objects.isNull(org)) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, "测试资产采集指标失败：资产缺少组织ID！", assetOutVo);
            AssetCollectTestVo vo = new AssetCollectTestVo();
            vo.setCode("1");
            vo.setMsg("资产缺少组织ID");
            return vo;
        }

        JSONObject reqBody = new JSONObject();
        reqBody.put("asset", JSONUtil.parseObj(assetOutVo));

        String resp = "";
        if (org.getType() == OrgTypeConst.CENTER) {
            //中心设备
            try {
                resp = collectAgent.sendPostToCenter(OutConst.ASSET_TEST_URI_V2, reqBody.toString(), 300 * 1000);
                AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_MANAGE, "测试资产采集指标中心采集器返回消息", resp);
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, "测试资产采集指标中心采集器异常", e.getMessage());
                AssetTestResult item = new AssetTestResult();
                item.setState(AssetTestResult.ERRO_CODE);
                item.setErrorMsg("测试资产采集指标异常" + e.toString());
                item.setTargetDescription("请求中心采集器异常");

                AssetCollectTestVo vo = new AssetCollectTestVo();
                vo.setCode(AssetCollectTestVo.ERRO_CODE);
                vo.setTestResultList(Collections.singletonList(item));
                vo.setMsg("网络异常");
                return vo;
            }
        } else if (org.getType() == OrgTypeConst.STATION) {
            //车站设备
            resp = stationService.sendPostToStation(OutConst.ASSET_TEST_URI, assetOutVo.getOrgId(), reqBody.toString());
            AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_MANAGE, "测试资产采集指标车站采集器返回消息", resp);
        }

        if (!JSONUtil.isJson(resp)) {
            AssetTestResult item = new AssetTestResult();
            item.setState(AssetTestResult.ERRO_CODE);
            item.setErrorMsg(resp);
            item.setTargetDescription("请求采集器出错");

            AssetCollectTestVo vo = new AssetCollectTestVo();
            vo.setCode(AssetCollectTestVo.ERRO_CODE);
            vo.setTestResultList(Collections.singletonList(item));
            vo.setMsg("错误信息：" + resp);
            return vo;
        }

        return JSONUtil.toBean(resp, AssetCollectTestVo.class);
    }

    /**
     * 获取资产所有进程信息
     *
     * @param assetId
     * @return
     * @throws Exception
     */
    @Override
    public List<AssetProcessVo> getAllProcess(String assetId) throws Exception {
        Asset asset = assetService.getById(assetId);
        SysOrg org = orgService.getById(asset.getOrgId());
        if (Objects.isNull(org)) {
            AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, "获取资产所有的进程失败：资产缺少组织ID！", assetId);
            throw new Exception("获取资产所有的进程失败：资产缺少组织ID！");
        }
        JSONObject reqJson = new JSONObject();
        reqJson.put("assetId", assetId);

        String resp = "";
        if (org.getType() == OrgTypeConst.CENTER) {
            //中心设备
            try {
                resp = collectAgent.sendPostToCenter(OutConst.ALL_PROCESS_URI, reqJson.toString(), 180 * 1000);
                AppLogUtils.buildLogInfo(LogFunctionEnum.PROCESS_CONFIG, "获取中心资产所有进程返回消息", resp);
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, "获取中心资产所有的进程异常", e.getMessage());
                throw new Exception("获取中心资产所有的进程失败:" + e.toString());
            }
        } else if (org.getType() == OrgTypeConst.STATION) {
            //车站设备
            resp = stationService.sendPostToStation(OutConst.ALL_PROCESS_URI, asset.getOrgId(), reqJson.toString());
            AppLogUtils.buildLogInfo(LogFunctionEnum.PROCESS_CONFIG, "获取车站资产所有进程返回消息", resp);
        }

        if (!JSONUtil.isJsonArray(resp)) {
            throw new Exception("获取进程失败:" + resp);
        }

        return JSONUtil.toList(JSONUtil.parseArray(resp), AssetProcessVo.class);
    }

    /**
     * 获取中心资产的进程
     *
     * @param req:
     * @return: java.util.List<com.jcca.web.asset.entity.ThresholdProcess>
     * @Author: syt
     * @Date: 2021/8/3/003 10:30
     */
    @Override
    public List<ThresholdProcess> getProcessByType(ProcessOutReq req) {
        // 根据所查得的资产查询相关进程
        QueryWrapper<ThresholdProcess> query = Wrappers.query();
        ArrayList<String> assetIds = new ArrayList<>();

        List<List<String>> inSplit = AppListUtils.inSplit(assetIds, 900);

        Consumer<QueryWrapper<ThresholdProcess>> consumer = null;
        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = wrapper -> wrapper.in("ASSET_ID", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<ThresholdProcess>> after = wrapper -> wrapper.or().in("ASSET_ID", list);
                consumer = consumer.andThen(after);
            }
        }
        if (Objects.nonNull(consumer)) {
            query.and(consumer);
        }

        List<ThresholdProcess> thresholdProcesses = thresholdProcessService.selectList(query);
        boolean notEmpty = CollectionUtil.isNotEmpty(thresholdProcesses);
        return notEmpty ? thresholdProcesses : null;
    }

    /**
     * 分组获取车站资产信息
     *
     * @param req
     * @return
     */
    @Override
    public List<AssetOutVo> findStationAssetByGroup(AssetOutReq req) {
        List<AssetOutVo> findStationAssetByGroup = outMapper.findStationAssetByGroup(req);
        for (AssetOutVo asset : findStationAssetByGroup) {
            if (StrUtil.isEmpty(asset.getOsPassword())) {
                asset.setOsPassword(EncryptUtil.aesEncryptHex("123"));
            }
        }
        return findStationAssetByGroup;
    }


    @Override
    public BusinessGetSnmpResultResp getSnmpResult(BusinessGetSnmpResultReq req) throws Exception {
        try {
            AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "中心采集器执行SNMP请求", req);
            String resp = collectAgent.sendPostToCenter("/business/getSnmpResult", JSONUtil.toJsonStr(req), 180 * 1000);
            AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "中心采集器执行SNMP响应", resp);
            return JSONUtil.toBean(JSONUtil.parseObj(resp), BusinessGetSnmpResultResp.class);
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ITSM_TO_COLLECTOR, "远程执行snmp异常", e.getMsg());
            throw new Exception(e.toString());
        }

    }

    @Override
    public String getSSHResult(Asset asset, String command) throws Exception {
        JSONObject reqJson = new JSONObject();
        reqJson.put("ip", asset.getIp());
        reqJson.put("port", asset.getLoginPort());
        reqJson.put("userName", asset.getOsUser());
        reqJson.put("password", asset.getOsPassword());
        reqJson.put("command", command);

        try {
            AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "中心采集器执行SSH请求", reqJson);
            String resp = collectAgent.sendPostToCenter("/business/getSshResult", reqJson.toString(), 180 * 1000);
            AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "中心采集器执行SSH响应", resp);
            JSONObject respObj = JSONUtil.parseObj(resp);
            if ("0".equals(respObj.getStr("code"))) {
                return respObj.getStr("result");
            }
            throw new Exception(respObj.getStr("msg"));
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ITSM_TO_COLLECTOR, "远程执行SSH异常", e.getMsg());
            throw new Exception(e.toString());
        }

    }

    @Override
    public List<String> getTelnetResult(Asset asset, Collection<String> commandList) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("ip", asset.getIp());
        reqJson.put("port", asset.getLoginPort());
        if (StrUtil.isNotEmpty(asset.getLoginName())) {
            reqJson.put("userName", asset.getLoginName());
        }
        reqJson.put("password", asset.getLoginPwd());
        if (StrUtil.isNotEmpty(asset.getOsPassword())) {
            reqJson.put("enablePassword", asset.getOsPassword());
        }
        reqJson.put("commandList", commandList);

        try {
            AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "中心采集器执行telnet请求", reqJson);
            String resp = collectAgent.sendPostToCenter("/business/getTelnetResult", reqJson.toString(), 180 * 1000);
            AppLogUtils.buildLogInfo(LogFunctionEnum.ITSM_TO_COLLECTOR, "中心采集器执行telnet响应", resp);
            JSONObject respObj = JSONUtil.parseObj(resp);
            if ("0".equals(respObj.getStr("code"))) {
                JSONArray resultList = respObj.getJSONArray("resultList");
                return JSONUtil.toList(resultList, String.class);
            }
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ITSM_TO_COLLECTOR, "远程执行telnet异常", e.getMsg());
        }
        return getErrorResultList(commandList, ERROR_FLAG);
    }

    /**
     * 异常的时候批量返回结果
     *
     * @param commandList
     * @return
     */
    private List<String> getErrorResultList(Collection<String> commandList, String errorMsg) {
        List<String> result = new ArrayList<String>();
        for (String command : commandList) {
            result.add(errorMsg);
        }
        return result;
    }

    @Override
    public String findMasterOrSlaveFlag(String assetId, String tabName) {
        Integer num = outMapper.findMasterOrSlaveTab(tabName);
        if (num < 1) {
            return "--";
        }
        List<String> flagList = outMapper.findMasterOrSlave(assetId);
        if (Objects.isNull(flagList) || flagList.isEmpty()) {
            return "--";
        }
        return flagList.get(0);
    }

}
