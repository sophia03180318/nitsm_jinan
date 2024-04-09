package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.common.enums.StatusEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.Entity.ProcessAlarmQueueEntity;
import com.jcca.dataProcessing.Entity.ProcessGroupEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.collect.entity.CollectProcess;
import com.jcca.web.collect.service.CollectProcessService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
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
        for (ProcessAlarmQueueEntity processAlarmQueueEntity : queueObj) {
            ThresholdProcess threshold = thresholdService.getById(processAlarmQueueEntity.getThresholdId());
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
