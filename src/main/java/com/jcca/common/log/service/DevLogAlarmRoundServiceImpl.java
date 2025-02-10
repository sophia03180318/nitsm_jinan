package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.controller.bean.AlarmVerifyBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 配置告警轮询次数 运维日志记录
 * @className DevLogAlarmRoundServiceImpl
 * @date 2024/4/8 10:53
 * @since 2.1.0.0
 */
@Service
public class DevLogAlarmRoundServiceImpl implements DevLogService {

    @Resource
    private SysModuleConfigService sysModuleConfServ;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.ALARM_ROUND_TIME;
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
        if (!(arg instanceof AlarmVerifyBean)) {
            return;
        }
        int nprocessSize, npingSize, oprocessSize, opingSize;
        AlarmVerifyBean oreq = sysModuleConfServ.getAlarmVerifyValue();
        AlarmVerifyBean nreq = (AlarmVerifyBean) arg;
        nprocessSize = nreq.getProcessSize() == null ? 1 : nreq.getProcessSize();
        npingSize = nreq.getPingSize() == null ? 1 : nreq.getPingSize();

        opingSize = oreq.getPingSize() == null ? 1 : oreq.getPingSize();
        oprocessSize = oreq.getProcessSize() == null ? 1 : oreq.getProcessSize();

        String description = "PING告警检查次数由【" + opingSize + "】变更为【" + npingSize + "】";
        description = description.replace("null", "无");
        SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                "", oreq.getId(), LogDetailItemIdType.CONFIG, description);
        sysActionLogDetailService.save(detail);

        description = "进程告警检查次数由【" + oprocessSize + "】变更为【" + nprocessSize + "】";
        description = description.replace("null", "无");
        detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                "", oreq.getId(), LogDetailItemIdType.CONFIG, description);
        sysActionLogDetailService.save(detail);
    }
}
