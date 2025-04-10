package com.jcca.web2.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.enums.*;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.WordUtil;
import com.jcca.component.quartz.alarm.QuartzUncertainAlarmJob;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.vo.AlarmExportVo;
import com.jcca.web.asset.controller.bean.Repository;
import com.jcca.web.broken.controller.bean.BrokenRecordWord;
import com.jcca.web.broken.service.BrokenRecordWordService;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web2.dto.*;
import com.jcca.web2.service.IndexPageService;
import com.jcca.web2.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 告警接口v2
 * @author: Lvyp
 * @create: 2023/11/16 11:43
 */
@RestController
@RequestMapping("/api/v2/alarm")
@Api(tags = "告警管理V2")
public class AlarmControllerV2 {


    @Resource
    private IndexPageService indexPageService;
    @Resource
    private AlarmInfoService alarmInfoServ;
    @Resource
    private AlarmEventService alarmEventService;
    @Resource
    private SysModuleConfigService configServ;
    @Resource
    private BrokenRecordWordService brokenRecordWordService;
    @Resource
    private SysOrgService orgServ;


    @GetMapping("/exportByReq")
    @ApiOperation("条件导出告警列表")
    public void exportByReq(AlarmPageDto query, HttpServletResponse response) {
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        if (Objects.isNull(query.getAssetDeskList()) || query.getAssetDeskList().isEmpty()) {
            query.setAssetDeskList(null);
        }
        query.setPage(1);
        query.setSize(Integer.MAX_VALUE);
        IPage<AlarmPageVo> page = alarmInfoServ.pageV2(query);
        List<AlarmPageVo> records = page.getRecords();

        exportData(records, response);
    }

    @GetMapping("/alarmAnalysisWordExportGet")
    @ApiOperation(value = "告警分析报告导出")
    @ActionLog(name = "导出告警分析报告", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    public void alarmAnalysisWordExportGet(AlarmInfoPageQuery query, HttpServletResponse response) {
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        if (Objects.isNull(query.getAssetDeskList()) || query.getAssetDeskList().isEmpty()) {
            query.setAssetDeskList(null);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        List<BrokenRecordWord> list = brokenRecordWordService.getBrokenRecordWordsV2(query);
        Map<String, Object> dataMap = brokenRecordWordService.getExportWordMapV2(query, list);
        // 文件唯一名称
        String fileOnlyName = "告警分析报告_" + sdf.format(new Date()) + ".doc";
        /** 生成word */
        WordUtil.createWord(dataMap, "AlarmWord.ftl", fileOnlyName, response);
    }


    @GetMapping("/getAlarmIdList")
    @ApiOperation("条件解决所有告警ID列表")
    public ResultVo getAlarmIdList(AlarmPageDto query) {
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        if (Objects.isNull(query.getAssetDeskList()) || query.getAssetDeskList().isEmpty()) {
            query.setAssetDeskList(null);
        }
        query.setStatus(AlarmStatusEnum.UNCONFIRM.getCode().intValue());
        query.setPage(1);
        query.setSize(Integer.MAX_VALUE);
        IPage<AlarmPageVo> page = alarmInfoServ.pageV2(query);
        List<AlarmPageVo> records = page.getRecords();
        List<String> idList = records.stream().map(item -> item.getAlarmId()).collect(Collectors.toList());
        return ResultVoUtil.success(idList);
    }


    @GetMapping("/pageStatistics")
    @ApiOperation("统计告警")
    public ResultVo pageStatistics(AlarmPageDto query) {
        query.setGroupField("ALARM_LEVEL");
        List<AlarmPageStatisticsVo> pieInfo = alarmInfoServ.statisticsV2(query);
        //填充告警级别统计
        List<String> levelList = pieInfo.stream().map(item -> item.getCode()).collect(Collectors.toList());
        AlarmLevelEnum[] values = AlarmLevelEnum.values();
        for (AlarmLevelEnum level : values) {
            if (level == AlarmLevelEnum.UNKNOW) {
                continue;
            }
            if (!levelList.contains(level.getCode().toString())) {
                AlarmPageStatisticsVo vo = new AlarmPageStatisticsVo();
                vo.setCode(level.getCode().toString());
                vo.setKey(level.getMsg());
                vo.setValue(0);
                pieInfo.add(vo);
            }
        }

        query.setGroupField("ALARM_CODE");
        List<AlarmPageStatisticsVo> classifyInfo = alarmInfoServ.statisticsV2(query);
        JSONObject respJson = new JSONObject();
        respJson.put("pieInfo", pieInfo);
        respJson.put("classifyInfo", classifyInfo);

        return ResultVoUtil.success(respJson);
    }

    @GetMapping("/pageAlarmList")
    @ApiOperation("分页查询告警列表")
    public ResultVo pageAlarmList(AlarmPageDto query) {
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        if (Objects.isNull(query.getAssetDeskList()) || query.getAssetDeskList().isEmpty()) {
            query.setAssetDeskList(null);
        }
        if (StrUtil.isNotEmpty(query.getOrgId())) {
            List<String> orgIds = orgServ.getStationOrgIdByLineId(query.getOrgId());
            orgIds.add(query.getOrgId());
            query.setOrgIdList(orgIds);
            query.setOrgId("");
        }
        if (StrUtil.isNotEmpty(query.getContent())) {
            query.setContent(query.getContent());
        }
        IPage<AlarmPageVo> page = alarmInfoServ.pageV2(query);
        List<AlarmPageVo> records = page.getRecords();
        for (AlarmPageVo record : records) {
            if (StrUtil.isEmpty(record.getRepoName())) {
                record.setRepoName("车站告警");
            }
        }
        page.setRecords(records);

        return ResultVoUtil.success(page);
    }

    @Resource
    private QuartzUncertainAlarmJob quartzUncertainAlarmJob;

    @PostMapping("/alarmMsgMock")
    @ApiOperation("模拟推送告警")
    public ResultVo index(AlarmSocketVo vo) {
        indexPageService.sendAlarmMsg(vo);
        return ResultVoUtil.success();
    }

    @GetMapping("/alarmList")
    @ApiOperation("查询告警列表")
    public ResultVo alarmList(@Validated DialogsAlarmListDto query) {
        SysConfig sysConfig = configServ.getSysConfig();
        String showJcca = sysConfig.getShowJcca();
        Integer jcca = "no".equals(showJcca) ? 2 : 1;
        query.setShowJcca(jcca);
        List<DialogsAlarmListVo> voList = alarmInfoServ.queryDialogsVoListV2(query);
        return ResultVoUtil.success(voList);
    }

    @GetMapping("/popupAlarmList")
    @ApiOperation("查询弹框告警列表")
    public ResultVo popupAlarmList() {
        SysConfig sysConfig = configServ.getSysConfig();

        DialogsAlarmListDto query = sysConfig.getAlarmQueryDto();
        List<DialogsAlarmListVo> voList = alarmInfoServ.queryDialogsVoListV2(query);
        return ResultVoUtil.success(voList);
    }

    @GetMapping("/descriptionList")
    @ApiOperation("查询解决方案")
    public ResultVo descriptionList(String alarmId) {
        List<Repository> repositories = alarmInfoServ.queryDescriptionListV2(alarmId);
        return ResultVoUtil.success(repositories);
    }


    @GetMapping("/eventList")
    @ApiOperation("查询事件列表")
    public ResultVo eventList(String alarmId) {
        if (StrUtil.isEmpty(alarmId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "alarmId不能为空");
        }
        List<AlarmEvent> alarmEvents = alarmEventService.selectAllEventByAlarmId(alarmId);
        return ResultVoUtil.success(alarmEvents);
    }

    @PostMapping("/disposeAlarm")
    @ApiOperation("处理告警")
    public ResultVo disposeAlarm(@Validated @RequestBody DisposeAlarmDto dto) {
        if (!dto.brokenVerify()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "转故障缺少必填参数");
        }
        if (!dto.recordVerify()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "施工计划缺少必填参数");
        }

        alarmInfoServ.disposeAlarmV2(dto);

        return ResultVoUtil.success();
    }

