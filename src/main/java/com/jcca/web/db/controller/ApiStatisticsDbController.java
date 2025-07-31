package com.jcca.web.db.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.PageQuery;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.DBTypeEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.FileUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectDB;
import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.entity.CollectTablespace;
import com.jcca.web.collect.service.CollectDBService;
import com.jcca.web.collect.service.CollectDBfileService;
import com.jcca.web.collect.service.CollectTablespaceService;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.common.service.bean.PullAlertLogResultReq;
import com.jcca.web.common.service.bean.PullAlertLogResultResp;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.db.vo.AllBrokenLineVo;
import com.jcca.web.db.vo.BrokenLineVo;
import com.jcca.web.db.vo.StatisticsDbBaseMsgVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 数据库汇总信息
 *
 * @author Lvyp
 */
@Api(tags = "数据库汇总")
@Slf4j
@RestController
@RequestMapping("/api/stdb")
public class ApiStatisticsDbController {

    @Resource
    private ManageDbService dbService;
    @Resource
    private BizManageService bizService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectDBService collectDbServ;
    @Resource
    private CollectTablespaceService collectTablespaceService;
    @Resource
    private CollectDBfileService dBfileService;

    /**
     * 分页查询数据库
     *
     * @return
     */
    @ApiOperation(value = "分页查询数据库")
    @RequiresPermissions({"api:stdb:page"})
    @PostMapping("/page")
    ResultVo<?> pageQuery(@RequestBody PageQuery query) {
        IPage<ManageDb> ipage = PagePlugin.startPageT(query.getPage(), query.getSize(), ManageDb.class);
        QueryWrapper<ManageDb> queryWrapper = new QueryWrapper<ManageDb>();
        List<String> listByBizAndOrg = bizService.listByBizAndOrg(BizManageConstant.M_DB);

        List<List<String>> inSplit = AppListUtils.inSplit(listByBizAndOrg, 900);

        Consumer<QueryWrapper<ManageDb>> consumer = null;
        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = wrapper -> wrapper.in("ID", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<ManageDb>> after = wrapper -> wrapper.or().in("ID", list);
                consumer = consumer.andThen(after);
            }
        }

        if (Objects.nonNull(consumer)) {
            queryWrapper.and(consumer);
        }

        IPage<ManageDb> page = dbService.page(ipage, queryWrapper);

        PageBean<ManageDb> pageBean = new PageBean<ManageDb>();
        pageBean.setContent(page.getRecords());
        pageBean.setTotal(page.getTotal());

