package com.jcca.dataProcessing.DataFilter.bhm.fan;


import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;

import com.jcca.dataProcessing.Entity.CollectBhmFanEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;

import com.jcca.web2.entity.CollectBhmFanInfo;
import com.jcca.web2.service.CollectBhmFanInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Component("bhmFanSaveHandler")
public class BhmFanSaveHandler extends IFilterHandler<List<CollectBhmFanEntity>> {

    @Resource
    private CollectBhmFanInfoService collectBhmFanInfoService;

    @Override
    public boolean handler(List<CollectBhmFanEntity> infoList) throws ResultException, Exception {
        if(infoList.isEmpty()){
            return false;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存管理口Fan数据", infoList.get(0).getAssetIp());
        List<CollectBhmFanInfo> saveList = new ArrayList<>();

        for (CollectBhmFanEntity info : infoList) {
            CollectBhmFanInfo copy = EntityBeanUtil.copy(info, CollectBhmFanInfo.class);

            ReadFishStatusEntity status = info.getStatus();
            if(Objects.nonNull(status)){
                copy.setHealth(status.getHealth());
                copy.setState(status.getState());
            }
            copy.setId(MyIdUtil.getId());
            copy.setAssetId(info.getAssetId());
            copy.setCreateTime(new Date());

            saveList.add(copy);
        }

        collectBhmFanInfoService.updateAssetFanInfoBatch(saveList);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
