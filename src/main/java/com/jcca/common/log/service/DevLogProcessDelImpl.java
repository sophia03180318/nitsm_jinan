package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HanHW
 * @description 删除进程 运维日志记录
 * @className DevLogProcessDelImpl
 * @date 2024/4/9 10:05
 * @since 2.1.0.0
 */
@Service
public class DevLogProcessDelImpl implements DevLogService {

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
        return DevLogConstant.PROCESS_DEL;
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
        ThresholdProcess process = thresholdProcessService.getById(arg.toString());
        if (Objects.isNull(process)) {
            actionLog.setLogMsg("失败-参数为空");
            return;
        }
        String assetId = process.getAssetId();
        Asset one = assetService.getById(assetId);
        String description = "删除【" + one.getName() + "】进程【" + process.getProcessName() + "】";
        actionLog.setLogName(description);

        SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                assetId, arg.toString(), LogDetailItemIdType.PROCESS, description);
        sysActionLogDetailService.save(detail);
    }
}
