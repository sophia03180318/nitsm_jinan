package com.jcca.web2.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.common.bean.constant.AssetModeConst;
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
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.component.quartz.inspect.XunjianJob;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmInfoService;
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
import com.jcca.web2.dto.xunjian.XunjianDataDto;
import com.jcca.web2.dto.xunjian.XunjianJobDto;
import com.jcca.web2.dto.xunjian.XunjianTask;
import com.jcca.web2.dto.xunjian.XunjianWSDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.*;
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

import static com.jcca.web2.constant.Web2Const.*;
import static com.jcca.web2.service.XunjianCollectRun.*;

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
    private InspectAssetService inspectAssetService;
    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private InspectDetailService inspectDetailService;
    @Resource
    private ThresholdManager thresholdManager;
    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private AlarmInfoService alarmInfoService;


    /**
     * 保存巡检任务
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addSchedule(XunjianJobDto dto) {

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
            String inspectRecordId = MyIdUtil.getId(); // 巡检记录ID
            schedule.setInspectRecordId(inspectRecordId);
            if (dto.getStartNow() == 2) {
                // 巡检前让采集器推送一次进程状态数据
                try {
                    collectAgent.sendPostToCenter(XUNJIAN_PROCESS_URI, "", XUNJIAN_TIME_OUT);
                } catch (CollectAgencyException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", dto.getJobId());
                    throw new ResultException(ResultEnum.INSPECT_COLLECT_ERROR, "向采集器获取状态数据异常");
                }

                // 将任务设置为正在巡检
                schedule.setJobState(Integer.parseInt(Web2Const.INSPECTING));
                this.updateById(schedule);
                // 将指标设置为最初状态
                List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
                for (InspectAsset asset : assetList) {
                    asset.setInspectState(Web2Const.INSPECT);
                }
                inspectAssetService.updateBatchById(assetList);

                Web2Const.XUNJIAN_JOB_RECORD.put(jobId, inspectRecordId);

                ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.XUNJIAN_FIANL);
                dto.setId(schedule.getId());
                dto.setAutoFlag(1);
                dto.setInspectRecordId(inspectRecordId);
                executor.execute(new XunjianTask(schedule.getJobId(), this, dto));
            }
            return jobId;
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
        return jobId;
    }


    // 巡检资产
    private void saveInspectAsset(XunjianJobDto dto) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "开始保存巡检资产", DateUtil.formatDateTime(new Date()));

        List<AlarmEventType> typeList = alarmEventTypeService.list();
        Map<String, List<AlarmEventType>> typeMap = typeList.stream().collect(Collectors.groupingBy(AlarmEventType::getId));

        List<AlarmRepository> repositorList = alarmRepositoryService.list();
        Map<String, List<AlarmRepository>> repositorMap = repositorList.stream().collect(Collectors.groupingBy(AlarmRepository::getEventTypeId));

        List<String> list1 = Arrays.asList(SYSPORT_TARGET_ARR);

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
            List<ThresholdProcess> thresholdProcessList = thresholdProcessService.selectByAssetList(Collections.singletonList(asset.getAssetId()));
            for (String eventTypeId : targetList) {
                Set<String> set = new HashSet<>();
                AlarmEventType alarmEventType = typeMap.get(eventTypeId).get(0);
                List<AlarmRepository> repositoryList = repositorMap.get(eventTypeId);
                for (AlarmRepository repository : repositoryList) {
                    if (set.contains(repository.getAlarmCode())) {
                        continue;
                    }
                    set.add(repository.getAlarmCode());
                    if (StatusInfoChangeTypeEnum.event_ping_group_all.getCode().equals(repository.getAlarmCode())
                            || StatusInfoChangeTypeEnum.event_ping_group_other.getCode().equals(repository.getAlarmCode())) {
                        continue;
                    }
                    if (StatusInfoChangeTypeEnum.event_clock_state.getCode().equals(repository.getAlarmCode())) {
                        continue;
                    }
                    if (StatusInfoChangeTypeEnum.event_cpu_state.getCode().equals(repository.getAlarmCode())) {
                        continue;
                    }
                    if (asset.getNtpFlag() == 0
                            && repository.getAlarmCode().startsWith(StatusInfoChangeTypeEnum.event_time.getCode())) {
                        continue;
                    }
                    if (AssetModeConst.SERVER.intValue() == asset.getAssetDesk() && list1.contains(repository.getAlarmCode())) {
                        boolean hasSysport = this.getSysport(asset.getAssetId());
                        if (!hasSysport) {
                            continue;
                        }
                    }

                    InspectAsset inspectAsset = new InspectAsset();
                    BeanUtils.copyProperties(asset, inspectAsset);
                    inspectAsset.setId(MyIdUtil.getId());
                    inspectAsset.setJobId(jobId);
                    inspectAsset.setTargetItem(repository.getAlarmCode());
                    inspectAsset.setTargetName(repository.getDescStr());
                    inspectAsset.setInspectState(Web2Const.INSPECT);
                    inspectAsset.setInspectType(dto.getAutoFlag());
                    inspectAsset.setEventTypeId(eventTypeId);
                    inspectAsset.setEventTypeName(alarmEventType.getTypeAlias());
                    inspectAsset.setRemark(repository.getPlanStr());
                    String thresholdValue = this.getThreshold(inspectAsset, thresholdProcessList);
                    if (repository.getDescStr().contains("阈值") && StringUtils.isEmpty(thresholdValue)) {
                        continue;
                    }
                    inspectAsset.setThresholdValue(thresholdValue);
                    if (repository.getAlarmCode().startsWith(StatusInfoChangeTypeEnum.event_process.getCode())
                            && thresholdProcessList.isEmpty()) {
                        continue;
                    }

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
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "结束保存巡检资产", DateUtil.formatDateTime(new Date()));
    }

    private boolean getSysport(String assetId) {
        Asset asset = assetService.getById(assetId);
        return !StringUtils.isEmpty(asset.getIpmiIp()) && !StringUtils.isEmpty(asset.getIpmiUser()) && !StringUtils.isEmpty(asset.getIpmiPwd());
    }

    private String getThreshold(InspectAsset inspectAsset, List<ThresholdProcess> thresholdProcessList) {
        ThresholdBaseEntity entity = null;
        String assetDesk = inspectAsset.getAssetDesk() + "";
        if (assetDesk.contains("183")) {
            for (ThresholdProcess thresholdProcess : thresholdProcessList) {
                entity = thresholdManager.xunjianGetThresholdValue(inspectAsset.getTargetItem(), inspectAsset.getAssetId(), thresholdProcess.getProcessName());
                break;
            }
        }
        if (entity == null) {
            entity = thresholdManager.xunjianGetThresholdValue(inspectAsset.getTargetItem(), inspectAsset.getAssetId(), "");
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
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "启动巡检任务异常", e);
            throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "启动巡检任务异常");
        }
    }

    @Resource
    private CollectAgent collectAgent;

    /**
     * 真正巡检开始
     *
     * @param dto
     */
    @Override
    public void beginXunjian(XunjianJobDto dto) {
        String id = dto.getId();
        XunjianSchedule schedule = this.getById(id);
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
        if (dto.getAutoFlag() == 2) {
            // 巡检前让采集器推送一次进程状态数据
            try {
                collectAgent.sendPostToCenter(XUNJIAN_PROCESS_URI, "", XUNJIAN_TIME_OUT);
            } catch (CollectAgencyException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", dto);
                return;
            }

            // 将任务设置为正在巡检
            schedule.setJobState(Integer.parseInt(Web2Const.INSPECTING));
            schedule.setLastTime(new Date());
            this.updateById(schedule);

            // 将指标设置为最初状态
            for (InspectAsset asset : assetList) {
                asset.setInspectState(Web2Const.INSPECT);
            }
            inspectAssetService.updateBatchById(assetList);

            Web2Const.XUNJIAN_JOB_RECORD.put(schedule.getJobId(), dto.getInspectRecordId());
        }

        // 保存巡检记录
        schedule.setInspectRecordId(dto.getInspectRecordId());
        schedule.setLastTime(new Date());
        this.saveInspectRecord(schedule);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "保存巡检记录", "任务名称：" + schedule.getJobName() + ",记录ID：" + dto.getInspectRecordId());

        Set<String> assetIdSet = new HashSet<>();
        Set<String> collect = assetList.stream().map(InspectAsset::getAssetId).collect(Collectors.toSet());
        int totalTarget = collect.size();

        try {
            // 开始巡检采集
            for (InspectAsset inspectAsset : assetList) {
                if (assetIdSet.contains(inspectAsset.getAssetId())) {
                    continue;
                }
                assetIdSet.add(inspectAsset.getAssetId());

                String flag = XUNJIAN_JOB_RECORD.get(schedule.getJobId());
                if (flag == null) {
                    return;
                }
                currentAssetIdMap.put(schedule.getInspectRecordId(), inspectAsset.getAssetId()); // 当前巡检资产
                this.sendMsg(inspectAsset.getCreator(), XunjianWSDto.XUNJIANING_ASSET, schedule.getJobId(), inspectAsset.getAssetId(), inspectAsset.getAssetName(), 2); // 当前巡检资产

                inspectAsset.setInspectRecordId(schedule.getInspectRecordId());
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "开始巡检资产 " + inspectAsset.getAssetName(), dto);
                inspectAssetService.xunjianCollect(inspectAsset);

                TimeUnit.SECONDS.sleep(2L);
                if (assetStateMap.get(schedule.getInspectRecordId()) != null
                        && assetStateMap.get(schedule.getInspectRecordId()).get(inspectAsset.getAssetId()) != null) {
                    this.sendMsg(inspectAsset.getCreator(), XunjianWSDto.ASSET_STATUS, schedule.getJobId(), inspectAsset.getAssetId(),
                            inspectAsset.getAssetName(), assetStateMap.get(schedule.getInspectRecordId()).get(inspectAsset.getAssetId())); // 资产状态
                }

                BigDecimal process = new BigDecimal(assetIdSet.size()).divide(new BigDecimal(totalTarget), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                if (process.intValue() < 100) {
                    currentProcessMap.put(schedule.getInspectRecordId(), process.intValue());
                    this.sendMsg(inspectAsset.getCreator(), XunjianWSDto.WHOLE_PROCESS, schedule.getJobId(), "100", "进度条", process.intValue());
                }
            }

            checkStatusTarget(schedule.getJobId(), schedule.getInspectRecordId());

            IEvent event = new IEvent();
            event.setXunjianIsFinish(1);
            event.setInspectRecordId(schedule.getInspectRecordId());
            Web2Const.XUNJIAN_COLLECT_QUEUE.put(event);
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "发送巡检结束标记，用户：" + schedule.getOperator()
                    + "，任务名称：" + schedule.getJobName(), schedule.getInspectRecordId());
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集执行中异常:" + e.getMessage(), dto);
        }
    }

    private void sendMsg(String operator, Integer msgType, String jobId, String id, String name, Integer status) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId(id);
        msg.setName(name);
        msg.setStatus(status);
        msg.setCount(0);
        wsDto.setMessage(msg);
        this.sendWsMsg(wsDto);
    }

    // 状态类单独处理
    private void checkStatusTarget(String jobId, String inspectRecordId) {
        QueryWrapper<InspectAsset> query1 = Wrappers.query();
        query1.eq("JOB_ID", jobId);
        query1.in("INSPECT_STATE", Arrays.asList("1", "2"));
        List<InspectAsset> list1 = inspectAssetService.list(query1);
        for (InspectAsset inspectAsset : list1) {
            List<String> list = Arrays.asList(ALARM_TARGET_ARR);
            if (!list.contains(inspectAsset.getTargetItem())) {
                continue;
            }
            inspectAsset.setInspectRecordId(inspectRecordId);

            QueryWrapper<AlarmInfo> query = Wrappers.query();
            query.eq("ASSET_ID", inspectAsset.getAssetId());
            query.eq("ALARM_CODE", inspectAsset.getTargetItem());
            query.eq("ALARM_STATE", 1);
            query.eq("BLANK", 1);
            List<AlarmInfo> infos = alarmInfoService.list(query);
            if (infos.isEmpty()) {
                inspectAsset.setInspectValue("1");
                inspectAsset.setInspectState(Web2Const.INSPECTED);
                inspectAsset.setResultMsg("正常");
                this.send2Queue(inspectAsset);
                continue;
            }
            for (AlarmInfo info : infos) {
                inspectAsset.setInspectValue(info.getDescription());
                inspectAsset.setInspectState(Web2Const.INSPECT_ERROR);
                inspectAsset.setResultMsg(info.getDescription());
                inspectAsset.setAlarmId(info.getId());
                this.send2Queue(inspectAsset);
            }
        }
    }

    private void send2Queue(InspectAsset asset) {
        XunjianDataDto dto = new XunjianDataDto();
        dto.setInspectRecordId(asset.getInspectRecordId());
        dto.setAssetId(asset.getAssetId());
        dto.setTargetItem(asset.getTargetItem());
        dto.setInspectValue(asset.getInspectValue());
        dto.setInspectState(asset.getInspectState());
        dto.setResultMsg(asset.getResultMsg());
        dto.setAlarmId(asset.getAlarmId());
        dto.setEventTypeId(asset.getEventTypeId());
        IEvent event = new IEvent();
        event.setInspectRecordId(asset.getInspectRecordId());
        event.setStatus(Web2Const.INSPECT_ERROR.equals(asset.getInspectState()) ? -1 : 1);
        event.setXunjianDataDto(dto);
        Web2Const.XUNJIAN_COLLECT_QUEUE.add(event);
    }

    private void saveInspectRecord(XunjianSchedule schedule) {
        InspectRecord inspectRecord = new InspectRecord();
        inspectRecord.setId(schedule.getInspectRecordId());
        inspectRecord.setScheduleId(schedule.getId());
        inspectRecord.setInspectCode(schedule.getJobId());
        inspectRecord.setInspectTime(new Date());
        inspectRecord.setCreator(schedule.getOperator());
        inspectRecord.setModeName(schedule.getJobName());
        inspectRecord.setModeType(schedule.getOperator());

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
                    AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "暂停周期巡检任务，jobId：" + schedule.getId(), cron);
                }
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "暂停周期巡检任务异常", e);
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
                    AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "恢复周期巡检任务，jobId：" + schedule.getId(), cron);
                }
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "恢复周期巡检任务异常，jobId：" + schedule.getId(), e);
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
            if (type == OrgTypeConst.GROUP || type == OrgTypeConst.STATION) {
                continue;
            }

            if (type == OrgTypeConst.CENTER) {
                ItemVo vo1 = new ItemVo();
                vo1.setId(org.getId());
                vo1.setName(org.getTitle());

                List<ItemVo> modelist = new ArrayList<>();
                List<StatisticsAlarmVo> mlist = assetService.getModeAsset(Collections.singletonList(org.getId()));
                getModeAssetList(resultList, Collections.singletonList(org.getId()), vo1, modelist, mlist);
            }

            // 车站暂时不做 20250606
