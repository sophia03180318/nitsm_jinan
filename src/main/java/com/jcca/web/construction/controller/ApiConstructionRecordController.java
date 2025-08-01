package com.jcca.web.construction.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.construction.controller.bean.ConstructionRecordPageReq;
import com.jcca.web.construction.entity.ConstructionRecord;
import com.jcca.web.construction.service.ConstructionRecordService;
import com.jcca.web.construction.vo.ConstructionRecordVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 维护计划
 *
 * @author lyp
 */
@Slf4j
@Api(tags = "维护计划相关接口")
@RestController
@RequestMapping("/api/construction")
public class ApiConstructionRecordController {

    private static final String ASSET_ID_SPLIT_FLG = ",";
    @Resource
    private ConstructionRecordService constructionServ;
    @Resource
    private AssetService assetService;
    @Resource
    private AlarmInfoService infoService;
    @Resource
    private SysOrgService sysOrgService;

    /**
     * 维护计划分页查询
     *
     * @param req 查询记录 ，参见ConstructionRecordPageReq
     * @return ResultVo<?>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiOperation(value = "维护计划分页查询")
    @RequiresPermissions({"api:construction:pageQuery"})
    @PostMapping("/pageQuery")
    @ActionLog(name = "查看维护记录列表", title = "维护记录", key = LogTypeConstant.QUERY)
    ResultVo pageQuery(@RequestBody ConstructionRecordPageReq req) {
        log.info("维护计划分页查询，参数：{}", JSONUtil.toJsonStr(req));

        IPage page = PagePlugin.startPage(req.getPage(), req.getSize());
        QueryWrapper<ConstructionRecord> queryWrapper = new QueryWrapper<>();

        String id = null;
        List<String> defaultAllOrg;
        if (StrUtil.isEmpty(req.getOrgId())) {
            // 查询条件为空时默认展示第一个组织下的手册 syt
            id = sysOrgService.getDefaultOrg().getId();
            defaultAllOrg = allOrgs(id);
        } else {
            id = req.getOrgId();
            defaultAllOrg = allOrgs(id);
        }


        queryWrapper.in("ORG_ID", defaultAllOrg);

        if (StrUtil.isNotEmpty(req.getName())) {
            queryWrapper.like("NAME", req.getName());
        }
        Date startTime = req.getStartTime();
        Date endTime = req.getEndTime();
        if (Objects.nonNull(startTime)) {
            Calendar instance = Calendar.getInstance();
            instance.setTime(startTime);
            Calendar beginOfMonth = DateUtil.beginOfMonth(instance);
            queryWrapper.ge("START_TIME", beginOfMonth.getTime());
        }
        if (Objects.nonNull(endTime)) {
            Calendar instance = Calendar.getInstance();
            instance.setTime(endTime);
            Calendar endOfMonth = DateUtil.endOfMonth(instance);
            queryWrapper.le("START_TIME", endOfMonth.getTime());
        }

        queryWrapper.orderByDesc("START_TIME");
        IPage iPage = constructionServ.page(page, queryWrapper);
        List records = iPage.getRecords();
        List<ConstructionRecordVo> copyList = EntityBeanUtil.copyList(records, ConstructionRecordVo.class);
        for (ConstructionRecordVo vo : copyList) {
            List<JSONObject> influenceList = getInfluenceList(vo);
            vo.setInfluenceMsg(influenceList);
        }

        PageBean<ConstructionRecordVo> pageResult = new PageBean<>();
        pageResult.setContent(copyList);
        pageResult.setTotal(iPage.getTotal());

        return ResultVoUtil.success(pageResult);
    }

    private List<String> allOrgs(String id) {
        List<String> defaultAllOrg = new ArrayList<>();
        // 查询组织ID
        QueryWrapper<SysOrg> query = Wrappers.query();
        query.like("PIDS", id);

        if (sysOrgService.getById(id).getType() == OrgTypeConst.CENTER) {
            // 组织级别为中心时只展示中心的数据
        } else {
            defaultAllOrg = sysOrgService.list(query).stream().map(SysOrg::getId).collect(Collectors.toList());
        }
        defaultAllOrg.add(id);
        return defaultAllOrg;
    }

    /**
     * @param vo 施工记录，参见ConstructionRecordVo
     * @return List<JSONObject>
     */
    private List<JSONObject> getInfluenceList(ConstructionRecordVo vo) {
        String[] assetIds = vo.getInfluence().split(ASSET_ID_SPLIT_FLG);
        List<JSONObject> assetList = new ArrayList<>();
        for (int i = 0; i < assetIds.length; i++) {
            Asset asset = assetService.getById(assetIds[i].trim());
            if (Objects.isNull(asset)) {
                continue;
            }
            JSONObject assetJson = new JSONObject();
            assetJson.put("orgId", asset.getOrgId());
            assetJson.put("id", asset.getId());
            assetJson.put("name", asset.getName());
            assetJson.put("ip", asset.getIp());
            assetList.add(assetJson);
        }

        return assetList;

    }


