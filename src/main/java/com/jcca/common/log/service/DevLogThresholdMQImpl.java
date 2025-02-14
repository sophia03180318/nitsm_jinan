package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.ibmMQ.domain.vo.WarningVo;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.service.IBMMonitorService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 中间件队列深度阈值设置 运维日志记录
 * @className DevLogThresholdMQImpl
 * @date 2024/4/8 15:29
 * @since 2.1.0.0
 */
@Service
public class DevLogThresholdMQImpl implements DevLogService {

    @Resource
    private IBMMonitorService ibmMonitorService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.THRESHOLD_MQ;
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
        if (!(arg instanceof WarningVo)) {
            return;
        }

        WarningVo req = (WarningVo) arg;
        String id = req.getId();
        String currentQDepth = req.getCurrentQDepth();

        IBMMonitor one = ibmMonitorService.getById(id);

        actionLog.setLogName("设置中间件【" + one.getName() + "】队列深度");

        String description = "表达式由【" + (StringUtils.isEmpty(one.getHealthRule()) ? "无" : one.getHealthRule())
                + "】变更为【error(currentQDepth>" + currentQDepth + ")】";
        description = description.replace("null", "无");
        SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                "", id, LogDetailItemIdType.MQ, description);
        sysActionLogDetailService.save(detail);
    }
}
