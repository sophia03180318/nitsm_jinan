package com.jcca.web.asset.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SoftwareType;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SoftwareTypeService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.annotation.DevLog;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.controller.bean.*;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.PromptInfo;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.PromptInfoService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.asset.vo.AssetProcessVo;
import com.jcca.web.asset.vo.ThresholdProcessVo;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.vo.ProcessOnChangeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 进程阈值
 *
 * @author Lvyp
 */
@Slf4j
@Api(tags = "进程阈值管理")
@RestController
@RequestMapping("/api/processThreshold")
public class ThresholdProcessController extends ListenerManager {

    private static Lock lock = new ReentrantLock(true);
    private static final Integer ADD_PROCESS_FLAG = 0;
    private static final Integer REMOVE_PROCESS_FLAG = 1;
    private static final Integer UPDATE_PROCESS_FLAG = 2;

    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private BizManageService bizService;
    @Resource
    private AssetService assetService;
    @Resource
    private OutService collectAgency;
    @Resource
    private RedisService redisService;
    @Resource
    private PromptInfoService promptInfoService;
    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private SoftwareTypeService softwareTypeService;
    @Resource
    private AlarmInfoService alarmInfoService;


    /**
     * 阈值配置分页查询
     *
     * @return ResultVo<?>
     */
    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询")
    ResultVo<?> pageQuery(@RequestBody ThresholdProcessPageQuery query) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        // 根据组织查询进程 syt
        List<String> changeOrgAndChildren = new ArrayList<>();
        if (StrUtil.isNotEmpty(query.getOrgId())) {
            if (query.getType() == OrgTypeConst.PARENT ||
                    query.getType() == OrgTypeConst.GROUP ||
                    query.getType() == OrgTypeConst.LINE ||
                    query.getType() == OrgTypeConst.STATION) {
                QueryWrapper<SysOrg> sysOrgQuery = Wrappers.query();
                sysOrgQuery.like("PIDS", query.getOrgId());
                changeOrgAndChildren = sysOrgService.list(sysOrgQuery).stream().map(SysOrg::getId).collect(Collectors.toList());
                changeOrgAndChildren.add(query.getOrgId());
            } else if (query.getType() == OrgTypeConst.CENTER) {
                changeOrgAndChildren.add(query.getOrgId());
            } else {
                queryWrapper.like("ID", query.getOrgId());
            }
        }
        if (StrUtil.isNotEmpty(query.getAssetCode())) {
            queryWrapper.like("ASSET_CODE", query.getAssetCode());
        }
        if (StrUtil.isNotEmpty(query.getAssetIp())) {
            queryWrapper.like("IP", query.getAssetIp());
        }
        if (StrUtil.isNotEmpty(query.getAssetName())) {
            queryWrapper.like("NAME", query.getAssetName());
        }

        List<List<String>> idSplis;
        boolean onces = true;
        Consumer<QueryWrapper<Asset>> consumer = null;
        if (StrUtil.isEmpty(query.getOrgId())) {
            List<String> shiroAssetIds = ShiroUtil.getSubjectAssetIds();
            shiroAssetIds.add("x");

            idSplis = AppListUtils.inSplit(shiroAssetIds, 900);
            for (List<String> list : idSplis) {
                if (onces) {
                    consumer = wrapper -> wrapper.in("ID", list);
                    onces = false;
                } else {
                    Consumer<? super QueryWrapper<Asset>> after = wrapper -> wrapper.or().in("ID", list);
                    consumer = consumer.andThen(after);
                }
            }
        } else {
            // 使用组织ID作为条件查询 syt
            idSplis = AppListUtils.inSplit(changeOrgAndChildren, 900);
            for (List<String> list : idSplis) {
                if (onces) {
                    consumer = wrapper -> wrapper.in("ORG_ID", list);
                    onces = false;
                } else {
                    Consumer<? super QueryWrapper<Asset>> after = wrapper -> wrapper.or().in("ORG_ID", list);
                    consumer = consumer.andThen(after);
                }
            }
        }