    /**
     * 维护计划更新
     *
     * @param req 更新的信息，参见ConstructionRecord
     * @return ResultVo<?>
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "维护计划更新")
    @RequiresPermissions({"api:construction:edit"})
    @PostMapping("/edit")
    @ActionLog(name = "修改维护计划", title = "维护记录", key = LogTypeConstant.MODIFY)
    ResultVo edit(@Validated @RequestBody ConstructionRecord req) {
        if (req.getStartTime().compareTo(req.getEndTime()) > 0) {
            return ResultVoUtil.error("开始时间不能晚于结束时间");
        }

        String[] split = req.getInfluence().split(ASSET_ID_SPLIT_FLG);
        for (String assetId : split) {
            Asset asset = assetService.getById(assetId);
            if (Objects.isNull(asset)) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "该资产不存在" + assetId);
            }
        }

        String id = req.getId();
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "缺少待更新的主键");
        }
        ConstructionRecord target = constructionServ.getById(id);
        if (Objects.isNull(target)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不存在");
        }
        ConstructionRecord record = new ConstructionRecord();
        BeanUtils.copyProperties(req, record);
        boolean ok = constructionServ.updateById(record);
        if (!ok) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "数据更新失败");
        }
        infoService.blankAlarm(record);
        return ResultVoUtil.success();
    }

    /**
     * 维护计划保存
     *
     * @param req 新增的具体信息，参见ConstructionRecord
     * @return ResultVo<?>
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "维护计划新增")
    @RequiresPermissions({"api:construction:save"})
    @PostMapping("/save")
    @ActionLog(name = "新增维护计划", title = "维护记录", key = LogTypeConstant.ADD)
    ResultVo save(@Validated @RequestBody ConstructionRecord req) {
        String[] split = req.getInfluence().split(ASSET_ID_SPLIT_FLG);
        for (String assetId : split) {
            Asset asset = assetService.getById(assetId);
            if (Objects.isNull(asset)) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "该资产不存在" + assetId);
            }
        }

        if (req.getStartTime().compareTo(req.getEndTime()) == 1) {
            return ResultVoUtil.error("开始时间不能晚于结束时间");
        }
        constructionServ.createV2(req);
        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除
     *
     * @param ids 删除记录的ID
     * @return ResultVo<?>
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "维护计划删除")
    @RequiresPermissions({"api:construction:remove"})
    @PostMapping("/remove/{id}")
    @ActionLog(name = "删除维护计划", title = "维护记录", key = LogTypeConstant.REMOVEE)
    ResultVo remove(@PathVariable("id") String ids) {
        if (StrUtil.isEmpty(ids)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不存在");
        }
        constructionServ.removeConstruction(ids);
        return ResultVoUtil.success();
    }

}
