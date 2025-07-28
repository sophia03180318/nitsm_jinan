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
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.service.OutService;

import com.jcca.web2.vo.ManufacturerSpecResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.*;

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
    @Resource
    private OutService outService;

    /**
     * 查询所有的基础配置列表
     */
    @PostMapping("/specDict")
    @ApiOperation("获取已配置采集列表")
    public ResultVo<Object> index(){
        Map<String, String> baseSpecMap = DictUtil.value("SPEC_DICT");
        Set<String> keyList = baseSpecMap.keySet();

        List<SpecDictionary> list = new ArrayList<>();
        for (String key : keyList) {
            SpecDictionary item = new SpecDictionary();
            item.setId(MyIdUtil.getId());
            item.setRemark(baseSpecMap.get(key));
            item.setSpecId(Integer.valueOf(key));

            list.add(item);
        }

        return ResultVoUtil.success(list);
    }

    /**
     * 查询厂商对应的执行列表
     * @param manufacturerId
     * @return
     */
    @GetMapping("/manufacturerSpec")
    @ApiOperation("查询厂商对应的采集列表")
    public ResultVo<Object> manufacturerSpec(String manufacturerId){
        QueryWrapper<SpecDictionary> query = new QueryWrapper<>();
        query.eq("MANUFACTURER_ID", manufacturerId);
        List<SpecDictionary> specDictList = specDictionaryService.list(query);

        List<ManufacturerSpecResp> specRespList = new ArrayList<>();

        for (SpecDictionary specDictionary : specDictList) {
            QueryWrapper<PerformanceTarget> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("SPEC_ID", specDictionary);
            List<PerformanceTarget> list = performanceTargetService.list(queryWrapper);

            ManufacturerSpecResp resp = new ManufacturerSpecResp();
            resp.setSpecDictTitle(StrUtil.isEmpty(specDictionary.getRemark())?"未命名":specDictionary.getRemark());
            resp.setSystemType(specDictionary.getSystemType());
            resp.setTargetList(list);

            specRespList.add(resp);
        }

        return ResultVoUtil.success(specRespList);

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

        QueryWrapper<SpecDictionary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_MODE", specDictionary.getAssetMode());
        queryWrapper.eq("ASSET_IMAGE", specDictionary.getAssetImage());
        queryWrapper.eq("MANUFACTURER_ID", specDictionary.getManufacturerId());
        queryWrapper.eq("SYSTEM_TYPE", specDictionary.getSystemType());

        if (ObjectUtil.isNull(specDictionary.getId())) {
            specDictionary.setId(MyIdUtil.getId());
            specDictionary.setCreateDate(new Date());
        }else {
            queryWrapper.ne("id",specDictionary.getId());
        }
        List<SpecDictionary> list = specDictionaryService.list(queryWrapper);
        if(!list.isEmpty()){
            return ResultVoUtil.error("此厂商的该型号存在相同系统类型的采集配置！");
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
        queryWrapper.select("id","COMMAND_NAME","TARGET_DESCRIPTION","COMMAND","TARGET_HANDLE","CRON_EXPRESS","IS_AVAILABLE");
        queryWrapper.eq("SPEC_ID", specDictionary.getSpecId());
        queryWrapper.orderByAsc("id");
        List<PerformanceTarget> list = performanceTargetService.list(queryWrapper);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/startOrStop")
    @ActionLog(name = "修改采集项状态", title = "采集配置", key = LogTypeConstant.MODIFY)
    public ResultVo startOrStop(@RequestBody PerformanceTarget req) {
        if(StrUtil.isEmpty(req.getId())){
            throw new ResultException(ResultEnum.COLLECTOR_NULL_ID);
        }
        if(Objects.isNull(req.getIsAvailable())){
            throw new ResultException(ResultEnum.COLLECTOR_NULL_STATUS);
        }
        PerformanceTarget performanceTarget = performanceTargetService.getById(req.getId());
        if(Objects.isNull(performanceTarget)){
            throw new ResultException(ResultEnum.COLLECTOR_NONE);
        }
        performanceTarget.setIsAvailable(req.getIsAvailable());
        performanceTargetService.updateById(performanceTarget);
        return ResultVoUtil.success("处理成功");
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


    /**
     * 跳转测试链接页面
     */
    @PostMapping("/test")
    public ResultVo test(@RequestBody  MinuteVo perform) {
        PerformanceTarget performanceTarget = performanceTargetService.getById(perform.getId());

        Asset asset = new Asset();
        asset.setIp(perform.getIp());
        asset.setOsUser(perform.getCommunity());
        asset.setOsPassword(perform.getPassword());
        asset.setName("采集项验证"+perform.getIp());
        return outService.testPerformanceTarget(asset,performanceTarget);


    }


}
