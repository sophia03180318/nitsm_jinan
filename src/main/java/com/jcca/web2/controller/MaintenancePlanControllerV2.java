package com.jcca.web2.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.poi.xssf.usermodel.XSSFWorkbook;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web2.entity.MaintenancePlan;
import com.jcca.web2.service.MaintenancePlanService;
import com.jcca.web2.vo.MaintenancePlanVo;
import com.jcca.web2.vo.MaintenanceStatisticsVo;
import com.jcca.web2.vo.MaintenanceVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @description: 维护计划
 * @author: sophia
 * @create: 2023/11/16 16:36
 **/
@RestController
@RequestMapping("/api/v2/maintenancePlan")
@Api(tags = "维护计划管理V2")
public class MaintenancePlanControllerV2 {

    @Resource
    MaintenancePlanService maintenancePlanService;


    @PostMapping("/list")
    @ApiOperation("维护计划列表")
    public ResultVo<Object> list(@RequestBody MaintenancePlan plan) {

        QueryWrapper<MaintenancePlan> query = Wrappers.query();
        this.getQueryParam(plan, query);
        query.orderByDesc("CREATE_TIME");

        List<MaintenancePlan> list = maintenancePlanService.list(query);
        for (MaintenancePlan p : list) {
            p.setInfluenceOrgList(Arrays.asList(p.getInfluenceOrg().split(",")));
        }

        return ResultVoUtil.success(list);
    }

    private void getQueryParam(MaintenancePlan plan, QueryWrapper<MaintenancePlan> query) {
        if (StringUtils.isNotEmpty(plan.getOrgId())) {
            query.eq("ORG_ID", plan.getOrgId());
        }
        if (StringUtils.isNotEmpty(plan.getName())) {
            query.like("NAME", plan.getName());
        }
        if (Objects.nonNull(plan.getStartTime())) {
            query.gt("START_TIME", plan.getStartTime());
        }
        if (Objects.nonNull((plan.getEndTime()))) {
            query.lt("END_TIME", plan.getEndTime());
        }
        if (StringUtils.isNotEmpty(plan.getModel())) {
            query.eq("MODEL", plan.getModel());
        }
        if (StringUtils.isNotEmpty(plan.getChain())) {
            query.eq("CHAIN", plan.getChain());
        }
        if (StringUtils.isNotEmpty(plan.getMonitor())) {
            query.eq("MONITOR", plan.getMonitor());
        }
        if (StringUtils.isNotEmpty(plan.getStatus())) {
            query.eq("STATUS", plan.getStatus());
        }
        if (StringUtils.isNotEmpty(plan.getType())) {
            query.eq("TYPE", plan.getType());
        }
    }

    @PostMapping("/save")
    @ApiOperation("保存维护计划")
    @RequiresPermissions("api:v2:maintenancePlan:save")
    @ActionLog(name = "保存维护计划", title = "维护计划", key = LogTypeConstant.ADD)
    public ResultVo<Object> add(@RequestBody @Validated MaintenanceVo vo) {

        maintenancePlanService.add(vo);

        return ResultVoUtil.success();
    }

    @GetMapping("/del/{id}")
    @ApiOperation("删除维护计划")
    @RequiresPermissions("api:v2:maintenancePlan:del")
    @ActionLog(name = "删除维护计划", title = "维护计划", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> del(@PathVariable String id) {

        maintenancePlanService.removeById(id);

        return ResultVoUtil.success();
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("维护计划详情")
    public ResultVo<Object> detail(@PathVariable String id) {

        MaintenancePlan one = maintenancePlanService.getById(id);

        return ResultVoUtil.success(one);
    }

    @GetMapping("/statistics")
    @ApiOperation("当年维护计划统计")
    public ResultVo<Object> statistics() {

        List<MaintenanceStatisticsVo> list = maintenancePlanService.statistics();

        return ResultVoUtil.success(list);
    }


    @ApiOperation("导出维护计划表")
    @GetMapping("/exportMaintenancePlan")
    public void exportMaintenancePlan(HttpServletResponse response) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/system/export/maintenancePlan.xlsx");
            InputStream is = resource.getInputStream();
            SXSSFWorkbook sheets = new SXSSFWorkbook(new XSSFWorkbook(is));
            DispatchRecordExcelUtil.responseBody(sheets, response, "维护计划表");
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CONSTRUCTION_RECORD, "maintenancePlan.xlsx模板下载错误", e);
        }
    }


    @PostMapping("/importPlan")
    @ApiOperation("导入维护计划")
    @RequiresPermissions("api:maintenancePlan:import")
    @ActionLog(name = "导入维护计划", title = "维护计划管理", key = LogTypeConstant.UPLOAD)
    public ResultVo<Object> importPlan(@RequestParam("file") MultipartFile file) {
        if (Objects.isNull(file)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "文件不可为空~");
        }
        try {
            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in);
            // 判断模板是否含有基础字段
            List<Object> titleList = reader.readRow(2);
            ArrayList<String> list = new ArrayList<>();
            list.add("startTimeStr");
            list.add("endTimeStr");
            list.add("orgName");
            list.add("content");
            if (!titleList.containsAll(list)) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "模板错误，请重新下载模板~");
            }
            List<MaintenancePlanVo> dataList = reader.read(2, 3, MaintenancePlanVo.class);
            if (dataList.isEmpty()) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "文件不可为空~");
            }
            List<MaintenancePlanVo> maintenancePlanList = maintenancePlanService.importProcess(dataList);
            if (maintenancePlanList.isEmpty()) {
                return ResultVoUtil.success(dataList.size() + "条数据,全部导入成功", null);
            }
            return ResultVoUtil.success(dataList.size() + "条数据，共" + maintenancePlanList.size() + "条失败 请查看详细日志", maintenancePlanList);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CONSTRUCTION_RECORD, "维护计划导入出现IO异常", e);
        }
        return ResultVoUtil.success("导入成功~");
    }

}