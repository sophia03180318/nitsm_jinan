package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.dataProcessing.Entity.ReadFishDiskEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.Entity.ReadFishStorageControllersEntity;
import com.jcca.web2.dao.CollectBhmStorageInfoMapper;
import com.jcca.web2.entity.CollectBhmStorageControllersInfo;
import com.jcca.web2.entity.CollectBhmStorageDiskInfo;
import com.jcca.web2.entity.CollectBhmStorageInfo;
import com.jcca.web2.service.CollectBhmStorageControllersInfoService;
import com.jcca.web2.service.CollectBhmStorageDiskInfoService;
import com.jcca.web2.service.CollectBhmStorageInfoService;
import org.apache.catalina.Store;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
public class CollectBhmStorageInfoServiceImpl extends ServiceImpl<CollectBhmStorageInfoMapper, CollectBhmStorageInfo> implements CollectBhmStorageInfoService {

    @Resource
    private CollectBhmStorageDiskInfoService diskInfoService;
    @Resource
    private CollectBhmStorageControllersInfoService controllersInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetPcieInfoBatch(List<CollectBhmStorageEntity> infoList) {
        if(Objects.isNull(infoList) || infoList.isEmpty()){
            return ;
        }
        List<CollectBhmStorageInfo> storageList = new ArrayList<>();
        List<CollectBhmStorageDiskInfo> storageDiskList = new ArrayList<>();
        for (CollectBhmStorageEntity item : infoList) {
            String storageId = MyIdUtil.getId();
            CollectBhmStorageInfo storage = new CollectBhmStorageInfo();
            storage.setId(storageId);
            storage.setAssetId(item.getAssetId());

            storage.setNumberId(item.getId());
            storage.setName(item.getName());

            storage.setCollectCode(item.getCollectCode());
            storage.setCreateTime(new Date());

            storageList.add(storage);

            List<ReadFishDiskEntity> diskInfos = item.getDiskInfos();
            if(Objects.nonNull(diskInfos)){
                for (ReadFishDiskEntity diskInfo : diskInfos) {
                    ReadFishStatusEntity status = diskInfo.getStatus();
                    CollectBhmStorageDiskInfo disk = EntityBeanUtil.copy(diskInfo, CollectBhmStorageDiskInfo.class);

                    if(Objects.nonNull(status)){
                        disk.setHealth(status.getHealth());
                        disk.setState(status.getState());
                    }
                    disk.setId(MyIdUtil.getId());
                    disk.setStorageId(storageId);
                    disk.setAssetId(item.getAssetId());
                    disk.setCreateTime(new Date());

                    storageDiskList.add(disk);
                }
            }

            List<ReadFishStorageControllersEntity> storageControllers = item.getStorageControllers();
            if(Objects.nonNull(storageControllers)){
                for (ReadFishStorageControllersEntity controllerInfo : storageControllers) {
                    ReadFishStatusEntity status = controllerInfo.getStatus();
                    CollectBhmStorageControllersInfo copy = EntityBeanUtil.copy(status, CollectBhmStorageControllersInfo.class);

                }
            }


        }


    }


}
