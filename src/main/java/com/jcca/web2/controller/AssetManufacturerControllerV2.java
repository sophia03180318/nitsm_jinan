package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.entity.AssetManufacturer;
import com.jcca.web2.entity.AssetModel;
import com.jcca.web2.service.AssetManufacturerService;
import com.jcca.web2.service.AssetModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: 资产厂商
 * @author: sophia
 * @create: 2023/11/02 14:43
 **/

@RestController
@RequestMapping("/api/v2/manufacturer")
@Api(tags = "资产厂商管理V2")
public class AssetManufacturerControllerV2 {
    @Resource
    private AssetManufacturerService manufacturerService;
    @Resource
    private AssetModelService modelService;
    @Resource
    private SpecDictionaryService specDictionaryService;

    @PostMapping("/index")
    @ApiOperation("获取厂商列表")
    public ResultVo<Object> index() {
        QueryWrapper<AssetManufacturer> query = Wrappers.query();
        query.orderByDesc("MODIFY_TIME");
        List<AssetManufacturer> list = manufacturerService.list(query);
        QueryWrapper<AssetModel> qw = new QueryWrapper<AssetModel>();
        qw.orderByAsc("MODEL");
        List<AssetModel> all = modelService.list(qw);
        for (AssetManufacturer a : list) {
            List<AssetModel> models = new ArrayList<>();
            Long aid = a.getId();
            for (AssetModel b : all) {
                Long bid = b.getManufacturerId();
                if (aid.longValue() == bid.longValue()) {
                    models.add(b);
                }
            }
            a.setModels(models);
        }

        return ResultVoUtil.success(list);
    }

    @GetMapping("/delete/{id}")
    @ApiOperation("删除厂商")
    @RequiresPermissions("api:manufacturer:delete")
    @ActionLog(name = "删除厂商", title = "资产厂商管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> delete(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }

        List<AssetModel> models = modelService.getByManufacturerId(id);
        if (!CollectionUtils.isEmpty(models)) {
            return ResultVoUtil.error(ResultEnum.DATA_DELETE.getCode(), "有在用厂商型号数据不能删除");
        }

        List<SpecDictionary> specs = specDictionaryService.findByManufacturerId(id);
        if (!CollectionUtils.isEmpty(specs)) {
            QueryWrapper<SpecDictionary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("MANUFACTURER_ID", id);
            specDictionaryService.remove(queryWrapper);
        }

        manufacturerService.removeById(id);
        return ResultVoUtil.success("删除厂商成功");
    }


    @PostMapping("/edit")
    @ApiOperation("编辑厂商")
    @RequiresPermissions("api:manufacturer:edit")
    @ActionLog(name = "编辑厂商", title = "资产厂商管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> edit(@RequestBody AssetManufacturer assetManufacturer) {
        if (StringUtils.isEmpty(assetManufacturer.getName())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可为空");
        }
        QueryWrapper<AssetManufacturer> qw = new QueryWrapper<>();
        qw.eq("NAME", assetManufacturer.getName());
        List<AssetManufacturer> name = manufacturerService.list(qw);
        if (ObjectUtil.isNotNull(name) && !name.isEmpty() && !name.get(0).getId().equals(assetManufacturer.getId())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可重复，请修改");
        }
        manufacturerService.saveOrUpdate(assetManufacturer);
        return ResultVoUtil.success("修改成功");
    }

    @PostMapping("/add")
    @ApiOperation("新增厂商")
    @RequiresPermissions("api:manufacturer:add")
    @ActionLog(name = "新增厂商", title = "资产厂商管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> add(@RequestBody AssetManufacturer assetManufacturer) {
        if (StringUtils.isEmpty(assetManufacturer.getName())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可为空");
        }
        QueryWrapper<AssetManufacturer> qw = new QueryWrapper<>();
        qw.eq("NAME", assetManufacturer.getName());
        List<AssetManufacturer> name = manufacturerService.list(qw);
        if (ObjectUtil.isNotNull(name) && !name.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "名称不可重复，请修改");
        }

        Long id = manufacturerService.getMaxId();
        assetManufacturer.setId(id == null ? 1 : id + 1);
        manufacturerService.save(assetManufacturer);
        return ResultVoUtil.success("保存成功");
    }

    @GetMapping("/getManufacturerMap")
    @ApiOperation("获取厂商列表")
    public ResultVo<Object> getModeMap() {
        Map<Long, String> manufacturerMap = manufacturerService.getManufacturerMap();
        return ResultVoUtil.success(manufacturerMap);
    }

    @GetMapping("/getManufacturerModelMap/{manufacturerId")
    @ApiOperation("获取厂商资产型号列表")
    public ResultVo<Object> getModelMap(@PathVariable String manufacturerId) {
        List<AssetModel> list = modelService.getByManufacturerId(manufacturerId);
        return ResultVoUtil.success(list);
    }

}