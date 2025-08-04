package com.jcca.web.common.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.biz.service.StationVersionLogService;
import com.jcca.admin.system.config.bean.SysModuleConfigReq;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.SysFolder;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.admin.system.service.SysFolderService;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.enums.FolderCategoryEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.*;
import com.jcca.web.asset.service.*;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.asset.utils.enums.ManufacturersEnum;
import com.jcca.web.asset.vo.AssetOutVo;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectDiskService;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.common.config.ProjectVersionConf;
import com.jcca.web.common.controller.bean.*;
import com.jcca.web.common.controller.req.AppAlarmDataReq;
import com.jcca.web.common.controller.req.AssetOutReq;
import com.jcca.web.common.controller.req.ProcessOutReq;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.vo.AssetCodeAndProcessNameVo;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.service.AlarmEventGroupService;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web2.entity.AssetManufacturer;
import com.jcca.web2.service.AssetManufacturerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ApiOutController
 * @Description 提供外部免密调用接口
 * @Date 2020/7/30 16:33
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/free")
@Api(tags = "外部免密接口")
public class ApiOutController {

    /**
     * 正常资产标识
     */
    private static final Byte NORMAL = 1;

    @Resource
    private OutService outService;
    @Resource
    private RedisService redisService;
    @Resource
    private ProjectVersionConf projectVersionConf;
    @Resource
    private AssetService assetSeerv;
    @Resource
    private AssetAttachService assetAttachServ;
    @Resource
    private AlarmInfoService alarmInfoServ;

    @Resource
    private CabinetService cabInetServ;
    @Resource
    private RoomService roomServ;
    @Resource
    private SysOrgService orgServ;

    @Resource
    private SysModuleConfigService configService;
    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private StationService stationServ;
    @Resource
    private StationVersionLogService stationVersionLogServ;
    @Resource
    private CollectSystemTimeService sysTimeServ;
    @Resource
    private CollectCpuService cpuService;
    @Resource
    private CollectMemoryService memoryServ;
    @Resource
    private CollectDiskService diskServ;
    @Resource
    private IpInfoService ipInfoServ;

    @Resource
    private SysFileService fileService;
    @Resource
    private SysFolderService folderService;
    @Resource
    private AlarmEventGroupService alarmEventGroupServ;

    @Resource
    private UploadProjectProperties fileProp;
    @Resource
    private AssetManufacturerService assetManufacturerService;


    /**
     * AI获取指定类型设备列表
     *
     * @return
     */
    @GetMapping("/getAssetInfo/{type}/{value}")
    public ResultVo getAssetInfo(@PathVariable String type, @PathVariable String value) {
        switch (type) {
            case "1":
                int desk = 0;
                try {
                    desk = Integer.parseInt(DictUtil.getKey("ASSET_MODE", value.trim()));
                } catch (Exception e) {
                    return ResultVoUtil.success("", value + "：系统未知类型");
                }
                QueryWrapper<Asset> qw = new QueryWrapper<>();
                qw.eq("DESK", desk);
                qw.eq("IS_DEL", 1);
                String list = assetSeerv.list(qw).stream().map(a -> {
                    return a.getName() + "[" + a.getIp() + "]";
                }).collect(Collectors.joining("、"));
                return ResultVoUtil.success("", list);

            case "2":
                Asset asset = assetSeerv.getOneByAllIp(value);
                if (ObjectUtil.isNull(asset)) {
                    return ResultVoUtil.success("", "系统没有ip为[" + value + "]的设备");
                }
                return ResultVoUtil.success("", asset.getName() + ":[" + asset.getAssetImage() + "]");

            case "3":
                QueryWrapper<Asset> qw2 = new QueryWrapper<>();
                qw2.like("NAME", value);
                qw2.eq("IS_DEL", 1);
                String list1 = assetSeerv.list(qw2).stream().map(a -> {
                    return a.getName() + "[" + a.getIp() + "]";
                }).collect(Collectors.joining("、"));
                return ResultVoUtil.success("", list1);
        }
        return ResultVoUtil.success("", "");
    }


    /**
     * AI获取所有设备信息
     *
     * @return
     */
    @GetMapping("/getAssetInfo")
    public ResultVo getAssetInfo() {
        QueryWrapper<Asset> qw2 = new QueryWrapper<>();
        qw2.eq("IS_DEL", 1);
        List<String> list = assetSeerv.list(qw2).stream().map(Asset::toString).collect(Collectors.toList());
        return ResultVoUtil.success("", list);
    }


