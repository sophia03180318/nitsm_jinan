package com.jcca.web2.service.impl;

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
import com.jcca.component.quartz.inspect.XunjianJob;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.XunjianScheduleDao;
import com.jcca.web2.dto.XunjianJobDto;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

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


    /**
     * 保存巡检任务
     *
     * @param dto
     */
    @Override
    public void addSchedule(XunjianJobDto dto) {

        Integer autoFlag = dto.getAutoFlag();
        String cronTimes = dto.getCronTimes();
        if (autoFlag == 2 && StringUtils.isEmpty(cronTimes)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        String jobId = MyIdUtil.getId();
        dto.setJobId(jobId);
        if (autoFlag == 1) {
            XunjianSchedule schedule = this.setJob(dto);
            this.save(schedule);

            // 保存资产和指标
            this.saveInspectAsset(dto);

            // 是否立即执行
            if (dto.getStartNow() == 2) {
                this.beginXunjian(dto);
            }
            return;
        }

        List<XunjianSchedule> list = new ArrayList<>();
        String[] tims = cronTimes.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(tims));
        for (String time : set) {
            XunjianSchedule schedule = this.setJob(dto);
            schedule.setCronTime(time);
            schedule.setCron(TimeToCronConverter.convertToCron(time));
            list.add(schedule);

            // 添加到周期任务
            try {
                jobManager.addJob(schedule.getJobId() + "_" + schedule.getCronTime(),
                        schedule.getOperator(), schedule.getCron(), XunjianJob.class);
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "添加巡检任务异常，jobId：" + jobId, e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "添加巡检任务异常");
            }
        }
        this.saveBatch(list);

        // 保存资产和指标
        this.saveInspectAsset(dto);
    }


    // 巡检资产 inspect_asset job_id == xunjian_schedule_job_id
    private void saveInspectAsset(XunjianJobDto dto) {
        Set<String> set = new HashSet<>();
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
                List<AlarmRepository> repositoryList = alarmRepositoryService.getAllByEventId(target);
                for (AlarmRepository repository : repositoryList) {
                    if (set.contains(repository.getAlarmCode())) {
                        continue;
                    }
                    set.add(repository.getAlarmCode());

                    asset.setId(MyIdUtil.getId());
                    asset.setJobId(jobId);
                    asset.setTargetItem(repository.getAlarmCode());
                    asset.setTargetName(repository.getDescStr());
                    asset.setInspectState(Web2Const.INSPECT);
                    asset.setInspectType(dto.getAutoFlag());
                    asset.setTargetId(target);
                    // 获取阈值设定 TODO
//                    asset.setThresholdValue();

                    batchList.add(asset);
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
                String jobName = schedule.getJobName() + "_" + schedule.getCronTime();
                jobManager.addJob(jobName, schedule.getOperator(), schedule.getCron(), XunjianJob.class);
                AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "启动巡检任务", jobName);
            }
        } catch (SchedulerException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "启动巡检任务异常", e);
        }
    }

    /**
     * 真正巡检开始
     *
     * @param dto
     */
    @Override
    public void beginXunjian(XunjianJobDto dto) {
        // TODO 巡检设备
        String jobId = dto.getJobId();
        String operator = dto.getOperator();

        // 巡检资产 inspect_asset job_id == xunjian_scheduled_job_id

        // 巡检记录 inspect_record scheduled_id == xunjian_scheduled_id

        // 巡检明细 inspect_detail inspect_code == inspect_record_id

    }

    /**
     * 暂停周期巡检任务
     *
     * @param id
     */
    @Override
    public void pauseJob(String id) {
        XunjianSchedule schedule = this.getById(id);
        schedule.setJobState(3);
        this.updateById(schedule);

        try {
            jobManager.deleteJob(schedule.getJobId() + "_" + schedule.getCronTime(), schedule.getOperator());
        } catch (SchedulerException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "暂停周期巡检任务异常", e);
        }
    }

    @Override
    public void recoverJob(String id) {
        XunjianSchedule schedule = this.getById(id);
        schedule.setJobState(1);
        this.updateById(schedule);

        try {
            jobManager.addJob(schedule.getJobId() + "_" + schedule.getCronTime(),
                    schedule.getOperator(), schedule.getCron(), XunjianJob.class);
        } catch (SchedulerException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "恢复周期巡检任务异常，jobId：" + schedule.getJobId(), e);
            throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "恢复周期巡检任务异常");
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
    public void removeSchedule(String id) {
        XunjianSchedule schedule = this.getById(id);
        Integer autoFlag = schedule.getAutoFlag();
        // 删除定时任务
        if (autoFlag == 2) {
            try {
                jobManager.deleteJob(schedule.getJobId() + "_" + schedule.getCronTime(), schedule.getOperator());
            } catch (SchedulerException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "删除周期巡检任务异常，jobId：" + schedule.getJobId(), e);
                throw new ResultException(ResultEnum.INSPECT_SCHEDULE_ERROR, "删除周期巡检任务异常");
            }
        }
        // 巡检资产 inspect_asset job_id == xunjian_schedule_job_id
        // 巡检记录 inspect_record schedule_id == xunjian_schedule_id
        // 巡检明细 inspect_detail inspect_code == inspect_record_id
        // 删除巡检资产
        QueryWrapper<XunjianSchedule> query = Wrappers.query();
        query.eq("JOB_ID", schedule.getJobId());
        int count = this.count(query);
        if (count == 1) {
            QueryWrapper<InspectAsset> query1 = Wrappers.query();
            query1.eq("JOB_ID", schedule.getJobId());
            inspectAssetService.remove(query1);
        }

        QueryWrapper<InspectRecord> query1 = Wrappers.query();
        query1.eq("SCHEDULE_ID", schedule.getId());
        List<InspectRecord> list = inspectRecordService.list(query1);
        for (InspectRecord record : list) {
            // 删除巡检明细
            QueryWrapper<InspectDetail> query2 = Wrappers.query();
            query2.eq("INSPECT_CODE", record.getId());
            inspectDetailService.remove(query2);

            // 删除巡检记录
            inspectRecordService.removeById(record.getId());
        }

        // 删除任务
        this.removeById(id);
    }

    @Override
    public void updateSchedule(XunjianJobDto dto) {
        this.removeSchedule(dto.getId());
        this.addSchedule(dto);
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
        schedule.setId(MyIdUtil.getId());
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
