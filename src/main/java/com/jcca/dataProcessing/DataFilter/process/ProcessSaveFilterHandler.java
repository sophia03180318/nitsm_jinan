package com.jcca.dataProcessing.DataFilter.process;

import com.jcca.common.enums.StatusEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.asset.utils.enums.ProcessHostModeEnum;
import com.jcca.web.collect.entity.CollectProcess;
import com.jcca.web.collect.service.CollectProcessService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 进程CPU过滤处理类
 * @className ProcessCpuFilterHandler
 * @date 2023/10/27 9:58
 * @since 2.1.0.0
 */
@Component("processSaveFilterHandler")
public class ProcessSaveFilterHandler extends IFilterHandler<CollectProcessEntity> {


    @Resource
    private ThresholdProcessService thresholdService;
    @Resource
    private CollectProcessService collectProcessServ;


    @Override
    public boolean handler(CollectProcessEntity info) {
        ThresholdProcess threshold = thresholdService.getById(info.getThresholdId());
        threshold.setProcessId(info.getProcessId());
        if (Objects.nonNull(info.getCpuRate())) {
            threshold.setCpuRate(info.getCpuRate().toString());
        }
        if (Objects.nonNull(info.getMemoryRate())) {
            threshold.setMemoryRate(info.getMemoryRate().toString());
        }
        // 这里不修改组进程状态
        if (threshold.getHostMode() == ProcessHostModeEnum.COMMON.getCode().intValue()) {
            threshold.setCollectStatus(info.getStatus() ? StatusEnum.OK.getCode() : StatusEnum.NO.getCode());
        }
        thresholdService.updateById(threshold);

        if (Objects.isNull(info.getCpuRate()) || Objects.isNull(info.getMemoryRate())) {
            return true;
        }
        Double cpuRate = info.getCpuRate();
        Double memoryRate = info.getMemoryRate();
        if (cpuRate < 0) {
            info.setCpuRate(0d);
        }
        if (cpuRate > 100 || cpuRate == 100) {
            info.setCpuRate(100d);
        }
        if (memoryRate < 0) {
            info.setMemoryRate(0d);
        }
        if (memoryRate > 100) {
            info.setMemoryRate(100d);
        }

        CollectProcess process = new CollectProcess();
        process.setId(MyIdUtil.getId());
        process.setCollectTime(new Date(info.getCollectTime()));
        process.setCollectCode(info.getCollectCode());
        process.setAssetId(info.getAssetId());
        process.setName(process.getName());
        process.setProcessId(info.getProcessId());
        process.setCpuRate(info.getCpuRate());
        process.setMemoryRate(info.getMemoryRate());
        process.setCreateTime(new Date());

        collectProcessServ.save(process);
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
