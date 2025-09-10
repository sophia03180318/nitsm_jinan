package com.jcca.dataProcessing.DataFilter.bhm.cpu;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmCpuEntity;

import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;

import com.jcca.web2.entity.CollectBhmCpuInfo;
import com.jcca.web2.service.CollectBhmCpuInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO CPU 普通阈值变动处理类
 * @className CpuFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("bhmCpuSaveHandler")
public class BhmCpuSaveHandler extends IFilterHandler<CollectBhmCpuEntity> {

    @Resource
    private CollectBhmCpuInfoService cpuService;

    @Override
    public boolean handler(CollectBhmCpuEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口CPU数据", info.getAssetIp());

        ReadFishStatusEntity status = info.getStatus();
        CollectBhmCpuInfo copy = EntityBeanUtil.copy(info, CollectBhmCpuInfo.class);
        if(Objects.nonNull(status)){
            copy.setHealth(status.getHealth());
            copy.setState(status.getState());
        }
        copy.setId(MyIdUtil.getId());
        copy.setCpuId(info.getId());
        copy.setAssetId(info.getAssetId());
        copy.setCreateTime(new Date());


        //删除原有的插入新的
        cpuService.updateAssetCpuInfo(copy);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
