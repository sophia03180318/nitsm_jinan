package com.jcca.dataProcessing.DataFilter.bhm.pcie;

import com.jcca.common.exception.ResultException;
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



@Component("bhmPcieSaveHandler")
public class BhmPcieSaveHandler extends IFilterHandler<List<CollectBhmPcieEntity>> {


    @Resource
    private CollectBhmPcieInfoService bhmPcieInfoService;


    @Override
    public boolean handler(List<CollectBhmPcieEntity> infoList) throws ResultException, Exception {
        if(infoList.isEmpty()){
            return false;
        }

        List<CollectBhmPcieInfo> saveList = new ArrayList<>();
        for (CollectBhmPcieEntity item : infoList) {
            ReadFishStatusEntity status = item.getStatus();

            CollectBhmPcieInfo copy = EntityBeanUtil.copy(item, CollectBhmPcieInfo.class);
            copy.setHealth(status.getHealth());
            copy.setState(status.getState());
            copy.setId(MyIdUtil.getId());
            copy.setPcieId(item.getId());
            copy.setAssetId(item.getAssetId());
            copy.setCreateTime(new Date());

            saveList.add(copy);
        }



        bhmPcieInfoService.updateAssetPcieInfoBatch(saveList);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
