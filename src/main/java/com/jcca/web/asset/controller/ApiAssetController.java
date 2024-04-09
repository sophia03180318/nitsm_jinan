package com.jcca.web.asset.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.ImportTemplate;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.ImportTemplateService;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.impl.SysModuleConfigServiceImpl;
import com.jcca.admin.system.util.TemplateExportUtil;
import com.jcca.admin.system.vo.Template;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.*;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.component.thresholds.bean.OpticalSwitchBean;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.poi.xssf.usermodel.XSSFWorkbook;
import com.jcca.web.alarm.controller.bean.CollectPortResp;
import com.jcca.web.asset.controller.bean.*;
import com.jcca.web.asset.detail.CommonService;
import com.jcca.web.asset.detail.DetailHandler;
import com.jcca.web.asset.entity.*;
import com.jcca.web.asset.service.*;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.asset.service.impl.ImportAsset;
import com.jcca.web.asset.utils.*;
import com.jcca.web.asset.utils.bean.*;
import com.jcca.web.asset.utils.enums.*;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.asset.vo.AssetLogExportVo;
import com.jcca.web.asset.vo.AssetManualVo;
import com.jcca.web.asset.vo.AvgVo;
import com.jcca.web.collect.entity.CollectPort;
import com.jcca.web.collect.service.CollectHPManagerLogService;
import com.jcca.web.collect.service.CollectPortService;
import com.jcca.web.collect.service.bean.HPManagerLogVo;
import com.jcca.web.collect.service.impl.CollectHPManagerLogServiceImpl;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.vo.AssetCollectTestVo;
import com.jcca.web.common.vo.AssetTestResult;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.statistics.service.HourCpuService;
import com.jcca.web.statistics.service.HourInterfacesService;
import com.jcca.web.statistics.service.HourMemoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AssetController
 * @Description 资产相关
 * @Date 2020/4/20 11:39
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/asset")
@Slf4j
@Api(tags = "资产相关接口")
public class ApiAssetController {

    public static final String ORTHER_MODEL_FLAG = "7";

    private static final String ERR_CODE = AssetCollectTestVo.ERRO_CODE;
    private static final String SUCCESS_CODE = AssetCollectTestVo.SUCCES_CODE;

    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService sysOrgServ;
    @Resource
    private RoomService roomServ;
    @Resource
    private CabinetService cabinetServ;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private DetailHandler detailHandler;
    @Resource
    private HourCpuService hourCpuService;
    @Resource
    private HourMemoryService hourMemoryService;
    @Resource
    private HourInterfacesService hourInterfacesService;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private AssetHardwareFixService assetHardwareFixService;
    @Resource
    private RoomService roomService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private AssetImportService assetImportService;
    @Resource
    private AssetImportTaskService assetImportTaskService;
    @Resource
    private ImportTemplateService importTemplateService;
    @Resource
    private RedisService redisService;
    @Resource
    private ImportAsset importAsset;
    @Resource
    private SysModuleConfigService sysModuleConfServ;
    @Resource
    private CollectHPManagerLogService collectHPManagerLogService;
    @Resource
    private AlarmEventService alarmEventService;
    @Resource
    private CollectPortService collectPortServ;
    @Resource
    private IpInfoService ipInfoService;
    @Resource
    private CommonService commonService;
    @Resource
    private OutService outServ;
    @Resource
    private AssetTemplateService assetTemplateService;


    @GetMapping("/getConfig")
    public ResultVo<?> getConfig(String assetId) {
        Asset asset = assetService.getById(assetId);
        if (StrUtil.isEmpty(asset.getLoginPwd())) {
            return ResultVoUtil.warning("此设备没有设定密码，无法下载配置信息！");
        }
        if (!AssetModeConst.ROUTER.equals(asset.getAssetMode()) && !AssetModeConst.SWITCH.equals(asset.getAssetMode())) {
            return ResultVoUtil.warning("仅可下载网络设备的配置备份！");
        }
        String command = "";
        if (AssetManufacturerEnum.CISCO.getCode().intValue() == asset.getManufacturerId()) {
            command = "enTPWDTshow run";
        } else if (AssetManufacturerEnum.HUAWEI.getCode().intValue() == asset.getManufacturerId()) {
            command = "sysTPWDTdis cu";
        } else {
            return ResultVoUtil.warning("暂时仅支持思科和华为设备！");
        }

        //发送命令
        Collection<String> commandList = new ArrayList<String>();
        commandList.add(command);
        try {
            List<String> telnetResult = outServ.getTelnetResult(asset, commandList);
            if (Objects.isNull(telnetResult) || telnetResult.isEmpty()) {
                return ResultVoUtil.warning("执行命令" + command + "获取配置信息失败");
            }
            return ResultVoUtil.success("", telnetResult.get(0));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultVoUtil.warning("执行命令" + command + "获取配置信息异常");
        }
    }

    /**
     * 查询windows linux 端口占用情况
     *
     * @param assetId
     * @return
     */
    @GetMapping("/`queryPortUsedMsg`")
    public ResultVo<?> queryPortUsedMsg(String assetId) {
        QueryWrapper<CollectPort> queryWrapper = new QueryWrapper<CollectPort>();
        queryWrapper.eq("ASSET_ID", assetId);
        queryWrapper.orderByDesc("CAST(PORT_NUM as integer)");
        List<CollectPort> list = collectPortServ.list(queryWrapper);

        Map<String, CollectPortResp> groupMap = new HashMap<>();
        for (CollectPort collectPort : list) {
            CollectPortResp value = groupMap.get(collectPort.getPortNum());

            if (Objects.isNull(value)) {
                value = EntityBeanUtil.copy(collectPort, CollectPortResp.class);
                value.setPortNumDouble(Double.valueOf(value.getPortNum()));
                value.setTcp("N");
                value.setUdp("N");
            }
            if (CollectPort.TCP.equals(collectPort.getType())) {
                value.setTcp("Y");
            } else {
                value.setUdp("Y");
            }
            groupMap.put(collectPort.getPortNum(), value);
        }

        Collection<CollectPortResp> values = groupMap.values();
        List<CollectPortResp> respList = new ArrayList<CollectPortResp>();
        for (CollectPortResp resp : values) {
            respList.add(resp);
        }

        respList.sort(Comparator.comparing(CollectPortResp::getPortNumDouble).reversed());

        return ResultVoUtil.success(respList);
    }


    @GetMapping("/collectHPManagerLog")
    @ActionLog(name = "收集惠普管理口日志", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> queryHPLog(String assetId) {
        Asset asset = assetService.getById(assetId);
        try {
            List<HPManagerLogVo> hpManagerLogVos = collectHPManagerLogService.queryHPManagerLog(asset);
            return ResultVoUtil.success(hpManagerLogVos);
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.ASSET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ASSET, ErrorCodeEnum.WEB_ASSET_GET_HP_MANAGER_MSG_ERROR, asset.getIp(), e.getMessage()), e);
            }
            Object logList = redisService.get(CollectHPManagerLogServiceImpl.CACHE_KEY + assetId);

            if (Objects.isNull(logList)) {
                return ResultVoUtil.error(e.getMessage());
            }

            JSONArray logArray = JSONUtil.parseArray(logList);
            List<HPManagerLogVo> hpManagerLogVos = JSONUtil.toList(logArray, HPManagerLogVo.class);

