package com.jcca.web.db.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.enums.DBTypeEnum;
import com.jcca.common.enums.ThresholdAutoFlagEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.entity.ThresholdAsset;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.AssetOutVo;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.vo.AssetCollectTestVo;
import com.jcca.web.db.controller.bean.*;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.db.vo.DbAssetVo;
import com.jcca.web.db.vo.ManageDbVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 数据库管理
 *
 * @author
 */
@Slf4j
@Api(tags = "数据库相关")
@RestController
@RequestMapping("/api/db")
public class ApiManageDbController extends ListenerManager {

    @Resource
    private ManageDbService manageDbServ;
    @Resource
    private AssetService assetService;
    @Resource
    private OutService outService;
    @Resource
    private BizManageService bizService;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private RedisService redisService;
    @Resource
    private SpecDictionaryService specDictionaryService;

    /**
     * 分页查询数据库
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "分页查询")
    @RequiresPermissions({"api:db:query"})
    @PostMapping("/query")
    @ActionLog(name = "查看被监控数据库列表", title = "数据库管理", key = LogTypeConstant.QUERY)
    ResultVo<?> query(@RequestBody DbPageReq req) {
        IPage<ManageDb> ipage = PagePlugin.startPageT(req.getPage(), req.getSize(), ManageDb.class);
        QueryWrapper<ManageDb> queryWrapper = new QueryWrapper<ManageDb>();
        if (StrUtil.isNotEmpty(req.getName())) {
            queryWrapper.like("NAME", req.getName());
        }
        if (StrUtil.isNotEmpty(req.getIp())) {
            Asset asset = assetService.findOneByIp(req.getIp());
            if (Objects.isNull(asset)) {
                asset = new Asset();
                asset.setId("x");
            }
            List<String> singletonList = Collections.singletonList(asset.getId());
            List<List<String>> inSplit = AppListUtils.inSplit(singletonList, 900);

            Consumer<QueryWrapper<ManageDb>> consumer = null;
            boolean once = true;
            for (List<String> list : inSplit) {
                if (once) {
                    consumer = wrapper -> wrapper.in("ASSET_ID", list);
                    once = false;
                } else {
                    Consumer<? super QueryWrapper<ManageDb>> after = wrapper -> wrapper.or().in("ASSET_ID", list);
                    consumer = consumer.andThen(after);
                }

            }

            if (Objects.nonNull(consumer)) {
                queryWrapper.and(consumer);
            }

        }

        List<String> bizIds = bizService.listByBizAndOrg(BizManageConstant.M_DB);
        bizIds.add("x");

        List<List<String>> inSplit = AppListUtils.inSplit(bizIds, 900);
        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                queryWrapper.in("ID", list);
                onces = false;
            } else {
                queryWrapper.or().in("ID", list);
            }
        }

        IPage<ManageDb> page = manageDbServ.page(ipage, queryWrapper);

        List<ManageDbVo> copyList = EntityBeanUtil.copyList(page.getRecords(), ManageDbVo.class);
        for (ManageDbVo vo : copyList) {
            Asset asset = assetService.getById(vo.getAssetId());
            vo.setAssetName(asset.getName());
            vo.setAliasStr(asset.getName());
            vo.setIp(asset.getIp());
            vo.setPassword(EncryptUtil.aesDecryptStr(vo.getPassword()));

            ThresholdAsset thresholdAsset = thresholdAssetService.getById(vo.getId());
            if (Objects.nonNull(thresholdAsset)) {
                vo.setTablespaceThreshold(thresholdAsset.getTablespace());
            }
        }

        PageBean<ManageDbVo> pageVo = new PageBean<ManageDbVo>();
        pageVo.setContent(copyList);
        pageVo.setTotal(page.getTotal());

        return ResultVoUtil.success(pageVo);
    }

    /**
     * 验证数据库配置是否有效
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "验证数据库")
    @RequiresPermissions({"api:db:verify"})
    @PostMapping("/verify")
    ResultVo<?> verifyDB(@Validated @RequestBody DbVerifyReq req) {
        String ip = req.getIp();
        Asset asset = assetService.findOneByIp(ip);
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error("非资产中的IP");
        }

        Boolean ping = null;
        try {
            ping = TestIpUtil.ping(ip, 3);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.DB_CHECK,ip,e);
            return ResultVoUtil.error("执行Ping发生异常");
        }
        if (!ping) {
            return ResultVoUtil.error("IP网络不通");
        }

        AssetOutVo assetOutVo = BeanUtil.copyProperties(asset, AssetOutVo.class);
        assetOutVo.setName(req.getDbName());
        assetOutVo.setPort(req.getPort());
        assetOutVo.setOsUser(EncryptUtil.aesEncryptHex(req.getUsername()));
        assetOutVo.setOsPassword(EncryptUtil.aesEncryptHex(req.getPassword()));
        assetOutVo.setAssetMode(AssetModeConst.ORACLE_DB);
        AssetCollectTestVo collectTest = outService.collectTest(assetOutVo);
        if (!"0".equals(collectTest.getCode())) {
            return ResultVoUtil.error(collectTest.getMsg());
        }
        return ResultVoUtil.success("ok");
    }

    /**
     * 新建数据库
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "新建数据库")
    @RequiresPermissions({"api:db:add"})
    @PostMapping("/add")
    @ActionLog(name = "新增数据库监控", title = "数据库管理", key = LogTypeConstant.ADD)
    ResultVo<?> add(@Validated @RequestBody DbAddReq req) {
        Asset asset = assetService.getById(req.getAssetId());
        if (Objects.isNull(asset)) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("资产[" + req.getAssetId() + "]不存在");
            return ResultVoUtil.success("资产[" + req.getAssetId() + "]不存在", testVo);
        }
        if (StrUtil.isEmpty(asset.getOrgId())) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            return ResultVoUtil.success("请先为该资产分配组织", testVo);
        }

        //验证是否有可用指标
        QueryWrapper<SpecDictionary> dictWrapper = new QueryWrapper<>();
        dictWrapper.eq("MANUFACTURER_ID", req.getManufacturerId());
        dictWrapper.eq("SYSTEM_TYPE", req.getDbProtocol());
        dictWrapper.eq("ASSET_IMAGE", req.getAssetImage());
        List<SpecDictionary> specDictionaries = specDictionaryService.list(dictWrapper);

        if(specDictionaries.isEmpty()){
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("请在模板管理设备型号管理中添加对应的采集配置");
            return ResultVoUtil.success("请在模板管理设备型号管理中添加对应的采集配置", testVo);
        }


        // 验证数据库是否已添加
        QueryWrapper<ManageDb> queryWrapper = new QueryWrapper<ManageDb>();
        queryWrapper.eq("ASSET_ID", asset.getId());
        queryWrapper.eq("DB_NAME", req.getDbName());
        queryWrapper.eq("PORT", req.getPort());
        queryWrapper.eq("USERNAME", req.getUsername());
        List<ManageDb> list = manageDbServ.list(queryWrapper);

        if (Objects.nonNull(list) && !list.isEmpty()) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("该数据库已存在请勿重复添加");
            return ResultVoUtil.success("资产[" + req.getAssetId() + "]不存在", testVo);
        }

        // 验证数据库
        Boolean ping = null;
        try {
            ping = TestIpUtil.ping(asset.getIp(), 3);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.DB_CHECK,asset.getIp(),e);
            return ResultVoUtil.error("执行Ping发生异常");
        }
        if (!ping) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("IP[" + asset.getIp() + "]网络不通");
            return ResultVoUtil.success("IP[" + asset.getIp() + "]网络不通", testVo);
        }

        //验证厂商对应是否存在可监控指标
        QueryWrapper<SpecDictionary> specQuery = new QueryWrapper<SpecDictionary>();
        specQuery.eq("ASSET_MODE", AssetModeEnum.DB.getCode());
        specQuery.eq("MANUFACTURER_ID", req.getManufacturerId());
        List<SpecDictionary> configSpec = specDictionaryService.list(specQuery);
        if(Objects.isNull(configSpec)||configSpec.isEmpty()){
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("系统缺少此厂家对应的采集项，请在采集配置中添加");
            return ResultVoUtil.success("系统缺少此厂家对应的采集项，请在采集配置中添加", testVo);
        }

        String pwd = EncryptUtil.aesEncryptHex(req.getPassword());
        AssetOutVo assetOutVo = BeanUtil.copyProperties(asset, AssetOutVo.class);
        assetOutVo.setName(req.getDbName());
        assetOutVo.setPort(req.getPort());
        assetOutVo.setOsUser(req.getUsername());
        assetOutVo.setOsPassword(pwd);
        assetOutVo.setAssetMode(AssetModeConst.ORACLE_DB);
        assetOutVo.setManufacturerId(req.getManufacturerId());
        assetOutVo.setAssetImage(req.getAssetImage());
        assetOutVo.setCollectionType(req.getDbProtocol());
        assetOutVo.setNtpFlag(StatusConst.NO);
        AssetCollectTestVo collectTest = outService.collectTest(assetOutVo);
        if (!"0".equals(collectTest.getCode())) {
            log.error("数据库管理-新增数据库返回失败消息：{}", JSONUtil.toJsonStr(collectTest));
            return ResultVoUtil.success(collectTest.getMsg(), collectTest);
        }

        // 脱敏保存数据
        ManageDb dbEntity = EntityBeanUtil.copy(req, ManageDb.class);
        dbEntity.setPassword(pwd);
        manageDbServ.saveDB(dbEntity, asset.getOrgId());

        // 保存附属信息
        AssetAttach attach = new AssetAttach();
        attach.setAssetId(dbEntity.getId());
        attach.setOrgId(asset.getOrgId());
        attach.setRoomId(asset.getId());
        assetAttachService.save(attach);

        // 通知外部应用
        this.notifyOnChange(dbEntity, OutConst.ADD_ASSET);

        collectTest = new AssetCollectTestVo();
        collectTest.setMsg("成功");
        collectTest.setCode("0");

        return ResultVoUtil.success(collectTest);
    }

    /**
     * 修改数据库
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "修改数据库")
    @RequiresPermissions({"api:db:update"})
    @PostMapping("/update")
    @ActionLog(name = "修改数据库监控", title = "数据库管理", key = LogTypeConstant.MODIFY)
    ResultVo<?> update(@Validated @RequestBody DbUpdateReq req) {
        if (req.getName().length() > 20) {
            return ResultVoUtil.error("数据库名称长度不可超出20");
        }

        Asset asset = assetService.getById(req.getAssetId());
        if (Objects.isNull(asset)) {
            AssetCollectTestVo collectTest = new AssetCollectTestVo();
            collectTest.setCode("1");
            collectTest.setMsg("资产不存在");
            return ResultVoUtil.success(collectTest);
        }
        if (StrUtil.isEmpty(asset.getOrgId())) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            return ResultVoUtil.success("请先为该资产分配组织", testVo);
        }
        ManageDb db = manageDbServ.getById(req.getId());
        if (Objects.isNull(db)) {
            AssetCollectTestVo collectTest = new AssetCollectTestVo();
            collectTest.setCode("1");
            collectTest.setMsg("数据库不存在");
            return ResultVoUtil.success(collectTest);
        }

        // 验证数据库是否已添加
        QueryWrapper<ManageDb> queryWrapper = new QueryWrapper<ManageDb>();
        queryWrapper.eq("ASSET_ID", asset.getId());
        queryWrapper.eq("DB_NAME", req.getDbName());
        queryWrapper.eq("PORT", req.getPort());
        queryWrapper.eq("USERNAME", req.getUsername());
        queryWrapper.ne("ID", db.getId());
        List<ManageDb> list = manageDbServ.list(queryWrapper);

        if (Objects.nonNull(list) && !list.isEmpty()) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("该数据库已存在请勿重复添加");
            return ResultVoUtil.success("资产[" + req.getAssetId() + "]不存在", testVo);
        }

        // 验证数据库
        Boolean ping = null;
        try {
            ping = TestIpUtil.ping(asset.getIp(), 3);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.DB_CHECK,asset.getIp(),e);
            return ResultVoUtil.error("执行Ping发生异常");
        }
        if (!ping) {
            AssetCollectTestVo collectTest = new AssetCollectTestVo();
            collectTest.setCode("1");
            collectTest.setMsg("IP网络不通");
            return ResultVoUtil.success(collectTest);
        }

        //验证厂商对应是否存在可监控指标
        QueryWrapper<SpecDictionary> specQuery = new QueryWrapper<SpecDictionary>();
        specQuery.eq("ASSET_MODE", AssetModeEnum.DB.getCode());
        specQuery.eq("MANUFACTURER_ID", req.getManufacturerId());
        List<SpecDictionary> configSpec = specDictionaryService.list(specQuery);
        if(Objects.isNull(configSpec)||configSpec.isEmpty()){
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode("1");
            testVo.setMsg("系统缺少此厂家对应的采集项，请在采集配置中添加");
            return ResultVoUtil.success("系统缺少此厂家对应的采集项，请在采集配置中添加", testVo);
        }

        ManageDb dbEntity = EntityBeanUtil.copy(req, ManageDb.class);
        String pwd = EncryptUtil.aesEncryptHex(req.getPassword());
        AssetOutVo assetOutVo = BeanUtil.copyProperties(asset, AssetOutVo.class);
        assetOutVo.setName(req.getDbName());
        assetOutVo.setPort(dbEntity.getPort());
        assetOutVo.setOsUser(dbEntity.getUsername());
        assetOutVo.setOsPassword(pwd);
        assetOutVo.setAssetMode(AssetModeConst.ORACLE_DB);
        assetOutVo.setManufacturerId(req.getManufacturerId());
        assetOutVo.setAssetImage(req.getAssetImage());
        assetOutVo.setCollectionType(req.getDbProtocol());
        assetOutVo.setNtpFlag(StatusConst.NO);
        AssetCollectTestVo collectTest = outService.collectTest(assetOutVo);
        if (!"0".equals(collectTest.getCode())) {
            log.error("数据库管理-修改数据库返回失败消息：{}", JSONUtil.toJsonStr(collectTest));
            return ResultVoUtil.success(collectTest.getMsg(), collectTest);
        }

        dbEntity.setPassword(pwd);
        manageDbServ.updateDb(dbEntity, asset);

        // 通知外部应用
        dbEntity.setPassword(pwd);
        this.notifyOnChange(dbEntity, OutConst.MODIFY_ASSET);

        collectTest = new AssetCollectTestVo();
        collectTest.setMsg("成功");
        collectTest.setCode("0");

        return ResultVoUtil.success(collectTest);
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @ApiOperation(value = "删除数据库")
    @RequiresPermissions({"api:db:remove"})
    @PostMapping("/remove/{id}")
    @ActionLog(name = "取消数据库监控", title = "数据库管理", key = LogTypeConstant.QUERY)
    ResultVo<?> remove(@PathVariable("id") String ids) {
        ManageDb db = manageDbServ.getById(ids);
        if (Objects.isNull(db)) {
            return ResultVoUtil.error("DB不存在");
        }

        manageDbServ.removeDBById(db, null);

        return ResultVoUtil.REMOVE_SUCCESS;
    }

    private void notifyOnChange(ManageDb db, Integer optFlag) {
        Asset a = assetService.getById(db.getAssetId());

        Asset asset = new Asset();
        asset.setOrgId(a.getOrgId());
        asset.setId(db.getId());
        asset.setName(db.getDbName());
        asset.setIp(a.getIp());
        asset.setPort(db.getPort());
        asset.setOsUser(db.getUsername());
        asset.setOsPassword(db.getPassword());
        asset.setAssetMode(AssetModeConst.ORACLE_DB);
        asset.setCollectionType(db.getDbProtocol());
        asset.setManufacturerId(db.getManufacturerId());
        asset.setAssetImage(db.getAssetImage());
        asset.setNtpFlag(StatusConst.NO);
        outService.notifyOnChange(optFlag, asset);
    }

    /**
     * 查询可用资产
     *
     * @return
     */
    @ApiOperation(value = "查询可用资产")
    @PostMapping("/assetList")
    ResultVo<?> assetList() {
        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        assetIds.add("x");
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();

        List<List<String>> inSplit = AppListUtils.inSplit(assetIds, 900);

        Consumer<QueryWrapper<Asset>> consumer = null;
        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = wrapper -> wrapper.in("ID", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<Asset>> after = wrapper -> wrapper.or().in("ID", list);
                consumer = consumer.andThen(after);
            }
        }

