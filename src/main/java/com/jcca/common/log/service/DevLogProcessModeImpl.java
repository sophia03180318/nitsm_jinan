package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.controller.bean.ChangeProcessModeReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 修改进程模式 运维日志记录
 * @className DevLogProcessModeImpl
 * @date 2024/4/11 16:59
 * @since 2.1.0.0
 */
@Service
public class DevLogProcessModeImpl implements DevLogService {

    @Resource
    private AssetService assetService;
    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.PROCESS_MODE;
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
        if (!(arg instanceof ChangeProcessModeReq)) {
            return;
        }
        ChangeProcessModeReq req = (ChangeProcessModeReq) arg;
        String id = req.getId();
        Integer mode = req.getMode();
        String processName = req.getProcessName();

        ThresholdProcess process = thresholdProcessService.getById(id);
        Integer hostMode = process.getHostMode();
        if (mode == hostMode.intValue()) {
            return;
        }
        String assetId = process.getAssetId();
        Asset one = assetService.getById(assetId);

        actionLog.setLogName("修改【" + one.getName() + "】进程【" + processName + "】模式");

        String description = "进程【" + processName + "】模式由【" + this.getModeStr(hostMode) + "】变更为【" + this.getModeStr(mode) + "】";
        description = description.replace("null", "无");
        SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                assetId, id, LogDetailItemIdType.PROCESS, description);
        sysActionLogDetailService.save(detail);

    }

    // 模式 1双击单活 2双机双活 其他普通
    private String getModeStr(Integer mode) {
        if (mode == 1) {
            return "双击单活";
        }
        if (mode == 2) {
            return "双机双活";
        }
        return "普通";
    }
}
