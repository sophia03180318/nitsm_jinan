package com.jcca.common.log.service;

import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web2.dto.ConfigProcessDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HanHW
 * @description 进程配置 运维日志记录
 * @className DevLogProcessConfigImpl
 * @date 2024/4/9 9:34
 * @since 2.1.0.0
 */
@Service
public class DevLogProcessConfigImpl implements DevLogService {

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
        return DevLogConstant.PROCESS_CONFIG;
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
        if (!(arg instanceof List)) {
            return;
        }

        List<ConfigProcessDto> reqList = new ArrayList<>();
        List dtoList = (List) arg;
        for (Object o : dtoList) {
            if (o instanceof ConfigProcessDto) {
                reqList.add((ConfigProcessDto) o);
            }
        }

        if (reqList.isEmpty()) {
            actionLog.setLogMsg("失败-参数为空");
            return;
        }

        String assetId = reqList.get(0).getAssetId();
        if (StringUtils.isEmpty(assetId)) {
            actionLog.setLogMsg("失败-参数为空");
            return;
        }
        Asset one = assetService.getById(assetId);
        actionLog.setLogName("配置【" + one.getName() + "】进程");
        List<SysActionLogDetail> detailList = new ArrayList<>();
        for (ConfigProcessDto process : reqList) {
            String thresholdCpu = process.getThresholdCpu();
            String thresholdMemory = process.getThresholdMemory();
            if (StringUtils.isEmpty(thresholdCpu)) {
                thresholdCpu = "无";
            }
            if (StringUtils.isEmpty(thresholdMemory)) {
                thresholdMemory = "无";
            }

            String description = "配置进程【" + process.getProcessName()
                    + "】，CPU阈值【" + thresholdCpu + "】，内存阈值【" + thresholdMemory + "】";
            SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLog.getId(),
                    assetId, process.getProcessId(), LogDetailItemIdType.PROCESS, description);

            detailList.add(detail);
        }

        sysActionLogDetailService.saveBatch(detailList);

    }
}
