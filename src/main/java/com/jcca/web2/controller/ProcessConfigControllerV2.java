package com.jcca.web2.controller;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.annotation.DevLog;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.PromptInfo;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.PromptInfoService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.asset.utils.enums.ProcessHostModeEnum;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.vo.ProcessOnChangeVo;
import com.jcca.web2.dto.ConfigProcessDto;
import com.jcca.web2.entity.BusinessServiceType;
import com.jcca.web2.service.BusinessServiceTypeService;
import com.jcca.web2.vo.ProcessPlateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 业务进程管理
 * @className ProcessConfigControllerV2
 * @date 2023/11/8 17:54
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/process")
@Api(tags = "业务进程管理V2")
public class ProcessConfigControllerV2 extends ListenerManager {

    @Resource
    private BusinessServiceTypeService bizTypeService;
    @Resource
    private PromptInfoService promptInfoService;
    @Resource
    private AssetService assetService;
    @Resource
    private ThresholdProcessService processService;
    @Resource
    private OutService outService;


    @GetMapping("/bizList")
    @ApiOperation("业务类型列表")
    public ResultVo<Object> bizList() {

        QueryWrapper<BusinessServiceType> query = Wrappers.query();
        query.orderByDesc("MODIFY_TIME");
        List<BusinessServiceType> list = bizTypeService.list(query);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/bizAdd")
    @ApiOperation("添加/修改业务类型")
    @RequiresPermissions("api:v2:process:bizAdd")
    @ActionLog(name = "进程配置相关", title = "添加/修改业务类型", key = LogTypeConstant.ADD)
    public ResultVo<String> bizAdd(@RequestBody BusinessServiceType type) {

        if (StringUtils.isEmpty(type.getId())) {
            QueryWrapper<BusinessServiceType> query = Wrappers.query();
            query.eq("NAME", type.getName());
            List<BusinessServiceType> list = bizTypeService.list(query);
            if (!CollectionUtils.isEmpty(list)) {
                return ResultVoUtil.error(ResultEnum.BUSINESS_NAME_EXIST);
            }
            Integer sort = bizTypeService.getMaxSort();
            if (Objects.isNull(sort)) sort = 0;
            type.setSort(sort + 1);
            bizTypeService.save(type);
        } else {
            bizTypeService.updateById(type);
        }

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @PostMapping("/bizDel/{id}")
    @ApiOperation("删除业务类型")
    @RequiresPermissions("api:v2:process:bizDel")
    @ActionLog(name = "进程配置相关", title = "删除业务类型", key = LogTypeConstant.REMOVEE)
    public ResultVo<String> bizDel(@PathVariable("id") String id) {

        promptInfoService.removePrompt(id);

        return ResultVoUtil.REMOVE_SUCCESS;
    }

    // ===========================以下为模板相关===========================

    @GetMapping("/plateList/{bizId}")
    @ApiOperation("进程模板列表")
    public ResultVo<Object> plateList(@PathVariable("bizId") String softwareTypeId) {

        List<ProcessPlateVo> resList = promptInfoService.findBySoftTypeIdV2(softwareTypeId);

        return ResultVoUtil.success(resList);
    }

    @PostMapping("/plateAdd")
    @ApiOperation("添加进程模板")
    @RequiresPermissions("api:v2:process:plateAdd")
    @ActionLog(name = "进程配置相关", title = "添加进程模板", key = LogTypeConstant.ADD)
    public ResultVo<String> plateAdd(@RequestBody ProcessPlateVo vo) {

        promptInfoService.addPlateV2(vo);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @PostMapping("/plateUpdate")
    @ApiOperation("编辑进程模板")
    @RequiresPermissions("api:v2:process:plateUpdate")
    @ActionLog(name = "进程配置相关", title = "编辑进程模板", key = LogTypeConstant.MODIFY)
    public ResultVo<String> plateUpdate(@RequestBody ProcessPlateVo vo) {

        String plateId = vo.getPlateId();
        if (StringUtils.isEmpty(plateId)) {
            return ResultVoUtil.error(ResultEnum.TEMPLATE_ID_NULL);
        }

        this.remove(plateId);

        promptInfoService.addPlateV2(vo);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @PostMapping("/plateDel/{plateId}")
    @ApiOperation("删除进程模板")
    @RequiresPermissions("api:v2:process:del")
    @ActionLog(name = "进程配置相关", title = "删除进程模板", key = LogTypeConstant.REMOVEE)
    public ResultVo<String> plateDel(@PathVariable("plateId") String plateId) {

        this.remove(plateId);

        return ResultVoUtil.REMOVE_SUCCESS;
    }

    private void remove(String plateId) {
        QueryWrapper<PromptInfo> wrapper = Wrappers.query();
        wrapper.eq("PLATE_ID", plateId);
        promptInfoService.remove(wrapper);
    }

    // ===========================以下为进程配置===========================

    @GetMapping("/assetPlate/{assetId}")
    @ApiOperation("资产进程模板列表")
    public ResultVo<Object> assetPlate(@PathVariable("assetId") String assetId) {

        Asset asset = assetService.getById(assetId);
        if (StringUtils.isEmpty(asset.getServiceTypeId())) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        String softTypeId = asset.getServiceTypeId();
        List<ProcessPlateVo> resList = promptInfoService.findBySoftTypeIdV2(softTypeId);

        return ResultVoUtil.success(resList);
    }

    @PostMapping("/configProcess")
    @ApiOperation("配置资产进程")
    @RequiresPermissions("api:v2:process:configProcess")
    @DevLog(title = "业务配置管理", name = "添加进程", dev = DevLogConstant.PROCESS_CONFIG, key = LogTypeConstant.DEV)
    public ResultVo<String> saveProcess(@RequestBody List<ConfigProcessDto> list) {

        if (CollectionUtils.isEmpty(list)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        String assetId = list.get(0).getAssetId();
        if (StringUtils.isEmpty(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        QueryWrapper<ThresholdProcess> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        List<ThresholdProcess> processes = processService.list(query);
        Set<String> names = processes.stream().map(ThresholdProcess::getProcessName).collect(Collectors.toSet());

        Set<String> nset = new HashSet<>();
        List<ThresholdProcess> dtoList = new ArrayList<>();
        for (ConfigProcessDto dto : list) {
            String processName = dto.getProcessName().trim();
            if (names.contains(processName)) {
                continue;
            }
            if (nset.contains(processName)) {
                continue;
            }
            nset.add(processName);

            this.checkData(dto);

            ThresholdProcess process = new ThresholdProcess();
            process.setId(MyIdUtil.getId());
            try {
                BeanUtils.copyProperties(process, dto);
            } catch (IllegalAccessException | InvocationTargetException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, dto, e);
                continue;
            }

            process.setCollectStatus(StatusConst.OK);
            process.setHostMode(ProcessHostModeEnum.COMMON.getCode());
            dtoList.add(process);
        }

        if (!CollectionUtils.isEmpty(dtoList)) {
            ProcessOnChangeVo changeVo = new ProcessOnChangeVo();
            changeVo.setAssetId(assetId);
            changeVo.setOptFlag(0); // ADD_PROCESS_FLAG
            changeVo.setProcessNameList(dtoList.stream().map(ThresholdProcess::getProcessName).collect(Collectors.toList()));
            try {
                outService.processOnChange(changeVo);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.PROCESS_CONFIG, "资产ID：" + assetId, e);
                return ResultVoUtil.error(ResultEnum.OUT_PROCESS_ERROR);
            }

            processService.createAll(dtoList);

            this.dispatureEvent(new Event());
        }

        return ResultVoUtil.SAVE_SUCCESS;
    }

    private void checkData(ConfigProcessDto dto) {
        String thresholdCpu = dto.getThresholdCpu();
        if (!StringUtils.isEmpty(thresholdCpu)) {
            if (!NumberUtil.isNumber(thresholdCpu)) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "cpu阈值应为数字");
            }
            double cpu = new BigDecimal(thresholdCpu).setScale(2, RoundingMode.HALF_UP).doubleValue();
            if (cpu > 100D || cpu < 0.01D) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "CPU阈值应在0.01到100之间");
            }
            dto.setThresholdCpu(cpu + "");
        }

        String thresholdMemory = dto.getThresholdMemory();
        if (!StringUtils.isEmpty(thresholdMemory)) {
            if (!NumberUtil.isNumber(thresholdMemory)) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "内存阈值应为数字");
            }
            double memory = new BigDecimal(thresholdMemory).setScale(2, RoundingMode.HALF_UP).doubleValue();
            if (memory > 100D || memory < 0.01D) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "内存阈值应在0.01到100之间");
            }
            dto.setThresholdMemory(memory + "");
        }
    }

}