            return ResultVoUtil.success(hpManagerLogVos);
        }
    }


    /**
     * 查询设置的告警轮询次数
     *
     * @return
     * @author lyp
     */
    @GetMapping("/queryAlarmConf")
    @ApiOperation(value = "查询告警轮询次数")
    public ResultVo<?> queryAlarmConf() {
        AlarmVerifyBean alarmVerifyValue = sysModuleConfServ.getAlarmVerifyValue();
        return ResultVoUtil.success(alarmVerifyValue);
    }

    /**
     * 查询设置的告警轮询次数
     *
     * @return
     * @author lyp
     */
    @PostMapping("/commitAlarmConf")
    @ApiOperation(value = "设置告警轮询次数")
    public ResultVo<?> commitAlarmConf(@RequestBody @Validated AlarmVerifyBean req) {
        SysModuleConfig sysModuleConfig = sysModuleConfServ.getSysModuleConfig(SysModuleConfigServiceImpl.SYS_ALARM_MODULE_CONFIG_KEY);
        if (Objects.isNull(sysModuleConfig)) {
            sysModuleConfig = new SysModuleConfig();
            sysModuleConfig.setCreateTime(new Date());
            sysModuleConfig.setCreator("root");
            sysModuleConfig.setDescription("系统创建的勿删！！");
            sysModuleConfig.setId(MyIdUtil.getId());
            sysModuleConfig.setName(SysModuleConfigServiceImpl.SYS_ALARM_MODULE_CONFIG_KEY);
            sysModuleConfig.setOrgId("0");
            sysModuleConfig.setServiceType(0);
            sysModuleConfig.setValue(JSONUtil.parseObj(req).toString());

            sysModuleConfServ.save(sysModuleConfig);
        } else {
            sysModuleConfig.setValue(JSONUtil.parseObj(req).toString());
            sysModuleConfServ.updateConfig(sysModuleConfig);
        }

        return ResultVoUtil.success();
    }

    /**
     * 资产列表
     *
     * @return
     */
    @PostMapping("/index")
    @ApiOperation(value = "资产首页")
    @RequiresPermissions(value = {"api:asset:index", "api:asset:monitorIndex"}, logical = Logical.OR)
    @ActionLog(name = "查看资产列表", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo index(@RequestBody AssetQueryReq asset, Integer page, Integer size) {
        Map<String, Object> map = pageQuery(asset, page, size);
        return ResultVoUtil.success(map);
    }


    /**
     * 分页查询
     *
     * @param assetQueryReq
     * @param page
     * @param size
     * @return
     */
    private Map<String, Object> pageQuery(AssetQueryReq assetQueryReq, Integer page, Integer size) {
        IPage<Asset> startPage = PagePlugin.startPageT(page, size, Asset.class);
        QueryWrapper<Asset> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(assetQueryReq.getName())) {
            wrapper.like("name", assetQueryReq.getName());
        }
        if (Objects.nonNull(assetQueryReq.getDesk())) {
            wrapper.eq("desk", assetQueryReq.getDesk());
        }
        if (Objects.nonNull(assetQueryReq.getManufacturerId())) {
            wrapper.eq("manufacturer_id", assetQueryReq.getManufacturerId());
        }
        if (Objects.nonNull(assetQueryReq.getOverhaul()) && assetQueryReq.getOverhaul() == 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, 1);
            wrapper.le("downline_time", calendar.getTime());
        }
        if (Objects.nonNull(assetQueryReq.getWatch())) {
            wrapper.eq("WATCH", assetQueryReq.getWatch());
        }
        if (Objects.nonNull(assetQueryReq.getStatus())) {
            wrapper.eq("STATUS", assetQueryReq.getStatus());
        }
        if (Objects.nonNull(assetQueryReq.getCollectionType())) {
            wrapper.eq("COLLECTION_TYPE", assetQueryReq.getCollectionType());
        }

        if (!CollectionUtils.isEmpty(assetQueryReq.getDesks())) {
            wrapper.in("DESK", assetQueryReq.getDesks());
        }
        if (!CollectionUtils.isEmpty(assetQueryReq.getAssetImages())) {
            wrapper.in("ASSET_IMAGE", assetQueryReq.getAssetImages());
        }

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new HashMap<>();
        }

        if (StrUtil.isNotEmpty(assetQueryReq.getOrgId())) {
            if (sysOrgServ.getById(assetQueryReq.getOrgId()).getType() == OrgTypeConst.LINE) {
                // 线节点展示线下所有车站的数据 syt
                List<String> idByline = sysOrgServ.getIdByline(assetQueryReq.getOrgId());
                // 交集
                List<String> needShowOrgIds = orgIds.stream().filter(idByline::contains)
                        .collect(Collectors.toList());
                needShowOrgIds.add("x");
                orgIds = needShowOrgIds;
            } else {
                orgIds = Collections.singletonList(assetQueryReq.getOrgId());
            }
        }

        wrapper.in("org_id", orgIds);

        boolean needShowPosition = false;

        if (Objects.nonNull(assetQueryReq.getListEntrance()) && assetQueryReq.getListEntrance() != AssetListEntranceEnum.EXPORT.getCode()) {
            // 监控列表不显示非监控的资产 syt 2021/5/20
            if (assetQueryReq.getListEntrance() == AssetListEntranceEnum.MONITOR.getCode()) {
                wrapper.isNotNull("IP");
            } else {
                needShowPosition = true;
            }
        }

        wrapper.eq("is_del", StatusEnum.OK.getCode());

        if (StrUtil.isNotEmpty(assetQueryReq.getIp())) {
            wrapper.and(wq -> wq.eq("ip", assetQueryReq.getIp())
                    .or()
                    .eq("ip2", assetQueryReq.getIp()));
        }

        if (Objects.nonNull(assetQueryReq.getSort()) && !assetQueryReq.getSort().isEmpty()) {
            if (Objects.nonNull(assetQueryReq.getOrder()) && assetQueryReq.getOrder().equals("ascending")) {
                wrapper.orderByAsc(assetQueryReq.getSort(), "id");
                wrapper.orderByDesc("modify_time");
            } else {
                wrapper.orderByDesc(assetQueryReq.getSort(), "modify_time", "id");
            }
        } else {//descending
            wrapper.orderByDesc("modify_time");
            wrapper.orderByAsc("id");
        }

        if (Objects.nonNull(assetQueryReq.getMonitorStatus())) {
            if (assetQueryReq.getMonitorStatus().equals("2")) {//选择未知时,将""也查询出来
                wrapper.and(wq -> wq.eq("MONITOR", 2).or().eq("MONITOR", 4));
            } else {
                wrapper.eq("MONITOR", Integer.valueOf(assetQueryReq.getMonitorStatus()));
            }
        }
        IPage<Asset> pageResult = assetService.page(startPage, wrapper);

        List<Asset> records = pageResult.getRecords();

        for (Asset record : records) {
            record.setMonitorStatus("");
            if (Objects.nonNull(record.getMonitor())) {
                if (record.getMonitor() == 0) {
                    record.setMonitorStatus("异常");
                } else if (record.getMonitor() == 1) {
                    record.setMonitorStatus("正常");
                } else if (record.getMonitor() == 2) {
                    record.setMonitorStatus("未知");
                } else if (record.getMonitor() == 3) {
                    record.setMonitorStatus("不监控");
                }
            }

            String pwd = record.getOsPassword();
            if (StrUtil.isNotEmpty(pwd)) {
                record.setOsPassword(EncryptUtil.aesDecryptStr(record.getOsPassword()));
            }
            if (StrUtil.isNotEmpty(record.getLoginPwd())) {
                record.setLoginPwd(EncryptUtil.aesDecryptStr(record.getLoginPwd()));
            }
            if (StrUtil.isNotEmpty(record.getIpmiPwd())) {
                record.setIpmiPwd(EncryptUtil.aesDecryptStr(record.getIpmiPwd()));
            }

            record.setProtocolTypeStr("SNMP_TELNET");
            if (Objects.nonNull(record.getCollectionType())) {
                if (record.getCollectionType() == 0) {
                    record.setProtocolTypeStr("SSH_LINUX");
                } else if (record.getCollectionType() == 1) {
                    record.setProtocolTypeStr("SNMP_WINDOWS");
                } else if (record.getCollectionType() == 2) {
                    record.setProtocolTypeStr("SSH_AIX");
                } else if (record.getCollectionType() == 3) {
                    record.setProtocolTypeStr("TELNET_LINUX");
                } else if (record.getCollectionType() == 4) {
                    record.setProtocolTypeStr("TELNET_AIX");
                } else if (record.getCollectionType() == -1) {
                    record.setProtocolTypeStr("SNMP_TELNET");
                }
            }

            // 查附属信息
            AssetAttach assetAttach = assetAttachService.getByAssetId(record.getId());
            if (Objects.nonNull(assetAttach)) {
                // 资产位置 syt 2021/6/9
                if (needShowPosition) {
                    this.assetPosition(record, assetAttach);
                }
                BeanUtil.copyProperties(assetAttach, record);
            }
            ThresholdAsset thresholdAsset = thresholdAssetService.getById(record.getId());
            if (Objects.nonNull(thresholdAsset)) {
                record.setAutoFlag(ThresholdAutoFlagEnum.getName(thresholdAsset.getAutoFlag()));
            } else {
                record.setAutoFlag("默认");
            }

            if (AssetModeConst.TERMINAL.equals(record.getDesk())) {
                record.setStartPosition(null);
                record.setEndPosition(null);
                record.setCabinetId("");
            }

        }

        Map<String, Object> map = new HashMap<>(16);

        map.put("assetList", records);
        map.put("total", pageResult.getTotal());

        return map;
    }


    private void assetPosition(Asset record, AssetAttach assetAttach) {
        // 机柜ID
        String cabinetId = assetAttach.getCabinetId();
        // 组织名
        SysOrg sysOrg = sysOrgServ.getById(record.getOrgId());
        // 机房名
        String roomName = roomService.getById(assetAttach.getRoomId()).getName();
        // U位
        if (assetAttach.getStartPosition() != null && assetAttach.getEndPosition() != null) {
            record.setAssetPosition(assetAttach.getStartPosition() + "-" + assetAttach.getEndPosition());
        }

        // 判断设备是否在机柜上
        if (StrUtil.isNotEmpty(cabinetId)) {
            // 机柜名
            String cabinetName = cabinetService.getById(cabinetId).getName();
            // 位置:组织名+机房名+机柜名
            if (OrgTypeConst.CENTER == sysOrg.getType()) {
                record.setAssetSite(roomName + "/" + cabinetName);
            } else if (OrgTypeConst.STATION == sysOrg.getType()) {
                record.setAssetSite(sysOrg.getTitle() + "/" + cabinetName);
            } else {
                record.setAssetSite(sysOrg.getTitle() + "/" + roomName + "/" + cabinetName);
            }

        } else {
            // 位置:组织名+机房名
            if (OrgTypeConst.CENTER == sysOrg.getType()) {
                record.setAssetSite(roomName);
            } else if (OrgTypeConst.STATION == sysOrg.getType()) {
                record.setAssetSite(sysOrg.getTitle());
            } else {
                record.setAssetSite(sysOrg.getTitle() + "/" + roomName);
            }

        }

    }

    /**
     * 监控保存资产中测试资产采集指标
     *
     * @param collectReq
     * @return
     * @author syt
     */
    @PostMapping("/collectTest")
    @ApiOperation(value = "测试资产采集指标")
    @ActionLog(name = "测试采集指标", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> collectTest(@RequestBody @Valid AssetCollectReq collectReq) {
        Integer port = collectReq.getPort();
        if (Objects.isNull(port)) {
            collectReq.setPort(Asset.getDefaultPort(collectReq.getCollectionType()));
        }
        // 1终端，2小型机，3工控机
        List<Integer> deskTypeList = Arrays.asList(1, 2, 3, 4);
        if (deskTypeList.contains(collectReq.getDesk())) {
            collectReq.setDesk(Integer.valueOf(collectReq.getAssetMode() + "" + collectReq.getDesk()));
        } else if (Objects.nonNull(collectReq.getDesk())) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg("设备小类型错误" + collectReq.getDesk());

            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        }

        Asset dbAsset = assetService.getOneByAllIp(collectReq.getIp());
        if (Objects.nonNull(dbAsset) && !dbAsset.getId().equals(collectReq.getId())) {
            return ResultVoUtil.error("设备IP已经被其他设备占用");
        }

        if (StrUtil.isNotEmpty(collectReq.getIp2())) {
            dbAsset = assetService.getOneByAllIp(collectReq.getIp2());
            if (Objects.nonNull(dbAsset) && !dbAsset.getId().equals(collectReq.getId())) {
                return ResultVoUtil.error("设备IP2已经被其他设备占用");
            }
        }

        // 判断机柜中设备位置
        if (StrUtil.isNotEmpty(collectReq.getCabinetId())) {
            Integer startPosition = collectReq.getStartPosition();
            Integer endPosition = collectReq.getEndPosition();
            String msg = this.checkPosition(startPosition, endPosition, collectReq.getCabinetId(), collectReq.getId());

            if (StrUtil.isNotEmpty(msg)) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg(msg);
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }

        String assetModelStr = collectReq.getAssetMode().toString();
        if (!assetModelStr.startsWith(ORTHER_MODEL_FLAG)) {
            ResultVo<?> resultVo = this.assetModeValitedUserAndPassword(collectReq.getAssetMode(),
                    collectReq.getCollectionType(),
                    StrUtil.isEmpty(collectReq.getOsUser()) ? "" : collectReq.getOsUser(),
                    StrUtil.isEmpty(collectReq.getOsPassword()) ? "" : collectReq.getOsPassword());
            if (Objects.nonNull(resultVo)) {
                // 验证失败,返回提示信息
                return resultVo;
            }

        }
        try {
            // 加密密码
            collectReq.setOsPassword(EncryptUtil.aesEncryptHex(collectReq.getOsPassword()));
            assetService.verifyIp(collectReq);
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(SUCCESS_CODE);
            testVo.setMsg("验证通过");

            return ResultVoUtil.success(testVo);
        } catch (AddAssetException e) {
            log.error(e.toString(), e);
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg(e.getAddErrorMsg());
            testVo.setTestResultList(null);
            List<AssetTestResult> testResultList = e.getTestResultList();
            if (AddAssetException.COLLECT_ERROR.equals(e.getAddErrorcode())) {
                if (testResultList.size() == 0) {
                    AssetTestResult result = new AssetTestResult();
                    result.setState(1);
                    result.setErrorMsg("验证指标异常：" + e.toString());
                    result.setTargetDescription("");
                    testResultList.add(result);
                }
                testVo.setTestResultList(testResultList);
            }
            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "成功", testVo);
        }
    }

    private ResultVo<?> assetModeValitedUserAndPassword(Integer assetMode, Integer collectionType, String osUser,
                                                        String osPassword) {
        // 网络设备不验证密码 syt
        if (AssetModeEnum.ROUTER.getCode() != assetMode) {
            if (AssetModeEnum.SWITCH.getCode() != assetMode) {
                // 采集协议要是 windows 的话，只有团体号，没有账号，密码. 团体号就是osUser
                if (SystemTypeEnum.WINDOWS.getCode() == collectionType) {
                    if (StrUtil.isEmpty(osUser)) {
                        AssetCollectTestVo testVo = new AssetCollectTestVo();
                        testVo.setCode(ERR_CODE);
                        testVo.setMsg("请输入团体名");

                        return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
                    }
                } else {
                    if (StrUtil.isEmpty(osUser) || StrUtil.isEmpty(osPassword)) {
                        AssetCollectTestVo testVo = new AssetCollectTestVo();
                        testVo.setCode(ERR_CODE);
                        testVo.setMsg("请输入用户名和密码");

                        return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
                    }

                }
            }
        }
        return null;
    }

    /**
     * 资产管理保存或编辑资产
     *
     * @return
     */
    @PostMapping("/save")
    @ApiOperation(value = "资产管理保存资产")
    @RequiresPermissions({"api:asset:save"})
    @ActionLog(name = "保存资产信息", title = "资产管理", key = LogTypeConstant.ADD)
    public ResultVo<?> save(@RequestBody Asset asset) {
        // 资产管理进行编辑
        if (StrUtil.isNotEmpty(asset.getId())) {
            Asset byId = assetService.getById(asset.getId());
            asset.setWatch(byId.getWatch());
            asset.setStatus(byId.getStatus());
        } else {
            // 资产录入 默认不监控
            asset.setWatch(AssetWatchStatusEnum.WATCH_STATUS_NO.getCode());
            asset.setShowTopo((byte) 0);
            // 资产录入 初始监控状态为不监控
            asset.setStatus(AssetStatusEnum.ASSET_STATUS_NO_WATCH.getCode());
            asset.setBeforeVerify("no");
        }
        return this.asset(asset);
    }

    /**
     * 监控管理保存资产
     *
     * @return
     */
    @PostMapping("/save/monitor")
    @ApiOperation(value = "监控管理保存资产")
    @RequiresPermissions({"api:asset:save"})
    @ActionLog(name = "保存资产信息", title = "监控管理", key = LogTypeConstant.ADD)
    public ResultVo<?> saveMonitor(@RequestBody Asset asset) {
        if (Objects.isNull(asset.getPort())) {
            asset.setPort(Asset.getDefaultPort(asset.getCollectionType()));
        }
        // 当选择不监控时 把设备监控状态状态 初始化为不监控
        if (asset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_NO.getCode()) {
            asset.setStatus(AssetStatusEnum.ASSET_STATUS_NO_WATCH.getCode());
        } else {
            // 选择监控 监控状态默认在线
            asset.setStatus(AssetStatusEnum.ASSET_STATUS_ONLINE.getCode());
        }
        return this.asset(asset);
    }

    // 资产录入验证信息是否合规
    public ResultVo<?> asset(Asset asset) {
        // 1终端，2小型机，3工控机,4工控机
        List<Integer> deskTypeList = Arrays.asList(1, 2, 3, 4);
        if (deskTypeList.contains(asset.getDesk())) {
            asset.setDesk(Integer.valueOf(asset.getAssetMode() + "" + asset.getDesk()));
        } else if (Objects.nonNull(asset.getDesk())) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg("设备小类型错误:" + asset.getDesk());

            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        }
        String assetModelStr = asset.getAssetMode().toString();

        // 非安全边界等类型资产 验证用户名密码和IP地址
        if (!assetModelStr.startsWith(ORTHER_MODEL_FLAG)) {
            // 资产拆分后从资产管理添加的默认不验证用户名密码, syt
            if (asset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()) {
                ResultVo<?> resultVo = this.assetModeValitedUserAndPassword(asset.getAssetMode(),
                        asset.getCollectionType(), StrUtil.isEmpty(asset.getOsUser()) ? "" : asset.getOsUser(),
                        StrUtil.isEmpty(asset.getOsPassword()) ? "" : asset.getOsPassword());
                if (Objects.nonNull(resultVo)) {
                    // 验证失败,返回提示信息
                    return resultVo;
                }
            }

            if (StrUtil.isEmpty(asset.getIp())) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg("请输入正确IP");
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            } else {
                Asset dbAsset = assetService.getOneByAllIp(asset.getIp());
                if (Objects.nonNull(dbAsset) && !dbAsset.getId().equals(asset.getId())) {
                    return ResultVoUtil.error("设备IP已经被其他设备占用");
                }
            }

            if (StrUtil.isNotEmpty(asset.getIp2())) {
                //校验设备IP是否重复
                if (asset.getIp().equals(asset.getIp2())) {
                    return ResultVoUtil.error("设备IP1和IP2重复！");
                }

                Boolean examineIp = ipInfoService.examineIp(asset.getIp2());
                if (!examineIp) {
                    return ResultVoUtil.error(asset.getIp2() + " 未配置IP网段");
                }
                Asset dbAsset = assetService.getOneByAllIp(asset.getIp2());
                if (Objects.nonNull(dbAsset) && !dbAsset.getId().equals(asset.getId())) {
                    return ResultVoUtil.error("设备IP2已经被其他设备占用");
                }
            }
        } else {
            // 安全边界 监控相关状态字段 默认置否
            asset.setWatch(StatusConst.NO);
            asset.setShowTopo(ShowTopoEnum.NOT_SHOW.getCode());
            asset.setNtpFlag(NtpFlagEnum.NTP_NO.getCode());
        }
        // 默认值
        if (Objects.isNull(asset.getDesk())) {
            asset.setDesk(asset.getAssetMode());
        }
        if (Objects.isNull(asset.getWatch())) {
            asset.setWatch(StatusConst.NO);
        }

        asset.setOsPassword(EncryptUtil.aesEncryptHex(asset.getOsPassword()));
        if (StrUtil.isNotEmpty(asset.getLoginPwd())) {
            asset.setLoginPwd(EncryptUtil.aesEncryptHex(asset.getLoginPwd()));
        }
        if (StrUtil.isNotEmpty(asset.getIpmiPwd())) {
            asset.setIpmiPwd(EncryptUtil.aesEncryptHex(asset.getIpmiPwd()));
        }

        if (!AssetModeConst.HVAC.equals(asset.getDesk()) && !AssetModeConst.UPS.equals(asset.getDesk())) {
            // 如果设备不是终端则应该在机柜中 20210112hanwone
            if (!AssetModeConst.TERMINAL.equals(asset.getDesk()) && StrUtil.isEmpty(asset.getCabinetId())) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg("非终端设备需要选择所属机柜");
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }

        // 判断上下架时间
        Date downlineTime = asset.getDownlineTime();
        Date onlineTime = asset.getOnlineTime();
        if (Objects.nonNull(downlineTime) && Objects.nonNull(onlineTime)) {
            if (downlineTime.before(onlineTime)) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg("上架时间不应晚于下架时间");
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }

        // 判断机柜中设备位置
        if (StrUtil.isNotEmpty(asset.getCabinetId())) {
            Integer startPosition = asset.getStartPosition();
            Integer endPosition = asset.getEndPosition();
            String msg = this.checkPosition(startPosition, endPosition, asset.getCabinetId(), asset.getId());

            if (StrUtil.isNotEmpty(msg)) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg(msg);
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }

        try {
            if (StrUtil.isEmpty(asset.getId())) {
                asset.setId(MyIdUtil.getId());
                assetService.createAsset(asset);
            } else {
                assetService.updateAsset(asset);
            }

            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(SUCCESS_CODE);
            testVo.setMsg("操作成功");
            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        } catch (AddAssetException e) {
            log.error(e.getAddErrorMsg(), e);
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg(e.getAddErrorMsg());

            if (AddAssetException.COLLECT_ERROR.equals(e.getAddErrorcode())) {
                testVo.setTestResultList(e.getTestResultList());
            }

            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        }
    }

    /**
     * 机柜中可用U位
     *
     * @param cabinetId 机柜ID
     * @return ResultVo
     */
    @PostMapping("/freePosition")
    @ApiOperation(value = "机柜中可用U位")
    @ApiImplicitParams({@ApiImplicitParam(name = "cabinetId", value = "机柜ID", required = true, dataType = "String")})
    public ResultVo<?> freePosition(@RequestParam @NotEmpty String cabinetId) {
        List<CabinetUsed> cabinetUseds = assetService.freePosition(cabinetId);

        if (cabinetUseds.size() != 0) {
            return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), cabinetUseds);
        }
        return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", null);
    }

    /**
     * 资产添加模版
     *
     * @param
     * @return
     */
    @PostMapping("/saveTemp")
    @ApiOperation(value = "保存资产模板")
    @RequiresPermissions({"api:asset:save"})
    @ActionLog(name = "保存资产模板", title = "资产管理", key = LogTypeConstant.ADD)
    public ResultVo<?> saveTemp(@RequestBody @Valid AssetTemplate assetTemp) {
        // 查询条件
        HashMap<String, Object> map = new HashMap<>();
        map.put("ASSET_IMAGE", assetTemp.getAssetImage());
        map.put("MANUFACTURER_ID", assetTemp.getManufacturerId());
        if (Objects.isNull(assetTemp.getDesk())) {
            map.put("DESK", assetTemp.getAssetMode());
            assetTemp.setDesk(assetTemp.getAssetMode());
        } else {
            map.put("DESK", Integer.valueOf(assetTemp.getAssetMode() + "" + assetTemp.getDesk()));
            assetTemp.setDesk(Integer.valueOf(assetTemp.getAssetMode() + "" + assetTemp.getDesk()));
        }
        // 只保存最新的一份模板,老的删掉
        UpdateWrapper<AssetTemplate> update = Wrappers.update();
        UpdateWrapper<AssetTemplate> updateWrapper = update.allEq(map);
        QueryWrapper<AssetTemplate> query = Wrappers.query();
        QueryWrapper<AssetTemplate> assetTemplateQueryWrapper = query.allEq(map);

        AssetTemplate one = assetTemplateService.getOne(assetTemplateQueryWrapper);
        try {
            if (Objects.nonNull(one)) {
                assetTemplateService.update(assetTemp, updateWrapper);
                return ResultVoUtil.success("更新模板成功");
            } else {
                assetTemplateService.save(assetTemp);
                return ResultVoUtil.success("保存模板成功");
            }
        } catch (Exception e) {
            log.error("保存或更新资产模版失败:{}", e.getMessage(), e);
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg("保存更新模板失败");
            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "参数错误", testVo);
        }
    }

    /**
     * 查询资产模板
     *
     * @param
     * @return
     */
    @PostMapping("/queryTemp")
    @ApiOperation(value = "查询资产模板")
    @RequiresPermissions({"api:asset:save"})
    @ActionLog(name = "查询资产模板", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> queryTemp(@RequestBody @Valid AssetTemplateReq assetTemp) {
//		if (assetTemp.getDesk() == null
//				|| assetTemp.getManufacturerId() == null
//				|| assetTemp.getAssetImage() == null) {
//			return ResultVoUtil.error(ResultEnum.OUT_PARAM_LOST.getCode(), ResultEnum.OUT_PARAM_LOST.getMessage(), null);
//		}
        // 当前登录的用户的ID
        String loginUserId = ShiroUtil.getSubject().getId();
        // 查询相应模板
        QueryWrapper<AssetTemplate> getTemp = Wrappers.query();
        getTemp.eq("DESK", assetTemp.getDesk());
        getTemp.eq("ASSET_IMAGE", assetTemp.getAssetImage());
        getTemp.eq("MANUFACTURER_ID", assetTemp.getManufacturerId());
        AssetTemplate oneTemp = assetTemplateService.getOne(getTemp);
        if (Objects.nonNull(oneTemp)) {
            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "查询成功", oneTemp);
        }
        AssetCollectTestVo testVo = new AssetCollectTestVo();
        testVo.setCode(ERR_CODE);
        testVo.setMsg("没有查询到模板");
        return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
    }

    /**
     * 检查资产位置是否重叠
     *
     * @param startPosition
     * @param endPosition
     * @param cabinetId
     * @param assetId
     * @return
     */
    private String checkPosition(Integer startPosition, Integer endPosition, String cabinetId, String assetId) {
        if (Objects.isNull(startPosition) || Objects.isNull(endPosition)) {
            return "请确认设备位置";
        }

        // 校验机柜中设备位置
        if (Integer.compare(startPosition, endPosition) == 1) {
            return "起始位置应小于等于结束位置";
        }
        // 同一机柜中U位不能重叠
        QueryWrapper<AssetAttach> query = Wrappers.query();
        query.eq("cabinet_id", cabinetId);
        if (Objects.nonNull(assetId)) {
            query.ne("asset_id", assetId);
        }
        List<AssetAttach> assetAttachList = assetAttachService.list(query);
        for (AssetAttach attach : assetAttachList) {
            int start = attach.getStartPosition();
            int end = attach.getEndPosition();

            if ((startPosition >= start && startPosition <= end) || (endPosition >= start && endPosition <= end)
                    || (startPosition < start && endPosition > end)) {
                return "设备位置与机柜内已有U位重叠";
            }
        }
        return "";
    }

    /**
     * 删除资产
     *
     * @return
     */
    @PostMapping("/del/{ids}")
    @ApiOperation(value = "删除资产")
    @RequiresPermissions({"api:asset:del"})
    @ActionLog(name = "删除资产", title = "资产管理", key = LogTypeConstant.REMOVEE)
    public ResultVo del(@PathVariable("ids") String ids) {
        try {
            assetService.deleteAsset(ids);
        } catch (Exception e) {
            return ResultVoUtil.error("失败：" + e.getMessage());
        }
        return ResultVoUtil.success("删除成功");
    }

    /**
     * 资产详情
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "资产详情")
    @RequiresPermissions("api:asset:detail")
    @ActionLog(name = "查看资产详情", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo detail(@PathVariable("id") String id) {
        Asset asset = assetService.getById(id);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("设备[" + id + "]未录入");
        }

        ResultVo handle = detailHandler.handle(String.valueOf(asset.getAssetMode()), asset);
        return handle;
    }

    @GetMapping("/commonDetail/{id}")
    @ApiOperation(value = "资产通用详情")
    public ResultVo commonDetail(@PathVariable("id") String id) {
        Asset asset = assetService.getById(id);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("资产[" + id + "]未录入");
        }
        return detailHandler.getAssetGeneralInfo(String.valueOf(asset.getAssetMode()), asset);
    }

    /**
     * 业务拓扑设备详情
     *
     * @param id 资产ID
     * @return
     */
    @GetMapping("/bizDetail/{id}")
    @ApiOperation(value = "业务设备详情")
    public ResultVo bizDetail(@PathVariable("id") String id) {
        Asset asset = assetService.getById(id);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("资产[" + id + "]未录入");
        }
        return commonService.getBizDetail(asset);
    }


    /**
     * 查看资产历史性能
     *
     * @param req
     * @return
     */
    @PostMapping("/history")
    @ApiOperation(value = "查看资产历史性能")
    @RequiresPermissions("api:asset:history")
    @ActionLog(name = "查看资产历史性能", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo history(@RequestBody AssetHistoryReq req) {
        int period = req.getPeriod();
        if (period != -1) {
            Calendar reqDate = this.getReqStartDate(period);
            req.setStartDate(reqDate.getTime());
            if (period == 0) {
                req.setStartDate(DateUtil.beginOfDay(new Date()));
            }
            req.setEndDate(DateUtil.endOfDay(new Date()));
        }

        String assetId = req.getAssetId();
        // cpu使用率折线
        List<AssetHistoryVo> cpuList = hourCpuService.findLineByDate(assetId, req.getStartDate(), req.getEndDate());
        // 内存交换空间使用率折线
        List<AssetHistoryVo> memoryList = hourMemoryService.findLineByDate(assetId, req.getStartDate(),
                req.getEndDate());

        Map<String, Object> resultMap = new HashMap<>(16);
        Asset asset = assetService.getById(assetId);
        Integer assetMode = asset.getAssetMode();
        if (AssetModeConst.SERVER.equals(assetMode)) {
            // 平均值统计
            AvgVo avgVo = hourCpuService.findAvgByDate(assetId, req.getStartDate(), req.getEndDate());
            if (Objects.isNull(avgVo)) {
                avgVo = new AvgVo();
                avgVo.setAvgCpu("0");
                avgVo.setAvgMem("0");
                avgVo.setAvgSwap("0");
            }
            resultMap.put("avgPie", avgVo);
        }
        if (AssetModeConst.ROUTER.equals(assetMode) || AssetModeConst.SWITCH.equals(assetMode)) {
            // 端口流入量流出量
            List<AssetHistoryVo> interfaceList = hourInterfacesService.findLineByDate(assetId, req.getStartDate(),
                    req.getEndDate());
            for (AssetHistoryVo assetHistoryVo : interfaceList) {
                String portIn = assetHistoryVo.getPortIn();
                String portOut = assetHistoryVo.getPortOut();

                if (StrUtil.isNotEmpty(portIn) && NumberUtil.isNumber(portIn)) {
                    BigDecimal portInBig = new BigDecimal(portIn).divide(new BigDecimal(UnitEnum.KB.getScale()), 0,
                            BigDecimal.ROUND_HALF_UP);
                    assetHistoryVo.setPortIn(portInBig + "");
                }
                if (StrUtil.isNotEmpty(portOut) && NumberUtil.isNumber(portOut)) {
                    BigDecimal portOutBig = new BigDecimal(portOut).divide(new BigDecimal(UnitEnum.KB.getScale()), 0,
                            BigDecimal.ROUND_HALF_UP);
                    assetHistoryVo.setPortOut(portOutBig + "");
                }
            }
            resultMap.put("portLine", interfaceList);
        }

        resultMap.put("cpuLine", cpuList);
        resultMap.put("memSwapLine", memoryList);

        return ResultVoUtil.success(resultMap);
    }

    private Calendar getReqStartDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - day);
        return calendar;
    }

    /**
     * 获取需要大修的资产数量
     */
    @GetMapping("/overhaul")
    @ApiOperation(value = "获取需要大修的资产数量")
    public ResultVo overhaul() {
        List<Asset> overhaulList = assetService.getOverhaulList();
        return ResultVoUtil.success(overhaulList.size());
    }

    /**
     * 查看日志 syslog
     *
     * @return ResultVo
     */
    @GetMapping("/listSyslog")
    @ApiOperation(value = "查看syslog日志")
    @ActionLog(name = "查看资产syslog日志信息", title = "资产详情", key = LogTypeConstant.QUERY)
    public ResultVo listSyslog(String assetId, Integer page, Integer size) {
        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("资产不存在");
        }

        // 光交日志采集 20230410 hanhw
        long total = 0L;
        List records = new ArrayList<>();
        if (AssetModeConst.B24.equals(asset.getAssetImage())) {
            Object o = redisService.get(RedisCacheConst.OPTICAL_SWITCH_MSG + assetId);
            List list = null;
            if (Objects.nonNull(o)) {
                OpticalSwitchBean optical = JSONUtil.toBean(o.toString(), OpticalSwitchBean.class);
                list = optical.getLogList();
            }
            if (Objects.isNull(list)) {
                list = new ArrayList<>(512);
            } else {
                total = list.size();
                page = Convert.toInt(page, 1);
                size = Convert.toInt(size, 15);
                int start = (page - 1) * size;
                int end = Math.min(start + size, list.size());
                list = list.subList(start, end);
                for (Object record : list) {
                    AlarmEvent event = new AlarmEvent();
                    event.setEventMsg(record.toString());
                    event.setCreateTime(new Date());
                    event.setUniqueCode("ERRDUMP");
                    records.add(event);
                }
            }
        } else {
            IPage objectIPage = PagePlugin.startPage(page, size);

            QueryWrapper<AlarmEvent> query = Wrappers.query();
            query.eq("ASSET_ID", assetId);
            query.like("UNIQUE_CODE", "event:log");
            query.orderByDesc("CREATE_TIME");
            IPage resPage = alarmEventService.page(objectIPage, query);

            records = resPage.getRecords();
            total = resPage.getTotal();
        }


        Map<String, Object> map = new HashMap<>();
        map.put("syslogList", records);
        map.put("total", total);

        return ResultVoUtil.success(map);
    }

    /**
     * 下载服务器，网络设备，惠普设备日志
     */
    @GetMapping("/download/log")
    @ActionLog(name = "下载日志", title = "资产详情", key = LogTypeConstant.DOWNLOAD)
    public void downloadSyslog(String assetId, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {

        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            return;
        }

        Integer manufacturerId = asset.getManufacturerId();
        ExcelWriter writer = ExcelUtil.getWriter();
        if (AssetModeConst.B24.equals(asset.getAssetImage())) {
            List records = new ArrayList<>();
            Object o = redisService.get(RedisCacheConst.OPTICAL_SWITCH_MSG + assetId);
            if (Objects.nonNull(o)) {
                OpticalSwitchBean optical = JSONUtil.toBean(o.toString(), OpticalSwitchBean.class);
                records = optical.getLogList();
            }

            writer.write(records, true);

            writer.setColumnWidth(0, 160);

            downloadExcel(response, "设备日志", writer);
            return;
        }


        if (AssetManufacturerEnum.HUIPU.getCode().intValue() == manufacturerId) {
            // 惠普日志下载
            Object logList = redisService.get(CollectHPManagerLogServiceImpl.CACHE_KEY + assetId);
            if (Objects.nonNull(logList)) {
                writer.addHeaderAlias("id", "id");
                writer.addHeaderAlias("severity", "级别");
                writer.addHeaderAlias("classStr", "类型");
                writer.addHeaderAlias("entryCode", "匹配码");
                writer.addHeaderAlias("lastUpdate", "最后时间");
                writer.addHeaderAlias("initialUpdate", "初始时间");
                writer.addHeaderAlias("count", "发生次数");
                writer.addHeaderAlias("description", "描述");

                writer.setColumnWidth(0, 30);
                writer.setColumnWidth(1, 10);
                writer.setColumnWidth(2, 20);
                writer.setColumnWidth(3, 20);
                writer.setColumnWidth(4, 20);
                writer.setColumnWidth(5, 20);
                writer.setColumnWidth(6, 10);
                writer.setColumnWidth(7, 60);

                JSONArray logArray = JSONUtil.parseArray(logList);
                List<HPManagerLogVo> hpManagerLogVos = JSONUtil.toList(logArray, HPManagerLogVo.class);
                writer.write(hpManagerLogVos, true);
                downloadExcel(response, "设备日志", writer);
            }
        } else {
            // syslog下载
            int start = 1, end = 5000;
            List<AlarmEvent> syslogList = alarmEventService.listSyslog(assetId, start, end);
            if (!CollectionUtils.isEmpty(syslogList)) {
                List<AssetLogExportVo> logList = new ArrayList<>();
                for (AlarmEvent event : syslogList) {
                    AssetLogExportVo vo = new AssetLogExportVo();
                    BeanUtils.copyProperties(vo, event);
                    logList.add(vo);
                }

                writer.addHeaderAlias("createTime", "日志时间");
                writer.addHeaderAlias("eventMsg", "原始报文");
                writer.addHeaderAlias("uniqueCode", "匹配码");
                writer.write(logList, true);

                writer.setColumnWidth(0, 20);
                writer.setColumnWidth(1, 160);
                writer.setColumnWidth(2, 20);

                downloadExcel(response, "设备日志", writer);
            }
        }
    }

    /**
     * 方法描述: 下载excel文件
     *
     * @param response 响应
     * @param fileName 文件名称
     * @param writer   writer
     * @date 2021/5/24 16:20
     */
    private static void downloadExcel(HttpServletResponse response, String fileName, ExcelWriter writer) {
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        // test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        ServletOutputStream out = null;
        try {
            // 设置请求头属性
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xls").getBytes(), StandardCharsets.ISO_8859_1));
            out = response.getOutputStream();
            // 写出到文件
            writer.flush(out, true);
            // 关闭writer，释放内存
            writer.close();
            // 此处记得关闭输出Servlet流
            IoUtil.close(out);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @GetMapping("/export/verify")
    @ApiOperation(value = "资产导出校验")
    public ResultVo exportAssetVerify(AssetQueryReq assetReq) {
        FileUtil.createTmpPath();
        List<Asset> assetList;
        QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<>();
        assetQueryWrapper.eq("IS_DEL", 1);

        if (ObjectUtil.isNotNull(assetReq.getWatch()) && assetReq.getWatch() == (byte) 1) {
            assetQueryWrapper.eq("WATCH", (byte) 1);
        }
        if (ObjectUtil.isNotNull(assetReq.getName()) && assetReq.getName() != "") {
            assetQueryWrapper.like("NAME", assetReq.getName());
        }
        if (ObjectUtil.isNotNull(assetReq.getManufacturerId())) {
            assetQueryWrapper.eq("MANUFACTURER_ID", assetReq.getManufacturerId());
        }
        if (ObjectUtil.isNotNull(assetReq.getDesk())) {
            assetQueryWrapper.eq("DESK", assetReq.getDesk());
        }
        if (ObjectUtil.isNotNull(assetReq.getIp()) && assetReq.getIp() != "") {
            assetQueryWrapper.like("IP", assetReq.getIp());
        }
        if (ObjectUtil.isNotNull(assetReq.getStatus())) {
            assetQueryWrapper.eq("STATUS", assetReq.getStatus());
        }


        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        if (StrUtil.isNotEmpty(assetReq.getOrgId())) {
            SysOrg org = sysOrgServ.getById(assetReq.getOrgId());
            if (Objects.nonNull(org) && org.getType() == OrgTypeConst.LINE) {
                List<String> idByline = sysOrgServ.getIdByline(assetReq.getOrgId());
                List<String> needShowOrgIds = orgIds.stream().filter(item -> idByline.contains(item))
                        .collect(Collectors.toList());
                needShowOrgIds.add("x");
                orgIds = needShowOrgIds;
            } else {
                orgIds = Collections.singletonList(assetReq.getOrgId());
            }
        }
        assetQueryWrapper.in("org_id", orgIds);

        if (ObjectUtil.isNotNull(assetReq.getListEntrance()) && assetReq.getListEntrance() == 1) {
            assetQueryWrapper.isNotNull("IP");
        }
        assetQueryWrapper.orderByDesc("modify_time");
        assetList = assetService.list(assetQueryWrapper);

        if (Objects.isNull(assetList) || assetList.isEmpty()) {
            return ResultVoUtil.error("未选择资产");
        }
        return ResultVoUtil.success();
    }


    @GetMapping("/export")
    @ApiOperation(value = "资产导出")
    @RequiresPermissions("api:asset:export")
    @ActionLog(name = "导出资产", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void exportAsset(AssetQueryReq assetReq, HttpServletResponse response) {

        List<Asset> assetList;

        ArrayList<AssetImportRecord> assetImportRecords = new ArrayList<>();
        QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<>();
        assetQueryWrapper.eq("IS_DEL", 1);

        if (ObjectUtil.isNotNull(assetReq.getWatch()) && assetReq.getWatch() == (byte) 1) {
            assetQueryWrapper.eq("WATCH", (byte) 1);
        }
        if (ObjectUtil.isNotNull(assetReq.getName()) && assetReq.getName() != "") {
            assetQueryWrapper.like("NAME", assetReq.getName());
        }
        if (ObjectUtil.isNotNull(assetReq.getManufacturerId())) {
            assetQueryWrapper.eq("MANUFACTURER_ID", assetReq.getManufacturerId());
        }
        if (ObjectUtil.isNotNull(assetReq.getDesk())) {
            assetQueryWrapper.eq("DESK", assetReq.getDesk());
        }
        if (ObjectUtil.isNotNull(assetReq.getIp()) && assetReq.getIp() != "") {
            assetQueryWrapper.like("IP", assetReq.getIp());
        }
        if (ObjectUtil.isNotNull(assetReq.getStatus())) {
            assetQueryWrapper.eq("STATUS", assetReq.getStatus());
        }

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        if (StrUtil.isNotEmpty(assetReq.getOrgId())) {
            if (sysOrgServ.getById(assetReq.getOrgId()).getType() == OrgTypeConst.LINE) {
                List<String> idByline = sysOrgServ.getIdByline(assetReq.getOrgId());
                List<String> needShowOrgIds = orgIds.stream().filter(item -> idByline.contains(item))
                        .collect(Collectors.toList());
                needShowOrgIds.add("x");
                orgIds = needShowOrgIds;
            } else {
                orgIds = Arrays.asList(assetReq.getOrgId());
            }
        }
        assetQueryWrapper.in("org_id", orgIds);

        if (ObjectUtil.isNotNull(assetReq.getListEntrance()) && assetReq.getListEntrance() == 1) {
            assetQueryWrapper.isNotNull("IP");
        }
        assetQueryWrapper.orderByDesc("modify_time");
        assetList = assetService.list(assetQueryWrapper);

        for (Asset asset : assetList) {
            AssetImportRecord assetImportRecord = this.translate(asset);
            assetImportRecords.add(assetImportRecord);
        }
        List<String> headerList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        Collections.addAll(headerList, "资产名称", "资产编号", "IP地址1", "资产类型", "资产型号", "资产厂商", "组织机构", "*机房", "机柜", "起始位置",
                "结束位置", "设备状态", "采集类型", "上架时间", "监控状态");
        Collections.addAll(titleList, "name", "assetCode", "ip", "assetMode", "assetImage", "manufacturerId", "orgId",
                "roomId", "cabinetId", "startPosition", "endPosition", "status", "collectionType", "onlineTime", "monitorStatus");

        SXSSFWorkbook excel = AssetReportUtil.exportAssetExecl(headerList, titleList, assetImportRecords);
        AssetImportUtils.responseBody(excel, response, "资产导出" + DateUtil.formatDate(new Date()));

    }


    @PostMapping("/export/other/verify")
    @ApiOperation(value = "资产按需导出校验")
    public ResultVo exportOtherAssetVerify(@RequestBody AssetQueryReq assetReq) {
        FileUtil.createTmpPath();
        Map<String, String> kvMap = assetReq.getFields();
        if (kvMap.isEmpty()) {
            return ResultVoUtil.error("未选择导出属性");
        }
        ArrayList<AssetImportRecord> assetImportRecords = new ArrayList<>();
        QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<>();
        assetQueryWrapper.eq("IS_DEL", 1);

        if (ObjectUtil.isNotNull(assetReq.getWatch()) && assetReq.getWatch() == (byte) 1) {
            assetQueryWrapper.eq("WATCH", (byte) 1);
        }
        if (ObjectUtil.isNotNull(assetReq.getName()) && assetReq.getName() != "") {
            assetQueryWrapper.like("NAME", assetReq.getName());
        }
        if (ObjectUtil.isNotNull(assetReq.getManufacturerId())) {
            assetQueryWrapper.eq("MANUFACTURER_ID", assetReq.getManufacturerId());
        }
        if (ObjectUtil.isNotNull(assetReq.getDesk())) {
            assetQueryWrapper.eq("DESK", assetReq.getDesk());
        }
        if (ObjectUtil.isNotNull(assetReq.getIp()) && assetReq.getIp() != "") {
            assetQueryWrapper.like("IP", assetReq.getIp());
        }
        if (ObjectUtil.isNotNull(assetReq.getStatus())) {
            assetQueryWrapper.eq("STATUS", assetReq.getStatus());
        }
        if (ObjectUtil.isNotNull(assetReq.getRunModel())) {
            assetQueryWrapper.eq("RUN_MODEL", assetReq.getRunModel());
        }

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        if (StrUtil.isNotEmpty(assetReq.getOrgId())) {
            if (sysOrgServ.getById(assetReq.getOrgId()).getType() == OrgTypeConst.LINE) {
                List<String> idByline = sysOrgServ.getIdByline(assetReq.getOrgId());
                List<String> needShowOrgIds = orgIds.stream().filter(item -> idByline.contains(item))
                        .collect(Collectors.toList());
                needShowOrgIds.add("x");
                orgIds = needShowOrgIds;
            } else {
                orgIds = Arrays.asList(assetReq.getOrgId());
            }
        }

        assetQueryWrapper.in("org_id", orgIds);

        if (ObjectUtil.isNotNull(assetReq.getListEntrance()) && assetReq.getListEntrance() == 1) {
            assetQueryWrapper.isNotNull("IP");
        }
        List<Asset> assetList = assetService.list(assetQueryWrapper);
        if (Objects.isNull(assetList) || assetList.isEmpty()) {
            return ResultVoUtil.error("未选择资产");
        }
        return ResultVoUtil.success();

    }

    /**
     * 资产按需导出
     *
     * @param
     */

    /**
     * 资产按需导出
     *
     * @param
     */
    @PostMapping("/export/other")
    @ApiOperation(value = "资产按需导出")
    @RequiresPermissions("api:asset:export")
    @ActionLog(name = "按需导出资产", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void exportOtherAsset(@RequestBody AssetQueryReq assetReq, HttpServletResponse response) {
        Map<String, String> kvMap = assetReq.getFields();
        if (kvMap.isEmpty()) {
            SXSSFWorkbook workbook = new SXSSFWorkbook(10);
            AssetImportUtils.responseBody(workbook, response, "资产导出");
        } else {
            ArrayList<AssetImportRecord> assetImportRecords = new ArrayList<>();
            QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<>();
            assetQueryWrapper.eq("IS_DEL", 1);

            if (ObjectUtil.isNotNull(assetReq.getWatch()) && assetReq.getWatch() == (byte) 1) {
                assetQueryWrapper.eq("WATCH", (byte) 1);
            }
            if (ObjectUtil.isNotNull(assetReq.getName()) && assetReq.getName() != "") {
                assetQueryWrapper.like("NAME", assetReq.getName());
            }
            if (ObjectUtil.isNotNull(assetReq.getManufacturerId())) {
                assetQueryWrapper.eq("MANUFACTURER_ID", assetReq.getManufacturerId());
            }
            if (ObjectUtil.isNotNull(assetReq.getDesk())) {
                assetQueryWrapper.eq("DESK", assetReq.getDesk());
            }
            if (ObjectUtil.isNotNull(assetReq.getIp()) && assetReq.getIp() != "") {
                assetQueryWrapper.like("IP", assetReq.getIp());
            }
            if (ObjectUtil.isNotNull(assetReq.getStatus())) {
                assetQueryWrapper.eq("STATUS", assetReq.getStatus());
            }
            if (ObjectUtil.isNotNull(assetReq.getRunModel())) {
                assetQueryWrapper.eq("RUN_MODEL", assetReq.getRunModel());
            }

            List<String> orgIds = ShiroUtil.getSubjectOrgIds();
            orgIds.add("x");
            if (StrUtil.isNotEmpty(assetReq.getOrgId())) {
                if (sysOrgServ.getById(assetReq.getOrgId()).getType() == OrgTypeConst.LINE) {
                    List<String> idByline = sysOrgServ.getIdByline(assetReq.getOrgId());
                    List<String> needShowOrgIds = orgIds.stream().filter(item -> idByline.contains(item))
                            .collect(Collectors.toList());
                    needShowOrgIds.add("x");
                    orgIds = needShowOrgIds;
                } else {
                    orgIds = Arrays.asList(assetReq.getOrgId());
                }
            }

            assetQueryWrapper.in("org_id", orgIds);

            if (ObjectUtil.isNotNull(assetReq.getListEntrance()) && assetReq.getListEntrance() == 1) {
                assetQueryWrapper.isNotNull("IP");
            }
            assetQueryWrapper.orderByDesc("modify_time");
            List<Asset> assetList = assetService.list(assetQueryWrapper);

            for (Asset asset : assetList) {
                AssetImportRecord assetImportRecord = this.translate(asset);
                assetImportRecords.add(assetImportRecord);
            }
            List<String> headerList = new ArrayList<>();
            List<String> titleList = new ArrayList<>();
            kvMap.forEach((key, value) -> {
                if (key.equals("assetSite")) {
                    titleList.add("orgId");
                    titleList.add("roomId");
                    titleList.add("cabinetId");
                    titleList.add("startPosition");
                    titleList.add("endPosition");
                    headerList.add("组织机构");
                    headerList.add("机房");
                    headerList.add("机柜");
                    headerList.add("起始位置");
                    headerList.add("结束位置");
                } else {
                    titleList.add(key);
                    headerList.add(value);
                }
            });
            SXSSFWorkbook excel = AssetReportUtil.exportAssetExecl(headerList, titleList, assetImportRecords);
            AssetImportUtils.responseBody(excel, response, "资产自定义导出" + DateUtil.formatDate(new Date()));
        }
    }


    @GetMapping("/serversRecordQuery/{assetId}")
    @ApiOperation(value = "一机一档信息查询")
    public ResultVo<?> serversRecord(@PathVariable("assetId") String assetId) {
        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("失败：资产不存在");
        }
        QueryWrapper<AssetAttach> queryWrapper = new QueryWrapper<AssetAttach>();
        queryWrapper.eq("ASSET_ID", assetId);
        AssetAttach assetAttach = assetAttachService.getOne(queryWrapper);
        SysOrg org = sysOrgServ.getById(asset.getOrgId());
        QueryWrapper<AssetHardwareFix> hardwareWrapper = new QueryWrapper<AssetHardwareFix>();
        hardwareWrapper.eq("ASSET_ID", assetId);
        hardwareWrapper.orderByDesc("CREATE_TIME");
        List<AssetHardwareFix> list = assetHardwareFixService.list(hardwareWrapper);

        AssetRecordReq resp = getAssetRecordReq(asset, assetAttach, org, list);
        return ResultVoUtil.success(resp);
    }

    @GetMapping("/serversRecordExcel/{assetId}")
    @RequiresPermissions("api:asset:serversRecordExcel")
    @ApiOperation(value = "一机一档")
    @ActionLog(name = "导出一机一档信息", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void serversRecord(HttpServletResponse response, @PathVariable("assetId") String assetId) {
        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            return;
        }
        QueryWrapper<AssetAttach> queryWrapper = new QueryWrapper<AssetAttach>();
        queryWrapper.eq("ASSET_ID", assetId);
        AssetAttach assetAttach = assetAttachService.getOne(queryWrapper);
        SysOrg org = sysOrgServ.getById(asset.getOrgId());
        QueryWrapper<AssetHardwareFix> hardwareWrapper = new QueryWrapper<AssetHardwareFix>();
        hardwareWrapper.eq("ASSET_ID", assetId);
        hardwareWrapper.orderByDesc("CREATE_TIME");
        List<AssetHardwareFix> list = assetHardwareFixService.list(hardwareWrapper);

        List<Integer> serversCode = Arrays.asList(183);
        List<Integer> netWorkCode = Arrays.asList(42, 201);

        AssetRecordReq req = getAssetRecordReq(asset, assetAttach, org, list);

        if (serversCode.contains(asset.getAssetMode())) {
            if (AssetModeConst.TERMINAL.equals(asset.getDesk())) {
                exportDispatchServers(response, req);
            } else {
                exportServers(response, req);
            }
        } else if (netWorkCode.contains(asset.getAssetMode())) {
            exportNetworkServers(response, req);
        }
    }

    /**
     * 生成终端
     *
     * @param response
     * @param req
     */
    private void exportDispatchServers(HttpServletResponse response, AssetRecordReq req) {
        SXSSFWorkbook createExcel = DispatchRecordExcelUtil.createExcel(req);
        DispatchRecordExcelUtil.responseBody(createExcel, response, "调度台终端履历动态记录表");
    }

    /**
     * 生成网络设备一机一册
     *
     * @param response
     * @param req
     */
    private void exportNetworkServers(HttpServletResponse response, AssetRecordReq req) {
        SXSSFWorkbook createExcel = NetworkRecordExcelUtil.createExcel(req);
        NetworkRecordExcelUtil.responseBody(createExcel, response);
    }

    /**
     * 生成主机的一机一册
     *
     * @param response
     * @param req
     */
    private void exportServers(HttpServletResponse response, AssetRecordReq req) {
        SXSSFWorkbook createExcel = ServersRecordExcelUtil.createExcel(req);
        ServersRecordExcelUtil.responseBody(createExcel, response);
    }

    /**
     * 获取一机一档请求
     *
     * @param asset
     * @param assetAttach
     * @param org
     * @param list
     * @return
     */
    private AssetRecordReq getAssetRecordReq(Asset asset, AssetAttach assetAttach, SysOrg org,
                                             List<AssetHardwareFix> list) {
        StringBuilder installPath = new StringBuilder("");
        if (Objects.nonNull(assetAttach)) {
            Room room = null;
            if (StrUtil.isNotEmpty(assetAttach.getRoomId())) {
                room = roomServ.getById(assetAttach.getRoomId());
            }
            if (Objects.nonNull(room)) {
                installPath.append("机房：");
                installPath.append(room.getName());
                installPath.append(" ");
            }
            Cabinet cabinet = null;
            if (StrUtil.isNotEmpty(assetAttach.getCabinetId())) {
                cabinet = cabinetServ.getById(assetAttach.getCabinetId());
            }
            if (Objects.nonNull(cabinet)) {
                installPath.append("机柜：");
                installPath.append(cabinet.getName());
                installPath.append(" ");
            }
            if (Objects.nonNull(assetAttach.getStartPosition())) {
                installPath.append("起始U位：");
                installPath.append(assetAttach.getStartPosition().toString());
                installPath.append(" ");
            }
            if (Objects.nonNull(assetAttach.getEndPosition())) {
                installPath.append("结束U位：");
                installPath.append(assetAttach.getEndPosition().toString());
                installPath.append(" ");
            }
        }

        String onlineTime = DateUtil.format(asset.getOnlineTime(), "yyyy-MM-dd HH:mm:ss");
        String downLineime = DateUtil.format(asset.getDownlineTime(), "yyyy-MM-dd HH:mm:ss");
        String validityDate = DateUtil.format(asset.getValidityDate(), "yyyy-MM-dd");
        ManufacturersEnum assetSupplier = ManufacturersEnum.getEnum(asset.getAssetSupplier());

        AssetRecordReq req = new AssetRecordReq();
        req.setAssetName(asset.getName());
        req.setHostNumber(asset.getHostNumber());
        req.setDisplayerPortModel(asset.getDisplayerPortModel());
        req.setDisplayerTotal(asset.getDisplayerTotal());
        req.setAssetIp(asset.getIp());
        req.setImei(asset.getSerialNumber());
        req.setInstallPath(installPath.toString());
        req.setAssetImage(asset.getAssetImage());
        req.setAssetMode(asset.getAssetMode());
        req.setServerName(asset.getOperationSystem());
        req.setStartDateStr(onlineTime == null ? "" : onlineTime);
        req.setEndDateStr(downLineime == null ? "" : downLineime);
        req.setManufacturers(assetSupplier);
        req.setPlace(AssetPlaceEnum.getEnum(org.getType()));
        req.setRunModel(AssetRunModelEnum.getMsg(asset.getRunModel()));
        req.setValidityDateStr(validityDate);
        String powerModel = asset.getPowerModel();
        String diskCapacity = asset.getDiskCapacity();
        String memory = asset.getMemory();
        req.setPowerTotal(asset.getPowerTotal() == null ? "" : asset.getPowerTotal().toString());
        req.setPowerModel(StrUtil.isEmpty(powerModel) ? "" : powerModel);
        req.setDiskTotal(asset.getDiskTotal() == null ? "" : asset.getDiskTotal().toString());
        req.setDiskCapacity(StrUtil.isEmpty(diskCapacity) ? "" : diskCapacity);
        req.setMemory(StrUtil.isEmpty(memory) ? "" : memory);

        List<ApplicationBase> applactionList = new ArrayList<ApplicationBase>();
        List<ApplicationVersionBase> versionList = new ArrayList<ApplicationVersionBase>();
        ApplicationVersionBase version = new ApplicationVersionBase();
        version.setCenterSignature("");
        version.setChangeRemark("");
        version.setEndDateStr("");
        version.setSignature("");
        version.setStartDateStr("");
        version.setVersion("");
        versionList.add(version);

        ApplicationBase app = new ApplicationBase();
        app.setName("");
        app.setVersionList(versionList);

        applactionList.add(app);

        req.setApplicationList(applactionList);

        List<HardwareBase> hardwareList = new ArrayList<HardwareBase>();

        for (AssetHardwareFix item : list) {
            HardwareBase hardwareBase = new HardwareBase();
            hardwareBase.setCenterSignature(item.getCenterSignature());
            hardwareBase.setSignature(item.getSupplierSignature());
            hardwareBase.setChangeDateStr(DateUtil.format(item.getFixTime(), "yyyy-MM-dd HH:mm:ss"));
            hardwareBase.setChangeRemark(item.getReason());
            hardwareBase.setName(AssetHardwareTypeEnum.getDecrip(item.getHardwareType()));
            hardwareList.add(hardwareBase);
        }

        HardwareBase hardwareBase = new HardwareBase();
        hardwareBase.setCenterSignature("");
        hardwareBase.setChangeDateStr("");
        hardwareBase.setChangeRemark("");
        hardwareBase.setName("");
        hardwareBase.setSignature("");
        hardwareList.add(hardwareBase);

        req.setHardwareList(hardwareList);
        return req;
    }

    /**
     * 下载导入资产模板
     *
     * @param response
     */
    @GetMapping("/download")
    @ApiOperation(value = "下载模板")
    @ActionLog(name = "下载导入资产模板", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void downloadTemplate(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=asset_import_template.xlsx");
        try {
            String filePath = "/templates/import/asset_import_template.xlsx";
            InputStream fileInput = ApiAssetController.class.getResourceAsStream(filePath);
            ExcelReader reader = ExcelUtil.getReader(fileInput);
            ExcelWriter writer = reader.getWriter();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
        } catch (IOException e) {
            log.error("资产管理-下载模板失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 资产IP地址ping测试
     *
     * @param pingReq: IP地址
     * @Author: syt
     * @Date: 2021/5/19/ 11:02
     */
    @PostMapping("/assetPing")
    @ApiOperation(value = "资产IP地址ping测试")
    @RequiresPermissions("api:asset:assetPing")
    @ActionLog(name = "资产PING测试", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo assetPing(@RequestBody @Valid AssetPingReq pingReq) {
        return assetService.assetPing(pingReq.getId());
    }

    /**
     * telnet测试主机端口是否启用
     *
     * @param telnetReq: telnetReq
     * @return: com.jcca.common.vo.ResultVo
     * @Author: syt
     * @Date: 2021/5/19/019 13:31
     */
    @PostMapping("/assetTelnet")
    @ApiOperation(value = "资产telnet测试主机端口是否启用")
    @ActionLog(name = "资产telnet测试", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo assetTelnet(@RequestBody AssetNetReq telnetReq) {
        return assetService.assetTelnet(telnetReq);
    }

    /**
     * 模板列表
     */
    @PostMapping("/templateList")
    @ApiOperation(value = "资产导入的模板列表")
    @ActionLog(name = "查看资产导入模板列表", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<Object> templateList(Integer page, Integer size) {
        IPage<ImportTemplate> iPage = PagePlugin.startPageT(page, size, ImportTemplate.class);
        QueryWrapper<ImportTemplate> assetQuery = Wrappers.query();
        assetQuery.isNotNull("ID");
        iPage = importTemplateService.page(iPage, assetQuery);
        Map<String, Object> resultMap = new HashMap<>();
        List<ImportTemplate> records = iPage.getRecords();
        resultMap.put("templateList", records);
        resultMap.put("total", iPage.getTotal());
        return ResultVoUtil.success(resultMap);
    }

    /**
     * 导出指定模板
     */
    @GetMapping("/exportTemplate/{id}")
    @ApiOperation(value = "下载指定模板")
    @ActionLog(name = "下载资产导入模板", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void exportExcel(@PathVariable("id") String id, HttpServletResponse response) {
        switch (id) {
            case "1131":
                try {
                    ClassPathResource resource = new ClassPathResource("templates/system/export/simple.xlsx");
                    InputStream is = resource.getInputStream();
                    SXSSFWorkbook sheets = new SXSSFWorkbook(new XSSFWorkbook(is));
                    DispatchRecordExcelUtil.responseBody(sheets, response, "精简版本");
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

                break;
            case "1132":
                try {
                    ClassPathResource resource = new ClassPathResource("templates/system/export/complete.xlsx");
                    InputStream is = resource.getInputStream();
                    SXSSFWorkbook sheets2 = new SXSSFWorkbook(new XSSFWorkbook(is));
                    DispatchRecordExcelUtil.responseBody(sheets2, response, "完整版本");
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                break;

            default:
                Template template = importTemplateService.transformTemplate(importTemplateService.findById(id));

                try {
                    String excelName = template.getTemplateName();
                    template.setTemplateName(null);
                    template.setId(null);
                    template.setType(null);
                    template.setRemark(null);
                    template.setCreateTime(null);
                    template.setModifyTime(null);
                    ArrayList<String> attributeList = TemplateExportUtil.isAllFieldNotNull(template);
                    SXSSFWorkbook excel = TemplateExportUtil.createExcel(attributeList);
                    DispatchRecordExcelUtil.responseBody(excel, response, excelName);
                } catch (Exception e) {
                    log.error("导出模板失败 : ", e);
                }
                break;

        }
    }

    /**
     * 进度条数据
     *
     * @return
     * @Author: sophia
     */

    @PostMapping("/pmgressBar")
    @ApiOperation(value = "资产导入进度条")
    public ResultVo pmgressBar() {
        String taskId = assetImportTaskService.getLastOneId();

        if (StrUtil.isEmpty(taskId)) {
            log.error("获取资产导入进度--任务ID 空");
            return ResultVoUtil.success();
        }

        AssetImportTask assetImportTask = assetImportTaskService.getById(taskId);
        return ResultVoUtil.success(assetImportTask);
    }

    /**
     * 失败资产列表
     *
     * @return
     * @Author: sophia
     */

    @PostMapping("/errorAssetList")
    @ApiOperation(value = "失败资产列表")
    @RequiresPermissions({"api:asset:errorAssetList"})
    @ActionLog(name = "查看导入失败资产列表", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<Object> errorAssetList() {
        List<AssetImportRecord> assetList = assetImportService.list();
        return ResultVoUtil.success(assetList);
    }

    /**
     * 终止运行
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/terminationImport")
    @ApiOperation(value = "终止导入")
    @ActionLog(name = "终止导入", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo terminationImport() {
        try {
            assetImportTaskService.setLastStatus(0);
            redisService.remove("IMPORT_TASK_STATUS");
            log.info("资产管理-请求终止导入任务");
        } catch (Exception e) {
            return ResultVoUtil.success(e.getMessage());
        }
        return ResultVoUtil.success();
    }

    /**
     * 查询导入状态
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/importStatus")
    @ApiOperation(value = "查询导入状态 true=runing false=stop")
    public ResultVo importStatus() {
        Object import_task_status = redisService.get("IMPORT_TASK_STATUS");
        if (ObjectUtil.isNotNull(import_task_status)) {
            return ResultVoUtil.success(true);
        }

        return ResultVoUtil.success(false);
    }

    /**
     * 资产模板开始导入
     *
     * @return
     * @Author: sophia
     */
    @PostMapping("/templateImport")
    @ApiOperation(value = "资产模板开始导入")
    public ResultVo templateImportAsset(@RequestParam("file") MultipartFile file) {
        Object import_task_status = redisService.get("IMPORT_TASK_STATUS");
        if (ObjectUtil.isNotNull(import_task_status)) {
            ResultVo<Object> resultVo = new ResultVo<>();
            resultVo.setCode(ResultEnum.ASSET_IMPORT_REP.getCode());
            resultVo.setMsg("已有导入任务正在运行中");
            return resultVo;
        } else {
            redisService.set("IMPORT_TASK_STATUS", "running", 3600L);
        }
        log.info("资产管理-使用模板批量导入资产信息");

        // 清空历史错误数据
        assetImportService.deleteAllAsset();
        AssetImportTask assetImportTask = new AssetImportTask();
        assetImportTask.setId(MyIdUtil.getId());
        assetImportTask.setSuccess(0);
        assetImportTask.setFail(0);
        assetImportTask.setStatus(1);// 设置程序运行状态

        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }

            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in);

            // 判断模板是否含有基础字段
            List<Object> titleList = reader.readRow(1);
            ResultVo resultVo1 = assetImportService.estimateTemplateEmpty(titleList);
            if (resultVo1.getCode() == ResultEnum.ERROR.getCode()) {
                throw new NullFieldException(resultVo1.getMsg());
            }

            // 判断数据为空
            List<Map<String, Object>> rowList = reader.read(1, 2, 2147483647);
            List<AssetImportRecord> assetImportList = reader.read(1, 2, AssetImportRecord.class);
            if (rowList.isEmpty()) {
                throw new NullFieldException("模板不可为空,请填写数据");
            }

            String header = Convert.toStr(reader.readRow(0));
            String title = Convert.toStr(titleList);
            header = header.substring(1);
            header = header.substring(0, header.length() - 1) + ",导入状态,错误日志";
            title = title.substring(1);
            title = title.substring(0, title.length() - 1) + ",status,errorLog";

            assetImportTask.setCount(rowList.size());
            assetImportTask.setHeader(header);
            assetImportTask.setTitle(title);
            assetImportTask.setLog("成功运行中");
            assetImportTaskService.save(assetImportTask);

            // 异步进行数据导入
            new Thread(new Runnable() {
                @Override
                public void run() {
                    importAsset.forImportAsset(rowList, assetImportList, assetImportTask);
                }
            }).start();

            return ResultVoUtil.success("任务成功开始运行");

        } catch (NullFieldException e) {
            redisService.remove("IMPORT_TASK_STATUS");
            assetImportTask.setLog(e.getMessage());
            assetImportTask.setStatus(0);
            assetImportTaskService.saveOrUpdate(assetImportTask);
            ResultVo<Object> resultVo = new ResultVo<>();
            resultVo.setCode(ResultEnum.ASSET_IMPORT_ERROR.getCode());
            resultVo.setMsg(assetImportTask.getLog());
            return resultVo;
        } catch (IOException e) {
            redisService.remove("IMPORT_TASK_STATUS");
            assetImportTask.setLog("建立链接失败  导入任务失败");
            assetImportTaskService.saveOrUpdate(assetImportTask);
            assetImportTask.setStatus(0);
            ResultVo<Object> resultVo = new ResultVo<>();
            resultVo.setCode(ResultEnum.ASSET_IMPORT_ERROR.getCode());
            resultVo.setMsg(assetImportTask.getLog());
            return resultVo;
        } catch (Exception e) {
            redisService.remove("IMPORT_TASK_STATUS");
            assetImportTask.setLog("导入任务失败");
            assetImportTask.setStatus(0);
            assetImportTaskService.saveOrUpdate(assetImportTask);
            return ResultVoUtil.error(e.getMessage());
        }
    }

    @GetMapping("/exportErrorAsset")
    @ApiOperation(value = "导出失败资产")
    @ActionLog(name = "导出导入失败的资产列表", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void exportErrorAsset(HttpServletResponse response) {
        String taskId = assetImportTaskService.getLastOneId();
        if (StrUtil.isEmpty(taskId)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook(10);
            AssetImportUtils.responseBody(workbook, response, "错误资产导出");
        }

        List<AssetImportRecord> assetList = assetImportService.selectAllAsset();
        AssetImportTask task = assetImportTaskService.getById(taskId);

        if (ObjectUtil.isNull(task.getHeader()) && ObjectUtil.isNull(task.getTitle())) {
            SXSSFWorkbook workbook = new SXSSFWorkbook(10);
            AssetImportUtils.responseBody(workbook, response, "错误资产导出");
        } else {
            List<String> headerList = Arrays.asList(task.getHeader().split(","));
            List<String> titleList = Arrays.asList(task.getTitle().split(","));
            SXSSFWorkbook excel = AssetReportUtil.createExcel(headerList, titleList, assetList);
            AssetImportUtils.responseBody(excel, response, "错误资产(可直接用于导入)");
            // AssetImportUtils.responseBody2(excel, response, "错误资产导出");
        }

    }

    public AssetImportRecord translate(Asset asset) {
        AssetImportRecord assetRecord = new AssetImportRecord();
        try {
            String id = asset.getId();
            AssetAttach attach = assetAttachService.getByAssetId(id);

            assetRecord.setOrgId(sysOrgServ.getById(attach.getOrgId()).getTitle());
            assetRecord.setRoomId(roomService.getById(attach.getRoomId()).getName());

            if (ObjectUtil.isNotNull(attach.getCabinetId())) {
                assetRecord.setCabinetId(cabinetService.getById(attach.getCabinetId()).getName());
            }

            if (ObjectUtil.isNotNull(attach.getStartPosition())) {
                assetRecord.setStartPosition(attach.getStartPosition() + "");
            }
            if (ObjectUtil.isNotNull(attach.getEndPosition())) {
                assetRecord.setEndPosition(attach.getEndPosition() + "");
            }

            if (ObjectUtil.isNotNull(asset.getABFlag())) {
                if (asset.getABFlag() == (byte) 0) {
                    assetRecord.setABFlag("A机");
                } else {
                    assetRecord.setABFlag("B机");
                }
            }

            if (ObjectUtil.isNotNull(asset.getNtpFlag())) {
                if (asset.getNtpFlag() == (byte) 0) {
                    assetRecord.setNtpFlag("否");
                } else {
                    assetRecord.setNtpFlag("是");
                }
            }

            if (ObjectUtil.isNotNull(asset.getShowTopo())) {
                if (asset.getShowTopo() == (byte) 0) {
                    assetRecord.setShowTopo("否");
                } else {
                    assetRecord.setShowTopo("是");
                }
            }

            if (ObjectUtil.isNotNull(asset.getShowCore())) {
                if (asset.getShowCore().equals("SHOW_TOPO_@_SHOW")) {
                    assetRecord.setShowCore("是");
                } else {
                    assetRecord.setShowCore("否");
                }
            }

            if (ObjectUtil.isNotNull(asset.getWatch())) {
                if (asset.getWatch() == (byte) 0) {
                    assetRecord.setWatch("否");
                } else {
                    assetRecord.setWatch("是");
                }
            }

            if (ObjectUtil.isNotNull(asset.getRunModel())) {
                assetRecord.setRunModel(AssetRunModelEnum.getMsg(asset.getRunModel()));
            }

            assetRecord.setMonitorStatus("");
            if (ObjectUtil.isNotNull(asset.getMonitor())) {
                if (asset.getMonitor() == 0) {
                    assetRecord.setMonitorStatus("异常");
                } else if (asset.getMonitor() == 1) {
                    assetRecord.setMonitorStatus("正常");
                } else if (asset.getMonitor() == 2) {
                    assetRecord.setMonitorStatus("未知");
                } else if (asset.getMonitor() == 3) {
                    assetRecord.setMonitorStatus("不监控");
                }
            }


            if (ObjectUtil.isNotNull(asset.getDownlineTime())) {
                assetRecord.setDownlineTime(DateUtil.format(asset.getDownlineTime(), "yyyy-MM-dd"));
            }

            if (ObjectUtil.isNotNull(asset.getOnlineTime())) {
                assetRecord.setOnlineTime(DateUtil.format(asset.getOnlineTime(), "yyyy-MM-dd"));
            }

            if (ObjectUtil.isNotNull(asset.getValidityDate())) {
                assetRecord.setValidityDate(DateUtil.format(asset.getValidityDate(), "yyyy-MM-dd"));
            }

            assetRecord.setAssetMode(DictUtil.keyValue("ASSET_MODE", "" + asset.getDesk()));


            if (ObjectUtil.isNotNull(asset.getCollectionType())) {
                if (asset.getCollectionType() == 0) {
                    assetRecord.setCollectionType("SSH_LINUX");
                } else if (asset.getCollectionType() == 1) {
                    assetRecord.setCollectionType("SNMP_WINDOWS");
                } else if (asset.getCollectionType() == 2) {
                    assetRecord.setCollectionType("SSH_AIX");
                } else if (asset.getCollectionType() == 3) {
                    assetRecord.setCollectionType("TELNET_LINUX");
                } else if (asset.getCollectionType() == 4) {
                    assetRecord.setCollectionType("TELNET_AIX");
                } else {
                    assetRecord.setCollectionType("未知");
                }
            }

            if (ObjectUtil.isNotNull(asset.getCpuCoreNumber())) {
                assetRecord.setCpuCoreNumber(asset.getCpuCoreNumber() + "");
            }

            if (ObjectUtil.isNotNull(asset.getCpuNumber())) {
                assetRecord.setCpuNumber(asset.getCpuNumber() + "");
            }

            if (ObjectUtil.isNotNull(asset.getDiskTotal())) {
                assetRecord.setDiskTotal(asset.getDiskTotal() + "");
            }

            if (ObjectUtil.isNotNull(asset.getLoginPort())) {
                assetRecord.setLoginPort(asset.getLoginPort() + "");
            }

            assetRecord.setManufacturerId(DictUtil.keyValue("ASSET_FACTORY", asset.getManufacturerId() + ""));

            if (ObjectUtil.isNotNull(asset.getPowerTotal())) {
                assetRecord.setPowerTotal(asset.getPowerTotal() + "");
            }

            assetRecord.setAssetCode(asset.getAssetCode());

            assetRecord.setAssetImage(asset.getAssetImage());

            if (ObjectUtil.isNotNull(asset.getAssetSupplier())) {
                assetRecord.setAssetSupplier(DictUtil.keyValue("ASSET_SUPPLIER", asset.getAssetSupplier() + ""));
            }

            assetRecord.setCpuFrequency(asset.getCpuFrequency());
            assetRecord.setCpuModel(asset.getCpuModel());
            assetRecord.setDiskCapacity(asset.getDiskCapacity());
            assetRecord.setDisplayerPortModel(asset.getDisplayerPortModel());
            assetRecord.setDisplayerTotal(asset.getDisplayerTotal());
            assetRecord.setHostNumber(asset.getHostNumber());
            assetRecord.setIp(asset.getIp());
            assetRecord.setIp2(asset.getIp2());
            assetRecord.setIpmiIp(asset.getIpmiIp());
            assetRecord.setIpmiPwd(EncryptUtil.aesDecryptStr(asset.getIpmiPwd()));
            assetRecord.setIpmiUser(asset.getIpmiUser());
            assetRecord.setOsUser(asset.getOsUser());
            assetRecord.setOsPassword(EncryptUtil.aesDecryptStr(asset.getOsPassword()));
            assetRecord.setMemory(asset.getMemory());
            assetRecord.setName(asset.getName());
            assetRecord.setOperationSystem(asset.getOperationSystem());
            assetRecord.setPowerModel(asset.getPowerModel());

            if (ObjectUtil.isNotNull(asset.getRunModel())) {
                assetRecord.setRunModel(DictUtil.keyValue("ASSET_RUN_MODEL", asset.getRunModel() + ""));
            }
            assetRecord.setSerialNumber(asset.getSerialNumber());

            if (ObjectUtil.isNotNull(asset.getStatus())) {
                if (asset.getStatus() == (byte) 0) {
                    assetRecord.setStatus("离线");
                } else if (asset.getStatus() == (byte) 1) {
                    assetRecord.setStatus("在线");
                } else {
                    assetRecord.setStatus("不监控");
                }
            }
            return assetRecord;
        } catch (Exception e) {
            log.error(e.getMessage());
            return assetRecord;
        }
    }


    @GetMapping("/getAssetlist")
    @ApiOperation(value = "获取资产列表")
    public ResultVo getAssetlist() {
        List<AssetManualVo> assetVoList = new ArrayList<>();
        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        for (String assetId : assetIds) {
            Asset byId = assetService.getById(assetId);
            if (Objects.isNull(byId)) continue;
            AssetManualVo vo = new AssetManualVo();
            BeanUtil.copyProperties(byId, vo);
            assetVoList.add(vo);
        }

        return ResultVoUtil.success(assetVoList);
    }

}