    /**
     * AI获取指定告警信息
     *
     * @return
     */
    @GetMapping("/getAlarmInfo/{alarmId}")
    public ResultVo getAlarmInfo(@PathVariable String alarmId) {
        AlarmInfo alarm = alarmInfoServ.getById(alarmId);
        if (ObjectUtil.isNull(alarm)) {
            return ResultVoUtil.success("未找到指定告警");
        }
        return ResultVoUtil.success("", alarm.getDescription());
    }


    /**
     * 同步车站的TOPO
     *
     * @return
     */
    @PostMapping("/syncTopo")
    public ResultVo syncTopo(@RequestBody String req) {
        JSONObject reqBody = JSONUtil.parseObj(req);
        String stationIp = reqBody.getStr("stationIp");
        if (StrUtil.isEmpty(stationIp)) {
            return ResultVoUtil.error("缺少车站IP");
        }
        try {
            return outService.queryStationTopo(stationIp);
        } catch (UnsupportedEncodingException e) {
            return ResultVoUtil.error(e.getMessage());
        }
    }

    @PostMapping("/verifyFilePath")
    public ResultVo verifyFilePath(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String fileId = reqJson.getStr("fileId");
        if (StrUtil.isEmpty(fileId)) {
            return ResultVoUtil.error("缺少文件ID传参");
        }

        SysFile file = fileService.getById(fileId);
        if (Objects.isNull(file)) {
            return ResultVoUtil.error("文件ID不存在");
        }
        String pathname = fileProp.getFilePath() + file.getFilePath().replace(fileProp.getStaticPath(), "");
        pathname = pathname.replace("///", "/");
        File downFile = new File(pathname);
        if (!downFile.exists()) {
            AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, pathname, "下载文件不存在");
            return ResultVoUtil.error("文件已被删除：" + pathname);
        }