//            if (type == OrgTypeConst.LINE) {
//                ItemVo vo1 = new ItemVo();
//                vo1.setId(org.getId());
//                vo1.setName(org.getTitle());
//
//                List<ItemVo> modelist = new ArrayList<>();
//                List<String> stationIds = sysOrgService.getStationOrgIdByLineId(org.getId());
//                stationIds.retainAll(ShiroUtil.getSubjectOrgIds());
//
//                List<StatisticsAlarmVo> mlist = assetService.getModeAsset(stationIds);
//                getModeAssetList(resultList, stationIds, vo1, modelist, mlist);
//            }
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
                        AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "删除周期巡检任务异常，jobId：" + schedule.getId(), e);
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
            String inspectRecordId = MyIdUtil.getId(); // 巡检记录ID
            if (dto.getStartNow() == 2) {
                // 巡检前让采集器推送一次进程状态数据
                try {
                    collectAgent.sendPostToCenter(XUNJIAN_PROCESS_URI, "", XUNJIAN_TIME_OUT);
                } catch (CollectAgencyException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", dto.getJobId());
                    throw new ResultException(ResultEnum.INSPECT_COLLECT_ERROR, "向采集器获取状态数据异常");
                }

                // 将任务设置为正在巡检
                schedule.setJobState(Integer.parseInt(Web2Const.INSPECTING));
                this.updateById(schedule);
                // 将指标设置为最初状态
                List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
                for (InspectAsset asset : assetList) {
                    asset.setInspectState(Web2Const.INSPECT);
                }
                inspectAssetService.updateBatchById(assetList);

                Web2Const.XUNJIAN_JOB_RECORD.put(dto.getJobId(), inspectRecordId);

                ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.XUNJIAN_FIANL);
                dto.setId(schedule.getId());
                dto.setAutoFlag(1);
                dto.setInspectRecordId(inspectRecordId);
                executor.execute(new XunjianTask(schedule.getJobId(), this, dto));
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
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "更新巡检任务异常，jobId：" + schedule.getId(), e);
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
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "更新巡检任务异常，jobId：" + schedule.getId(), e);
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

    @Override
    public void resetJob(XunjianSchedule schedule) {

        // 将任务设置为最初状态
        schedule.setJobState(Integer.parseInt(Web2Const.INSPECT));
        this.updateById(schedule);

        // 将指标设置为最初状态
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
        for (InspectAsset asset : assetList) {
            asset.setInspectState(Web2Const.INSPECT);
        }
        inspectAssetService.updateBatchById(assetList);

        String inspectRecordId = Web2Const.XUNJIAN_JOB_RECORD.get(schedule.getJobId());
        if (StringUtils.isEmpty(inspectRecordId)) {
            return;
        }
        // 删除巡检记录
        inspectRecordService.removeById(inspectRecordId);
        // 删除巡检详情
        UpdateWrapper<InspectDetail> update = Wrappers.update();
        update.eq("INSPECT_CODE", inspectRecordId);
        inspectDetailService.remove(update);

        // 清空缓存
        XUNJIAN_JOB_RECORD.remove(schedule.getJobId());
        XunjianCollectRun collectRun = SpringContextUtil.getBean(XunjianCollectRun.class);
        collectRun.clearMap(inspectRecordId);
    }

    @Override
    public void sendWsMsg(XunjianWSDto wsDto) {
        String operator = wsDto.getUsername();
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null || !webSocketSession.isOpen()) {
//            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "用户WEBSOCKET连接失效", wsDto);
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(wsDto)));
            XunjianWSDto message = wsDto.getMessage();
            if ("100".equals(message.getId()) && message.getStatus() == 100) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集给前端发送消息", wsDto);
            }
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集给前端发送消息异常", wsDto);
        }
    }

    private void getModeAssetList(List<ItemVo> resultList, List<String> orgIds, ItemVo vo1,
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
            query.in("ORG_ID", orgIds);
            query.eq("WATCH", 1);
            query.eq("IS_DEL", 1);
            query.eq("MONITOR", 1);
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
