package com.jcca.dataProcessing.DataFilter.bhm.memory;

import com.jcca.common.exception.ResultException;

import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectBhmMemoryEntity;
import com.jcca.dataProcessing.Entity.ReadFishStatusEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.entity.CollectBhmMemoryInfo;
import com.jcca.web2.service.CollectBhmMemoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Component("bhmMemorySaveHandler")
public class BhmMemorySaveHandler  extends IFilterHandler<List<CollectBhmMemoryEntity>> {

    @Resource
    private CollectBhmMemoryService collectBhmMemoryService;

    @Override
    public boolean handler(List<CollectBhmMemoryEntity> info) throws ResultException, Exception {
        if(info.isEmpty()){
            return false;
        }
        List<CollectBhmMemoryInfo> infoList = new ArrayList<>();
        for (CollectBhmMemoryEntity item : info) {
            CollectBhmMemoryInfo copy = EntityBeanUtil.copy(item, CollectBhmMemoryInfo.class);
            ReadFishStatusEntity status = item.getStatus();
            if(Objects.nonNull(status)){
                copy.setHealth(status.getHealth());
                copy.setState(status.getState());
            }
            copy.setId(MyIdUtil.getId());
            copy.setMemoryId(item.getId());
            copy.setAssetId(item.getAssetId());
            copy.setCreateTime(new Date());

            List<Integer> allowedSpeedsMHz = item.getAllowedSpeedsMHz();
            if(Objects.nonNull(allowedSpeedsMHz) && allowedSpeedsMHz.size()>0){
                copy.setAllowedSpeedsMHz(allowedSpeedsMHz.toString());
            }
            if(Objects.nonNull(item.getIsRankSpareEnabled())){
                copy.setIsRankSpareEnabled(item.getIsRankSpareEnabled()?"已启用":"未启用");
            }
            if(Objects.nonNull(item.getIsSpareDeviceEnabled())){
                copy.setIsSpareDeviceEnabled(item.getIsSpareDeviceEnabled()?"已启用":"未启用");
            }
            infoList.add(copy);
        }

        collectBhmMemoryService.updateAssetFanInfoBatch(infoList);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
