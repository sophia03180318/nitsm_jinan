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
import com.jcca.web2.constant.XunJianConst;
import com.jcca.web2.dao.XunjianScheduleDao;
import com.jcca.web2.dto.xunjian.*;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.enums.xunjian.InspectionMode;
import com.jcca.web2.enums.xunjian.InspectionStatus;
import com.jcca.web2.service.*;
import com.jcca.web2.service.xunjian.InspectSessionManager;
import com.jcca.web2.service.xunjian.XunJianEventHandler;
import com.jcca.web2.service.xunjian.XunjianNotifier;
import com.jcca.web2.util.TimeToCronConverter;
import com.jcca.web2.vo.ItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.jcca.web2.constant.Web2Const.*;

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

    @Resource
    private XunjianNotifier notifier;

    @Resource
    private InspectSessionManager sessionManager;

    @Resource
    private XunJianEventHandler eventHandler;

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
                    collectAgent.sendPostToCenter(XunJianConst.XUNJIAN_PROCESS_URI, "", XunJianConst.XUNJIAN_TIME_OUT);
                } catch (CollectAgencyException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", dto.getJobId());
                    throw new ResultException(ResultEnum.INSPECT_COLLECT_ERROR, "向采集器获取状态数据异常");
                }

                // 将任务设置为正在巡检
                schedule.setJobState(Integer.parseInt(InspectionStatus.INSPECTING.getCode()));
                this.updateById(schedule);
                // 将指标设置为最初状态
                List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
                for (InspectAsset asset : assetList) {
                    asset.setInspectState(InspectionStatus.INSPECT.getCode());
                }
                inspectAssetService.updateBatchById(assetList);

                XunJianConst.XUNJIAN_JOB_RECORD.put(jobId, inspectRecordId);

                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianAsync);
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
            jobManager.addJob(schedule.getId() + "_" + cron.replace(" ", "-"), schedule.getOperator(), cron, XunjianJob.class);
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
                    inspectAsset.setInspectState(InspectionStatus.INSPECT.getCode());
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
        String targetItem = inspectAsset.getTargetItem();
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

        if (targetItem.contains("sectionOne")) {
            return entity.getOneLevelValue() + "";
        } else if (targetItem.contains("sectionTwo")) {
            return entity.getTwoLevelValue() + "";
        } else if (targetItem.contains("sectionThree")) {
            return entity.getThreeLevelValue() + "";
        } else if (targetItem.contains("section")) {
            return entity.getMinValue() + "_" + entity.getMaxValue();
        }

        if (!entity.baseValueIsNull()) {
            return entity.getBaseValue() + "";
        }
        if (!entity.sectionValueIsNull()) {
            return entity.getMinValue() + "-" + entity.getMaxValue();
        }
