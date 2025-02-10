package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HanHW
 * @description 告警清除运维日志记录
 * @className DevLogAlarmClearImpl
 * @date 2024/4/1 9:46
 * @since 2.1.0.0
 */
@Service
public class DevLogAlarmClearImpl implements DevLogService {

    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.ALARM_CLEAR_CONDITION;
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
        List<String> idList = req.getIdList();
        List<SysActionLogDetail> detailList = new ArrayList<>();
        AlarmInfo info;
        Asset one;
        for (String id : idList) {
            info = alarmInfoService.getById(id);
            one = assetService.getById(info.getAssetId());

            String description = "清除【" + one.getName() + "】告警【" + info.getContent() + "】";
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    one.getId(), id, LogDetailItemIdType.ALARM, description);
            detailList.add(detail);
        }
        actionLog.setLogName("批量清除" + idList.size() + "条告警");

        sysActionLogDetailService.saveBatch(detailList);
    }
}
