package com.jcca.web.xunjian.adapter.v1.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.broken.service.BrokenRecordOpinionService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.entity.XunjianAlarmMsg;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import com.jcca.web.xunjian.service.XunjianAlarmMsgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警巡检
 * <p>
 * 未确认未恢复,不包含信息通知
 *
 * @author Lvyp
 */
@Service
public class AlarmAdpaterImpl implements XunJianAdapter {

    @Resource
    private AlarmInfoService alarmServ;
    @Resource
    private XunjianAlarmMsgService xunjianAlarmServ;
    @Resource
    private BrokenRecordOpinionService opinionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<AlarmInfo>();
        queryWrapper.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
        queryWrapper.eq("ASSET_ID", asset.getAssetId());
        queryWrapper.ne("ALARM_LEVEL", AlarmLevelEnum.LEVEL_MSG.getCode());
        List<AlarmInfo> list = alarmServ.list(queryWrapper);

        if (list.isEmpty()) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg(String.format("【一级告警】：%s,【二级告警】：%s,【三级告警】：%s,【未知告警】：%s", 0, 0, 0, 0));
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        List<AlarmInfo> oneLevel = list.stream()
                .filter(item -> AlarmLevelEnum.LEVEL_ONE.getCode() == item.getAlarmLevel().byteValue())
                .collect(Collectors.toList());

        List<AlarmInfo> twoLevel = list.stream()
                .filter(item -> AlarmLevelEnum.LEVEL_TWO.getCode() == item.getAlarmLevel().byteValue())
                .collect(Collectors.toList());

        List<AlarmInfo> threeLevel = list.stream()
                .filter(item -> AlarmLevelEnum.LEVEL_THREE.getCode() == item.getAlarmLevel().byteValue())
                .collect(Collectors.toList());

        BigDecimal unkonow = new BigDecimal(list.size()).subtract(new BigDecimal(oneLevel.size()))
                .subtract(new BigDecimal(twoLevel.size())).subtract(new BigDecimal(threeLevel.size()));

        List<XunjianAlarmMsg> copyList = new ArrayList<XunjianAlarmMsg>();
        for (AlarmInfo alarmInfo : list) {
            String opinion = opinionService.getMsgByAlarmInfoId(alarmInfo.getId());
            XunjianAlarmMsg copy = EntityBeanUtil.copy(alarmInfo, XunjianAlarmMsg.class);
            copy.setOpinion(opinion);
            copy.setAlarmInfoId(alarmInfo.getId());
            copy.setId(MyIdUtil.getId());
            copy.setXunjianRecordId(detail.getXunjianRecordId());
            copy.setCreateDate(new Date());
            copyList.add(copy);
        }

        if (!copyList.isEmpty()) {
            xunjianAlarmServ.saveBatch(copyList);
        }

        detail.setXunJianValue("");
        detail.setThresholdValue("");
        detail.setResultMsg(String.format("【一级告警】：%s,【二级告警】：%s,【三级告警】：%s,【未知告警】：%s", oneLevel.size(), twoLevel.size(),
                threeLevel.size(), unkonow.intValue()));
        detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
        return detail;

    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.ALARM.getCode();
    }

}
