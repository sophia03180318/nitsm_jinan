package com.jcca.dataProcessing.DataFilter.memory;

import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectMemoryEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectMemoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 内存普通阈值信息过滤处理类
 * @className MemoryFilterHandler
 * @date 2023/10/27 9:44
 * @since 2.1.0.0
 */
@Component("memorySaveFilterHandler")
public class MemorySaveFilterHandler extends IFilterHandler<CollectMemoryEntity> {

    @Resource
    private CollectMemoryService memService;

    @Override
    public boolean handler(CollectMemoryEntity info) {

        Date date = new Date();
        date.setTime(info.getCollectTime());

        CollectMemory mem = EntityBeanUtil.copy(info, CollectMemory.class);
        double memUsedRate = AppMathUtil.percentageDouble(info.getMemUsed(), info.getMemTotal(), 2);
        double swapUsedRate = AppMathUtil.percentageDouble(info.getSwapUsed(), info.getSwapTotal(), 2);
        mem.setSwapUsedRate(swapUsedRate);
        mem.setMemUsedRate(memUsedRate);
        mem.setCollectCode(MyIdUtil.getId());
        mem.setCollectTime(date);
        mem.setId(MyIdUtil.getId());
        memService.save(mem);
        return true;


    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
