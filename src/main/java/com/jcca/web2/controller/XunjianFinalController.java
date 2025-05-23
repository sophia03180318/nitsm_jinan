package com.jcca.web2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dto.InspectTargetDetailInfo;
import com.jcca.web2.dto.InspectTargetDetailInfoVo;
import com.jcca.web2.dto.XunjianJobDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.*;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: hhw
 * @description: XunjianFinalController 主要是用来处理终版智能巡检
 * @date: 2025-05-18  15:38
 * @since: 2.1.6.0
 */
@RestController
@RequestMapping("/api/v2/xfinal")
@Api(tags = "终版巡检")
public class XunjianFinalController {

    @Resource
    private XunjianScheduleService xunjianScheduleService;
    @Resource
    private AssetModeService assetModeService;
    @Resource
    private AlarmEventTypeService alarmEventTypeService;
    @Resource
    private InspectAssetService inspectAssetService;
    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private InspectDetailService inspectDetailService;



    @GetMapping("/job/list")
    @ApiOperation("巡检任务列表")
    public ResultVo<Object> jobList(Integer autoFlag, Integer jobState) {
        String username = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("OPERATOR", username);
        if (jobState != null) {
            query.eq("JOB_STATE", jobState);
        }
        if (autoFlag != null) {
            query.eq("AUTO_FLAG", autoFlag);
        }
        query.orderByDesc("JOB_ID");
        query.orderByAsc("CRON_TIME");
        List<XunjianSchedule> list = xunjianScheduleService.list(query);
        if (list.isEmpty()) {
            return ResultVoUtil.success(list);
        }

        for (XunjianSchedule schedule : list) {
            String cronTime = schedule.getCronTime();
            if (StringUtils.isEmpty(cronTime)) {
                continue;
            }
            String[] split = cronTime.split(",");
            schedule.setCronList(Arrays.asList(split));
        }

        return ResultVoUtil.success(list);
    }

    @GetMapping("/record/list")
    @ApiOperation("巡检记录列表")
    public ResultVo<Object> recordList() {
        String username = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.select("JOB_ID", "JOB_NAME");
        query.eq("OPERATOR", username);
        query.groupBy("JOB_ID", "JOB_NAME");
        query.orderByDesc("JOB_ID");
        List<XunjianSchedule> list = xunjianScheduleService.list(query);
        if (list.isEmpty()) {
            return ResultVoUtil.success(list);
        }

        List<ItemVo> resultList = new ArrayList<>();
        for (XunjianSchedule schedule : list) {
            List<ItemVo> voList = inspectRecordService.findBySchuduleId(schedule.getJobId());
            if (voList.isEmpty()) {
                continue;
            }
            ItemVo itemVo = new ItemVo();
            itemVo.setId(schedule.getJobId());
            itemVo.setName(schedule.getJobName());
            itemVo.setChildren(voList);
            resultList.add(itemVo);
        }
        return ResultVoUtil.success(resultList);
    }

    @GetMapping("/record/detail")
    @ApiOperation("巡检记录详情")
    public ResultVo<Object> recordDetail(String id) {
        Map<String, Object> recordDetail = inspectDetailService.getRecordDetail(id);
        return ResultVoUtil.success(recordDetail);
    }

    @GetMapping("/target/detail")
    @ApiOperation("资产指标详情")
    public ResultVo<Object> targetDetail(String inspectCode, String assetId) {
        InspectTargetDetailInfoVo result = inspectDetailService.getTargetDetail(inspectCode, assetId);
        return ResultVoUtil.success(result);
    }

    @GetMapping("/target/asset")
    @ApiOperation("指标下异常资产详情")
    public ResultVo<Object> targetAsset(String jobId, String targetId) {
        List<InspectTargetDetailInfo> resultList = inspectAssetService.getTargetAssetInfo(jobId, targetId);
        return ResultVoUtil.success(resultList);
    }

    @GetMapping("/asset/target")
    @ApiOperation("资产下异常指标详情")
    public ResultVo<Object> assetTarget(String jobId, String assetId) {
        List<InspectTargetDetailInfo> resultList = inspectAssetService.getAssetTargetInfo(jobId, assetId);
        return ResultVoUtil.success(resultList);
    }