        return ResultVoUtil.success();
    }

    /**
     * 查询告警规则配置的所有标题
     *
     * @return
     */
    @GetMapping("/queryAlarmTitleList")
    public ResultVo queryAlarmTitleList() {
        QueryWrapper<AlarmEventGroup> queryWrapper = new QueryWrapper<AlarmEventGroup>();
        queryWrapper.select("id", "NAME");
        queryWrapper.orderByAsc("ID");
        List<AlarmEventGroup> groupList = alarmEventGroupServ.list(queryWrapper);
        return ResultVoUtil.success(groupList);
    }

    @GetMapping("/queryAssetSearchList")
    public ResultVo queryAssetSearchList() {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("IS_DEL", StatusEnum.OK.getCode());
        queryWrapper.select("id", "NAME", "IP");
        queryWrapper.orderByAsc("ID");
        List<Asset> assetList = assetSeerv.list(queryWrapper);
        return ResultVoUtil.success(assetList);
    }

    @GetMapping("/queryIpSearchList")
    public ResultVo queryIpSearchList() {
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
        queryWrapper.select("id", "IP");
        queryWrapper.orderByAsc("IP");
        List<IpInfo> assetList = ipInfoServ.list(queryWrapper);
        return ResultVoUtil.success(assetList);
    }

    /**
     * 获取项目版本号及GIT版本号
     *
     * @return
     */
    @GetMapping("/version")
    @ApiOperation(value = "程序版本信息")
    public ResultVo<Object> version() {
        Map<String, Object> versionMap = new HashMap<>();
        versionMap.put("tags", projectVersionConf.getTags());
        versionMap.put("branch", projectVersionConf.getBranch());
        versionMap.put("version", projectVersionConf.getVersion());
        versionMap.put("commitId", projectVersionConf.getCommitId());
        return ResultVoUtil.success(versionMap);
    }

    /**
     * 0-不监控，1-监控
     *
     * @param assetId
     * @return
     */
    @PostMapping("/watch/{assetId}")
    @ApiOperation(value = "获取资产信息")
    public ResultVo watchAsset(@PathVariable("assetId") String assetId) {
        Asset asset = assetSeerv.getById(assetId);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.success(0);
        }
        if (NORMAL.equals(asset.getIsDel())) {
            return ResultVoUtil.success(asset.getWatch());
        }
        return ResultVoUtil.success(0);
    }

    /**
     * 外部接口获取资产
     *
     * @param req
     * @return
     */
    @PostMapping("/asset")
    @ApiOperation(value = "获取资产信息")
    public ResultVo asset(@Validated @RequestBody AssetOutReq req, HttpServletRequest request) {
        if (Objects.isNull(req)) {
            return null;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECTOR_TO_ITSM, "ITSM收到中心获取资产请求", req);
        if (StrUtil.isEmpty(req.getAssetId())) {
            if (Objects.isNull(req.getType())) {
                AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, req, "ITSM收到获取资产失败，资产ID和类型不能同时为空");
                return ResultVoUtil.error(ResultEnum.OUT_PARAM_LOST.getCode(), "资产ID和类型不能同时为空");
            }
        }
        List<AssetOutVo> assetOutVoList = outService.findForOutRequest(req);
        for (AssetOutVo assetOutVo : assetOutVoList) {
            if (StrUtil.isEmpty(assetOutVo.getOsPassword())) {
                assetOutVo.setOsPassword(EncryptUtil.aesEncryptHex("123"));
            }
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.COLLECTOR_TO_ITSM, "ITSM收到中心获取资产响应", "资产数量：" + assetOutVoList.size());
        return ResultVoUtil.success(assetOutVoList);
    }

    @PostMapping("/ip/asset/{ip}")
    @ApiOperation(value = "通过IP获取资产(组)的进程信息")
    public ResultVo getAssetByIp(@PathVariable("ip") String ip) {
        if (StrUtil.isEmpty(ip)) {
            return ResultVoUtil.error(ResultEnum.OUT_PARAM_LOST.getCode(), "资产IP不可为空");
        }
        String assetCode = assetSeerv.findOneByIp(ip).getAssetCode();
        if (StrUtil.isEmpty(assetCode)) {
            return ResultVoUtil.error(ResultEnum.OUT_PARAM_LOST.getCode(), "没有找到该IP对应的资产信息,请检查IP是否正确");
        }
        // 资产组
        List<Asset> gAssetList = assetSeerv.findGroupAssetByCode(assetCode);
        // 返回的资产code和进程名称
        List<AssetCodeAndProcessNameVo> tp = new ArrayList<>();

        for (Asset asset : gAssetList) {
            AssetCodeAndProcessNameVo acpn = new AssetCodeAndProcessNameVo();
            QueryWrapper query = new QueryWrapper();
            query.eq("ASSET_ID", asset.getId());
            ThresholdProcess one = thresholdProcessService.getOne(query);
            if (Objects.isNull(one)) {
                continue;
            }
            acpn.setProcessName(one.getProcessName());
            acpn.setAssetCode(asset.getAssetCode());
            acpn.setHostMode(one.getHostMode());
            acpn.setIp(asset.getIp());
            tp.add(acpn);
        }
        return ResultVoUtil.success(tp);
    }

    /**
     * 获取所有进程信息(就是表中所有数据)
     *
     * @return: com.jcca.common.vo.ResultVo<List <
            * com.jcca.web.collect.entity.CollectProcess>>
     * @Author: syt
     * @Date: 2021/7/29/029 11:29
     */
    @PostMapping("/collectProcess")
    @ApiOperation(value = "获取采集进程")
    public ResultVo process(@RequestBody ProcessOutReq req, HttpServletRequest request) {
        String ip = request.getRemoteHost() + ":" + request.getRemotePort();
        AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, ip, "采集器获取进程信息");
        // type not null
        if (Objects.isNull(req.getType())) {
            return ResultVoUtil.error(ResultEnum.OUT_PARAM_LOST.getCode(), "类型不能为空");
        }
        // 返回进程数据
        List<ThresholdProcess> thresholdProcesses = outService.getProcessByType(req);

        return ResultVoUtil.success(thresholdProcesses);
    }

    /**
     * 外部接口分组获取车站资产
     *
     * @param req
     * @return
     */
    @PostMapping("/station/asset")
    @ApiOperation(value = "分组获取车站资产信息")
    public ResultVo stationAsset(@RequestBody AssetOutReq req, HttpServletRequest request) {
        if (Objects.isNull(req)) {
            return null;
        }

        AppLogUtils.buildLogDebug(LogFunctionEnum.COLLECTOR_TO_ITSM, "ITSM收到车站获取资产请求", req);
        if (StrUtil.isEmpty(req.getStationGroup())) {
            AppLogUtils.buildLogWarn(LogFunctionEnum.COLLECTOR_TO_ITSM, req, "ITSM收到获取资产失败：请求缺少StationGroup参数");
            return ResultVoUtil.error(ResultEnum.OUT_PARAM_LOST.getCode(), ResultEnum.OUT_PARAM_LOST.getMessage());
        }

        List<AssetOutVo> assetOutVoList = outService.findStationAssetByGroup(req);
        AppLogUtils.buildLogDebug(LogFunctionEnum.COLLECTOR_TO_ITSM, "ITSM收到车站获取资产响应，" + JSONUtil.toJsonStr(req), "资产数量：" + assetOutVoList.size());

        for (AssetOutVo assetOutVo : assetOutVoList) {
            if (StrUtil.isEmpty(assetOutVo.getOsPassword())) {
                assetOutVo.setOsPassword(EncryptUtil.aesEncryptHex("123"));
            }
        }

        return ResultVoUtil.success(assetOutVoList);
    }

    /**
     * ITSM接收采集器告警
     */
    @PostMapping("/ping")
    @ApiOperation(value = "ITSM接收采集器告警")
    public ResultVo ping(@RequestBody ReceiveAlarmDto dto, HttpServletRequest request) {
        String ip = request.getRemoteHost() + ":" + request.getRemotePort();
        AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "ITSM接收采集器PING告警，采集器IP：" + ip, dto);
        redisService.convertAndSend(RedisQueueConst.ALARM_QUEUE, JSONUtil.toJsonStr(dto));
        return ResultVoUtil.success();
    }

    /**
     * 获取系统模块配置
     *
     * @param req
     * @return
     */
    @PostMapping("/getSysModuleConfig")
    @ApiOperation(value = "获取系统模块配置")
    public ResultVo getSysModuleConfig(@RequestBody SysModuleConfigReq req, HttpServletRequest request) {
        String ip = request.getRemoteHost() + ":" + request.getRemotePort();
        AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "获取系统模块配置IP：" + ip, req);
        if (req.getServiceType() == null) {
            return ResultVoUtil.error("服务类型参数不能为空");
        }
        List<SysModuleConfig> list = configService.getSysModuleConfigList(req);
        return ResultVoUtil.success(list);
    }

    /**
     * 按照指定key获取系统模块配置
     *
     * @param name 配置key
     * @return 配置项值
     */
    @GetMapping("/config/{name}")
    @ApiOperation(value = "按照指定名称获取系统模块配置(名称只需要config:后面数据)")
    public ResultVo<Object> config(@PathVariable("name") String name) {
        name = "config:" + name;
        Object o = 0;
        SysModuleConfig config = configService.getSysModuleConfig(name);
        if (Objects.nonNull(config)) {
            o = config.getValue();
        }

        return ResultVoUtil.success(o);
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/getImageList")
    @ApiOperation(value = "获取型号列表")
    public ResultVo getImageList(String model) {
        if (StrUtil.isEmpty(model)) {
            return ResultVoUtil.success(new ArrayList<String>());
        }
        Map<String, String> dictMap = DictUtil.value("ASSET_IMAGE");
        Set<String> keySet = dictMap.keySet();

        List<String> imageList = new ArrayList<String>();
        for (String key : keySet) {
            String value = dictMap.get(key);
            if (value.equals(model)) {
                imageList.add(key);
            }
        }

        return ResultVoUtil.success(imageList);
    }

    /**
     * 车站用来查询版本需要更新的最新的jar位置
     *
     * @param req
     * @return
     */
    @PostMapping("/queryJarList")
    @ResponseBody
    RestBean queryJarList(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String stationIp = reqJson.getStr("stationIp");
        if (StrUtil.isEmpty(stationIp)) {
            return RestBean.ofError("未传车站IP");
        }
        List<Station> stationList = stationServ.findByIp(stationIp);
        if (stationList.isEmpty()) {
            return RestBean.ofError("车站IP对应的车站不存在");
        }
        if (stationList.size() != 1) {
            return RestBean.ofError("车站IP对应有多个车站，无法查询");
        }
        Station station = stationList.get(0);

        StationJarQuertResp resp = new StationJarQuertResp();

        List<String> listJarName = stationVersionLogServ.listJarName(station.getOrgId());
        if (listJarName.isEmpty()) {
            resp.setJarPathList(new ArrayList<String>());
            return RestBean.ofSuccess(resp);
        }

        List<String> pathList = new ArrayList<String>();
        for (String jarName : listJarName) {
            StationVersionLog lastLog = stationVersionLogServ.lastLog(station.getOrgId(), jarName);
            String savePath = lastLog.getSavePath() + "/" + lastLog.getFutureVersion() + "/" + jarName;
            pathList.add(savePath);
        }

        resp.setJarPathList(pathList);

        return RestBean.ofSuccess(resp);
    }

    /**
     * 获取车站配置信息
     *
     * @param req
     * @return
     */
    @PostMapping("/getStationInfoByIp")
    @ResponseBody
    ResultVo getStationInfoByIp(@RequestBody JSONObject req) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "收到获取车站配置信息请求", req);
        String stationIp = req.getStr("stationIp");
        if (StrUtil.isEmpty(stationIp)) {
            return ResultVoUtil.error("车站ip不能为空");
        }

        return ResultVoUtil.success(stationServ.getstationInfoByIp(stationIp));

    }

    /**
     * 接收APP回传的告警处理情况
     *
     * @param req
     * @return
     */
    @PostMapping("/receiveAppAlarm")
    @ResponseBody
    RestBean appData(@RequestBody @Validated AppAlarmDataReq req) {
        AlarmInfo alarmInfo = alarmInfoServ.getById(req.getAlarmId());
        if (Objects.isNull(alarmInfo)) {
            return RestBean.ofError("该条告警已经不存在");
        }
        if (!AlarmStatusEnum.UNCONFIRM.getCode().equals(alarmInfo.getStatus())) {
            //其他用户已经确认，增加备注进去
            alarmInfo.setRemark(req.getRemark());
        } else {
            alarmInfo.setRemark(req.getRemark());
            alarmInfo.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
            alarmInfo.setConfirmor(req.getConfirmor());
            alarmInfo.setConfirmTime(new Date());
        }

        alarmInfoServ.updateById(alarmInfo);

        return RestBean.ofSuccess("update ok");
    }


    /**
     * 获取机柜数据
     *
     * @param pageSize
     * @param pageIndex
     * @return
     */
    private List<Cabinet> getCabinet(Integer pageSize, Integer pageIndex) {
        List<Cabinet> list = null;
        QueryWrapper<Cabinet> queryWrapper = new QueryWrapper<Cabinet>();
        queryWrapper.select("id", "QR_CODE_NUM", "NAME", "ROOM_ID");
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            list = cabInetServ.list(queryWrapper);
        } else {
            IPage<Cabinet> page = PagePlugin.startPageT(pageIndex, pageSize, Cabinet.class);

            IPage<Cabinet> pageResult = cabInetServ.page(page, queryWrapper);
            list = pageResult.getRecords();
        }
        return list;
    }

    /**
     * 获取机房数据
     *
     * @param pageSize
     * @param pageIndex
     * @return
     */
    private List<Room> getRoomData(Integer pageSize, Integer pageIndex) {
        List<Room> list = null;
        QueryWrapper<Room> queryWrapper = new QueryWrapper<Room>();
        queryWrapper.select("id", "NAME", "ORG_ID");
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            list = roomServ.list(queryWrapper);
        } else {
            IPage<Room> page = PagePlugin.startPageT(pageIndex, pageSize, Room.class);
            IPage<Room> pageResult = roomServ.page(page, queryWrapper);
            list = pageResult.getRecords();
        }
        return list;
    }

    /**
     * 获取组织数据
     *
     * @param pageSize
     * @param pageIndex
     * @return
     */
    private List<SysOrg> getOrgData(Integer pageSize, Integer pageIndex) {
        List<SysOrg> list = null;
        QueryWrapper<SysOrg> queryWrapper = new QueryWrapper<SysOrg>();
        queryWrapper.select("id", "title", "type", "pid", "sort");
        queryWrapper.eq("status", 1);
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            list = orgServ.list(queryWrapper);
        } else {
            IPage<SysOrg> page = PagePlugin.startPageT(pageIndex, pageSize, SysOrg.class);
            IPage<SysOrg> pageResult = orgServ.page(page, queryWrapper);
            list = pageResult.getRecords();
        }
        return list;
    }

    /**
     * 获取资产信息
     *
     * @param orgId
     * @param pageSize
     * @param pageIndex
     * @return
     */
    private List<AppAssetResp> getAssetData(String orgId, Integer pageSize, Integer pageIndex) {
        List<Asset> list = null;
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("IS_DEL", 1);
        if (StrUtil.isNotEmpty(orgId)) {
            queryWrapper.eq("ORG_ID", orgId);
        }
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            list = assetSeerv.list(queryWrapper);
        } else {
            IPage<Asset> page = PagePlugin.startPageT(pageIndex, pageSize, Asset.class);
            IPage<Asset> pageResult = assetSeerv.page(page, queryWrapper);
            list = pageResult.getRecords();
        }
        List<AppAssetResp> respList = new ArrayList<AppAssetResp>();
        for (Asset asset : list) {
            AssetAttach attach = assetAttachServ.getByAssetId(asset.getId());

            AppAssetResp resp = new AppAssetResp();
            resp.setId(asset.getId());
            resp.setQrCodeNum(asset.getQrCodeNum());
            resp.setName(asset.getName());
            resp.setIp(asset.getIp());
            resp.setIp2(asset.getIp2());
            resp.setManufacturer("--");
            if (Objects.nonNull(asset.getManufacturerId())) {
                AssetManufacturer manufacturer = assetManufacturerService.getById(asset.getManufacturerId());
                if(Objects.nonNull(manufacturer)){
                    resp.setManufacturer(manufacturer.getName());
                }
            }
            resp.setSerialNumber(asset.getSerialNumber());
            resp.setAssetMode(asset.getDesk());
            resp.setAssetImage(StrUtil.isEmpty(asset.getAssetImage()) ? "--" : asset.getAssetImage());
            resp.setSystemType(asset.getCollectionType());
            resp.setShowCore("SHOW_TOPO_@_NO_SHOW");
            if (Objects.nonNull(asset.getShowCore())) {
                resp.setShowCore(asset.getShowCore());
            }
            if (Objects.nonNull(asset.getOnlineTime())) {
                resp.setOnlineTime(DateUtil.format(asset.getOnlineTime(), "yyyy-MM-dd"));
            }
            if (Objects.nonNull(asset.getDownlineTime())) {
                resp.setDownlineTime(DateUtil.format(asset.getDownlineTime(), "yyyy-MM-dd"));
            }

            ManufacturersEnum enum1 = ManufacturersEnum.getEnum(asset.getAssetSupplier());
            if (Objects.nonNull(enum1)) {
                resp.setAssetSupplier(enum1.getMsg());
            }

            if (StrUtil.isNotEmpty(asset.getOsPassword())) {
                resp.setPwd(EncryptUtil.aesDecryptStr(asset.getOsPassword()));
            }

            if (Objects.nonNull(asset.getCollectionType())
                    && (asset.getCollectionType() == 1 || asset.getCollectionType() == -1)) {
                resp.setSnmpUser(asset.getOsUser());
            } else {
                resp.setUserName(asset.getOsUser());
            }
            resp.setLoginPort(Objects.isNull(asset.getPort()) ? "" : asset.getPort() + "");

            resp.setEndPosition(attach.getEndPosition());
            resp.setStartPosition(attach.getStartPosition());
            resp.setOrgId(attach.getOrgId());
            resp.setRoomId(attach.getRoomId());
            resp.setCabinetId(attach.getCabinetId());
            if (StrUtil.isNotEmpty(attach.getRoomId())) {
                Room room = roomServ.getById(attach.getRoomId());
                if (Objects.nonNull(room)) {
                    resp.setRoomName(room.getName());
                }
            }
            if (StrUtil.isNotEmpty(asset.getOrgId())) {
                SysOrg org = orgServ.getById(asset.getOrgId());
                if (Objects.nonNull(org)) {
                    resp.setOrgName(org.getTitle());
                }
            }
            if (StrUtil.isNotEmpty(attach.getCabinetId())) {
                Cabinet cabint = cabInetServ.getById(attach.getCabinetId());
                if (Objects.nonNull(cabint)) {
                    resp.setCabName(cabint.getName());
                }

            }

            if (AssetWatchStatusEnum.WATCH_STATUS_YES.getCode() == asset.getWatch()) {
                QueryWrapper<CollectCpu> cpuQueryWrapper = new QueryWrapper<CollectCpu>();
                cpuQueryWrapper.eq("ASSET_ID", asset.getId());
                cpuQueryWrapper.orderByAsc("COLLECT_TIME");
                List<CollectCpu> cpuData = cpuService.list(cpuQueryWrapper);
                List<CpuResp> cpuRespList = new ArrayList<CpuResp>();
                for (CollectCpu cpu : cpuData) {
                    CpuResp respCpu = new CpuResp();
                    respCpu.setCollectTime(DateUtil.format(cpu.getCollectTime(), "HH:mm:ss"));
                    respCpu.setCollectDate(DateUtil.format(cpu.getCollectTime(), "yyyy-MM-dd HH:mm:ss"));
                    respCpu.setCpuUsedRate(cpu.getCpuUsedRate());
                    cpuRespList.add(respCpu);
                }

                QueryWrapper<CollectMemory> memoryQueryWrapper = new QueryWrapper<CollectMemory>();
                memoryQueryWrapper.eq("ASSET_ID", asset.getId());
                memoryQueryWrapper.orderByAsc("COLLECT_TIME");
                List<CollectMemory> memoryData = memoryServ.list(memoryQueryWrapper);

                List<MemoryResp> memoryList = new ArrayList<MemoryResp>();
                for (CollectMemory memory : memoryData) {
                    MemoryResp respMemory = new MemoryResp();
                    respMemory.setCollectTime(DateUtil.format(memory.getCollectTime(), "HH:mm:ss"));
                    respMemory.setCollectDate(DateUtil.format(memory.getCollectTime(), "yyyy-MM-dd HH:mm:ss"));
                    respMemory.setMemTotal(memory.getMemTotal());
                    respMemory.setMemUsed(memory.getMemUsed());
                    respMemory.setMemUsedRate(memory.getMemUsedRate());
                    memoryList.add(respMemory);
                }

                resp.setDiskData(diskServ.getRealTimeData(asset.getId()));
                resp.setCpuData(cpuRespList);
                resp.setMemoryData(memoryList);
            }
            try {
                String flag = outService.findMasterOrSlaveFlag(asset.getId(), "broker_topo_business");
                resp.setMasterOrSlave(flag);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "获取资产信息失败", e);
            }
            respList.add(resp);
        }
        return respList;
    }


    /**
     * 获取运行时长
     *
     * @return
     */
    private List<DurationResp> getDurationData(Integer pageSize, Integer pageIndex) {
        List<DurationResp> respList = null;
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            respList = sysTimeServ.listGroupByAsset();
        } else {
            respList = sysTimeServ.pageGroupByAsset(pageSize, pageIndex);
        }
        return respList;
    }

    /**
     * 获取上传的文件
     *
     * @return
     */
    private List<DocFileResp> getFileList(Integer pageSize, Integer pageIndex) {
        QueryWrapper<SysFolder> queryFolder = new QueryWrapper<SysFolder>();
        queryFolder.eq("STATUS", StatusEnum.OK.getCode());
        queryFolder.eq("CATEGORY", FolderCategoryEnum.MAINTAIN_HAND_BOOK.getCode());
        List<SysFolder> allFolder = folderService.list(queryFolder);
        List<String> folderIdList = allFolder.stream().map(item -> item.getId()).collect(Collectors.toList());
        if (folderIdList.isEmpty()) {
            folderIdList.add("x");
        }

        QueryWrapper<SysFile> wrapper = new QueryWrapper<>();
        wrapper.eq("STATUS", StatusEnum.OK.getCode());
        wrapper.in("FOLDER_ID", folderIdList);

        List<SysFile> fileList = null;
        if (Objects.isNull(pageSize) || Objects.isNull(pageIndex)) {
            fileList = fileService.list(wrapper);
        } else {
            IPage<SysFile> page = PagePlugin.startPageT(pageIndex, pageSize, SysFile.class);
            IPage<SysFile> pageResult = fileService.page(page, wrapper);
            fileList = pageResult.getRecords();
        }

        List<DocFileResp> respList = new ArrayList<>();
        for (SysFile item : fileList) {
            //文件
            DocFileResp resp = new DocFileResp();
            List<SysFolder> folderList = allFolder.stream().filter(e -> e.getId().equals(item.getFolderId())).collect(Collectors.toList());
            if (!folderList.isEmpty()) {
                resp.setCabinetId(folderList.get(0).getRemark());
            }
            resp.setFileId(item.getId());
            resp.setFileName(item.getOrignName());
            resp.setFilePath(item.getFilePath());

            respList.add(resp);
        }

        return respList;
    }

    /**
     * 与APP同步数据
     *
     * @return
     */
    @PostMapping("/appData")
    @ApiOperation(value = "获取app数据")
    @ResponseBody
    ResultVo<List<JSONObject>> appData(String type, String orgId, Integer pageSize, Integer pageIndex) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "收到APP同步数据请求组织ID：" + orgId, type);
        List<JSONObject> respList = new ArrayList<JSONObject>();
        if ("CABINET".equals(type)) {
            // 机柜数据
            List<Cabinet> cabinetList = getCabinet(pageSize, pageIndex);
            for (Cabinet cabinet : cabinetList) {
                JSONObject jsonObject = JSONUtil.parseObj(cabinet);
                respList.add(jsonObject);
            }
        } else if ("ROOM".equals(type)) {
            // 机房数据
            List<Room> roomData = getRoomData(pageSize, pageIndex);
            for (Room room : roomData) {
                JSONObject jsonObject = JSONUtil.parseObj(room);
                respList.add(jsonObject);
            }
        } else if ("ORG".equals(type)) {
            // 组织数据
            List<SysOrg> orgData = getOrgData(pageSize, pageIndex);
            for (SysOrg org : orgData) {
                JSONObject jsonObject = JSONUtil.parseObj(org);
                respList.add(jsonObject);
            }
        } else if ("ASSET".equals(type)) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "收到APP同步数据请求，开始同步资产数据，组织ID：" + orgId, type);
            List<AppAssetResp> assetData = getAssetData(orgId, pageSize, pageIndex);
            for (AppAssetResp org : assetData) {
                JSONObject jsonObject = JSONUtil.parseObj(org);
                respList.add(jsonObject);
            }
        } else if ("DURATION".equals(type)) {
            // 设备运行时长
            List<DurationResp> durationData = getDurationData(pageSize, pageIndex);
            for (DurationResp duration : durationData) {
                JSONObject jsonObject = JSONUtil.parseObj(duration);
                respList.add(jsonObject);
            }
        } else if ("DOC".equals(type)) {
            //
            List<DocFileResp> fileList = getFileList(pageSize, pageIndex);
            for (DocFileResp file : fileList) {
                JSONObject jsonObject = JSONUtil.parseObj(file);
                respList.add(jsonObject);
            }
        } else if ("ALARM".equals(type)) {
            respList = alarmInfoServ.selectAppAlarmInfo(pageSize, pageIndex);
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.OUT_API, "响应APP同步数据结束，组织ID：" + orgId, type);
        return ResultVoUtil.success(respList);
    }


    /**
     * 与DSS同步数据
     *
     * @return
     */
    @PostMapping("/dssData")
    @ResponseBody
    ResultVo<List<JSONObject>> dssData() {
        List<JSONObject> respList = new ArrayList<JSONObject>();
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("IS_DEL", 1);
        List<Asset> assetList = assetSeerv.list(queryWrapper);
        for (Asset asset : assetList) {
            AppAssetResp resp = new AppAssetResp();
            resp.setId(asset.getId());
            resp.setName(asset.getName());
            resp.setIp(asset.getIp());
            resp.setAssetMode(asset.getDesk());
            if (StrUtil.isNotEmpty(asset.getOrgId())) {
                SysOrg org = orgServ.getById(asset.getOrgId());
                resp.setOrgName(org.getTitle());
            }
            respList.add(JSONUtil.parseObj(resp));
        }
        return ResultVoUtil.success(respList);
    }

    /**
     * 车站同步ITSM进程
     *
     * @return
     */
    @PostMapping("/getStationProcess")
    @ResponseBody
    public List<CollectProcess> getStationProcess(@RequestBody String params) {
        if (StrUtil.isEmpty(params)) {
            return null;
        }
        List<CollectProcess> resultList = new ArrayList<>();
        List<ThresholdProcess> list;
        try {
            List<String> assetIdList = JSONUtil.toList(JSONUtil.parseArray(params), String.class);
            if (CollectionUtils.isEmpty(assetIdList)) {
                return resultList;
            }
            QueryWrapper<ThresholdProcess> query = Wrappers.query();
            query.in("ASSET_ID", assetIdList);
            list = thresholdProcessService.list(query);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.OUT_API, "车站同步ITSM进程异常，参数：" + params, e);
            return resultList;
        }
        for (ThresholdProcess thresholdProcess : list) {
            CollectProcess process = new CollectProcess();
            process.setAssetId(thresholdProcess.getAssetId());
            process.setProcessName(thresholdProcess.getProcessName());
            process.setStatus(EventLevelEnum.NORMAL.name().toLowerCase());
            if (thresholdProcess.getCollectStatus() == 0) {
                process.setStatus(EventLevelEnum.ABNORMAL.name().toLowerCase());
            }

            resultList.add(process);
        }
        return resultList;
    }

}
