package com.jcca.common.log.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 条件确认告警 运维日志记录
 * @className DevLogAlarmConfirmConditionImpl
 * @date 2024/4/1 9:46
 * @since 2.1.0.0
 */
@Service
public class DevLogAlarmConfirmConditionImpl implements DevLogService {

    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.ALARM_CONFIRM_CONDITION;
    }

    /**
     * 设置运维日志内容
     *
     * @param actionLog 日志
     * @param args      参数
     */
    @Override
    public void setDevLog(SysActionLog actionLog, Object[] args) {
        Object arg = args[0];
        if (!(arg instanceof AlarmInfoPageQuery)) {
            return;
        }
        AlarmInfoPageQuery req = (AlarmInfoPageQuery) arg;
        QueryWrapper<AlarmInfo> query = this.getQueryWrapper(req);
        query.eq("status", AlarmStatusEnum.UNCONFIRM.getCode());
        List<AlarmInfo> alarmList = alarmInfoService.list(query);
        if (alarmList.isEmpty()) {
            return;
        }

        List<SysActionLogDetail> detailList = new ArrayList<>();
        AlarmInfo info;
        Asset one;
        for (AlarmInfo alarmInfo : alarmList) {
            String id = alarmInfo.getId();
            info = alarmInfoService.getById(id);
            one = assetService.getById(info.getAssetId());

            String description = "确认【" + one.getName() + "】告警内容：" + info.getContent();
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    one.getId(), id, LogDetailItemIdType.ALARM, description);
            detailList.add(detail);
        }
        actionLog.setLogName("按条件确认" + alarmList.size() + "条告警");

        sysActionLogDetailService.saveBatch(detailList);
    }

    private QueryWrapper<AlarmInfo> getQueryWrapper(AlarmInfoPageQuery req) {
        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<AlarmInfo>();
        if (StrUtil.isNotEmpty(req.getOrgId())) {
            String id = req.getOrgId();
            List<SysOrg> orgList = ShiroUtil.getSubjectOrgs();
            SysOrg org = orgService.getById(id);
            if (org.getType() == 3) {
                List<String> collect = orgList.stream().filter(o -> o.getPid().equals(id)).map(SysOrg::getId)
                        .collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    queryWrapper.in("ORG_ID", collect);
                } else {
                    queryWrapper.eq("ORG_ID", id);
                }
            } else {
                queryWrapper.eq("ORG_ID", id);
            }
        } else {
            List<String> orgIds = ShiroUtil.getSubjectOrgIds();
            orgIds.add("x");
            queryWrapper.in("ORG_ID", orgIds);
        }
        if (StrUtil.isNotEmpty(req.getAssetName())) {
            queryWrapper.like("ASSET_NAME", req.getAssetName());
        }
        if (StrUtil.isNotEmpty(req.getTitle())) {
            queryWrapper.eq("TITLE", req.getTitle());
        }
        if (StrUtil.isNotEmpty(req.getIp())) {
            Asset one = assetService.getOneByAllIp(req.getIp());
            if (Objects.nonNull(one)) {
                queryWrapper.eq("ASSET_ID", one.getId());
            } else {
                queryWrapper.eq("ASSET_IP", req.getIp());
            }
        }
        if (Objects.nonNull(req.getStartDate()) && Objects.nonNull(req.getEndDate())) {
            queryWrapper.between("OCCUR_TIME", req.getStartDate(), req.getEndDate());
        }
        if (Objects.nonNull(req.getStatus())) {
            queryWrapper.eq("STATUS", req.getStatus());
        }
        if (Objects.nonNull(req.getType())) {
            queryWrapper.eq("TYPE", req.getType());
        }
        if (Objects.nonNull(req.getAlarmLevel())) {
            queryWrapper.eq("ALARM_LEVEL", req.getAlarmLevel());
        }
        if (Objects.nonNull(req.getAlarmState())) {
            queryWrapper.eq("ALARM_STATE", req.getAlarmState());
        }
        if (Objects.nonNull(req.getBlank())) {
            queryWrapper.eq("BLANK", req.getBlank());
        }
        if (Objects.nonNull(req.getAssetMode())) {
            queryWrapper.eq("ASSET_MODE", req.getAssetMode());
        }
        if (!StrUtil.equals("yes", req.getShowJcca())) {
            //显示
            List<String> jccaAssetIds = assetService.listJccaId();
            jccaAssetIds.add("-1");
            queryWrapper.notIn("ASSET_ID", jccaAssetIds);
            //过滤掉 “采集器相关告警”
            queryWrapper.notLike("CONTENT", "采集器");
        }

        //  queryWrapper.orderByDesc("OCCUR_TIME");
        return queryWrapper;
    }
}
