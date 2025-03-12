package com.jcca.dataProcessing.DataFilter.process;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.enums.StatusEnum;
import com.jcca.dataProcessing.Entity.ProcessAlarmQueueEntity;
import com.jcca.dataProcessing.Entity.ProcessGroupEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.ThresholdProcessService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 进程CPU过滤处理类
 * @className ProcessCpuFilterHandler
 * @date 2023/10/27 9:58
 * @since 2.1.0.0
 */
@Component("processSaveGroupFilterHandler")
public class ProcessSaveGroupFilterHandler extends IFilterHandler<ProcessGroupEntity> {


    @Resource
    private ThresholdProcessService thresholdService;

    @Override
    public boolean handler(ProcessGroupEntity info) {
        List<ProcessAlarmQueueEntity> queueObj = info.getQueueObj();
        QueryWrapper<ThresholdProcess> query;
        for (ProcessAlarmQueueEntity processAlarmQueueEntity : queueObj) {
            String assetId = processAlarmQueueEntity.getAssetId();
            String processName = processAlarmQueueEntity.getProcessName();
            query = Wrappers.query();
            query.eq("ASSET_ID", assetId);
            query.eq("PROCESS_NAME", processName);
            List<ThresholdProcess> list = thresholdService.list(query);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            ThresholdProcess threshold = list.get(0);
            threshold.setProcessId(processAlarmQueueEntity.getProcessId());
            threshold.setCollectStatus(processAlarmQueueEntity.getProcessStatus() ? StatusEnum.OK.getCode() : StatusEnum.NO.getCode());
            thresholdService.updateById(threshold);
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
