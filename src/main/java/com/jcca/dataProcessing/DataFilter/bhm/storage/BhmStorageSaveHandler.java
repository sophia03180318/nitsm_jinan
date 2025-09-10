package com.jcca.dataProcessing.DataFilter.bhm.storage;

import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.service.CollectBhmStorageInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component("bhmStorageSaveHandler")
public class BhmStorageSaveHandler extends IFilterHandler<CollectBhmStorageEntity> {


    @Resource
    private CollectBhmStorageInfoService collectBhmStorageInfoService;


    @Override
    public boolean handler(CollectBhmStorageEntity info) throws ResultException, Exception {

        if(Objects.isNull(info)){
            return false;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口存储数据", info.getAssetIp());

        collectBhmStorageInfoService.updateAssetStorageInfo(info);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
