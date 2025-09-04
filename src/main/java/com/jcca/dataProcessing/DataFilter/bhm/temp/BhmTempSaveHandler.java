package com.jcca.dataProcessing.DataFilter.bhm.temp;

import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.dataProcessing.Entity.CollectBhmTempEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.entity.CollectBhmTempInfo;
import com.jcca.web2.service.CollectBhmStorageInfoService;
import com.jcca.web2.service.CollectBhmTempInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("bhmTempSaveHandler")
public class BhmTempSaveHandler extends IFilterHandler<List<CollectBhmTempEntity>> {


    @Resource
    private CollectBhmTempInfoService collectBhmService;


    @Override
    public boolean handler(List<CollectBhmTempEntity> infoList) throws ResultException, Exception {
        if(infoList.isEmpty()){
            return false;
        }
        List<CollectBhmTempInfo> saveList = new ArrayList<>();
        for (CollectBhmTempEntity collectBhmTempEntity : infoList) {
            CollectBhmTempInfo copy = EntityBeanUtil.copy(collectBhmTempEntity, CollectBhmTempInfo.class);
            ReadFishStatusEntity status = collectBhmTempEntity.getStatus();

            copy.setHealth(status.getHealth());
            copy.setState(status.getState());
            copy.setId(MyIdUtil.getId());
            copy.setCreateTime(new Date());
            saveList.add(copy);
        }
        collectBhmService.updateAssetPcieInfoBatch(saveList);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