    @PostMapping("/job/add")
    @ApiOperation("新增巡检任务")
    public ResultVo<Object> jobAdd(@RequestBody @Validated XunjianJobDto dto) {
        String username = ShiroUtil.getSubject().getUsername();
        dto.setOperator(username);
        xunjianScheduleService.addSchedule(dto);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "添加巡检任务", dto.getJobName());
        return ResultVoUtil.success();
    }

    @GetMapping("/job/begin")
    @ApiOperation("开始巡检")
    public ResultVo<Object> jobBegin(String jobId) {
        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.size() > 1) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "周期巡检不需要手动开始");
        }
        XunjianSchedule schedule = list.get(0);
        if (schedule.getAutoFlag() != 1) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "只能开始手动巡检");
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "开始巡检任务", jobId);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianExecutor);
        executor.execute(() -> {
            XunjianJobDto dto = new XunjianJobDto();
            dto.setId(list.get(0).getId());
            dto.setOperator(schedule.getOperator());
            xunjianScheduleService.beginXunjian(dto);
        });
        return ResultVoUtil.success();
    }

    @GetMapping("/job/remove")
    @ApiOperation("删除巡检任务")
    public ResultVo<Object> jobRemove(String jobId) {
        xunjianScheduleService.removeSchedule(jobId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "删除巡检任务", jobId);
        return ResultVoUtil.success();
    }

    @PostMapping("/job/update")
    @ApiOperation("修改巡检任务")
    public ResultVo<Object> jobUpdate(@RequestBody @Validated XunjianJobDto dto) {
        String jobId = dto.getJobId();
        if (StringUtils.isEmpty(jobId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }

        String username = ShiroUtil.getSubject().getUsername();
        dto.setOperator(username);
        xunjianScheduleService.updateSchedule(dto);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "修改巡检任务", jobId);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/pause")
    @ApiOperation("暂停周期巡检任务")
    public ResultVo<Object> jobPause(String jobId) {
        xunjianScheduleService.pauseJob(jobId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "暂停巡检任务", jobId);
        return ResultVoUtil.success();
    }

    @GetMapping("/job/recover")
    @ApiOperation("恢复周期巡检任务")
    public ResultVo<Object> jobRecover(String jobId) {
        xunjianScheduleService.recoverJob(jobId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "恢复巡检任务", jobId);
        return ResultVoUtil.success();
    }

    @GetMapping("/asset/list")
    @ApiOperation("资产分类列表")
    public ResultVo<Object> assetList() {
        List<ItemVo> list = xunjianScheduleService.getOrgModeAssetList();
        return ResultVoUtil.success(list);
    }

    @PostMapping("/target/list")
    @ApiOperation("指标分类列表")
    public ResultVo<Object> targetList(@RequestBody List<String> assetDesks) {
        List<ItemVo> resultList = new ArrayList<>();
        Map<Integer, String> modeMap = assetModeService.getModeMap();
        Set<String> keySet = new HashSet<>(assetDesks);
        for (String key : keySet) {
            String id = key + ",";
            String mode = modeMap.get(Integer.parseInt(key));
            ItemVo vo = new ItemVo();
            vo.setId(id);
            vo.setName(mode);

            List<ItemVo> list = alarmEventTypeService.listTypeByAssetDesk(id);
            if (!list.isEmpty()) {
                vo.setChildren(list);
                resultList.add(vo);
            }
        }

        return ResultVoUtil.success(resultList);
    }

    @GetMapping("/checked/list")
    @ApiOperation("资产分类列表")
    public ResultVo<Object> getCheckedAssetTarget(String jobId) {
        InspectAssetAndTarget result = inspectAssetService.getCheckedAssetTarget(jobId);
        return ResultVoUtil.success(result);
    }

    @GetMapping("/asset/status")
    @ApiOperation("资产状态列表")
    public ResultVo<Object> assetStatus(String jobId) {
        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }
        if (list.get(0).getJobState() != 2) {
            return ResultVoUtil.success("只能查看正在巡检的任务");
        }
        List<ItemVo> resultList = inspectAssetService.getAllCheckedAsset(jobId);
        return ResultVoUtil.success(resultList);
    }

    @GetMapping("/target/status")
    @ApiOperation("指标状态列表")
    public ResultVo<Object> targetStatus(String jobId) {
        List<ItemVo> resultList = inspectAssetService.getTargetStatus(jobId);
        QueryWrapper<InspectAsset> query = Wrappers.query();
        query.eq("job_id", jobId);
        int totalCount = inspectAssetService.count(query);

        query = Wrappers.query();
        query.eq("job_id", jobId);
        query.eq("inspect_state", Web2Const.INSPECTED);
        int normalCount = inspectAssetService.count(query);

        query = Wrappers.query();
        query.eq("job_id", jobId);
        query.eq("inspect_state", Web2Const.INSPECT_ERROR);
        int abnormalCount = inspectAssetService.count(query);

        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", totalCount);
        map.put("normalCount", normalCount);
        map.put("abnormalCount", abnormalCount);
        map.put("resultList", resultList);

        return ResultVoUtil.success(map);
    }

}
