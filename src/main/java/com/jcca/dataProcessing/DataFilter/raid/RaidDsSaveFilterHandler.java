package com.jcca.dataProcessing.DataFilter.raid;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.DSEntity;
import com.jcca.dataProcessing.Entity.DsSystemFattenEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.service.CollectDsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO DS系列Raid存储
 * @className StorageCapacityFilterHandler
 * @date 2023/10/27 9:35
 * @since 2.1.0.0
 */
@Component("raidDsSaveFilterHandler")
public class RaidDsSaveFilterHandler extends IFilterHandler<DsSystemFattenEntity> {

    @Resource
    private CollectDsService dsService;

    @Override
    public boolean handler(DsSystemFattenEntity info) {
        Date date = new Date();
        date.setTime(info.getCollectTime());
        if (ObjectUtil.isNotNull(info.getControllers()) && !info.getControllers().isEmpty()) {
            List<DSEntity> controllers = info.getControllers();
            for (DSEntity dsBean : controllers) {
                CollectDS collectDS = EntityBeanUtil.copy(dsBean, CollectDS.class);
                collectDS.setAssetId(info.getAssetId());
                collectDS.setCollectCode(info.getCollectCode());
                collectDS.setCollectTime(date);
                dsService.saveOrUpdate(collectDS);
            }
        }

        //存储Array
        if (ObjectUtil.isNotNull(info.getArrays()) && !info.getArrays().isEmpty()) {
            List<DSEntity> arrays  = info.getArrays();
            for (DSEntity dsbean : arrays) {
                CollectDS collectDS = new CollectDS();
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setAssetId(info.getAssetId());
                collectDS.setCollectCode(info.getCollectCode());
                collectDS.setCollectTime(date);
                dsService.saveOrUpdate(collectDS);
            }
        }

        //存储Drives
        if (ObjectUtil.isNotNull(info.getDrives()) && !info.getDrives().isEmpty()) {
            List<DSEntity> drives = info.getDrives();
            for (DSEntity dsbean : drives) {
                CollectDS collectDS = new CollectDS();
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setAssetId(info.getAssetId());
                collectDS.setCollectCode(info.getCollectCode());
                collectDS.setCollectTime(date);
                dsService.saveOrUpdate(collectDS);
            }
        }

        //存储logical Drives
        if (ObjectUtil.isNotNull(info.getLogicalDrives()) && !info.getLogicalDrives().isEmpty()) {
            List<DSEntity> logicalDrives = info.getLogicalDrives();
            for (DSEntity dsbean : logicalDrives) {
                CollectDS collectDS = new CollectDS();
                collectDS.setId(MyIdUtil.getId());
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setAssetId(info.getAssetId());
                collectDS.setCollectCode(info.getCollectCode());
                collectDS.setCollectTime(date);
                collectDS.setFreeCapacityStr(dsbean.getFreeCapacityStr());
                dsService.saveOrUpdate(collectDS);
            }
        }

        if (ObjectUtil.isNotNull(info.getLogInfo()) && !info.getLogInfo().isEmpty()) {
            List<String> logInfo = info.getLogInfo();
            for (String log : logInfo) {
                CollectDS collectDS = new CollectDS();
                collectDS.setAssetId(info.getAssetId());
                collectDS.setType(5);
                collectDS.setLogInfo(log + "<br/>");
                collectDS.setCollectCode(info.getCollectCode());
                collectDS.setCollectTime(date);
                dsService.saveOrUpdate(collectDS);
            }

        }

        if (Objects.nonNull(info.getCapacity()) && Objects.nonNull(info.getFreeCapacity())) {
            if (info.getCapacity() != 0 || (ObjectUtil.isNotNull(info.getDrives()) && !info.getDrives().isEmpty())) {
                CollectDS collectDS = new CollectDS();
                collectDS.setType(4);
                collectDS.setId(MyIdUtil.getId());
                collectDS.setCapacity(info.getCapacity());
                collectDS.setFreeCapacity(info.getFreeCapacity());
                collectDS.setAssetId(info.getAssetId());
                collectDS.setCollectCode(info.getCollectCode());
                collectDS.setCollectTime(date);
                dsService.saveOrUpdate(collectDS);
            }
        }


        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
