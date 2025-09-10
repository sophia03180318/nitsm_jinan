package com.jcca.dataProcessing.DataFilter.bhm.pcie;

import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmPcieEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.entity.CollectBhmPcieInfo;
import com.jcca.web2.service.CollectBhmPcieInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Component("bhmPcieSaveHandler")
public class BhmPcieSaveHandler extends IFilterHandler<CollectBhmPcieEntity> {


    @Resource
    private CollectBhmPcieInfoService bhmPcieInfoService;


    @Override
    public boolean handler(CollectBhmPcieEntity item) throws ResultException, Exception {
        if(Objects.isNull(item)){
            return false;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口PCIE数据", item.getAssetIp());
        ReadFishStatusEntity status = item.getStatus();
        CollectBhmPcieInfo copy = EntityBeanUtil.copy(item, CollectBhmPcieInfo.class);
        if(Objects.nonNull(status)){
            copy.setHealth(status.getHealth());
            copy.setState(status.getState());
        }
        copy.setId(MyIdUtil.getId());
        copy.setPcieId(item.getId());
        copy.setAssetId(item.getAssetId());
        copy.setCreateTime(new Date());

        bhmPcieInfoService.updateAssetPcieInfo(copy);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