//        if (!entity.oneLevelIsNull() || !entity.twoLevelIsNull() || !entity.threeLevelIsNull()) {
//            String level = "one-two-three";
//            if (!entity.oneLevelIsNull()) {
//                level = level.replace("one", entity.getOneLevelValue() + "");
//            }
//            if (!entity.twoLevelIsNull()) {
//                level = level.replace("two", entity.getTwoLevelValue() + "");
//            }
//            if (!entity.threeLevelIsNull()) {
//                level = level.replace("three", entity.getThreeLevelValue() + "");
//            }
//            return level;
//        }
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
        for (XunjianSchedule schedule : list) {
            String[] crons = schedule.getCron().split(",");
            for (String cron : crons) {
                if (StringUtils.isEmpty(cron)) {
                    continue;
                }
                String jobName = schedule.getId() + "_" + cron.replace(" ", "-");
                jobManager.addJob(jobName, schedule.getOperator(), cron, XunjianJob.class);
            }
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
        if (schedule == null) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "开始巡检", "基础数据缺失，退出巡检");
            return;
        }

        String jobId = schedule.getJobId();
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(jobId);
        String inspectRecordId = dto.getInspectRecordId();

        try {
            if (Objects.equals(dto.getAutoFlag(), InspectionMode.SCHEDULED.getCode())) {
                // 巡检前让采集器推送一次进程状态数据
                try {
                    collectAgent.sendPostToCenter(XunJianConst.XUNJIAN_PROCESS_URI, "", XunJianConst.XUNJIAN_TIME_OUT);
                } catch (CollectAgencyException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", dto);
                    return;
                }

                // 将任务设置为正在巡检(定时巡检)
                schedule.setJobState(Integer.parseInt(InspectionStatus.INSPECTING.getCode()));
                schedule.setLastTime(new Date());
                this.updateById(schedule);

                // 将指标设置为最初状态
                for (InspectAsset asset : assetList) {
                    asset.setInspectState(InspectionStatus.INSPECT.getCode());
                }
                inspectAssetService.updateBatchById(assetList);

                //记录任务状态(定时巡检)
                XunJianConst.XUNJIAN_JOB_RECORD.put(jobId, inspectRecordId);
            }

            // 保存巡检记录
            schedule.setInspectRecordId(inspectRecordId);
            schedule.setLastTime(new Date());
            this.saveInspectRecord(schedule);
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "保存巡检记录", "任务名称：" + schedule.getJobName() + ",记录ID：" + inspectRecordId);

            // 创建巡检 session
            sessionManager.getOrCreateSession(inspectRecordId, () -> new InspectSession(inspectRecordId, schedule, assetList));
            InspectSession session = sessionManager.getSession(inspectRecordId);

            // 初始化队列
            XunJianConst.initXunJianCollectQueue(inspectRecordId);

            Set<String> assetIdSet = new HashSet<>();
            // 开始巡检(设备)采集
            for (InspectAsset inspectAsset : assetList) {
                String assetId = inspectAsset.getAssetId();
                if (!assetIdSet.add(assetId)) {
                    continue;
                }

                // 检查任务是否已被取消
                if (XunJianConst.XUNJIAN_JOB_RECORD.get(jobId) == null) {
                    AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "巡检任务被取消", jobId);
                    return;
                }

                // 记录/更新当前巡检资产
                XunJianConst.currentAssetIdMap.put(inspectRecordId, assetId);
                String assetName = inspectAsset.getAssetName();
                session.updateLatestAsset(assetId, assetName, Integer.parseInt(InspectionStatus.INSPECTING.getCode()));

                // 如果任务已被停止，那么有可能上一次采集任务有遗留脏数据未处理完，需过滤
                if (!XunJianConst.hasXunJianCollectQueue(inspectRecordId)) {
                    log.info("当前任务<{}>已停止运行，属于上一次任务脏数据", inspectRecordId);
                    return;
                }

                // 推送设备采集中
                InspectBaseDataWsVo snapshot = session.buildSnapshot(session.getSchedule().getJobId(), false, true);
                notifier.sendSnapshot(snapshot, session.getSchedule().getOperator());

                // 开始采集
                inspectAsset.setInspectRecordId(inspectRecordId);
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "开始巡检资产 " + inspectAsset.getAssetName(), dto);
                inspectAssetService.xunjianCollect(inspectAsset);

                Thread.sleep(1000);// 等待时间，保障 xunjianCollect 方法中得指标都存入到队列-非必须

                // 发送当前设备结束标识
                IEvent info = new IEvent();
                info.setStatus(XunJianConst.FINISH_FLAG);
                info.setAssetId(assetId);
                info.setInspectRecordId(inspectRecordId);

                // 入队列即开始消费队列 ，否则是上一次执行任务脏数据
                if (XunJianConst.putXunJianCollectQueue(inspectRecordId, info)) {
                    // 同步消费队列数据
                    consumeLoop(inspectRecordId);
                }
            }

            // 整个job任务巡检结束
            inspectRecordService.lambdaUpdate()
                    .eq(InspectRecord::getId, inspectRecordId)
                    .set(InspectRecord::getInspectState, InspectionStatus.INSPECTED.getCode())
                    .update();

            schedule.setJobState(Integer.parseInt(InspectionStatus.INSPECT.getCode()));
            schedule.setLastTime(new Date());
            this.updateById(schedule);

            XunJianConst.INSPECT_THREAD_MAP.remove(schedule.getJobId());
            XunJianConst.XUNJIAN_JOB_RECORD.remove(schedule.getJobId());
            sessionManager.removeSession(inspectRecordId);

            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "发送巡检结束标记，用户：" + schedule.getOperator()
                    + "，任务名称：" + schedule.getJobName(), schedule.getInspectRecordId());
        } catch (Exception e) {
            e.printStackTrace();
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集执行中异常:" + e.getMessage(), dto);
            // 异常清理
            sessionManager.removeSession(inspectRecordId);
        }
    }

    /**
     * 同步处理队列数据
     *
     * @param inspectRecordId
     */
    private void consumeLoop(String inspectRecordId) {
        BlockingQueue<IEvent> queue = XunJianConst.XUNJIAN_COLLECT_QUEUE.get(inspectRecordId);
        if (queue == null) return;

        IEvent event;
        while ((event = queue.poll()) != null) {
            try {
                boolean isFinish = eventHandler.handleEvent(event);
                log.info("资产：{} 响应值：{}", event.getAssetId(), isFinish);

//                if (isFinish) {
//                    // 结束标志
//                    break;
//                }
            } catch (Exception e) {
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检事件处理异常", e.getMessage());
            }
        }
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
        // 设置当前报告状态正在巡检中
        inspectRecord.setInspectState(InspectionStatus.INSPECTING.getCode());

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

            String[] crons = schedule.getCron().split(",");
            for (String cron : crons) {
                if (StringUtils.isEmpty(cron)) {
                    continue;
                }
                jobManager.deleteJob(schedule.getId() + "_" + cron.replace(" ", "-"), schedule.getOperator());
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "暂停周期巡检任务，jobId：" + schedule.getId(), cron);
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

            String[] crons = schedule.getCron().split(",");
            for (String cron : crons) {
                if (StringUtils.isEmpty(cron)) {
                    continue;
                }
                jobManager.addJob(schedule.getId() + "_" + cron.replace(" ", "-"), schedule.getOperator(), cron, XunjianJob.class);
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "恢复周期巡检任务，jobId：" + schedule.getId(), cron);
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
                vo1.setFlag(OrgTypeConst.CENTER);
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
                    jobManager.deleteJob(schedule.getId() + "_" + cron.replace(" ", "-"), schedule.getOperator());
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
            String inspectRecordId = MyIdUtil.getId(); // 巡检记录ID
            if (dto.getStartNow() == 2) {
                // 巡检前让采集器推送一次进程状态数据
                try {
                    collectAgent.sendPostToCenter(XunJianConst.XUNJIAN_PROCESS_URI, "", XunJianConst.XUNJIAN_TIME_OUT);
                } catch (CollectAgencyException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集获取状态数据异常", dto.getJobId());
                    throw new ResultException(ResultEnum.INSPECT_COLLECT_ERROR, "向采集器获取状态数据异常");
                }

                // 将任务设置为正在巡检
                schedule.setJobState(Integer.parseInt(InspectionStatus.INSPECTING.getCode()));
                this.updateById(schedule);
                // 将指标设置为最初状态
                List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
                for (InspectAsset asset : assetList) {
                    asset.setInspectState(InspectionStatus.INSPECT.getCode());
                }
                inspectAssetService.updateBatchById(assetList);

                XunJianConst.XUNJIAN_JOB_RECORD.put(dto.getJobId(), inspectRecordId);

                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianAsync);
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
            // 删除原周期任务
            jobManager.deleteJob(schedule.getId() + "_" + cron.replace(" ", "-"), schedule.getOperator());
        }

        String[] tims = cronTimes.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(tims));
        StringBuilder cronSB = new StringBuilder();
        for (String time : set) {
            String cron = TimeToCronConverter.convertToCron(time);
            cronSB.append(cron).append(",");
            // 添加到周期任务
            jobManager.addJob(schedule.getId() + "_" + cron.replace(" ", "-"), schedule.getOperator(), cron, XunjianJob.class);
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
        schedule.setJobState(Integer.parseInt(InspectionStatus.INSPECT.getCode()));
        this.updateById(schedule);

        // 将指标设置为最初状态
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(schedule.getJobId());
        for (InspectAsset asset : assetList) {
            asset.setInspectState(InspectionStatus.INSPECT.getCode());
        }
        inspectAssetService.updateBatchById(assetList);

        String inspectRecordId = XunJianConst.XUNJIAN_JOB_RECORD.get(schedule.getJobId());
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
        XunJianConst.XUNJIAN_JOB_RECORD.remove(schedule.getJobId());
        XunJianConst.XUNJIAN_COLLECT_QUEUE.remove(inspectRecordId);

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
            vo2.setName(mode.getName());
            vo2.setFlag(OrgTypeConst.ASSET_TYPE);
            List<ItemVo> assetlist = new ArrayList<>();
            query = Wrappers.query();
            query.select("id", "name", "desk","org_id");
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
                // 用于智能巡检模板 适配组织机构父子关系
                vo3.setOrgId(asset.getOrgId());
                vo3.setFlag(OrgTypeConst.ASSET);
                assetlist.add(vo3);
            }
            vo2.setChildren(assetlist);

            // orgId,id 用于智能巡检模板 适配组织机构父子关系
            String orgId = mode.getOrgId();
            vo2.setOrgId(orgId);
            vo2.setId(mode.getId() + orgId);
            // 筛选掉没有监控设备的指标
            if (!list.isEmpty()) {
                modelist.add(vo2);
            }
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