    @GetMapping("/getEventCategory")
    @ApiOperation("查询监控项目状态")
    public ResultVo getMonitoringItem(@RequestParam("refuseList") List<String> refuseList) {
        List<MonitoringItemVo> voList = alarmInfoServ.getMonitoringItemV2(refuseList);

        SysConfig sysConfig = configServ.getSysConfig();
        String showJcca = sysConfig.getShowJcca();
        Integer jcca = "no".equals(showJcca) ? 2 : 1;

        //非0需要重新查询看是否存在未恢复未确认非天窗的告警如果没有值改为0
        for (MonitoringItemVo monitoringItemVo : voList) {
            if (monitoringItemVo.getTotal() > 0) {
                DialogsAlarmListDto query = new DialogsAlarmListDto();
                query.setEventCategory(monitoringItemVo.getCode());
                query.setAlarmState(AlarmStateEnum.ALARM.getCode().intValue());
                query.setStatus(AlarmStatusEnum.UNCONFIRM.getCode().intValue());
                query.setBlank(AlarmBlankConst.NORMARL);
                query.setStatusLogical(1);
                query.setUserName("root");
                query.setShowJcca(jcca);
                List<DialogsAlarmListVo> alarmList = alarmInfoServ.queryDialogsVoListV2(query);
                monitoringItemVo.setTotal(alarmList.size());
            }
        }
        return ResultVoUtil.success(voList);
    }

    @ApiOperation("查询机柜内的告警列表")
    @GetMapping("/queryCabinetAlarm")
    public ResultVo<Object> queryCabinetAlarm(@Validated CabinetAlarmQueryDto query) {
        List<CabinetAlarmInfoVo> cabinetAlarmInfoVos = alarmInfoServ.selectCabinetAlarmV2(query);
        return ResultVoUtil.success(cabinetAlarmInfoVos);
    }


