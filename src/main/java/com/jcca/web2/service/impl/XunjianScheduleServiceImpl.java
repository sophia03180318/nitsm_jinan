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
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.dao.XunjianScheduleDao;
import com.jcca.web2.dto.XunjianJobDto;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.XunjianScheduleService;
import com.jcca.web2.util.TimeToCronConverter;
import com.jcca.web2.vo.OrgModeAssetVo;
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
    private AssetService assetService;
    @Resource
    private SysOrgService sysOrgService;


    /**
     * 保存巡检任务
     *
     * @param dto
     */
    @Override
    public void saveSchedule(XunjianJobDto dto) {

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

    private void saveInspectAsset(XunjianJobDto dto) {
        String jobId = dto.getJobId();
        List<String> assetIds = dto.getAssetIds();
        List<String> targetIds = dto.getTargetIds();

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
    public List<OrgModeAssetVo> getOrgModeAssetList() {
        List<SysOrg> orgs = ShiroUtil.getSubjectOrgs();
        List<OrgModeAssetVo> resultList = new ArrayList<>();
        for (SysOrg org : orgs) {
            Integer type = org.getType();
            if (type == OrgTypeConst.GROUP) {
                continue;
            }

            if (type == OrgTypeConst.CENTER) {
                OrgModeAssetVo vo1 = new OrgModeAssetVo();
                vo1.setId(org.getId());
                vo1.setName(org.getTitle());

                List<OrgModeAssetVo> modelist = new ArrayList<>();
                List<StatisticsAlarmVo> mlist = assetService.getModeAsset(Collections.singletonList(org.getId()));
                getModeAssetList(resultList, org, vo1, modelist, mlist);
            }

            if (type == OrgTypeConst.LINE) {
                OrgModeAssetVo vo1 = new OrgModeAssetVo();
                vo1.setId(org.getId());
                vo1.setName(org.getTitle());

                List<OrgModeAssetVo> modelist = new ArrayList<>();
                List<String> stationIds = sysOrgService.getStationOrgIdByLineId(org.getId());
                stationIds.retainAll(ShiroUtil.getSubjectOrgIds());

                List<StatisticsAlarmVo> mlist = assetService.getModeAsset(stationIds);
                getModeAssetList(resultList, org, vo1, modelist, mlist);
            }
        }
        return resultList;
    }

    private void getModeAssetList(List<OrgModeAssetVo> resultList, SysOrg org, OrgModeAssetVo vo1,
                                  List<OrgModeAssetVo> modelist, List<StatisticsAlarmVo> mlist) {
        QueryWrapper<Asset> query;
        for (StatisticsAlarmVo mode : mlist) {
            if ((mode.getId()).startsWith("7")) {
                continue;
            }
            OrgModeAssetVo vo2 = new OrgModeAssetVo();
            vo2.setId(mode.getId());
            vo2.setName(mode.getName());

            List<OrgModeAssetVo> assetlist = new ArrayList<>();
            query = Wrappers.query();
            query.select("id", "name");
            query.eq("DESK", mode.getId());
            query.eq("ORG_ID", org.getId());
            query.eq("WATCH", 1);
            query.eq("IS_DEL", 1);
            query.orderByAsc("id", "name");
            List<Asset> list = assetService.list(query);
            for (Asset asset : list) {
                OrgModeAssetVo vo3 = new OrgModeAssetVo();
                vo3.setId(asset.getId());
                vo3.setName(asset.getName());
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
