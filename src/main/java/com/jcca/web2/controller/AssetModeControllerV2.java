package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.entity.AssetMode;
import com.jcca.web2.entity.AssetModel;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.AssetModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @description: 资产类型  182 201
 * @author: sophia
 * @create: 2023/11/02 14:43
 **/

@RestController
@RequestMapping("/api/v2/assetMode")
@Api(tags = "资产类型管理V2")
public class AssetModeControllerV2 {

    @Resource
    private AssetModeService modeService;
    @Resource
    private AssetModelService assetModelService;


    @PostMapping("/index")
    @ApiOperation("获取类型列表")
    @RequiresPermissions("api:assetMode:index")
    public ResultVo<Object> index(@RequestBody AssetMode mode) {
        QueryWrapper<AssetMode> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(mode.getName())) {
            queryWrapper.like("NAME", mode.getName());
        }
        if (ObjectUtil.isNotNull(mode.getCode())) {
            queryWrapper.eq("CODE", mode.getCode());
        }
            queryWrapper.orderByDesc("MODIFY_TIME");
        return ResultVoUtil.success(modeService.list(queryWrapper));
    }


    @GetMapping("/delete/{id}")
    @ApiOperation("删除类型")
    @RequiresPermissions("api:assetMode:delete")
    @ActionLog(name = "删除类型", title = "资产类型管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> delete(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        AssetMode mode = modeService.getById(id);
        if (ObjectUtil.isNull(mode)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND.getCode(), "未找到对应数据");
        }
        QueryWrapper<AssetModel> qw = new QueryWrapper<>();
        qw.eq("ASSET_MODE_ID", mode.getId());
        List<AssetModel> list = assetModelService.list(qw);
        if (list.isEmpty()) {
            modeService.removeById(id);
            return ResultVoUtil.success("删除类型成功");
        } else {
            return ResultVoUtil.warning("类型下已有型号,无法直接删除");
        }
    }


    @PostMapping("/edit")
    @ApiOperation("编辑类型")
    @RequiresPermissions("api:assetMode:edit")
    @ActionLog(name = "编辑类型", title = "资产类型管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> edit(@RequestBody AssetMode mode) {
        if (ObjectUtil.isNull(mode.getCode())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "类型代码不可为空");
        }
        if (StringUtils.isEmpty(mode.getName())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可为空");
        }

        QueryWrapper<AssetMode> qw = new QueryWrapper<>();
        qw.eq("NAME", mode.getName());
        List<AssetMode> name = modeService.list(qw);
        if (ObjectUtil.isNotNull(name) && !name.isEmpty() && !name.get(0).getId().equals(mode.getId())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可重复，请修改");
        }

        QueryWrapper<AssetMode> qw1 = new QueryWrapper<>();
        qw1.eq("CODE", mode.getCode());
        List<AssetMode> code = modeService.list(qw1);
        if (ObjectUtil.isNotNull(code) && !code.isEmpty() && !code.get(0).getId().equals(mode.getId())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "类型代码不可重复，请修改");
        }
        if (ObjectUtil.isNull(mode.getAmode())) {
            mode.setAmode(mode.getCode());
        }
        modeService.saveOrUpdate(mode);
        return ResultVoUtil.success("修改成功");
    }


    @PostMapping("/add")
    @ApiOperation("新增类型")
    @RequiresPermissions("api:assetMode:add")
    @ActionLog(name = "新增类型", title = "资产类型管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> add(@RequestBody AssetMode mode) {
        if (ObjectUtil.isNull(mode.getCode())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "类型代码不可为空");
        }
        if (StringUtils.isEmpty(mode.getName())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可为空");
        }

        QueryWrapper<AssetMode> qw = new QueryWrapper<>();
        qw.eq("NAME", mode.getName());
        List<AssetMode> name = modeService.list(qw);
        if (ObjectUtil.isNotNull(name) && !name.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可重复，请修改");
        }

        QueryWrapper<AssetMode> qw1 = new QueryWrapper<>();
        qw1.eq("CODE", mode.getCode());
        List<AssetMode> code = modeService.list(qw1);
        if (ObjectUtil.isNotNull(code) && !code.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "类型代码不可重复，请修改");
        }
        if (ObjectUtil.isNull(mode.getAmode())) {
            mode.setAmode(mode.getCode());
        }
        modeService.save(mode);
        return ResultVoUtil.success("保存成功");
    }

    @GetMapping("/getModeMap")
    @ApiOperation("获取类型列表")
    public ResultVo<Object> getModeMap() {
        Map<Integer, String> modeMap = modeService.getModeMap();
        return ResultVoUtil.success(modeMap);
    }

    @GetMapping("/getModeCode")
    @ApiOperation("获取资产类型列表")
    public ResultVo<Object> getModeCode() {
        List<AssetMode> list = modeService.list();
        return ResultVoUtil.success(list);
    }

    @GetMapping("/getManufacturerMode/{manufacturerId}")
    @ApiOperation("获取厂商类型列表")
    public ResultVo<Object> getManufacturerMode(@PathVariable String manufacturerId) {
        List<AssetMode> list = modeService.getManufacturerMode(manufacturerId);
        return ResultVoUtil.success(list);
    }

    @GetMapping("/getModelMode/{modelId}")
    @ApiOperation("获取型号类型")
    public ResultVo<Object> getModelMode(@PathVariable String modelId) {
        AssetMode mode = modeService.getModelMode(modelId);
        return ResultVoUtil.success(mode);
    }

}