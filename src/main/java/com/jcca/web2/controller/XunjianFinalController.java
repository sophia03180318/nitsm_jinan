package com.jcca.web2.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfo;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfoVo;
import com.jcca.web2.dto.xunjian.XunjianJobDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.enums.ThresholdCategoryEnum;
import com.jcca.web2.service.*;
import com.jcca.web2.vo.InspectAssetAndTarget;
import com.jcca.web2.vo.ItemVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static com.jcca.web2.constant.Web2Const.*;
import static com.jcca.web2.service.XunjianCollectRun.targetAbnormalSet;
import static com.jcca.web2.service.XunjianCollectRun.targetNormalSet;

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
    @Resource
    private CollectAgent collectAgent;
    @Resource
    private ThresholdManageService thresholdManageService;
    @Resource
    private ManageDbService manageDbService;
    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private AssetService assetService;


    @GetMapping("/job/list")
    @ApiOperation("巡检任务列表")
    public ResultVo<Object> jobList(Integer autoFlag, Integer jobState, Integer page, Integer size) {
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
        IPage<XunjianSchedule> iPage = PagePlugin.startPageT(page, size, XunjianSchedule.class);
        IPage<XunjianSchedule> resultPage = xunjianScheduleService.page(iPage, query);
        List<XunjianSchedule> list = resultPage.getRecords();
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
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", resultPage.getTotal());

        return ResultVoUtil.success(result);
    }

    @GetMapping("/record/list")
    @ApiOperation("巡检记录列表")
    public ResultVo<Object> recordList() {
        String username = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.select("JOB_ID", "JOB_NAME", "AUTO_FLAG");
        query.eq("OPERATOR", username);
        query.groupBy("JOB_ID", "JOB_NAME", "AUTO_FLAG");
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
            itemVo.setAutoFlag(schedule.getAutoFlag());
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
        List<String> assetList = dto.getAssetList();
        Map<String, List<String>> targetList = dto.getTargetList();
        if (CollectionUtils.isEmpty(assetList) || CollectionUtils.isEmpty(targetList)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        String username = ShiroUtil.getSubject().getUsername();
        dto.setOperator(username);
        String jobId = xunjianScheduleService.addSchedule(dto);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "添加巡检任务", dto.getJobName());
        Map<String, String> map = new HashMap<>();
        map.put("jobId", jobId);
        return ResultVoUtil.success(map);
    }

    @GetMapping("/job/check")
    @ApiOperation("校验任务名称")
    public ResultVo<Object> checkName(String jobId, String jobName) {

        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("JOB_NAME", jobName);
        query.eq("OPERATOR", ShiroUtil.getSubject().getUsername());
        if (!StringUtils.isEmpty(jobId)) {
            query.ne("JOB_ID", jobId);
        }
        List<XunjianSchedule> list = xunjianScheduleService.list(query);
        if (!list.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "任务名称不能重复");
        }

        return ResultVoUtil.success();
    }

    @GetMapping("/job/begin")
    @ApiOperation("开始巡检")
    public ResultVo<Object> jobBegin(String jobId) {
        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }
        XunjianSchedule schedule = list.get(0);
        if (schedule.getAutoFlag() != 1) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "只能开始手动巡检");
        }
        if (schedule.getJobState() != 1) {
            return ResultVoUtil.error(ResultEnum.ERROR.getCode(), "任务已开始或已暂停");
        }

        // 巡检前让采集器推送一次进程状态数据
        try {
            collectAgent.sendPostToCenter(XUNJIAN_PROCESS_URI, "", XUNJIAN_TIME_OUT);
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", jobId);
            throw new ResultException(ResultEnum.INSPECT_COLLECT_ERROR, "向采集器获取状态数据异常");
        }

        // 将任务设置为正在巡检
        schedule.setJobState(Integer.parseInt(Web2Const.INSPECTING));
        schedule.setLastTime(new Date());
        xunjianScheduleService.updateById(schedule);
        // 将指标设置为最初状态
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
        for (InspectAsset asset : assetList) {
            asset.setInspectState(Web2Const.INSPECT);
        }
        inspectAssetService.updateBatchById(assetList);

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "开始巡检任务", jobId);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianExecutor);
        executor.execute(() -> {
            XunjianJobDto dto = new XunjianJobDto();
            dto.setAutoFlag(1);
            dto.setId(schedule.getId());
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
        List<String> assetList = dto.getAssetList();
        Map<String, List<String>> targetList = dto.getTargetList();
        if (CollectionUtils.isEmpty(assetList) || CollectionUtils.isEmpty(targetList)) {
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

    @GetMapping("/job/reset")
    @ApiOperation("重置任务状态")
    public ResultVo<Object> resetJob(String jobId) {
        List<XunjianSchedule> list = xunjianScheduleService.findByJobId(jobId);
        if (list.isEmpty()) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }
        XunjianSchedule schedule = list.get(0);
        if (schedule.getJobState() != 2) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "只能停止正在进行中的任务");
        }

        // 将任务设置为最初状态
        schedule.setJobState(Integer.parseInt(Web2Const.INSPECT));
        xunjianScheduleService.updateById(schedule);

        // 将指标设置为最初状态
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
        for (InspectAsset asset : assetList) {
            asset.setInspectState(Web2Const.INSPECT);
        }
        inspectAssetService.updateBatchById(assetList);

        xunjianScheduleService.resetJob(schedule);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "手动停止巡检任务", jobId);
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
    public ResultVo<Object> targetList(@RequestBody Map<String, List<String>> map) {
        List<String> assetDesks = map.get("assetDesks");
        List<String> assetIds = map.get("assetIds");
        if (CollectionUtils.isEmpty(assetDesks) || CollectionUtils.isEmpty(assetIds)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        List<ItemVo> resultList = new ArrayList<>();
        Map<Integer, String> modeMap = assetModeService.getModeMap();
        Set<String> keySet = new HashSet<>(assetDesks);
        for (String key : keySet) {
            String id = key + ",";
            String mode = modeMap.get(Integer.parseInt(key));
            ItemVo vo = new ItemVo();
            vo.setId(id);
            vo.setName(mode);


            List<ItemVo> reslist = new ArrayList<>();
            List<ItemVo> list = alarmEventTypeService.listTypeByAssetDesk(id);
            for (ItemVo itemVo : list) {
                int count = this.checkTarget(itemVo.getEventCategory(), key, assetIds);
                if (count == 0) {
                    continue;
                }
                reslist.add(itemVo);
            }
            vo.setChildren(reslist);
            resultList.add(vo);
        }

        return ResultVoUtil.success(resultList);
    }


    private int checkTarget(String eventCategory, String assetDesk, List<String> assetIds) {
        int count = 1;
        // 查磁盘阈值
        if (StatusInfoChangeTypeEnum.event_disk.getCode().equals(eventCategory)) {
            QueryWrapper<ThresholdManage> query = Wrappers.query();
            query.in("ASSET_ID", assetIds);
            query.eq("CATEGORY", ThresholdCategoryEnum.DISK.name());
            query.eq("ASSET_DESK", assetDesk);
            count = thresholdManageService.count(query);
        }
        // 查内存阈值
        if (StatusInfoChangeTypeEnum.event_memory.getCode().equals(eventCategory)) {
            QueryWrapper<ThresholdManage> query = Wrappers.query();
            query.in("ASSET_ID", assetIds);
            query.eq("CATEGORY", ThresholdCategoryEnum.MEMORY.name());
            query.eq("ASSET_DESK", assetDesk);
            count = thresholdManageService.count(query);
        }
        // 查数据库
        if (StatusInfoChangeTypeEnum.event_db.getCode().equals(eventCategory)
                || StatusInfoChangeTypeEnum.event_db_tableSpace.getCode().equals(eventCategory)) {
            QueryWrapper<ManageDb> query = Wrappers.query();
            query.in("ASSET_ID", assetIds);
            count = manageDbService.count(query);
        }
        if (StatusInfoChangeTypeEnum.event_db_tableSpace.getCode().equals(eventCategory)) {
            QueryWrapper<ThresholdManage> query = Wrappers.query();
            query.in("ASSET_ID", assetIds);
            query.eq("CATEGORY", ThresholdCategoryEnum.TABLE_SPACE.name());
            query.eq("ASSET_DESK", assetDesk);
            count = thresholdManageService.count(query);
        }
        // 查进程
        if (StatusInfoChangeTypeEnum.event_process.getCode().equals(eventCategory)) {
            QueryWrapper<ThresholdProcess> query = Wrappers.query();
            query.in("ASSET_ID", assetIds);
            count = thresholdProcessService.count(query);
        }
        // 查时间
        if (StatusInfoChangeTypeEnum.event_time.getCode().equals(eventCategory)) {
            QueryWrapper<Asset> query = Wrappers.query();
            query.in("ID", assetIds);
            query.eq("WATCH", 1);
            query.eq("IS_DEL", 1);
            query.eq("NTP_FLAG", 1);
            List<Asset> list = assetService.list(query);
            return list.size();
        }

        // 查管理口
        if (AssetModeConst.SERVER == Integer.parseInt(assetDesk)) {
            for (String s : SYSPORT_TARGET_ARR) {
                if (s.startsWith(eventCategory)) {
                    QueryWrapper<Asset> query = Wrappers.query();
                    query.in("ID", assetIds);
                    query.eq("WATCH", 1);
                    query.eq("IS_DEL", 1);
                    query.isNotNull("IPMI_IP");
                    query.isNotNull("IPMI_USER");
                    query.isNotNull("IPMI_PWD");
                    List<Asset> list = assetService.list(query);
                    return list.size();
                }
            }
        }

        return count;
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
            return ResultVoUtil.warning("只能查看正在巡检的任务");
        }
        List<ItemVo> resultList = inspectAssetService.getAllCheckedAsset(jobId);
        return ResultVoUtil.success(resultList);
    }

    @GetMapping("/target/status")
    @ApiOperation("指标状态列表")
    public ResultVo<Object> targetStatus(String jobId) {
        List<ItemVo> resultList = new ArrayList<>();
        QueryWrapper<InspectAsset> query = Wrappers.query();
        query.select("EVENT_TYPE_ID", "EVENT_TYPE_NAME", "MAX(INSPECT_STATE) AS INSPECT_STATE");
        query.eq("JOB_ID", jobId);
        query.groupBy("EVENT_TYPE_ID", "EVENT_TYPE_NAME");
        query.orderByAsc("EVENT_TYPE_ID");
        List<InspectAsset> list = inspectAssetService.list(query);
        for (InspectAsset inspectAsset : list) {
            ItemVo vo = new ItemVo();
            vo.setId(inspectAsset.getEventTypeId());
            vo.setName(inspectAsset.getEventTypeName());
            vo.setStatus(Integer.parseInt(inspectAsset.getInspectState()));
            query = Wrappers.query();
            query.select("ASSET_ID");
            query.eq("EVENT_TYPE_ID", inspectAsset.getEventTypeId());
            query.eq("JOB_ID", jobId);
            query.groupBy("ASSET_ID");
            vo.setTotal(inspectAssetService.list(query).size());
            query = Wrappers.query();
            query.select("ASSET_ID");
            query.eq("EVENT_TYPE_ID", inspectAsset.getEventTypeId());
            query.eq("JOB_ID", jobId);
            query.eq("INSPECT_STATE", INSPECTED);
            query.groupBy("ASSET_ID");
            vo.setNormal(inspectAssetService.list(query).size());
            query = Wrappers.query();
            query.select("ASSET_ID");
            query.eq("EVENT_TYPE_ID", inspectAsset.getEventTypeId());
            query.eq("JOB_ID", jobId);
            query.eq("INSPECT_STATE", INSPECT_ERROR);
            query.groupBy("ASSET_ID");
            vo.setAbnormal(inspectAssetService.list(query).size());
            resultList.add(vo);
        }

        query = Wrappers.query();
        query.select("EVENT_TYPE_ID");
        query.eq("JOB_ID", jobId);
        query.groupBy("EVENT_TYPE_ID");
        int totalCount = inspectAssetService.list(query).size();

        int normalCount = 0, abnormalCount = 0;
        String inspectRecordId = XUNJIAN_JOB_RECORD.get(jobId);
        if (inspectRecordId != null) {
            normalCount = targetNormalSet.get(inspectRecordId) == null ? 0 : targetNormalSet.get(inspectRecordId).size();
            abnormalCount = targetAbnormalSet.get(inspectRecordId) == null ? 0 : targetAbnormalSet.get(inspectRecordId).size();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", totalCount);
        map.put("normalCount", normalCount);
        map.put("abnormalCount", abnormalCount);
        map.put("resultList", resultList);

        return ResultVoUtil.success(map);
    }

    @GetMapping("/detail/report1View")
    @ApiOperation("巡检报告单1")
    public ResultVo<Object> report1(String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.warning("暂无数据");
        }
        Map<String, Object> list = inspectDetailService.getReport1(id);
        return ResultVoUtil.success(list);
    }

    @GetMapping("/detail/report1Down")
    @ApiOperation("巡检报告单1下载")
    public void report1Down(String id, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(id)) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "暂无数据");
        }
        Map<String, Object> map = inspectDetailService.report1Down(id);

        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.merge(5, "综合维护平台巡检报告");
        writer.merge(5, map.get("header1") + " " + map.get("header2"));

        writer.addHeaderAlias("index", "序号");
        writer.addHeaderAlias("assetDeskStr", "设备类型");
        writer.addHeaderAlias("assetName", "设备名称");
        writer.addHeaderAlias("alarmLevelStr", "告警级别");
        writer.addHeaderAlias("description", "告警描述");
        writer.addHeaderAlias("remarkStr", "参考建议");
        writer.setOnlyAlias(true);

        writer.setRowHeight(0, 18);
        writer.setColumnWidth(2, 20);
        writer.setColumnWidth(4, 100);
        writer.setColumnWidth(5, 30);

        writer.write((List) map.get("list"), true);

        String fileName = URLEncoder.encode("综合维护平台巡检报告.xlsx", "UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ServletOutputStream out = response.getOutputStream();

        writer.flush(out);
        writer.close();

    }
}
