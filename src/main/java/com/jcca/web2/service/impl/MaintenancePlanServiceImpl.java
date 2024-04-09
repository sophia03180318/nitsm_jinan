package com.jcca.web2.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.MaintenancePlanMapper;
import com.jcca.web2.entity.MaintenancePlan;
import com.jcca.web2.service.MaintenancePlanService;
import com.jcca.web2.vo.MaintenancePlanVo;
import com.jcca.web2.vo.MaintenanceStatisticsVo;
import com.jcca.web2.vo.MaintenanceVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/16 16:33
 **/
@Service
public class MaintenancePlanServiceImpl extends ServiceImpl<MaintenancePlanMapper, MaintenancePlan> implements MaintenancePlanService {

    @Resource
    private SysOrgService orgService;
    @Resource
    private MaintenancePlanMapper maintenancePlanMapper;
    @Resource
    private RedisService redisService;

    @Override
    public List<MaintenancePlanVo> importProcess(List<MaintenancePlanVo> dataList) {
        ArrayList<MaintenancePlanVo> errorList = new ArrayList<>();
        //判断每一个资产是否符合条件
        data:
        for (MaintenancePlanVo maintenancePlanVo : dataList) {
            MaintenancePlan maintenancePlan = new MaintenancePlan();
            /*//名称
            if (ObjectUtil.isNull(maintenancePlanVo.getName()) || maintenancePlanVo.getName().trim().isEmpty()) {
                maintenancePlanVo.setLog("维护计划名称不可为空");
                errorList.add(maintenancePlanVo);
                continue;
            }
            if (maintenancePlanVo.getName().length() > 50) {
                maintenancePlanVo.setLog("维护计划名称太长");
                errorList.add(maintenancePlanVo);
                continue;
            }
            maintenancePlan.setName(maintenancePlanVo.getName().trim());*/

            //内容
            if (ObjectUtil.isNull(maintenancePlanVo.getContent()) || maintenancePlanVo.getContent().trim().isEmpty()) {
                maintenancePlanVo.setLog("维护计划内容不可为空");
                errorList.add(maintenancePlanVo);
                continue;
            }
            if (maintenancePlanVo.getContent().length() > 1000) {
                maintenancePlanVo.setLog("施工内容太长");
                errorList.add(maintenancePlanVo);
                continue;
            }
            maintenancePlan.setContent(maintenancePlanVo.getContent().trim());

            //起始时间
            if (ObjectUtil.isNull(maintenancePlanVo.getStartTimeStr()) || maintenancePlanVo.getStartTimeStr().trim().isEmpty()) {
                maintenancePlanVo.setLog("开始时间不可为空~");
                errorList.add(maintenancePlanVo);
                continue;
            }
            try {
                Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(maintenancePlanVo.getStartTimeStr().trim());
                maintenancePlan.setStartTime(startTime);
            } catch (ParseException e) {
                maintenancePlanVo.setLog("日期不合格式 请按照 2023-06-12 03:22 格式进行填写");
                errorList.add(maintenancePlanVo);
                continue;
            }


            //结束时间
            if (ObjectUtil.isNull(maintenancePlanVo.getEndTimeStr()) || maintenancePlanVo.getEndTimeStr().trim().isEmpty()) {
                maintenancePlanVo.setLog("结束时间不可为空~");
                errorList.add(maintenancePlanVo);
                continue;
            }
            try {
                Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(maintenancePlanVo.getEndTimeStr().trim());
                maintenancePlan.setEndTime(endTime);
            } catch (ParseException e) {
                maintenancePlanVo.setLog("日期不合格式 请按照 2023-06-12 03:22 格式进行填写");
                errorList.add(maintenancePlanVo);
                continue;
            }

            //组织
            if (ObjectUtil.isNull(maintenancePlanVo.getOrgName()) || maintenancePlanVo.getOrgName().trim().isEmpty()) {
                maintenancePlanVo.setLog("施工站点不可为空~");
                errorList.add(maintenancePlanVo);
                continue;
            }
            String[] orgNames = maintenancePlanVo.getOrgName().trim().split(",|、");
            Set<String> orgIds = new HashSet<>();
            for (String orgName : orgNames) {
                List<SysOrg> orgList = orgService.findAllByName(orgName.trim());
                if (ObjectUtil.isNull(orgList) || orgList.isEmpty()) {
                    maintenancePlanVo.setLog("未找到组织： [" + orgName + "] ");
                    errorList.add(maintenancePlanVo);
                    continue data;
                }
                SysOrg org = orgList.get(0);
                if (OrgTypeEnum.STATION.getCode() != org.getType() && OrgTypeEnum.CENTER.getCode() != org.getType()) {
                    maintenancePlanVo.setLog("[" + org.getTitle() + "] ：并不是中心或站");
                    errorList.add(maintenancePlanVo);
                    continue data;
                }
                orgIds.add(org.getId());
            }
            StringBuilder orgIdStr = new StringBuilder();
            for (String orgId : orgIds) {
                orgIdStr.append(orgId).append(",");
            }
            //去逗号
            maintenancePlan.setOrgId(orgIdStr.toString().substring(0, orgIdStr.indexOf(",")));
            maintenancePlan.setInfluenceOrg(orgIdStr.toString().substring(0, orgIdStr.length() - 1));
            maintenancePlan.setName(maintenancePlanVo.getOrgName().trim() + "施工计划");

            if (ObjectUtil.isNotNull(maintenancePlanVo.getLevel()) && maintenancePlanVo.getLevel().length() < 10) {
                maintenancePlan.setPlanLevel(maintenancePlanVo.getLevel());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getStation()) && maintenancePlanVo.getStation().length() < 80) {
                maintenancePlan.setDianWuDuan(maintenancePlanVo.getStation());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getJobNumber()) && maintenancePlanVo.getJobNumber().length() < 80) {
                maintenancePlan.setJobNumber(maintenancePlanVo.getJobNumber());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getModel()) && maintenancePlanVo.getModel().length() < 80) {
                maintenancePlan.setModel(maintenancePlanVo.getModel());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getChain()) && maintenancePlanVo.getChain().length() < 80) {
                maintenancePlan.setChain(maintenancePlanVo.getChain());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getMonitor()) && maintenancePlanVo.getMonitor().length() < 80) {
                maintenancePlan.setMonitor(maintenancePlanVo.getMonitor());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getSoftware()) && maintenancePlanVo.getSoftware().length() < 80) {
                maintenancePlan.setSoftware(maintenancePlanVo.getSoftware());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getFieldForce()) && maintenancePlanVo.getFieldForce().length() < 80) {
                maintenancePlan.setFieldForce(maintenancePlanVo.getFieldForce());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getAmend()) && maintenancePlanVo.getAmend().length() < 80) {
                maintenancePlan.setAmend(maintenancePlanVo.getAmend());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getRemark()) && maintenancePlanVo.getRemark().length() < 80) {
                maintenancePlan.setRemark(maintenancePlanVo.getRemark());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getStatus()) && maintenancePlanVo.getStatus().length() < 80) {
                maintenancePlan.setStatus(maintenancePlanVo.getStatus());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getType()) && maintenancePlanVo.getType().length() < 80) {
                maintenancePlan.setType(maintenancePlanVo.getType());
            }

            if (ObjectUtil.isNotNull(maintenancePlanVo.getDebug()) && maintenancePlanVo.getDebug().length() < 80) {
                maintenancePlan.setDebug(maintenancePlanVo.getDebug());
            }

            maintenancePlan.setId(MyIdUtil.getId());
            maintenancePlan.setOccurYear(DateUtil.thisYear());
            maintenancePlan.setOccurMonth(DateUtil.thisMonth());
            maintenancePlan.setOccurDay(DateUtil.thisDayOfMonth());
            this.save(maintenancePlan);

        }
        return errorList;
    }

    /**
     * 新增维护计划
     *
     * @param vo
     */
    @Override
    public void add(MaintenanceVo vo) {
        DateTime startTime = DateUtil.parse(vo.getStartTime(), "yyyy-MM-dd HH:mm");
        DateTime endTime = DateUtil.parse(vo.getEndTime(), "yyyy-MM-dd HH:mm");
        if (DateUtil.compare(startTime, endTime) >= 0) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "结束日期不能大于开始日期");
        }

        MaintenancePlan plan = new MaintenancePlan();
        BeanUtils.copyProperties(vo, plan);
        List<String> influenceOrgIds = vo.getInfluenceOrgIds();
        StringBuilder sb = new StringBuilder();
        for (String orgId : influenceOrgIds) {
            sb.append(orgId).append(",");
        }
        plan.setInfluenceOrg(sb.substring(0, sb.lastIndexOf(",")));
        plan.setEndTime(endTime);
        plan.setStartTime(startTime);
        plan.setOccurYear(startTime.year());
        plan.setOccurMonth(startTime.month() + 1);
        plan.setOccurDay(startTime.dayOfMonth());
        this.saveOrUpdate(plan);

        long expireTime = (endTime.getTime() - System.currentTimeMillis()) / 1000;
        for (String orgId : influenceOrgIds) {
            redisService.set(Web2Const.MAINTENANCE_FIX + orgId, AlarmBlankConst.BLANK, expireTime);
        }
    }

    /**
     * 维护计划统计
     *
     * @return
     */
    @Override
    public List<MaintenanceStatisticsVo> statistics() {
        int year = DateUtil.thisYear();
        List<MaintenanceStatisticsVo> list = maintenancePlanMapper.statistics(year);
        this.fillMap(list);

        list.sort(Comparator.comparing(MaintenanceStatisticsVo::getMonth));

        return list;
    }

    /**
     * 查询是否天窗告警
     *
     * @param orgId 资产所属组织ID
     * @return AlarmBlankConst 天窗告警标记，1正常时段告警，2天窗时段告警
     */
    @Override
    public Byte isAlarmBlank(String orgId) {

        Object o = redisService.get(Web2Const.MAINTENANCE_FIX + orgId);
        if (Objects.isNull(o)) {
            return AlarmBlankConst.NORMARL;
        }

        return AlarmBlankConst.BLANK;
    }

    private List<String> getTypeList() {
        int year = DateUtil.thisYear();
        return maintenancePlanMapper.getTypeList(year);
    }

    private void fillMap(List<MaintenanceStatisticsVo> list) {
        int mth = DateUtil.thisMonth() + 1;
        if (CollectionUtils.isEmpty(list)) {
            for (int month = 1; month < mth + 1; month++) {
                MaintenanceStatisticsVo vo = new MaintenanceStatisticsVo();
                vo.setMonth(month);
                vo.setCount(0);
                vo.setType("");
                list.add(vo);
            }
            return;
        }
        List<String> typeList = this.getTypeList();
        for (int month = 1; month < mth + 1; month++) {
            Set<String> typeSet = new HashSet<>();
            for (MaintenanceStatisticsVo vo : list) {
                Integer m = vo.getMonth();
                if (month == m) {
                    typeSet.add(vo.getType());
                }
            }
            for (String type : typeList) {
                if (typeSet.contains(type)) {
                    continue;
                }
                MaintenanceStatisticsVo vo = new MaintenanceStatisticsVo();
                vo.setMonth(month);
                vo.setCount(0);
                vo.setType(type);
                list.add(vo);
            }
        }
    }
}