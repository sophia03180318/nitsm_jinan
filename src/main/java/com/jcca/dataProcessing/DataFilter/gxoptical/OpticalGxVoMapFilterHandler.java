package com.jcca.dataProcessing.DataFilter.gxoptical;

import com.jcca.component.thresholds.bean.OpticalSwitchV2Bean;
import com.jcca.component.thresholds.bean.OpticalVo;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 光纤交换机光功率过滤处理类
 */
@Component("opticalGxVoMapFilterHandler")
public class OpticalGxVoMapFilterHandler extends IFilterHandler<OpticalSwitchV2Bean> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(OpticalSwitchV2Bean info) {
        List<OpticalVo> opticalVoMap = info.getOpticalVoMap();

        if (null == opticalVoMap) return true;

        for (OpticalVo item : opticalVoMap) {

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_interface.getCode() + ":" + item;
            String mapKey = item + "_" + StatusInfoChangeTypeEnum.status_interface_txPower.getCode();
            String mapKey1 = item + "_" + StatusInfoChangeTypeEnum.status_interface_rxPower.getCode();

            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(), redisKey, mapKey, item.getTxPower());
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(item.getTxPower());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                info.getMaps().put(mapKey, changeInfo);
            }

            boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(), redisKey, mapKey1, item.getRxPower());
            if (flag1) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(item.getRxPower());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey1);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                info.getMaps().put(mapKey1, changeInfo);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
