package com.jcca.dataProcessing.DataFilter.disk;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectDiskEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.service.CollectDiskService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 磁盘阈值过滤处理类 普通阈值
 * @className DiskFilterHandler
 * @date 2023/10/27 9:31
 * @since 2.1.0.0
 */
@Component("diskSaveFilterHandler")
public class DiskSaveFilterHandler extends IFilterHandler<CollectDiskEntity> {
    @Resource
    private CollectDiskService diskService;


    @Override
    public boolean handler(CollectDiskEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "磁盘数据保存", info.getAssetIp());
        if(StrUtil.isNotEmpty(info.getMountPoint())){
            if(info.getMountPoint().toLowerCase().contains("/mnt")||info.getMountPoint().toLowerCase().contains("/media")){
                //不采集/mnt 路径
               return false;
            }
        }

        Date date = new Date();
        date.setTime(Long.valueOf(info.getCollectTime()));
        String collectUsedRate = AppMathUtil.percentage(info.getUsed(), info.getTotal(), 4);
        String collectFreeRate = AppMathUtil.percentage(info.getAvailable(), info.getTotal(), 4);
        CollectDisk item = new CollectDisk();
        item.setId(MyIdUtil.getId());
        item.setAssetId(info.getAssetId());
        item.setCollectCode(info.getCollectCode());
        item.setCollectTime(date);
        item.setCreateTime(date);
        item.setFree(info.getAvailable());
        item.setMountPoint(info.getMountPoint());
        item.setTotal(info.getTotal());
        item.setUsed(info.getUsed());
        item.setFreeRate(Double.valueOf(collectFreeRate));
        item.setUsedRate(Double.valueOf(collectUsedRate));
        String assetId = info.getAssetId();

        QueryWrapper<CollectDisk> wrapper = new QueryWrapper<CollectDisk>();
        wrapper.eq("ASSET_ID", assetId);
        wrapper.eq("MOUNT_POINT", info.getMountPoint());
        diskService.remove(wrapper);

        diskService.save(item);
        return true;


    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