        if (Objects.nonNull(consumer)) {
            queryWrapper.and(consumer);
        }

        queryWrapper.eq("ASSET_MODE", AssetModeEnum.SERVER.getCode());
        queryWrapper.isNotNull("IP");

        List<Asset> assetList = assetService.list(queryWrapper);
        List<DbAssetVo> collect = assetList.stream().map(item -> EntityBeanUtil.copy(item, DbAssetVo.class))
                .collect(Collectors.toList());

        return ResultVoUtil.success(collect);
    }

    /**
     * 设置数据库阈值
     *
     * @return
     */
    @PostMapping("/threshold")
    @ApiOperation(value = "设置数据库表空间阈值")
    @RequiresPermissions({"api:db:threshold"})
    @ActionLog(name = "设置数据库表空间阈值", title = "数据库管理", key = LogTypeConstant.MODIFY)
    public ResultVo<?> threshold(@Validated @RequestBody DbThresholdReq req) {
        Integer threshold = req.getTablespaceThreshold();
        if (Objects.nonNull(threshold) && threshold > 100) {
            return ResultVoUtil.error("阈值不能大于100");
        }
        ManageDb db = manageDbServ.getById(req.getId());
        ThresholdAsset thresholdAsset = thresholdAssetService.findByAssetId(db.getAssetId());
        boolean isOnce = Objects.isNull(thresholdAsset);
        if (isOnce) {
            thresholdAsset = new ThresholdAsset();
        }
        thresholdAsset.setAssetId(db.getAssetId());
        thresholdAsset.setTablespace(threshold);
        thresholdAsset.setAutoFlag(ThresholdAutoFlagEnum.MODE_THRESHOLD.getCode());
        thresholdAsset.setAssetMode(AssetModeConst.ORACLE_DB);

        if (isOnce) {
            thresholdAssetService.save(thresholdAsset);
        } else {
            thresholdAssetService.updateById(thresholdAsset);
        }

        redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + req.getId());
        this.dispatureEvent(new Event());

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 查看数据库阈值
     *
     * @return
     */
    @PostMapping("/view")
    @ApiOperation(value = "查看数据库表空间阈值")
    @ActionLog(name = "查看数据库表空间阈值", title = "数据库管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> view(@Validated @RequestBody DbThresholdReq req) {
        ManageDb db = manageDbServ.getById(req.getId());
        ThresholdAsset thresholdAsset = thresholdAssetService.getById(db.getAssetId());
        if (Objects.isNull(thresholdAsset)) {
            return ResultVoUtil.success();
        }

        return ResultVoUtil.success(thresholdAsset.getTablespace());
    }

}
