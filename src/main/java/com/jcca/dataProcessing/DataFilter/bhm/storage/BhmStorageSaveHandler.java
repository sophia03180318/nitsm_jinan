package com.jcca.dataProcessing.DataFilter.bhm.storage;

import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmPowerEntity;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.entity.CollectBhmPowerInfo;
import com.jcca.web2.service.CollectBhmPowerInfoService;
import com.jcca.web2.service.CollectBhmStorageInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("bhmStorageSaveHandler")
public class BhmStorageSaveHandler extends IFilterHandler<List<CollectBhmStorageEntity>> {


    @Resource
    private CollectBhmStorageInfoService collectBhmStorageInfoService;


    @Override
    public boolean handler(List<CollectBhmStorageEntity> infoList) throws ResultException, Exception {

        if(infoList.isEmpty()){
            return false;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口存储数据", infoList.get(0).getAssetIp());

        collectBhmStorageInfoService.updateAssetPcieInfoBatch(infoList);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