        return ResultVoUtil.success(pageBean);
    }

    /**
     * 数据库详情--基础信息查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "数据库基础信息")
    @PostMapping("/baseMsg/{id}")
    ResultVo<?> baseMsg(@PathVariable("id") String id) {
        ManageDb db = dbService.getById(id);
        if (Objects.isNull(db)) {
            return ResultVoUtil.paramError("ID 不存在", String.class);
        }
        Asset asset = assetService.getById(db.getAssetId());
        if (Objects.isNull(asset)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND.getCode(), "资产不存在");
        }
        List<CollectDB> collectDbList = collectDbServ.getRealTimeData(db.getAssetId());

        StatisticsDbBaseMsgVo vo = new StatisticsDbBaseMsgVo();
        vo.setAssetIp(asset.getIp());
        vo.setName(db.getName());
        vo.setDbName(db.getDbName());

        if (CollUtil.isNotEmpty(collectDbList)) {
            CollectDB collectDB = collectDbList.get(0);

            BeanUtil.copyProperties(collectDB, vo);

            vo.setSysUpTime(collectDB.getSysUpTime());
            vo.setSysUpTimeStr(DateUtil.formatBetween(collectDB.getSysUpTime()));
            vo.setCanUseLockSize(collectDB.getCanUseLockSize());
            vo.setDbCachePoolSizeStr(collectDB.getDbCachePoolSize().toString());
            vo.setDbCacheStr(collectDB.getDbCache().toString());
            vo.setDbMemTotalStr(collectDB.getDbMemTotal().toString());
            vo.setDbSessionSize(collectDB.getDbSessionSize());
            vo.setDbTypeStr(DictUtil.getValue("COLLECT_DB_TYPE",collectDB.getDbType().toString()));
            vo.setDbVersion(collectDB.getDbVersion());
            vo.setLanguage(collectDB.getLanguage());
            // 查询表空间
            QueryWrapper<CollectTablespace> query = Wrappers.query();
            query.eq("asset_id", collectDB.getAssetId());
            List<CollectTablespace> tablespaces = collectTablespaceService.list(query);
            for (CollectTablespace item : tablespaces) {
                if (StrUtil.isEmpty(item.getStatus())) {
                    item.setStatus("ONLINE");
                }
            }
            vo.setTablespaceList(tablespaces);

            // 数据文件
            QueryWrapper<CollectDBfile> query1 = Wrappers.query();
            query1.eq("asset_id", collectDB.getAssetId());
            query1.eq("category", 1);
            List<CollectDBfile> list = dBfileService.list(query1);
            for (CollectDBfile item : list) {
                if (StrUtil.isEmpty(item.getStatus())) {
                    item.setStatus("正常");
                }
            }
            vo.setDataFiles(list);

            // 控制文件.
            QueryWrapper<CollectDBfile> query2 = Wrappers.query();
            query2.eq("asset_id", collectDB.getAssetId());
            query2.eq("category", 2);
            List<CollectDBfile> list2 = dBfileService.list(query2);
            for (CollectDBfile item : list2) {
                if (StrUtil.isEmpty(item.getStatus())) {
                    item.setStatus("正常");
                }
            }
            vo.setControlFiles(list2);

            // 日志文件
            QueryWrapper<CollectDBfile> query3 = Wrappers.query();
            query3.eq("asset_id", collectDB.getAssetId());
            query3.eq("category", 3);

            List<CollectDBfile> list3 = dBfileService.list(query3);
            for (CollectDBfile item : list3) {
                if (StrUtil.isEmpty(item.getStatus())) {
                    item.setStatus("正常");
                }
            }
            vo.setLogFiles(list3);
        }

        return ResultVoUtil.success(vo);
    }

    /**
     * 数据库详情--汇总信息查询
     */
    @ApiOperation(value = "汇总信息查询")
    @RequiresPermissions({"api:detail:oracle"})
    @PostMapping("/brokenLine/{id}")
    ResultVo<?> brokenLine(@PathVariable("id") String id) {
        ManageDb db = dbService.getById(id);
        if (Objects.isNull(db)) {
            return ResultVoUtil.paramError("ID 不存在", String.class);
        }

        IPage<CollectDB> iPage = PagePlugin.startPageT(1, 20, CollectDB.class);
        QueryWrapper<CollectDB> queryWrapper = new QueryWrapper<CollectDB>();
        queryWrapper.eq("ASSET_ID", db.getAssetId());
        queryWrapper.orderByDesc("COLLECT_TIME");
        IPage<CollectDB> collectPage = collectDbServ.page(iPage, queryWrapper);

        List<CollectDB> collectList = collectPage.getRecords();

        CollUtil.reverse(collectList);

        List<BrokenLineVo> lockWaitList = new ArrayList<BrokenLineVo>();
        List<BrokenLineVo> lockUsedList = new ArrayList<BrokenLineVo>();
        List<BrokenLineVo> cachePoolList = new ArrayList<BrokenLineVo>();
        List<BrokenLineVo> cacheList = new ArrayList<BrokenLineVo>();
        List<BrokenLineVo> busynessList = new ArrayList<BrokenLineVo>();

        for (CollectDB item : collectList) {
            String hourStr = DateUtil.format(item.getCollectTime(), "HH:mm");

            BrokenLineVo lockLine = new BrokenLineVo();
            lockLine.setX(hourStr);
            lockLine.setY(item.getDbLockWaitRate());
            lockWaitList.add(lockLine);

            BrokenLineVo lockUsed = new BrokenLineVo();
            lockUsed.setX(hourStr);
            lockUsed.setY(item.getDbLockUsedRate());
            lockUsedList.add(lockUsed);

            BrokenLineVo cachePool = new BrokenLineVo();
            cachePool.setX(hourStr);
            cachePool.setY(item.getDbCachePoolhit());
            cachePoolList.add(cachePool);

            BrokenLineVo cache = new BrokenLineVo();
            cache.setX(hourStr);
            cache.setY(item.getCacheHitRate());
            cacheList.add(cache);

            BrokenLineVo busyness = new BrokenLineVo();
            busyness.setX(hourStr);
            busyness.setY(item.getDbBusynessRate());
            busynessList.add(busyness);
        }

        AllBrokenLineVo resp = new AllBrokenLineVo();
        resp.setBusynessList(busynessList);
        resp.setCacheList(cacheList);
        resp.setLockUsedList(lockUsedList);
        resp.setLockWaitList(lockWaitList);
        resp.setSharePoolList(cachePoolList);

        return ResultVoUtil.success(resp);
    }

    /**
     * 数据库详情--汇总信息查询
     */
    @ApiOperation(value = "警告日志下载验证")
    @PostMapping("/verifylAlterLog/{id}")
    ResultVo verifylAlterLog(@PathVariable("id") String id) {
        FileUtil.createTmpPath();
        PullAlertLogResultReq pullAlertLogResultReq = new PullAlertLogResultReq();
        ManageDb db = dbService.getById(id);
        List<CollectDB> realTimeData = collectDbServ.getRealTimeData(db.getAssetId());
        if (realTimeData.isEmpty()) {
            return ResultVoUtil.warning("暂未查询到该数据库采集信息");
        }
        if (Objects.isNull(realTimeData.get(0).getAlertPath()) || realTimeData.get(0).getAlertPath().isEmpty()) {
            return ResultVoUtil.warning("暂未查询到该数据库日志文件地址");
        }
        pullAlertLogResultReq.setCommand(realTimeData.get(0).getAlertPath());
        Asset asset = assetService.getById(db.getAssetId());
        if (Objects.isNull(asset)) {
            return ResultVoUtil.warning("系统未录入数据库所在资产,请先录入并监控此资产");
        }
        if (Objects.isNull(asset.getIp())) {
            return ResultVoUtil.warning("系统未录入数据库所在资产,请先录入并监控此资产");
        }
        if (Objects.isNull(asset.getCollectionType()) || asset.getCollectionType() != 0) {
            return ResultVoUtil.warning("暂时只支持拉取Linux系统日志~");
        }
        if (Objects.isNull(asset.getOsUser())) {
            return ResultVoUtil.warning("未查询到数据库所在资产用户名密码,请在监控管理录入");
        }
        if (Objects.isNull(asset.getOsPassword())) {
            return ResultVoUtil.warning("未查询到数据库所在资产用户名密码,请在监控管理录入");
        }
        pullAlertLogResultReq.setIp(asset.getIp());
        pullAlertLogResultReq.setUserName(asset.getOsUser());
        if (StrUtil.isNotEmpty(asset.getOsPassword())) {
            pullAlertLogResultReq.setPassword(EncryptUtil.aesDecryptStr(asset.getOsPassword()));
        }

        if (Objects.isNull(asset.getPort())) {
            pullAlertLogResultReq.setPort(22);
        } else {
            pullAlertLogResultReq.setPort(asset.getPort());
        }
        try {
            PullAlertLogResultResp PullAlertLogResultResp = dBfileService.verifyDBfile(pullAlertLogResultReq);
            if (StrUtil.isEmpty(PullAlertLogResultResp.getMsg())) {
                PullAlertLogResultResp.setCode(PullAlertLogResultResp.WARRING);
                PullAlertLogResultResp.setMsg("未查询到数据库日志");
            }
            String[] split = PullAlertLogResultResp.getMsg().split("\n");
            pullAlertLogResultReq.setCommand(split[0]);
            return ResultVoUtil.success(pullAlertLogResultReq);
        } catch (Exception e) {
            log.error("数据库日志文件获取失败:{}", e.getMessage(), e);
            return ResultVoUtil.warning("数据库日志文件查询失败:" + e.getMessage());
        }

    }


    /**
     * 数据库详情--汇总信息查询
     */
    @ApiOperation(value = "警告日志下载")
    @PostMapping("/downloalAlertLog")
    @ActionLog(name = "数据库警告日志下载", title = "数据库管理", key = LogTypeConstant.DOWNLOAD)
    void downloalAlterLog(@RequestBody PullAlertLogResultReq pullAlertLogResultReq, HttpServletResponse response) {
        try {
            String[] split = pullAlertLogResultReq.getCommand().split("/");
            String fileName = split[split.length - 1];
            PullAlertLogResultResp PullAlertLogResultResp = dBfileService.downDBfile(pullAlertLogResultReq);
            byte[] bytes = PullAlertLogResultResp.getMsg().getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            response.reset();
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 循环取出流中的数据
            byte[] b = new byte[100];
            int len;
            while ((len = inputStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inputStream.close();
        } catch (Exception e) {
            log.error("数据库日志文件下载失败:{}", e.getMessage(), e);
        }

    }
}