        if (Objects.nonNull(consumer)) {
            queryWrapper.and(consumer);
        }

        List<Asset> assetList = assetService.list(queryWrapper);
        List<String> accetIds = assetList.stream().map(item -> item.getId()).collect(Collectors.toList());
        accetIds.add("x");

        // 所有的进程ID
        List<String> idList = bizService.listByBizAndOrg(BizManageConstant.THRESHOLD_PROCESS);
        IPage<ThresholdProcess> ipage = PagePlugin.startPageT(query.getPage(), query.getSize(), ThresholdProcess.class);
        QueryWrapper<ThresholdProcess> queryWrapperProcess = new QueryWrapper<ThresholdProcess>();
        idList.add("x");
        if (StrUtil.isNotEmpty(query.getProcessName())) {
            queryWrapperProcess.like("PROCESS_NAME", query.getProcessName());
        }
        // queryWrapperProcess.in("ID", idList);
        if (StrUtil.isNotEmpty(query.getProcessName())) {
            queryWrapperProcess.like("PROCESS_NAME", query.getProcessName());
        }
        if (Objects.nonNull(query.getCollectStatus())) {
            queryWrapperProcess.eq("COLLECT_STATUS", query.getCollectStatus());
        }

        List<List<String>> inSplit = AppListUtils.inSplit(accetIds, 900);
        boolean first = true;
        Consumer<QueryWrapper<ThresholdProcess>> processConsumer = null;
        for (List<String> list : inSplit) {
            if (first) {
                processConsumer = wrapper -> wrapper.in("ASSET_ID", list);
                first = false;
            } else {
                Consumer<? super QueryWrapper<ThresholdProcess>> after = wrapper -> wrapper.or().in("ASSET_ID", list);
                processConsumer = processConsumer.andThen(after);
            }
        }

        if (Objects.nonNull(processConsumer)) {
            queryWrapperProcess.and(processConsumer);
        }

        queryWrapperProcess.orderByDesc("CREATE_TIME");
        IPage<ThresholdProcess> page = thresholdProcessService.page(ipage, queryWrapperProcess);
        List<ThresholdProcessVo> bodyList = fatten(page.getRecords(), assetList);

