package com.jcca.web2.controller;

import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web2.dto.CollectConfigDto;
import com.jcca.web2.service.CollectConfigService;
import com.jcca.web2.vo.CollectConfigVo;
import com.jcca.web2.vo.TargetVerifyVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HanHW
 * @description 采集配置
 * @className CollectConfigController
 * @date 2024/2/18 15:55
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/ccf")
@Api(tags = "采集配置V2")
public class CollectConfigController {

    @Resource
    private CollectConfigService collectConfigService;

    @GetMapping("/list")
    @ApiOperation("获取采集配置列表")
    public ResultVo<Object> list(CollectConfigDto dto) {

        List<PerformanceTarget> list = collectConfigService.list(dto);

        return ResultVoUtil.success(list);
    }

    @GetMapping("/modifyCron")
    @ApiOperation("修改采集周期")
    @RequiresPermissions("api:v2:ccf:modifyCron")
    public ResultVo<Object> modifyCron(String targetId, String unit, Integer interval) {
        if (StringUtils.isEmpty(targetId)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "指标ID不能为空");
        }
        if (StringUtils.isEmpty(unit)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "时间单位不能为空");
        }
        if (Objects.isNull(interval)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "时间间隔不能为空");
        }

        collectConfigService.modifyCron(targetId, unit, interval);

        return ResultVoUtil.success();
    }

    @GetMapping("/modifyStatus")
    @ApiOperation("设置指标启停状态")
    @RequiresPermissions("api:v2:ccf:modifyStatus")
    public ResultVo<Object> modifyStatus(String targetId, Integer isAvaliable) {
        if (StringUtils.isEmpty(targetId)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "指标ID不能为空");
        }
        if (Objects.isNull(isAvaliable)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "启停状态不能为空");
        }

        collectConfigService.modifyStatus(targetId, isAvaliable);

        return ResultVoUtil.success();
    }


    @GetMapping("/del/{specDictId}")
    @ApiOperation("指标字典删除")
    @RequiresPermissions("api:v2:ccf:del")
    public ResultVo<Object> del(@PathVariable String specDictId) {

        collectConfigService.delSpecDict(specDictId);

        return ResultVoUtil.success();
    }

    @PostMapping("/save")
    @ApiOperation("保存采集配置")
    @RequiresPermissions("api:v2:ccf:save")
    public ResultVo<Object> save(@Validated CollectConfigVo vo) {

        collectConfigService.save(vo);

        return ResultVoUtil.success();
    }

    @PostMapping("/verify")
    @ApiOperation("指标验证")
    public ResultVo<Object> verify(@Validated TargetVerifyVo vo) {

        Map<String, Object> map = collectConfigService.verifyTarget(vo);

        return ResultVoUtil.success(map);
    }
}
