package com.jcca.web2.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.config.quartz.QuartzJobManager;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.webssh.websocket.XunjianWebSocketHandler;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.component.quartz.inspect.XunjianJob;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.XunjianScheduleDao;
import com.jcca.web2.dto.XunjianJobDto;
import com.jcca.web2.dto.XunjianWSDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.service.XunjianScheduleService;
import com.jcca.web2.util.TimeToCronConverter;
import com.jcca.web2.vo.ItemVo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: hhw
 * @description: XunjianScheduleServiceImpl主要是用来
 * @date: 2025-02-25  15:55
 * @since: 2.0.11.0
 */
@Slf4j
@Service
public class XunjianScheduleServiceImpl extends ServiceImpl<XunjianScheduleDao, XunjianSchedule> implements XunjianScheduleService {


    @Resource
    private QuartzJobManager jobManager;
    @Resource
    private AlarmEventTypeService alarmEventTypeService;
    @Resource
    private AlarmRepositoryService alarmRepositoryService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private InspectAssetService inspectAssetService;
    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private InspectDetailService inspectDetailService;
    @Resource
    private ThresholdManager thresholdManager;
    @Resource
    private ThresholdProcessService thresholdProcessService;


    /**
     * 保存巡检任务
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSchedule(XunjianJobDto dto) {

        Integer autoFlag = dto.getAutoFlag();
        String cronTimes = dto.getCronTimes();
        if (autoFlag == 2 && StringUtils.isEmpty(cronTimes)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        String jobName = dto.getJobName();
        String operator = dto.getOperator();
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("job_name", jobName);
        query.eq("operator", operator);
        int count = this.count(query);
        if (count > 0) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "任务名称不能重复");
        }

        String jobId = MyIdUtil.getId();
        dto.setJobId(jobId);
        if (autoFlag == 1) {
            XunjianSchedule schedule = this.setJob(dto);
            schedule.setId(MyIdUtil.getId());
            schedule.setStartNow(dto.getStartNow() == null ? 1 : dto.getStartNow());
            this.save(schedule);

            // 保存资产和指标
            this.saveInspectAsset(dto);

            // 是否立即执行
            if (dto.getStartNow() == 2) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianExecutor);
                executor.execute(() -> {
                    dto.setId(schedule.getId());
                    this.beginXunjian(dto);
                });
            }
            return;
        }

        XunjianSchedule schedule = this.setJob(dto);
        schedule.setId(MyIdUtil.getId());
        String[] tims = cronTimes.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(tims));
        StringBuilder cronSB = new StringBuilder();
        for (String time : set) {
            String cron = TimeToCronConverter.convertToCron(time);
            cronSB.append(cron).append(",");
            // 添加到周期任务
            try {
                jobManager.addJob(schedule.getId() + "_" + cron, schedule.getOperator(), cron, XunjianJob.class);
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "添加巡检任务异常，jobId：" + schedule.getId(), e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "添加巡检任务异常");
            }
        }

        schedule.setStartNow(1);
        schedule.setCronTime(cronTimes);
        schedule.setCron(cronSB.toString());
        this.save(schedule);

        // 保存资产和指标
        this.saveInspectAsset(dto);
    }


    // 巡检资产 inspect_asset job_id == xunjian_schedule_job_id
    private void saveInspectAsset(XunjianJobDto dto) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "开始保存巡检资产", DateUtil.formatDateTime(new Date()));

        String jobId = dto.getJobId();
        List<String> assetIds = dto.getAssetList();
        Map<String, List<String>> targetMap = dto.getTargetList();
        List<InspectAsset> batchList = new ArrayList<>();
        List<InspectAsset> list = inspectAssetService.getInspectAssets(assetIds);
        for (InspectAsset asset : list) {
            String desk = asset.getAssetDesk() + ",";
            List<String> targetList = targetMap.get(desk);
            if (targetList == null) {
                continue;
            }
            for (String target : targetList) {
                Set<String> set = new HashSet<>();
                AlarmEventType alarmEventType = alarmEventTypeService.getById(target);
                List<AlarmRepository> repositoryList = alarmRepositoryService.getAllByEventId(target);
                for (AlarmRepository repository : repositoryList) {
                    if (set.contains(repository.getAlarmCode())) {
                        continue;
                    }
                    set.add(repository.getAlarmCode());
                    InspectAsset inspectAsset = new InspectAsset();
                    BeanUtils.copyProperties(asset, inspectAsset);
                    inspectAsset.setId(MyIdUtil.getId());
                    inspectAsset.setJobId(jobId);
                    inspectAsset.setTargetItem(repository.getAlarmCode());
                    inspectAsset.setTargetName(repository.getDescStr());
                    inspectAsset.setInspectState(Web2Const.INSPECT);
                    inspectAsset.setInspectType(dto.getAutoFlag());
                    inspectAsset.setEventTypeId(target);
                    inspectAsset.setEventTypeName(alarmEventType.getTypeAlias());
                    String thresholdValue = this.getThreshold(inspectAsset);
                    if (repository.getDescStr().contains("阈值") && StringUtils.isEmpty(thresholdValue)) {
                        continue;
                    }
                    inspectAsset.setThresholdValue(this.getThreshold(inspectAsset));

                    batchList.add(inspectAsset);
                    if (batchList.size() >= 900) {
                        inspectAssetService.saveBatch(batchList);
                        batchList.clear();
                    }
                }
            }
        }
        if (!batchList.isEmpty()) {
            inspectAssetService.saveBatch(batchList);
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "结束保存巡检资产", DateUtil.formatDateTime(new Date()));
    }

    private String getThreshold(InspectAsset inspectAsset) {
        ThresholdBaseEntity entity = null;
        String assetDesk = inspectAsset.getAssetDesk() + "";
        if (assetDesk.contains("183")) {
            List<ThresholdProcess> thresholdProcessList = thresholdProcessService.selectByAssetList(Collections.singletonList(inspectAsset.getAssetId()));
            for (ThresholdProcess thresholdProcess : thresholdProcessList) {
                entity = thresholdManager.getThresholdValue(inspectAsset.getTargetItem(), inspectAsset.getAssetId(), thresholdProcess.getProcessName());
                break;
            }
        }
        if (entity == null) {
            entity = thresholdManager.getThresholdValue(inspectAsset.getTargetItem(), inspectAsset.getAssetId(), null);
        }

        if (Objects.isNull(entity)) {
            return "";
        }
        if (!entity.baseValueIsNull()) {
            return entity.getBaseValue() + "";
        }
        if (!entity.sectionValueIsNull()) {
            return entity.getMinValue() + "-" + entity.getMaxValue();
        }
        if (!entity.oneLevelIsNull() || !entity.twoLevelIsNull() || !entity.threeLevelIsNull()) {
            String level = "one-two-three";
            if (!entity.oneLevelIsNull()) {
                level = level.replace("one", entity.getOneLevelValue() + "");
            }
            if (!entity.twoLevelIsNull()) {
                level = level.replace("two", entity.getTwoLevelValue() + "");
            }
            if (!entity.threeLevelIsNull()) {
                level = level.replace("three", entity.getThreeLevelValue() + "");
            }
            return level;
        }
        return "";
    }

    /**
     * 项目启动加入到周期任务
     */
    @Override
    public void joinScheduleJob() {
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("JOB_STATE", 1);
        query.eq("AUTO_FLAG", 2);
        List<XunjianSchedule> list = this.list(query);
        try {
            for (XunjianSchedule schedule : list) {
                String[] crons = schedule.getCron().split(",");
                for (String cron : crons) {
                    if (StringUtils.isEmpty(cron)) {
                        continue;
                    }
                    String jobName = schedule.getId() + "_" + cron;
                    jobManager.addJob(jobName, schedule.getOperator(), cron, XunjianJob.class);
                }
            }
        } catch (SchedulerException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "启动巡检任务异常", e);
            throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "启动巡检任务异常");
        }
    }

    /**
     * 真正巡检开始
     *
     * @param dto
     */
    @Override
    public void beginXunjian(XunjianJobDto dto) {
        // 巡检任务 xunjian_schedule id == inspect_record_schedule_id  job_id == inspect_record_inspect_code
        // 巡检资产 inspect_asset job_id == xunjian_schedule_job_id
        // 巡检记录 inspect_record scheduled_id == xunjian_schedule_id  inspect_code == xunjian_schedule_job_id
        // 巡检明细 inspect_detail inspect_code == inspect_record_id
        String id = dto.getId();
        String operator = dto.getOperator();
        // 设置为正在巡检
        XunjianSchedule schedule = this.getById(id);
        schedule.setJobState(2);
        schedule.setLastTime(new Date());
        this.updateById(schedule);

        // 保存巡检记录
        String inspectRecordId = MyIdUtil.getId();
        schedule.setInspectRecordId(inspectRecordId);
        this.saveInspectRecord(schedule);

        // 巡检设备
        Map<String, Integer> assetStateMap = new HashMap<>();
        Map<String, Integer> targetStateMap = new HashMap<>();
        List<InspectDetail> detailList = new ArrayList<>();
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
        Map<String, List<InspectAsset>> assetCollect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getAssetId));
        Map<String, Integer> assetTargetMap = new HashMap<>();
        assetCollect.keySet().forEach(key -> {
            assetTargetMap.put(key, assetCollect.get(key).size());
        });

        Map<String, Long> targetItemMap = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getTargetItem, Collectors.counting()));

        Map<String, Integer> processMap = new HashMap<>();
        Map<String, Integer> targetMap = new HashMap<>();
        int total = assetList.size();
        int count = 0, state = 0;
        for (InspectAsset asset : assetList) {
            String assetId = asset.getAssetId();
            String targetItem = asset.getTargetItem();
            count++;
            // 设置资产指标为巡检中状态
            asset.setInspectState(Web2Const.INSPECTING);
            inspectAssetService.updateById(asset);

            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "正在巡检：" + asset.getAssetName(), asset.getTargetName());
            int result = 1;
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                result = -1;
            }

            // 资产进度
            if (assetStateMap.get(assetId) == null) {
                assetStateMap.put(assetId, state);
            } else {
                if (assetStateMap.get(assetId) > result) {
                    assetStateMap.put(assetId, result);
                }
            }
            if (processMap.get(assetId) == null) {
                processMap.put(assetId, 1);
            } else {
                processMap.put(assetId, processMap.get(assetId) + 1);
                if (processMap.get(assetId).intValue() == assetTargetMap.get(assetId).intValue()) {
                    this.sendMsg(operator, XunjianWSDto.ASSET_STATUS, asset.getJobId(), assetId, asset.getAssetName(), assetStateMap.get(assetId));
                }
            }

            // 指标进度
            if (targetStateMap.get(targetItem) == null) {
                targetStateMap.put(targetItem, state);
            } else {
                if (targetStateMap.get(targetItem) > result) {
                    targetStateMap.put(targetItem, result);
                }
            }
            if (targetStateMap.get(targetItem) == 4) {
                this.sendMsg(operator, XunjianWSDto.TARGET_STATUS, asset.getJobId(), asset.getEventTypeId(), asset.getEventTypeName(), targetStateMap.get(targetItem));
            }
            if (targetMap.get(targetItem) == null) {
                targetMap.put(targetItem, 1);
            } else {
                targetMap.put(targetItem, targetMap.get(targetItem) + 1);
                if (targetMap.get(targetItem) == targetItemMap.get(targetItem).intValue()) {
                    this.sendMsg(operator, XunjianWSDto.TARGET_STATUS, asset.getJobId(), asset.getEventTypeId(), asset.getEventTypeName(), targetStateMap.get(assetId));
                }
            }

            // 设置资产指标为巡检完成状态
            asset.setInspectState(result == 1 ? Web2Const.INSPECTED : Web2Const.INSPECT_ERROR);
            inspectAssetService.updateById(asset);

            BigDecimal process = new BigDecimal(count).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            this.sendMsg(operator, XunjianWSDto.XUNJIANING_ASSET, asset.getJobId(), asset.getAssetIp1(), asset.getAssetName(), 0);
            this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, asset.getJobId(), "100", "进度条", process.intValue());
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "进度条：" + asset.getJobId(), process.intValue());

            // 保存巡检详情
            InspectDetail inspectDetail = new InspectDetail();
            BeanUtils.copyProperties(asset, inspectDetail);
            inspectDetail.setId(MyIdUtil.getId());
            inspectDetail.setInspectCode(inspectRecordId);
            inspectDetail.setInspectTime(new Date());
            detailList.add(inspectDetail);

            if (detailList.size() >= 900) {
                inspectDetailService.saveBatch(detailList);
                detailList.clear();
            }
        }
        if (!detailList.isEmpty()) {
            inspectDetailService.saveBatch(detailList);
        }

        // 设置为结束巡检
        schedule.setJobState(1);
        this.updateById(schedule);

        // 设置资产指标为初始状态
        for (InspectAsset asset : assetList) {
            asset.setInspectState(Web2Const.INSPECT);
        }
        inspectAssetService.updateBatchById(assetList, 900);

        // 推送完成消息
        this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, schedule.getJobId(), "100", "进度条", 100);
    }

    private void sendMsg(String operator, Integer msgType, String jobId, String id, String name, Integer status) {
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null) {
            return;
        }
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        wsDto.setJobId(jobId);
        wsDto.setId(id);
        wsDto.setName(name);
        wsDto.setStatus(status);
        try {
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(wsDto)));
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "给前端发送消息", JSONUtil.toJsonStr(wsDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveInspectRecord(XunjianSchedule schedule) {
        InspectRecord inspectRecord = new InspectRecord();
        inspectRecord.setId(schedule.getInspectRecordId());
        inspectRecord.setScheduleId(schedule.getId());
        inspectRecord.setInspectCode(schedule.getJobId());
        inspectRecord.setInspectTime(schedule.getLastTime());
        inspectRecord.setCreator(schedule.getOperator());
        inspectRecord.setModeName(schedule.getJobName());

        inspectRecord.setAssetId("--");
        inspectRecord.setAssetName("--");
        inspectRecord.setAssetIp1("--");
        inspectRecord.setOrgId("--");
        inspectRecord.setOrgName("--");

        inspectRecordService.save(inspectRecord);
    }

    /**
     * 暂停周期巡检任务
     *
     * @param jobId
     */
    @Override
    public void pauseJob(String jobId) {
        List<XunjianSchedule> list = this.findByJobId(jobId);
        for (XunjianSchedule schedule : list) {
            if (schedule.getAutoFlag() != 2) {
                continue;
            }
            schedule.setJobState(3);
            this.updateById(schedule);

            try {
                String[] crons = schedule.getCron().split(",");
                for (String cron : crons) {
                    if (StringUtils.isEmpty(cron)) {
                        continue;
                    }
                    jobManager.deleteJob(schedule.getId() + "_" + cron, schedule.getOperator());
                    AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "暂停周期巡检任务，jobId：" + schedule.getId(), cron);
                }
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "暂停周期巡检任务异常", e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "暂停周期巡检任务异常");
            }
        }
    }

    @Override
    public void recoverJob(String jobId) {
        List<XunjianSchedule> list = this.findByJobId(jobId);
        for (XunjianSchedule schedule : list) {
            if (schedule.getAutoFlag() != 2) {
                continue;
            }
            schedule.setJobState(1);
            this.updateById(schedule);

            try {
                String[] crons = schedule.getCron().split(",");
                for (String cron : crons) {
                    if (StringUtils.isEmpty(cron)) {
                        continue;
                    }
                    jobManager.addJob(schedule.getId() + "_" + cron, schedule.getOperator(), cron, XunjianJob.class);
                    AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "恢复周期巡检任务，jobId：" + schedule.getId(), cron);
                }
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "恢复周期巡检任务异常，jobId：" + schedule.getId(), e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "恢复周期巡检任务异常");
            }
        }
    }

    /**
     * 资产组织树
     *
     * @return List
     */
    @Override
    public List<ItemVo> getOrgModeAssetList() {
        List<SysOrg> orgs = ShiroUtil.getSubjectOrgs();
        List<ItemVo> resultList = new ArrayList<>();
        for (SysOrg org : orgs) {
            Integer type = org.getType();
            if (type == OrgTypeConst.GROUP) {
                continue;
            }

            if (type == OrgTypeConst.CENTER) {
                ItemVo vo1 = new ItemVo();
                vo1.setId(org.getId());
                vo1.setName(org.getTitle());

                List<ItemVo> modelist = new ArrayList<>();
                List<StatisticsAlarmVo> mlist = assetService.getModeAsset(Collections.singletonList(org.getId()));
                getModeAssetList(resultList, org, vo1, modelist, mlist);
            }

            if (type == OrgTypeConst.LINE) {
                ItemVo vo1 = new ItemVo();
                vo1.setId(org.getId());
                vo1.setName(org.getTitle());

                List<ItemVo> modelist = new ArrayList<>();
                List<String> stationIds = sysOrgService.getStationOrgIdByLineId(org.getId());
                stationIds.retainAll(ShiroUtil.getSubjectOrgIds());

                List<StatisticsAlarmVo> mlist = assetService.getModeAsset(stationIds);
                getModeAssetList(resultList, org, vo1, modelist, mlist);
            }
        }
        return resultList;
    }

    @Override
    public void removeSchedule(String jobId) {
        List<XunjianSchedule> jobList = this.findByJobId(jobId);
        for (XunjianSchedule schedule : jobList) {
            Integer autoFlag = schedule.getAutoFlag();
            // 删除定时任务
            if (autoFlag == 2) {
                String[] crons = schedule.getCron().split(",");
                for (String cron : crons) {
                    if (StringUtils.isEmpty(cron)) {
                        continue;
                    }
                    try {
                        jobManager.deleteJob(schedule.getId() + "_" + cron, schedule.getOperator());
                    } catch (SchedulerException e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "删除周期巡检任务异常，jobId：" + schedule.getId(), e);
                        throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "删除周期巡检任务异常");
                    }
                }
            }
            // 删除巡检资产
            inspectAssetService.removeByJobId(schedule.getJobId());

            QueryWrapper<InspectRecord> query1 = Wrappers.query();
            query1.eq("SCHEDULE_ID", schedule.getId());
            List<InspectRecord> list = inspectRecordService.list(query1);
            for (InspectRecord record : list) {
                // 删除巡检记录
                inspectRecordService.removeById(record.getId());
                // 删除巡检明细
                QueryWrapper<InspectDetail> query2 = Wrappers.query();
                query2.eq("INSPECT_CODE", record.getId());
                inspectDetailService.remove(query2);
            }

            // 删除任务
            this.removeById(schedule.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchedule(XunjianJobDto dto) {
        Integer autoFlag = dto.getAutoFlag();
        String cronTimes = dto.getCronTimes();
        if (autoFlag == 2 && StringUtils.isEmpty(cronTimes)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        List<XunjianSchedule> oldList = this.findByJobId(dto.getJobId());
        XunjianSchedule oldSchedule = oldList.get(0);
        if (oldSchedule.getAutoFlag().intValue() != dto.getAutoFlag()) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "任务类型不能修改");
        }

        String jobName = dto.getJobName();
        String operator = dto.getOperator();
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("job_name", jobName);
        query.eq("operator", operator);
        query.ne("JOB_ID", dto.getJobId());
        int count = this.count(query);
        if (count > 0) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "任务名称不能重复");
        }

        // 手动巡检
        if (autoFlag == 1) {
            XunjianSchedule schedule = this.setJob(dto);
            schedule.setId(oldSchedule.getId());
            this.updateById(schedule);

            // 修改资产和指标
            inspectAssetService.removeByJobId(dto.getJobId());
            this.saveInspectAsset(dto);

            // 是否立即执行
            if (dto.getStartNow() == 2) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianExecutor);
                executor.execute(() -> {
                    dto.setId(schedule.getId());
                    this.beginXunjian(dto);
                });
            }
            return;
        }
        // 周期巡检
        XunjianSchedule schedule = this.setJob(dto);
        schedule.setId(oldSchedule.getId());
        String[] crons = oldSchedule.getCron().split(",");
        for (String cron : crons) {
            if (StringUtils.isEmpty(cron)) {
                continue;
            }
            try {
                // 删除原周期任务
                jobManager.deleteJob(schedule.getId() + "_" + cron, schedule.getOperator());
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "更新巡检任务异常，jobId：" + schedule.getId(), e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "更新巡检任务异常");
            }
        }

        String[] tims = cronTimes.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(tims));
        StringBuilder cronSB = new StringBuilder();
        for (String time : set) {
            String cron = TimeToCronConverter.convertToCron(time);
            cronSB.append(cron).append(",");
            try {
                // 添加到周期任务
                jobManager.addJob(schedule.getId() + "_" + cron, schedule.getOperator(), cron, XunjianJob.class);
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "更新巡检任务异常，jobId：" + schedule.getId(), e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "更新巡检任务异常");
            }
        }

        schedule.setStartNow(1);
        schedule.setCronTime(cronTimes);
        schedule.setCron(cronSB.toString());
        this.updateById(schedule);

        // 修改资产和指标
        inspectAssetService.removeByJobId(dto.getJobId());
        this.saveInspectAsset(dto);
    }

    @Override
    public List<XunjianSchedule> findByJobId(String jobId) {
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("JOB_ID", jobId);
        return this.list(query);
    }

    private void getModeAssetList(List<ItemVo> resultList, SysOrg org, ItemVo vo1,
                                  List<ItemVo> modelist, List<StatisticsAlarmVo> mlist) {
        QueryWrapper<Asset> query;
        for (StatisticsAlarmVo mode : mlist) {
            if ((mode.getId()).startsWith("7")) {
                continue;
            }
            ItemVo vo2 = new ItemVo();
            vo2.setId(mode.getId());
            vo2.setName(mode.getName());

            List<ItemVo> assetlist = new ArrayList<>();
            query = Wrappers.query();
            query.select("id", "name", "desk");
            query.eq("DESK", mode.getId());
            query.eq("ORG_ID", org.getId());
            query.eq("WATCH", 1);
            query.eq("IS_DEL", 1);
            query.orderByAsc("id", "name");
            List<Asset> list = assetService.list(query);
            for (Asset asset : list) {
                ItemVo vo3 = new ItemVo();
                vo3.setId(asset.getId());
                vo3.setName(asset.getName());
                vo3.setAssetDesk(asset.getDesk() + "");
                vo3.setFlag(true);
                assetlist.add(vo3);
            }
            vo2.setChildren(assetlist);
            modelist.add(vo2);
        }
        vo1.setChildren(modelist);
        resultList.add(vo1);
    }


    private XunjianSchedule setJob(XunjianJobDto dto) {
        XunjianSchedule schedule = new XunjianSchedule();
        schedule.setJobId(dto.getJobId());
        schedule.setJobName(dto.getJobName());
        schedule.setOperator(dto.getOperator());
        schedule.setJobState(1);
        schedule.setAutoFlag(dto.getAutoFlag());
        schedule.setStartNow(dto.getStartNow());
        schedule.setRemark(dto.getRemark());
        return schedule;
    }
}
