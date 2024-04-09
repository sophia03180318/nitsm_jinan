package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.ThresholdAutoFlagConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web2.dto.ThresholdAssetListDto;
import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.service.ThresholdManageService;
import com.jcca.web2.vo.AssetBaseInfoVo;
import com.jcca.web2.vo.ThresholdManageVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 阈值管理
 * @className ThresholdControllerV2
 * @date 2023/12/8 15:46
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/threshold")
@Api(tags = "阈值管理V2")
public class ThresholdControllerV2 extends ListenerManager {

    @Resource
    private ThresholdManageService thresholdManageService;

    @PostMapping("/list")
    @ApiOperation("获取阈值配置列表")
    public ResultVo<Object> list(@RequestBody ThresholdManageQuery manage) {

        List<ThresholdManageVo> voList = thresholdManageService.getList(manage);

        return ResultVoUtil.success(voList);
    }

    @PostMapping("/save")
    @ApiOperation("保存阈值配置")
    @RequiresPermissions("api:v2:threshold:save")
    @ActionLog(name = "保存阈值配置", title = "阈值管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> save(@RequestBody List<ThresholdManage> list) {

        for (ThresholdManage manage : list) {
            Integer reset = manage.getReset();
            if (Objects.isNull(reset)) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "是否重置不能为空");
            }

            thresholdManageService.add(manage);
        }

        this.dispatureEvent(new Event());

        return ResultVoUtil.success();
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取阈值详情")
    public ResultVo<Object> detail(@PathVariable String id) {

        return ResultVoUtil.success(thresholdManageService.getById(id));
    }

    @GetMapping("/del/{id}")
    @ApiOperation("删除阈值配置")
    @RequiresPermissions("api:v2:threshold:del")
    @ActionLog(name = "删除阈值配置", title = "阈值管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> del(@PathVariable String id) {
        ThresholdManage one = thresholdManageService.getById(id);
        if (Objects.isNull(one)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }

        thresholdManageService.removeById(id);

        this.dispatureEvent(new Event());

        return ResultVoUtil.success();
    }

    @PostMapping("/assetList")
    @ApiOperation("获取阈值列表")
    public ResultVo<Object> assetList(@RequestBody ThresholdAssetListDto dto) {

        if (CollectionUtils.isEmpty(dto.getOrgIds())) {
            dto.setOrgIds(null);
        }

        List<AssetBaseInfoVo> voList = thresholdManageService.getAssetList(dto);

        return ResultVoUtil.success(voList);
    }

    @PostMapping("/batchList")
    @ApiOperation("获取批量阈值列表")
    public ResultVo<Object> batchList(@RequestBody ThresholdAssetListDto dto) {

        List<ThresholdManageVo> list = thresholdManageService.batchList(dto);

        return ResultVoUtil.success(list);
    }

    @GetMapping("/batchDel")
    @ApiOperation("批量删除阈值")
    @RequiresPermissions("api:v2:threshold:del")
    @ActionLog(name = "批量删除阈值", title = "阈值管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> batchDel(String orgId, Integer assetDesk, String category) {
        if (StringUtils.isEmpty(orgId) || Objects.isNull(assetDesk) || StringUtils.isEmpty(category)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        UpdateWrapper<ThresholdManage> update = Wrappers.update();
        update.eq("ORG_ID", orgId);
        update.eq("ASSET_DESK", assetDesk);
        update.eq("CATEGORY", category);
        update.eq("AUTO_FLAG", ThresholdAutoFlagConst.ORG_THRESHOLD);

        thresholdManageService.remove(update);

        this.dispatureEvent(new Event());

        return ResultVoUtil.success();
    }
}
