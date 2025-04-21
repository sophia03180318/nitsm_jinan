package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.xunjian.adapter.v1.util.TemplateUtil;
import com.jcca.web.xunjian.controller.bean.XunjianRepoBody;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import com.jcca.web.xunjian.controller.util.bean.XunjianReportTemp;
import com.jcca.web.xunjian.entity.XunjianAlarmMsg;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.vo.InspectOrgAssetVo;
import com.jcca.web2.vo.InspectRecordListVo;
import com.jcca.web2.vo.InspectTargetVo;
import com.jcca.web2.vo.InspectVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 巡检管理
 * @className InspectControllerV2
 * @date 2023/11/14 13:12
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/inspect")
@Api(tags = "巡检管理V2")
public class InspectControllerV2 {

    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private InspectDetailService inspectDetailService;
    @Resource
    private AlarmInfoService alarmInfoService;

    @GetMapping("/modeTarget")
    @ApiOperation("按资产类型获取指标列表")
    public ResultVo<Object> modeTarget() {
        String inspectCode = inspectRecordService.getLastInspectCode();
        if (StringUtils.isEmpty(inspectCode)) {
            inspectRecordService.checkRecord();
        }
        List<InspectVo> resList = inspectRecordService.getNextRecords(inspectCode);
        return ResultVoUtil.success(resList);
    }

    @GetMapping("/orgCabinet")
    @ApiOperation("分组查询组织机柜")
    public ResultVo<Object> orgCabinet() {
        String inspectCode = inspectRecordService.getLastInspectCode();

        if (StringUtils.isEmpty(inspectCode)) {
            inspectRecordService.checkRecord();
        }
        List<InspectVo> resList = inspectRecordService.getOrgCabinet(inspectCode);

        return ResultVoUtil.success(resList);
    }

    @GetMapping("/orgAsset")
    @ApiOperation("按组织资产类型获取设备列表")
    public ResultVo<Object> orgAsset() {
        String inspectCode = inspectRecordService.getLastInspectCode();
        if (StringUtils.isEmpty(inspectCode)) {
            inspectRecordService.checkRecord();
        }
        List<InspectOrgAssetVo> resList = inspectRecordService.getOrgAsset();
        return ResultVoUtil.success(resList);
    }

    @GetMapping("/cabinetAsset/{cabinetId}")
    @ApiOperation("查询机柜内资产信息")
    public ResultVo<Object> cabinetAsset(@PathVariable("cabinetId") String cabinetId) {

        List<DetailCabinetVo> resList = inspectRecordService.findByCabinetId(cabinetId);

        return ResultVoUtil.success(resList);
    }

    @GetMapping("/assetTarget/{assetId}")
    @ApiOperation("获取资产所有指标信息")
    public ResultVo<Object> assetTarget(@PathVariable("assetId") String assetId) {

        InspectTargetVo res = inspectRecordService.getAssetTarget(assetId);

        return ResultVoUtil.success(res);
    }

    @GetMapping("/modeAsset/{modeType}")
    @ApiOperation("按资产类型获取资产列表")
    public ResultVo<Object> modeAsset(@PathVariable("modeType") String modeType) {

        List<InspectVo> resList = inspectRecordService.getModeAsset(modeType);

        return ResultVoUtil.success(resList);
    }

