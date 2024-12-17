package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.vo.DetailCabinetVo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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


}
