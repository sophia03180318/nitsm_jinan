package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.vo.AlarmHandleVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 告警确认运维日志记录
 * @className DevLogAlarmConfirmSingleImpl
 * @date 2024/4/1 9:46
 * @since 2.1.0.0
 */
@Service
public class DevLogAlarmConfirmSingleImpl implements DevLogService {

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
        return DevLogConstant.ALARM_CONFIRM_SINGLE;
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
        if (arg instanceof AlarmHandleVo) {
            AlarmHandleVo vo = (AlarmHandleVo) arg;
            String id = vo.getId();
            AlarmInfo info = alarmInfoService.getById(id);
            Asset one = assetService.getById(info.getAssetId());

            String description = "确认【" + one.getName() + "】告警内容【" + info.getContent() + "】";
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    one.getId(), id, LogDetailItemIdType.ALARM, description);

            String logName = "确认【" + one.getName() + "】告警【" + info.getTitle() + "】";
            actionLog.setLogName(logName);
            sysActionLogDetailService.save(detail);
        }
    }
}
