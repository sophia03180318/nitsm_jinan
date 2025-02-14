package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.controller.bean.ThresholdProcessUpdateReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 进程阈值 运维日志记录
 * @className DevLogThresholdProcessImpl
 * @date 2024/4/8 15:51
 * @since 2.1.0.0
 */
@Service
public class DevLogThresholdProcessImpl implements DevLogService {

    @Resource
    private ThresholdProcessService thresholdProcessService;
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
        return DevLogConstant.THRESHOLD_PROCESS;
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
        if (!(arg instanceof ThresholdProcessUpdateReq)) {
            return;
        }

        ThresholdProcessUpdateReq req = (ThresholdProcessUpdateReq) arg;
        String reqId = req.getId();
        ThresholdProcess process = thresholdProcessService.getById(reqId);
        Asset asset = assetService.getById(process.getAssetId());

        actionLog.setLogName("配置【" + asset.getName() + "】进程【" + req.getProcessName() + "】阈值");

        String ocpu = StringUtils.isEmpty(process.getThresholdCpu()) ? "无" : process.getThresholdCpu();
        String omem = StringUtils.isEmpty(process.getThresholdMemory()) ? "无" : process.getThresholdMemory();
        String ncpu = StringUtils.isEmpty(req.getThresholdCpu()) ? "无" : req.getThresholdCpu();
        String nmem = StringUtils.isEmpty(req.getThresholdMemory()) ? "无" : req.getThresholdMemory();
        if (!ocpu.equals(ncpu)) {
            String description = "CPU阈值由【" + ocpu + "】变更为【" + ncpu + "】";
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    asset.getId(), reqId, LogDetailItemIdType.PROCESS, description);
            sysActionLogDetailService.save(detail);
        }
        if (!omem.equals(nmem)) {
            String description = "内存阈值由【" + omem + "】变更为【" + nmem + "】";
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    asset.getId(), reqId, LogDetailItemIdType.PROCESS, description);
            sysActionLogDetailService.save(detail);
        }

    }
}
