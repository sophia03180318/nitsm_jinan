package com.jcca.web2.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.PerformanceTargetService;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.admin.system.vo.MinuteVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 采集配置相关接口
 */
@Api(tags = "采集配置V2")
@RestController
@RequestMapping("/api/v2/collect")
public class CollectConfigControllerV2 {

    @Resource
    private SpecDictionaryService specDictionaryService;
    @Resource
    private PerformanceTargetService performanceTargetService;

    /**
     * 查询所有的型号配置列表
     */
    @PostMapping("/specDict")
    @ApiOperation("获取已配置采集列表")
    public ResultVo<Object> index(){
        List<SpecDictionary> list = specDictionaryService.list();
        return ResultVoUtil.success(list);
    }


    /**
     * 保存添加/修改的数据
     *
     * @param specDictionary 实体对象
     */
    @PostMapping("/save")
    @ActionLog(name = "保存采集配置", title = "采集配置", key = LogTypeConstant.ADD)
    public ResultVo save(@RequestBody SpecDictionary specDictionary) {

        // 判断适配类型是否为空
        if (Objects.isNull(specDictionary.getAssetMode())) {
            throw new ResultException(ResultEnum.COLLECTOR_ASSET_MDOE);
        }
        // 判断适配型号是否为空
        if (StrUtil.isEmpty(specDictionary.getAssetImage())) {
            throw new ResultException(ResultEnum.COLLECTOR_ASSET_IMAGE);
        }
        // 判断适配厂家是否为空
        if (StrUtil.isEmpty(specDictionary.getManufacturerId())) {
            throw new ResultException(ResultEnum.COLLECTOR_ASSET_FACTORY);
        }
        // 判断操作系统是否为空
        if (Objects.isNull(specDictionary.getSystemType())) {
            throw new ResultException(ResultEnum.COLLECTOR_SYSTEM_TYPE);
        }
        // 判断执行代码是否为空
        if (Objects.isNull(specDictionary.getSpecId())) {
            throw new ResultException(ResultEnum.COLLECTOR_SPEC_DICT);
        }
        if(StrUtil.isEmpty(specDictionary.getRemark())){
            throw new ResultException(ResultEnum.COLLECTOR_REMARK);
        }
        if (ObjectUtil.isNull(specDictionary.getId())) {
            specDictionary.setCreateDate(new Date());
        }
        specDictionaryService.saveOrUpdate(specDictionary);
        return ResultVoUtil.SAVE_SUCCESS;
    }

    @GetMapping("/deleteById")
    @ActionLog(name = "删除采集配置", title = "采集配置", key = LogTypeConstant.REMOVEE)
    public ResultVo deleteById(String id) {
        boolean b = specDictionaryService.removeById(id);
        if (b) {
            return ResultVoUtil.success("模板删除成功");
        }
        return ResultVoUtil.error("模板删除失败");
    }


    @GetMapping("/queryPerformanceTarget")
    public ResultVo<Object> queryPerformanceTarget(String id) {
        SpecDictionary specDictionary = specDictionaryService.getById(id);
        if(Objects.isNull(specDictionary)){
            throw new ResultException(ResultEnum.COLLECTOR_NONE);
        }
        QueryWrapper<PerformanceTarget> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SPEC_ID", specDictionary.getSpecId());
        queryWrapper.select("id","COMMAND_NAME","TARGET_DESCRIPTION","COMMAND","TARGET_HANDLE","CRON_EXPRESS");
        List<PerformanceTarget> list = performanceTargetService.list(queryWrapper);

        return ResultVoUtil.success(list);
    }


    @PostMapping("/saveTime")
    @ActionLog(name = "修改采集时间配置", title = "采集配置", key = LogTypeConstant.MODIFY)
    public ResultVo setTime(@RequestBody  MinuteVo minte) {
        try {
            if (StrUtil.isEmpty(minte.getMinute())) {
                throw new ResultException(ResultEnum.COLLECTOR_NULL);
            }

            int min = Integer.parseInt(minte.getMinute().trim());
            if (min < 1 || min > 59) {
                throw new ResultException(ResultEnum.COLLECTOR_NUM);
            }
            PerformanceTarget performanceTarget = performanceTargetService.getById(minte.getId());
            performanceTarget.setCronExpress("0 0/" + min + " * * * ? *");
            performanceTargetService.saveOrUpdate(performanceTarget);
        } catch (NumberFormatException e) {
            throw new ResultException(ResultEnum.COLLECTOR_NUM);
        }
        return ResultVoUtil.success();
    }


}
