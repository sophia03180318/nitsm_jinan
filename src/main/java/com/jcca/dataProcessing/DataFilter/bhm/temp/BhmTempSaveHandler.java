package com.jcca.dataProcessing.DataFilter.bhm.temp;

import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmTempEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.entity.CollectBhmTempInfo;
import com.jcca.web2.service.CollectBhmTempInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("bhmTempSaveHandler")
public class BhmTempSaveHandler extends IFilterHandler<CollectBhmTempEntity> {


    @Resource
    private CollectBhmTempInfoService collectBhmService;


    @Override
    public boolean handler(CollectBhmTempEntity collectBhmTempEntity) throws ResultException, Exception {
        if(Objects.isNull(collectBhmTempEntity)){
            return false;
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口温度数据", collectBhmTempEntity.getAssetIp());

        CollectBhmTempInfo copy = EntityBeanUtil.copy(collectBhmTempEntity, CollectBhmTempInfo.class);
        ReadFishStatusEntity status = collectBhmTempEntity.getStatus();

        copy.setHealth(status.getHealth());
        copy.setState(status.getState());
        copy.setId(MyIdUtil.getId());
        copy.setCreateTime(new Date());

        collectBhmService.updateAssetTempInfo(copy);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