    @PostMapping("/modifyTarget")
    @ApiOperation("增减指标")
    @ActionLog(name = "增减指标", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> modifyTarget(@RequestBody List<Map<String, String>> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        for (Map<String, String> map : list) {
            String modeType = map.get("modeType");
            String targetItem = map.get("targetItem");
            String status = map.get("status");
            if (StringUtils.isEmpty(targetItem)) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "targetItem 不能为空");
            }
            if (StringUtils.isEmpty(modeType)) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "modeType 不能为空");
            }
            if (StringUtils.isEmpty(status)) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "status 不能为空");
            }

            inspectRecordService.modifyTarget(modeType, targetItem, status);
        }
        return ResultVoUtil.success();
    }

    @PostMapping("/modifyAsset")
    @ApiOperation("增减资产")
    @ActionLog(name = "增减资产", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> modifyAsset(@RequestBody List<Map<String, String>> list) {

        if (CollectionUtils.isEmpty(list)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        inspectRecordService.modifyAsset(list);

        return ResultVoUtil.success();
    }

    @GetMapping("/start")
    @ApiOperation("开始巡检")
    @ActionLog(name = "开始巡检", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> start(String inspectType) {

        if (StringUtils.isEmpty(inspectType)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        // 若提示有任务正在执行，可先暂停一下后再开始巡检
        inspectRecordService.start(inspectType);

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, ShiroUtil.getSubject().getNickname(), "开始巡检");
        return ResultVoUtil.success();
    }

    @GetMapping("/targetState")
    @ApiOperation("查询指标状态")
    public ResultVo<Object> targetState(String assetId, String targetItem, String modeType) {

        String inspectCode = inspectRecordService.getLastInspectCode();
        InspectRecord record = inspectRecordService.findTargetState(assetId, targetItem, modeType, inspectCode);

        return ResultVoUtil.success(record);
    }

    @GetMapping("/assetState")
    @ApiOperation("查询资产状态")
    public ResultVo<Object> assetState(String assetId) {

        InspectRecord record = inspectRecordService.findAssetState(assetId);

        return ResultVoUtil.success(record);
    }

    @GetMapping("/cabinetState")
    @ApiOperation("查询机柜状态")
    public ResultVo<Object> cabinetState(String cabinetId) {

        InspectRecord record = inspectRecordService.findCabinetState(cabinetId);

        return ResultVoUtil.success(record);
    }

    @PostMapping("/pause")
    @ApiOperation("暂停巡检")
    @ActionLog(name = "暂停巡检", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> pause() {

        inspectRecordService.pause();

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, ShiroUtil.getSubject().getNickname(), "暂停巡检");
        return ResultVoUtil.success();
    }

    @PostMapping("/end")
    @ApiOperation("结束巡检")
    @ActionLog(name = "结束巡检", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> end() {

        inspectRecordService.end("手动结束巡检");

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, ShiroUtil.getSubject().getNickname(), "手动结束巡检");
        return ResultVoUtil.success();
    }

    @PostMapping("/autoEnd")
    @ApiOperation("自动结束巡检")
    @ActionLog(name = "自动结束巡检", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> autoEnd() {

        inspectRecordService.end("");

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, ShiroUtil.getSubject().getNickname(), "巡检结束");
        return ResultVoUtil.success();
    }

    @GetMapping("/getState")
    @ApiOperation("获取巡检状态")
    public ResultVo<Object> getState() {

        String state = inspectRecordService.getState();
        Map<String, String> map = new HashMap<>();
        map.put("state", state);

        return ResultVoUtil.success(map);
    }


    @GetMapping("/modifyInspectType/{inspectType}")
    @ApiOperation("更新巡检类型")
    public ResultVo<Object> modifyInspectType(@PathVariable("inspectType") String inspectType) {

        inspectRecordService.updateInspect(inspectType);

        return ResultVoUtil.success();
    }

    // =================================================================================================================

    @GetMapping("/recordHistory")
    @ApiOperation("巡检历史记录")
    public ResultVo<Object> recordList() {

        List<InspectRecordListVo> records = inspectDetailService.recordList();

        return ResultVoUtil.success(records);
    }

    @GetMapping("/recordDetail/{inspectCode}")
    @ApiOperation("巡检记录详情")
    public ResultVo<Object> recordDetail(@PathVariable String inspectCode) {

        List<InspectRecordListVo> records = inspectDetailService.findRecordDetail(inspectCode);

        return ResultVoUtil.success(records);
    }

    @GetMapping("/assetDetail")
    @ApiOperation("资产巡检记录详情")
    public ResultVo<Object> assetDetail(String assetId, String inspectCode) {

        List<InspectDetail> records = inspectDetailService.findAssetDetail(assetId, inspectCode);

        return ResultVoUtil.success(records);
    }

    @GetMapping("/editRemark")
    @ApiOperation("编辑备注")
    @ActionLog(name = "编辑备注", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> editRemark(String inspectCode, String remark) {

        if (StringUtils.isEmpty(inspectCode)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }
        UpdateWrapper<InspectDetail> update = Wrappers.update();
        update.eq("INSPECT_CODE", inspectCode);
        update.set("REMARK", remark);
        inspectDetailService.update(update);

        return ResultVoUtil.success();
    }

    @PostMapping("/editRecord")
    @ApiOperation("编辑巡检记录")
    @ActionLog(name = "编辑巡检记录", title = "巡检管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> editRecord(@RequestBody InspectRecord record) {

        InspectDetail one = inspectDetailService.getById(record.getId());
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }

        one.setTargetItem(record.getTargetItem());
        one.setCommand(record.getCommand());
        one.setInspectState(record.getInspectState());
        one.setResultMsg(record.getResultMsg());
        inspectDetailService.updateById(one);

        return ResultVoUtil.success();
    }

    @PostMapping("/deleteRecord/{inspectCode}")
    @ApiOperation("删除巡检记录")
    @ActionLog(name = "删除巡检记录", title = "巡检管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> deleteRecord(@PathVariable String inspectCode) {

        inspectDetailService.deleteRecord(inspectCode);

        return ResultVoUtil.success();
    }

    @GetMapping("/exportAsset")
    @ApiOperation(value = "导出单个设备巡检报告")
    public void exportAsset(String inspectCode, String assetId, HttpServletResponse response) {
        if (StrUtil.isEmpty(inspectCode)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        inspectDetailService.exportAssetRecord(inspectCode, assetId, response);

    }

    @GetMapping("/exportReport")
    @ApiOperation(value = "导出智能巡检报告单")
    public void exportReport(String inspectCode, HttpServletResponse response) {
        QueryWrapper<InspectDetail> detailQuery = Wrappers.query();
        detailQuery.eq("INSPECT_CODE", inspectCode);
        detailQuery.eq("TARGET_STATUS", Web2Const.AVAILABLE);
        detailQuery.eq("ASSET_STATUS", Web2Const.AVAILABLE);
        List<InspectDetail> list = inspectDetailService.list(detailQuery);
        if (list.isEmpty()) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, inspectCode, "智能巡检找不到指定巡检报告");
            return;
        }

        list = list.stream().filter(s -> Arrays.asList(itemArr).contains(s.getTargetItem())).collect(Collectors.toList());
        if (list.isEmpty()) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, inspectCode, "没有该类型设备的可导出报表");
            return;
        }

        ArrayList<XunjianAlarmMsg> alarmList2 = new ArrayList<>();
        Set<String> assetIds = list.stream().map(InspectDetail::getAssetId).collect(Collectors.toSet());
        Set<String> targetNames = list.stream().map(InspectDetail::getTargetName).collect(Collectors.toSet());
        QueryWrapper<AlarmInfo> alarmQuery = Wrappers.query();
        alarmQuery.in("ASSET_ID", assetIds);
        alarmQuery.eq("STATUS", AlarmStatusEnum.UNCONFIRM.getCode());
        alarmQuery.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
        alarmQuery.eq("BLANK", AlarmStateEnum.ALARM.getCode());
        List<AlarmInfo> alarmList = alarmInfoService.list(alarmQuery);
        for (AlarmInfo alarmInfo : alarmList) {
            XunjianAlarmMsg alarmMsg = new XunjianAlarmMsg();
            List<String> descriptionList = alarmInfoService.queryDescriptionList(alarmInfo.getId());
            alarmMsg.setOpinion(descriptionList.toString());
            alarmMsg.setContent(alarmInfo.getContent());
            alarmList2.add(alarmMsg);
        }

        Map<String, XunjianRepoBody> map = new HashMap<>();
        Set<String> nset = new HashSet<>();
        int exceptionNum = 0;
        for (InspectDetail detail : list) {
            String assetId = detail.getAssetId();
            XunjianRepoBody report = map.get(assetId);
            if (Objects.isNull(report)) {
                report = new XunjianRepoBody();
                report.setAssetId(assetId);
                report.setAssetName(detail.getAssetName());
                map.put(assetId, report);

                Integer assetDesk = detail.getAssetDesk();
                this.setDefaultResult(report, assetDesk);
            }
            this.setResult(detail, report);

            if (nset.contains(assetId)) {
                continue;
            }
            if (!(Web2Const.INSPECTED.equals(detail.getInspectState())
                    || Web2Const.UNKNOWN.equals(detail.getInspectState()))) {
                exceptionNum += 1;
                nset.add(assetId);
            }
        }

        List<XunjianRepoBody> reportList = new ArrayList<>(map.values());
        XunjianReportTemp req = new XunjianReportTemp();
        req.setAlarmList(alarmList2);
        req.setReportList(reportList);
        req.setOperator(ShiroUtil.getSubject().getNickname());
        req.setXunjianShift("");
        req.setXunjianTarget(targetNames.toString());
        req.setExceptionNum(exceptionNum);
        req.setNormalNum(assetIds.size() - req.getExceptionNum());
        req.setXunjianTime(list.get(0).getInspectTime());

        SXSSFWorkbook createExcel = XunjianReportUtil.createExcel(req);
        DispatchRecordExcelUtil.responseBody(createExcel, response, "智能巡检报告单");
    }

    private final String[] itemArr = {
            StatusInfoChangeTypeEnum.event_net_state.getCode(), StatusInfoChangeTypeEnum.event_process_status.getCode(),
            StatusInfoChangeTypeEnum.event_CPU_normal.getCode(), StatusInfoChangeTypeEnum.event_disk_normal.getCode(),
            StatusInfoChangeTypeEnum.event_memory_normal.getCode(), StatusInfoChangeTypeEnum.event_run_time_state.getCode(),
            StatusInfoChangeTypeEnum.event_port_in_normal.getCode(), StatusInfoChangeTypeEnum.event_port_out_normal.getCode(),
            StatusInfoChangeTypeEnum.event_db_connect.getCode()
    };

    private void setResult(InspectDetail detail, XunjianRepoBody report) {
        Integer flag = "3".equals(detail.getInspectState()) ? XunjianDetail.NORMAL_FLAG : XunjianDetail.EXCEPTION_FLAG;
        String targetItem = detail.getTargetItem();
        String result = StringUtils.isEmpty(detail.getResultMsg()) ? "暂无" : detail.getResultMsg();

        this.setAlarmResult(detail, report);

        if (StatusInfoChangeTypeEnum.event_net_state.getCode().equals(targetItem)) {
            report.setNetCardNormalFlag(flag);
            report.setNetCardResultMsg(result);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_process_status.getCode().equals(targetItem)) {
            report.setSoftwareNormalFlag(flag);
            report.setSoftwareResultMsg(result);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_db_connect.getCode().equals(targetItem)) {
            report.setOracleFlag(flag);
            report.setOracleMag(result);
            return;
        }
        if (!(targetItem.contains("normal") || targetItem.contains("run_state"))) {
            return;
        }

        String thresholdValue = detail.getThresholdValue();
        String inspectValue = detail.getInspectValue();
        if (StatusInfoChangeTypeEnum.event_CPU_normal.getCode().equals(targetItem)) {
            if (StringUtils.isEmpty(thresholdValue)) {
                report.setCpuNormalFlag(XunjianDetail.NORMAL_FLAG);
                report.setCpuResultMsg("未设置CPU阈值");
                return;
            }
            String thresholdTemp = TemplateUtil.getThresholdTemp(Double.parseDouble(thresholdValue), Double.parseDouble(inspectValue), false, true);
            report.setCpuNormalFlag(flag);
            report.setCpuResultMsg(thresholdTemp);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_disk_normal.getCode().equals(targetItem)) {
            if (StringUtils.isEmpty(thresholdValue)) {
                report.setDiskNormalFlag(XunjianDetail.NORMAL_FLAG);
                report.setDiskResultMsg("未设置磁盘阈值");
                return;
            }
            String thresholdTemp = TemplateUtil.getThresholdTemp(Double.parseDouble(thresholdValue), Double.parseDouble(inspectValue), true, false);
            report.setDiskNormalFlag(flag);
            report.setDiskResultMsg(thresholdTemp);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_memory_normal.getCode().equals(targetItem)) {
            if (StringUtils.isEmpty(thresholdValue)) {
                report.setMemoryNormalFlag(XunjianDetail.NORMAL_FLAG);
                report.setMemoryResultMsg("未设置内存阈值");
                return;
            }
            String thresholdTemp = TemplateUtil.getThresholdTemp(Double.parseDouble(thresholdValue), Double.parseDouble(inspectValue), false, true);
            report.setMemoryNormalFlag(flag);
            report.setMemoryResultMsg(thresholdTemp);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_run_time_state.getCode().equals(targetItem)) {
            if (StringUtils.isEmpty(thresholdValue)) {
                report.setRunTimelog(XunjianDetail.NORMAL_FLAG);
                report.setRunTimeMag("未设置运行时长阈值");
                return;
            }
            String thresholdTemp = TemplateUtil.getThresholdTemp(Double.parseDouble(thresholdValue), Double.parseDouble(inspectValue), false, false);
            report.setRunTimelog(flag);
            report.setRunTimeMag(thresholdTemp);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_port_in_normal.getCode().equals(targetItem)) {
            if (StringUtils.isEmpty(thresholdValue)) {
                report.setPortInNormalFlag(XunjianDetail.NORMAL_FLAG);
                report.setPortInResultMsg("未设置端口流入阈值");
                return;
            }
            String thresholdTemp = TemplateUtil.getThresholdTemp(Double.parseDouble(thresholdValue), Double.parseDouble(inspectValue), true, false);
            report.setPortInNormalFlag(flag);
            report.setPortInResultMsg(thresholdTemp);
            return;
        }
        if (StatusInfoChangeTypeEnum.event_port_out_normal.getCode().equals(targetItem)) {
            if (StringUtils.isEmpty(thresholdValue)) {
                report.setPortOutNormalFlag(XunjianDetail.NORMAL_FLAG);
                report.setPortOutResultMsg("未设置端口流出阈值");
                return;
            }
            String thresholdTemp = TemplateUtil.getThresholdTemp(Double.parseDouble(thresholdValue), Double.parseDouble(inspectValue), true, false);
            report.setPortOutNormalFlag(flag);
            report.setPortOutResultMsg(thresholdTemp);
        }
    }

    private void setAlarmResult(InspectDetail detail, XunjianRepoBody report) {
        if (detail == null || detail.getAssetId() == null) {
            report.setAlarmResultMsg(String.format("【一级告警】：%s,【二级告警】：%s,【三级告警】：%s,【未知告警】：%s", 0, 0, 0, 0));
            report.setAlarmNormalFlag(XunjianDetail.NORMAL_FLAG);
            return;
        }

        String assetId = detail.getAssetId();
        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<AlarmInfo>();
        queryWrapper.eq("ASSET_ID", assetId);
        queryWrapper.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
        queryWrapper.eq("BLANK", AlarmStateEnum.ALARM.getCode());
        queryWrapper.ne("ALARM_LEVEL", AlarmLevelEnum.LEVEL_MSG.getCode());

        List<AlarmInfo> list = alarmInfoService.list(queryWrapper);
        if (list == null || list.isEmpty()) {
            report.setAlarmResultMsg(String.format("【一级告警】：%s,【二级告警】：%s,【三级告警】：%s,【未知告警】：%s", 0, 0, 0, 0));
            report.setAlarmNormalFlag(XunjianDetail.NORMAL_FLAG);
            return;
        }

        int oneLevelCount = 0, twoLevelCount = 0, threeLevelCount = 0, unknownCount = 0;

        for (AlarmInfo item : list) {
            byte alarmLevel = item.getAlarmLevel();
            if (alarmLevel == AlarmLevelEnum.LEVEL_ONE.getCode()) {
                oneLevelCount++;
            } else if (alarmLevel == AlarmLevelEnum.LEVEL_TWO.getCode()) {
                twoLevelCount++;
            } else if (alarmLevel == AlarmLevelEnum.LEVEL_THREE.getCode()) {
                threeLevelCount++;
            } else {
                unknownCount++;
            }
        }

        report.setAlarmResultMsg(String.format("【一级告警】：%s,【二级告警】：%s,【三级告警】：%s,【未知告警】：%s", oneLevelCount, twoLevelCount, threeLevelCount, unknownCount));
        report.setAlarmNormalFlag(XunjianDetail.EXCEPTION_FLAG);
    }


    private void setDefaultResult(XunjianRepoBody report, Integer assetDesk) {
        report.setOracleMag("该设备类型无此指标");
        report.setOracleFlag(XunjianDetail.NORMAL_FLAG);
        if (assetDesk == 201 || assetDesk == 42) {
            report.setSoftwareNormalFlag(XunjianDetail.NORMAL_FLAG);
            report.setRunTimelog(XunjianDetail.NORMAL_FLAG);
            report.setNetCardNormalFlag(XunjianDetail.NORMAL_FLAG);
            report.setDiskNormalFlag(XunjianDetail.NORMAL_FLAG);

            report.setSoftwareResultMsg("该设备类型无此指标");
            report.setRunTimeMag("此设备无法获取运行时长");
            report.setNetCardResultMsg("该设备类型无此指标");
            report.setDiskResultMsg("该设备类型无此指标");
        } else {
            report.setRunTimelog(XunjianDetail.NORMAL_FLAG);
            report.setPortOutNormalFlag(XunjianDetail.NORMAL_FLAG);
            report.setPortInNormalFlag(XunjianDetail.NORMAL_FLAG);

            report.setRunTimeMag("此设备未采集系统时间");
            report.setPortOutResultMsg("该设备类型无此指标");
            report.setPortInResultMsg("该设备类型无此指标");
        }
    }
}
