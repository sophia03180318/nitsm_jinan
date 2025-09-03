package com.jcca.dataProcessing.DataFilter.cpu;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectCpuEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.service.CollectCpuService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO CPU 普通阈值变动处理类
 * @className CpuFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("cpuSaveFilterHandler")
public class CpuSaveFilterHandler extends IFilterHandler<CollectCpuEntity> {

    @Resource
    private CollectCpuService cpuService;

    @Override
    public boolean handler(CollectCpuEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存CPU数据", info.getAssetIp());
        Date date = new Date();
        date.setTime(info.getCollectTime());
        CollectCpu cpu = EntityBeanUtil.copy(info, CollectCpu.class);
        cpu.setId(MyIdUtil.getId());
        cpu.setCollectCode(MyIdUtil.getId());
        cpu.setCollectTime(date);
        cpuService.save(cpu);
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
