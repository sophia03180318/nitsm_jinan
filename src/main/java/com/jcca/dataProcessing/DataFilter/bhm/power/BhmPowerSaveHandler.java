package com.jcca.dataProcessing.DataFilter.bhm.power;


import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmPowerEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;

import com.jcca.web2.entity.CollectBhmPowerInfo;
import com.jcca.web2.service.CollectBhmPowerInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("bhmPowerSaveHandler")
public class BhmPowerSaveHandler  extends IFilterHandler<List<CollectBhmPowerEntity>> {

    @Resource
    private CollectBhmPowerInfoService collectBhmPowerInfoService;


    @Override
    public boolean handler(List<CollectBhmPowerEntity> infoList) throws ResultException, Exception {
        if(infoList.isEmpty()){
            return false;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口电源数据", infoList.get(0).getAssetIp());

        List<CollectBhmPowerInfo> saveList = new ArrayList<>();
        for (CollectBhmPowerEntity item : infoList) {
            ReadFishStatusEntity status = item.getStatus();
            CollectBhmPowerInfo copy = EntityBeanUtil.copy(item, CollectBhmPowerInfo.class);
            if(Objects.nonNull(status)){
                copy.setHealth(status.getHealth());
                copy.setState(status.getState());
            }
            copy.setId(MyIdUtil.getId());
            copy.setAssetId(item.getAssetId());
            copy.setCreateTime(new Date());
            if(Objects.nonNull(item.getPresent())){
                copy.setPresent(item.getPresent()?"已安装":"未安装");
            }

            saveList.add(copy);
        }



        collectBhmPowerInfoService.updateAssetPcieInfoBatch(saveList);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