        PageBean<ThresholdProcessVo> pageResult = new PageBean<>();
        pageResult.setContent(bodyList);
        pageResult.setTotal(page.getTotal());
        return ResultVoUtil.success(pageResult);
    }


    /**
     * 进程改造后查询资产可配置的进程
     *
     * @return com.jcca.common.vo.ResultVo<?>
     * @Param [assetId] 资产ID
     * @Author syt
     * @Date 2021/10/26 18:03
     */
    @PostMapping("/queryProcess/{assetId}")
    @ApiOperation(value = "查询资产可配置进程,可获取提示信息")
    ResultVo<?> processQuery(@PathVariable("assetId") String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            return ResultVoUtil.paramError("assetId is null", String.class);
        }
        List<AssetProcessVo> processList;
        try {
            processList = collectAgency.getAllProcess(assetId);
            AppLogUtils.buildLogInfo(LogFunctionEnum.PROCESS_CONFIG, "设备ID：" + assetId, processList);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, "设备ID：" + assetId, e);
            throw new ResultException(ResultEnum.ERROR.getCode(), "拉取采集器进程失败：" + e.getMessage());
        }
        if (Objects.isNull(processList)) {
            return ResultVoUtil.success(new ArrayList<AssetProcessVo>());
        }
        List<AssetProcessVo> result = new ArrayList<>();
        // 查询
        QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_ID", assetId);
        List<String> processNames = thresholdProcessService.list(queryWrapper).stream()
                .map(item -> item.getProcessName()).collect(Collectors.toList());
        // 剔除配置过的进程
        for (AssetProcessVo process : processList) {
            String name = process.getName();
            List<String> filterList = processNames.stream().filter(item -> name.contains(item)).collect(Collectors.toList());
            if (!filterList.isEmpty()) {
                continue;
            }
            if (NumberUtil.isNumber(process.getProcessId())) {
                process.setSortFlag(Long.valueOf(process.getProcessId()));
            } else {
                process.setSortFlag(0L);
            }
            result.add(process);
        }

        result.sort(Comparator.comparing(AssetProcessVo::getSortFlag));

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        if (!result.isEmpty()) {
            return ResultVoUtil.success(resultMap);
        } else {
            return ResultVoUtil.warning("没有可配置进程！请核实后重试！");
        }

    }


    /**
     * 添加进程阈值配置
     *
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "保存资产进程阈值配置")
    @RequiresPermissions("api:processThreshold:add")
    @ActionLog(name = "设置进程阈值", title = "进程管理", key = LogTypeConstant.MODIFY)
    ResultVo<?> add(@RequestBody String addList) {
        JSONObject parseObj = JSONUtil.parseObj(addList);
        String addListStr = parseObj.getStr("addList");
        if (!JSONUtil.isJsonArray(addListStr)) {
            return ResultVoUtil.paramError("请至少选择一个进程", String.class);
        }
        JSONArray parseArray = JSONUtil.parseArray(addListStr);
        List<ThresholdProcessAddReq> reqList = JSONUtil.toList(parseArray, ThresholdProcessAddReq.class);
        if (reqList.isEmpty()) {
            return ResultVoUtil.paramError("请至少选择一个进程", String.class);
        }
        List<ThresholdProcess> entityList = new ArrayList<ThresholdProcess>();

        List<String> processNames = new ArrayList<>();
        try {
            lock.lock();
            for (ThresholdProcessAddReq body : reqList) {
                String validateReq = ValidatorUtils.validateReq(body);
                if (StrUtil.isNotEmpty(validateReq)) {
                    return ResultVoUtil.error("【进程】" + body.getProcessName() + "设置参数校验不通过:" + validateReq);
                }
                // 验证是否已存在
                QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
                queryWrapper.eq("ASSET_ID", body.getAssetId());
                queryWrapper.eq("PROCESS_NAME", body.getProcessName());
                ThresholdProcess one = thresholdProcessService.getOne(queryWrapper);
                if (Objects.nonNull(one)) {
                    return ResultVoUtil.error("该进程已配置同名进程：" + body.getProcessName());
                }
                if (processNames.contains(body.getProcessName())) {
                    return ResultVoUtil.error("提交的进程配置中存在同名进程：" + body.getProcessName());
                }
                ThresholdProcess entity = EntityBeanUtil.copy(body, ThresholdProcess.class);
                entity.setId(MyIdUtil.getId());
                entity.setCollectStatus(StatusEnum.OK.getCode());
                entity.setHostMode(3);
                String remark = body.getRemark();
                if (StrUtil.isNotEmpty(remark) && remark.length() > 500) {
                    entity.setRemark(remark.substring(0, 500));
                } else {
                    entity.setRemark(remark);
                }
                /*entity.setHostMode(Integer.valueOf((int) Math.floor(Math.random() * 1000000000) + (int) Math.floor(Math.random() * 1000)));*/
                entityList.add(entity);
                processNames.add(body.getProcessName());
            }
            // 通知采集变动
            ThresholdProcess process = entityList.get(0);
            ProcessOnChangeVo changeVo = new ProcessOnChangeVo();
            changeVo.setAssetId(process.getAssetId());
            changeVo.setOptFlag(ADD_PROCESS_FLAG);
            changeVo.setProcessNameList(processNames);
            try {
                collectAgency.processOnChange(changeVo);
            } catch (Exception e) {
                if (LogInputUtils.inputError(ServerTypeEnum.PROCESS_MANAGER)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.PROCESS_MANAGER, ErrorCodeEnum.WEB_PROCESS_SEND_PROCESS_CHANGE, "设备ID：" + process.getAssetId(), e.getMessage()), e);
                }
                return ResultVoUtil.error("采集器处理异常");
            }

            try {
                thresholdProcessService.createAll(entityList);
            } catch (Exception e) {
                if (LogInputUtils.inputError(ServerTypeEnum.PROCESS_MANAGER)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.PROCESS_MANAGER, ErrorCodeEnum.WEB_PROCESS_CREATE_ERROR, "设备ID：" + process.getAssetId(), e.getMessage()), e);
                }
                return ResultVoUtil.error("保存失败");
            }
        } finally {
            lock.unlock();
            this.dispatureEvent(new Event());
        }

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 保存软件类型、提示信息
     * sty
     *
     * @return
     */
    @PostMapping("/saveOrUpdatePromptInfo")
    @ApiOperation(value = "保存软件类型、提示信息")
    @RequiresPermissions("api:processThreshold:saveOrUpdatePromptInfo")
    @ActionLog(name = "保存软件类型、提示信息", title = "监控管理", key = LogTypeConstant.ADD)
    public ResultVo<?> saveOrUpdatePromptInfo(@RequestBody PromptInfoReq req) {

        String username = ShiroUtil.getSubject().getUsername();
        String softwareTypeId = req.getSoftwareTypeId();
        List<Map<String, String>> processes = req.getProcesses();
        // 新增类型
        if (StrUtil.isEmpty(softwareTypeId)) {
            if (StrUtil.isEmpty(req.getSoftwareTypeName())) {
                return ResultVoUtil.paramError("新增软件类型名称不可为空！", null);
            }
            QueryWrapper<SoftwareType> softwareTypeQueryWrapper = new QueryWrapper<>();
            softwareTypeQueryWrapper.eq("NAME", req.getSoftwareTypeName());
            List<SoftwareType> list = softwareTypeService.list(softwareTypeQueryWrapper);
            if (!list.isEmpty()) {
                return ResultVoUtil.paramError("此软件类型名称已存在!", null);
            }
            // 新增软件类型
            SoftwareType swt = new SoftwareType();
            swt.setName(req.getSoftwareTypeName());
            String id = MyIdUtil.getId();
            swt.setId(id);
            swt.setCreator(username);
            swt.setCreateTime(new Date());
            boolean save = softwareTypeService.save(swt);
            if (!save) {
                return ResultVoUtil.error("新增软件类型失败！");
            }
            // 新的软件类型下新增的进程名
            List<String> oldProcessNames = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(processes)) {
                for (Map<String, String> map : processes) {
                    String name = map.get("value");
                    if (oldProcessNames.contains(name)) {
                        continue;
                    }
                    oldProcessNames.add(name);
                    PromptInfo promptInfo = new PromptInfo();
                    promptInfo.setId(MyIdUtil.getId());
                    promptInfo.setValue(name);
                    String remark = map.get("remark");
                    if (StrUtil.isNotEmpty(remark) && remark.length() > 500) {
                        promptInfo.setRemark(remark.substring(0, 500));
                    } else {
                        promptInfo.setRemark(remark);
                    }
                    promptInfo.setSoftwareTypeId(id);
                    promptInfo.setCreator(username);
                    promptInfoService.save(promptInfo);
                }
            }

            return ResultVoUtil.success("提示信息模板保存成功！");
        }

        if (CollectionUtil.isEmpty(processes) || processes.size() < 1) {
            return ResultVoUtil.success("提示信息无需更新！");
        }
        promptInfoService.remove(new QueryWrapper<PromptInfo>().eq("SOFTWARETYPE_ID", softwareTypeId));

        ArrayList<PromptInfo> promptInfos = new ArrayList<>();
        List<String> oldProcessNames = new ArrayList<>();
        for (Map<String, String> map : processes) {
            String name = map.get("value");
            if (oldProcessNames.contains(name)) {
                continue;
            }
            oldProcessNames.add(name);
            PromptInfo promptInfo = new PromptInfo();
            promptInfo.setValue(name);
            String remark = map.get("remark");
            if (StrUtil.isNotEmpty(remark) && remark.length() > 500) {
                promptInfo.setRemark(remark.substring(0, 500));
            } else {
                promptInfo.setRemark(remark);
            }
            promptInfo.setSoftwareTypeId(softwareTypeId);
            promptInfos.add(promptInfo);
        }
        promptInfoService.saveBatch(promptInfos);

        return ResultVoUtil.success("提示信息模板更新成功！");
    }

    /**
     * 获取提示信息
     *
     * @return
     */
    @PostMapping("/getPrompt/{typeId}")
    @ApiOperation(value = "获取提示信息")
    @RequiresPermissions("api:processThreshold:getPrompt")
    public ResultVo<?> getPrompt(@PathVariable("typeId") String typeId) {
        List<PromptInfo> infos = promptInfoService.list(new QueryWrapper<PromptInfo>().eq("SOFTWARETYPE_ID", typeId));
        return ResultVoUtil.success(infos);
    }

    @PostMapping("/remark")
    @ApiOperation(value = "设置备注信息{id:'xxx',remark:'xxxxx'}")
    @ActionLog(name = "设置备注信息", title = "监控管理", key = LogTypeConstant.MODIFY)
    ResultVo<?> remark(@RequestBody String req) {
        JSONObject reqjson = JSONUtil.parseObj(req);
        String id = reqjson.getStr("id");
        String remark = reqjson.getStr("remark");

        ThresholdProcess process = thresholdProcessService.getById(id);
        if (Objects.isNull(process)) {
            return ResultVoUtil.paramError("该配置不存在", String.class);
        }

        process.setRemark(remark);

        thresholdProcessService.updateById(process);

        return ResultVoUtil.SAVE_SUCCESS;
    }


    /**
     * 修改进程阈值配置
     *
     * @return ResultVo<?>
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改资产进程阈值配置")
    @RequiresPermissions("api:processThreshold:update")
    @DevLog(title = "业务配置管理", name = "进程阈值配置", dev = DevLogConstant.THRESHOLD_PROCESS, key = LogTypeConstant.DEV)
    ResultVo<?> update(@RequestBody ThresholdProcessUpdateReq req) {
        String validateReq = ValidatorUtils.validateReq(req);
        if (StrUtil.isNotEmpty(validateReq)) {
            return ResultVoUtil.error(validateReq);
        }
        ThresholdProcess process = thresholdProcessService.getById(req.getId());
        if (Objects.isNull(process)) {
            return ResultVoUtil.paramError("该配置不存在", String.class);
        }

        process.setRemark(req.getRemark());
        process.setThresholdCpu(req.getThresholdCpu());
        process.setThresholdMemory(req.getThresholdMemory());

        Asset asset = assetService.getById(process.getAssetId());
        if (!process.getProcessName().equals(req.getProcessName())) {
            //进程名字变更
            process.setProcessName(req.getProcessName());

            ProcessOnChangeVo changeVo = new ProcessOnChangeVo();
            changeVo.setAssetId(process.getAssetId());
            changeVo.setOptFlag(UPDATE_PROCESS_FLAG);
            changeVo.setProcessNameList(Arrays.asList(req.getProcessName()));
            try {
                collectAgency.processOnChange(changeVo);
            } catch (Exception e) {
                if (LogInputUtils.inputError(ServerTypeEnum.PROCESS_MANAGER)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.PROCESS_MANAGER, ErrorCodeEnum.WEB_PROCESS_SEND_PROCESS_CHANGE, "设备ID：" + process.getAssetId(), e.getMessage()), e);
                }
                return ResultVoUtil.error("因采集器异常等原因不可以删除进程，待启动后可以维护");
            }


            thresholdProcessService.updateById(process);
            //更新后刷新下状态重新推送一次 删除标记位置
            redisService.delProcess(asset.getId(), asset.getAssetCode());
        } else {
            thresholdProcessService.updateById(process);
        }

        this.dispatureEvent(new Event());

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除进程阈值配置
     *
     * @param id 删除的IDs
     * @return ResultVo<?>
     */
    @PostMapping("/remove/{id}")
    @ApiOperation(value = "删除资产进程阈值配置")
    @RequiresPermissions("api:processThreshold:remomve")
    @DevLog(title = "业务配置管理", name = "删除进程", dev = DevLogConstant.PROCESS_DEL, key = LogTypeConstant.DEV)
    ResultVo<?> remove(@PathVariable("id") String id) {
        ThresholdProcess process = thresholdProcessService.getById(id);
        if (Objects.isNull(process)) {
            return ResultVoUtil.paramError("该配置不存在", String.class);
        }
        Asset asset = assetService.getById(process.getAssetId());
        // 通知采集变动
        ProcessOnChangeVo changeVo = new ProcessOnChangeVo();
        changeVo.setAssetId(process.getAssetId());
        changeVo.setOptFlag(REMOVE_PROCESS_FLAG);
        changeVo.setProcessNameList(Arrays.asList(process.getProcessName()));
        try {
            collectAgency.processOnChange(changeVo);
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.PROCESS_MANAGER)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.PROCESS_MANAGER, ErrorCodeEnum.WEB_PROCESS_SEND_PROCESS_CHANGE, "设备ID：" + process.getAssetId(), e.getMessage()), e);
            }
            return ResultVoUtil.error("因采集器异常等原因不可以删除进程，待启动后可以维护");
        }
        //  同时清除redis中该进程的缓存信息
        String mapKey = String.format("%s:%s:statusEvent", asset.getIp(), asset.getId());
        String mapKey2 = String.format("%s:%s:statusEventValue", asset.getIp(), asset.getId());
        String assetCode = assetService.getById(process.getAssetId()).getAssetCode();
        redisService.remove("GROUP:PROCESS:" + assetCode + "_" + process.getProcessName() + "_" + process.getHostMode());
        Map<String, Object> hashMap = redisService.getHashMap(mapKey);
        Map<String, Object> hashMap2 = redisService.getHashMap(mapKey2);
        Set<String> keySet1 = hashMap.keySet();
        for (String key : keySet1) {
            if(key.contains(process.getProcessName())){
                redisService.deleteHashMap(mapKey,key);
            }
        }
        Set<String> keySet2 = hashMap2.keySet();
        for (String key : keySet2) {
            Object value = hashMap2.get(key);
            if(Objects.isNull(value)){
                continue;
            }
            if(value.toString().contains(process.getProcessName())){
                redisService.deleteHashMap(mapKey2,key);
            }
        }


        //组进程采集模式集体更改为普通
        if (process.getHostMode() == 1 || process.getHostMode() == 2) {
            thresholdProcessService.updateMode(assetCode, 3);
        }

        //恢复并确认进程相关告警
        alarmInfoService.recoverProcess(process.getProcessName(), process.getAssetId());

        thresholdProcessService.remove(process);
        this.dispatureEvent(new Event());

        return ResultVoUtil.success("删除成功");
    }

    private List<ThresholdProcessVo> fatten(List<ThresholdProcess> process, List<Asset> assetList) {
        List<ThresholdProcessVo> voList = new ArrayList<>();
        for (ThresholdProcess item : process) {
            ThresholdProcessVo body = EntityBeanUtil.copy(item, ThresholdProcessVo.class);
            Asset asset = assetList.stream().filter(obj -> obj.getId().equals(item.getAssetId())).findFirst()
                    .orElse(null);
            body.setAssetCode(asset.getAssetCode());
            body.setAssetIp(asset.getIp());
            body.setAssetName(asset.getName());
            body.setCollectStatusStr(StatusEnum.getMsgByCode(item.getCollectStatus()));
            if (item.getHostMode() != 1 && item.getHostMode() != 2 && item.getHostMode() != 3) {
                body.setHostMode(3);
            }
            voList.add(body);
        }
        return voList;
    }

    /**
     * 双机双活,双击单活,普通模式的改变
     *
     * @param req(id)          : 进程ID
     * @param req(mode)        : 模式:1双机单活,2双机双活,3普通
     * @param req(processName) : 进程名称
     * @return: com.jcca.common.vo.ResultVo<?>
     * @Author: syt
     * @Date: 2021/8/10/010 16:34
     */
    @PostMapping("/modeChange")
    @ApiOperation(value = "改变进程配置模式",
            notes = "同一组下两台机器配置相同进程,进程模式分为双机双活、双击单活，另外还有普通模式。进程可以在这三种模式间进行切换")
    @RequiresPermissions("api:processThreshold:modeChange")
    @DevLog(title = "业务配置管理", name = "修改进程模式", dev = DevLogConstant.PROCESS_MODE, key = LogTypeConstant.DEV)
    public ResultVo<?> modeChange(@RequestBody ChangeProcessModeReq req) {
        ResultVo<?> vo = thresholdProcessService.modeChange(req);
        return ResultVoUtil.success(vo);
    }


    /**
     * 获取组织和资产的树结构
     *
     * @return com.jcca.common.vo.ResultVo<?>
     * @Author: syt
     * @Date: 2021/10/22 16:34
     */
    @PostMapping("/assetTree/{type}")
    @ApiOperation(value = "获取组织资产树", notes = "根据用户获取其组织以及组织下的资产树数据")
    public ResultVo<?> assetTree(@PathVariable("type") Integer type) {
        // 当前用户所有组织
        List<SysOrg> subjectOrgs = ShiroUtil.getSubjectOrgs();
        if (CollectionUtils.isEmpty(subjectOrgs)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "用户没有组织权限");
        }
        // 所有的资产
        QueryWrapper<Asset> query = Wrappers.query();
        List<String> orgIds = subjectOrgs.stream().map(SysOrg::getId).collect(Collectors.toList());
        query.in("ORG_ID", orgIds);
        query.eq("IS_DEL", 1);
        query.eq("WATCH", AssetWatchStatusEnum.WATCH_STATUS_YES.getCode());
        if (type == AssetModeEnum.SERVER.getCode()) {
            query.eq("ASSET_MODE", type);
        }
        List<Asset> assets = assetService.list(query);

        // 精简组装数据
        String topId = "";
        int lenth = 1000;
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<Object> res = new ArrayList<>();
        for (SysOrg org : subjectOrgs) {
            if (org.getPids().length() < lenth) {
                lenth = org.getPids().length();
                topId = org.getId();
            }
            OrgProcessTreeVo orgVo = new OrgProcessTreeVo();
            orgVo.setId(org.getId());
            orgVo.setPid(org.getPid());
            orgVo.setPIds(org.getPids());
            orgVo.setTitle(org.getTitle());
            orgVo.setType(org.getType());
            res.add(orgVo);
        }
        for (Asset asset : assets) {
            AssetProcessTreeVo assetVo = new AssetProcessTreeVo();
            assetVo.setTitle(asset.getName());
            assetVo.setPId(asset.getOrgId());
            assetVo.setStatus(asset.getStatus());
            assetVo.setId(asset.getId());
            assetVo.setType(asset.getAssetMode());
            assetVo.setServiceTypeId(asset.getServiceTypeId());
            res.add(0, assetVo);
        }

        map.put("id", topId);
        map.put("data", res);
        return ResultVoUtil.success(map);
    }


    /**
     * 获取软件类型
     *
     * @return com.jcca.common.vo.ResultVo<?>
     * @Author: syt
     * @Date: 2021/11/10 16:34
     */
    @PostMapping("/getSoftwareType")
    @ApiOperation(value = "获取软件类型", notes = "获取软件类型")
    @RequiresPermissions("api:processThreshold:getSoftwareType")
    public ResultVo<?> getSoftwareType() {
        return ResultVoUtil.success(softwareTypeService.list(new QueryWrapper<SoftwareType>().orderByDesc("CREATE_TIME")));
    }

    /**
     * 删除提示信息
     *
     * @return com.jcca.common.vo.ResultVo<?>
     * @Author: syt
     * @Date: 2021/11/10 10:34
     */
    @PostMapping("/removePrompt/{id}")
    @ApiOperation(value = "删除提示信息",
            notes = "删除提示信息")
    @RequiresPermissions("api:processThreshold:removePrompt")
    @ActionLog(name = "删除提示信息", title = "监控管理", key = LogTypeConstant.MODIFY)
    public ResultVo<?> removePrompt(@PathVariable("id") String id) {
        return ResultVoUtil.success(promptInfoService.removeById(id));
    }
}