    /**
     * 导出告警信息
     *
     * @param exportVoList
     * @param response
     */
    private void exportData(List<AlarmPageVo> exportVoList, HttpServletResponse response) {
        List<AlarmExportVo> exportList = new ArrayList<>();
        for (AlarmPageVo alarmPageVo : exportVoList) {
            AlarmExportVo copy = EntityBeanUtil.copy(alarmPageVo, AlarmExportVo.class);
            copy.setStatus(AlarmStatusEnum.getMsg(alarmPageVo.getStatus().byteValue()));
            copy.setAlarmStatus(AlarmStateEnum.getMsg(alarmPageVo.getAlarmState().byteValue()));
            copy.setAlarmLevel(AlarmLevelEnum.getMsg(alarmPageVo.getAlarmLevel().byteValue()));
            if (Objects.nonNull(alarmPageVo.getAlarmType())) {
                copy.setAlarmType(AlarmTypeEnum.getMsg(alarmPageVo.getAlarmType().intValue()));
            }
            exportList.add(copy);
        }


        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet("告警信息");

        writer.addHeaderAlias("title", "标题");
        writer.addHeaderAlias("assetName", "资产名称");
        writer.addHeaderAlias("assetIp", "资产IP");
        writer.addHeaderAlias("alarmLevel", "告警级别");
        writer.addHeaderAlias("alarmType", "告警类型");

        writer.addHeaderAlias("content", "告警内容");
        writer.addHeaderAlias("description", "原始告警");

        writer.addHeaderAlias("occurTime", "告警时间");
        writer.addHeaderAlias("status", "确认状态");
        writer.addHeaderAlias("alarmStatus", "告警状态");
        writer.addHeaderAlias("confirmor", "确认人");
        writer.addHeaderAlias("confirmTime", "确认时间");
        writer.addHeaderAlias("remark", "备注");

        writer.getStyleSet().setAlign(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

        writer.write(exportList, true);

        String fileName = "TDCS-CTC综合维护平台告警-" + DateUtil.formatDate(new Date());
        this.stream(response, writer, fileName);
    }

    @GetMapping("/sendUncertainAlarmJob")
    @ApiOperation("立刻执行未确认告警")
    String UncertainAlarmJob() {
        quartzUncertainAlarmJob.init();
        quartzUncertainAlarmJob.exeJob();

        return "ok";
    }

    public void stream(HttpServletResponse response, ExcelWriter writer, String fileName) {
        try {
            String utf8FileName = URLEncoder.encode(fileName, "utf8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename= " + utf8FileName + ".xlsx");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ALARM_LIST, "导出告警列表", e);
        }
    }

    @PostMapping("/countAlarm")
    @ApiOperation("告警统计")
    @RequiresPermissions("api:v2:alarm:countAlarm")
    public void countAlarm(HttpServletResponse response, @RequestBody AlarmPageDto req) {
        String startTime = req.getStartTime();
        String endTime = req.getEndTime();
        if (StrUtil.isEmpty(startTime) || StrUtil.isEmpty(endTime)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "请选择起止时间");
        }

        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.setColumnWidth(0, 18);
        writer.setColumnWidth(1, 18);
        writer.setColumnWidth(2, 18);
        writer.setColumnWidth(3, 18);
        writer.setColumnWidth(4, 18);
        writer.setColumnWidth(5, 18);
        writer.renameSheet("告警统计");

        writer.addHeaderAlias("total", "告警总数");
        writer.addHeaderAlias("level1", "一级告警数");
        writer.addHeaderAlias("level2", "二级告警数");
        writer.addHeaderAlias("level3", "三级告警数");
        writer.addHeaderAlias("unHandled", "未处理告警数");
        writer.addHeaderAlias("handled", "已处理告警数");
        writer.addHeaderAlias("assetName", "设备名称");
        writer.addHeaderAlias("assetIp", "设备IP");
        writer.addHeaderAlias("occurTime", "告警时间");
        writer.addHeaderAlias("content", "告警内容");
        writer.addHeaderAlias("description", "原始信息");
        writer.addHeaderAlias("opinion", "解决方案");

        writer.merge(5, "告警数量统计");
        AlarmCountDto countDto = alarmInfoServ.countAlarm(req);
        List<AlarmCountDto> list = Collections.singletonList(countDto);
        writer.write(list, true);

        List<AlarmUnhandledDto> infos = alarmInfoServ.findUnhandledAlarm(req);
        writer.merge(5, "未处理告警信息（共" + infos.size() + "条）");
        writer.write(infos, true);

        List<AlarmUnhandledDto> alarms = alarmInfoServ.find5TimesUp(req);
        writer.merge(5, "告警超过5次信息（共" + alarms.size() + "条）");
        writer.write(alarms, true);

        writer.getStyleSet().setAlign(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

        Cell cell00 = writer.getCell(0, 0);
        cell00.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
        cell00.getCellStyle().setFillBackgroundColor(IndexedColors.WHITE.index);

        Cell cell30 = writer.getCell(3, 0);
        cell30.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
        cell30.getCellStyle().setFillBackgroundColor(IndexedColors.WHITE.index);

        String fileName = "告警统计-" + DateUtil.formatDate(new Date());
        this.stream(response, writer, fileName);
    }

}
