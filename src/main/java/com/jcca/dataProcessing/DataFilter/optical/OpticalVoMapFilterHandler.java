package com.jcca.dataProcessing.DataFilter.optical;

import com.jcca.component.thresholds.bean.OpticalVo;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.OpticalSwitchEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Zhaozheng
 * @description TODO 光纤交换机光功率过滤处理类
 * @className opticalVoMapFilterHandler
 * @date 2023/10/27 9:49
 * @since 2.1.0.0
 */
@Component("opticalVoMapFilterHandler")
public class OpticalVoMapFilterHandler extends IFilterHandler<OpticalSwitchEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(OpticalSwitchEntity info) {

        Map<String, OpticalVo> VoMap = info.getOpticalVoMap();
        if (Objects.isNull(VoMap)) {
            return true;
        }

        Set<String> list = VoMap.keySet();
        for (String item : list) {

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_interface.getCode() + ":" + item;
            String mapKey = item + "_" + StatusInfoChangeTypeEnum.status_interface_txPower.getCode();
            String mapKey1 = item + "_" + StatusInfoChangeTypeEnum.status_interface_rxPower.getCode();

            OpticalVo opticalVo = VoMap.get(item);
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, opticalVo.getTxPower());
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(opticalVo.getTxPower());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                info.getMaps().put(mapKey, changeInfo);
            }

            boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, opticalVo.getRxPower());
            if (flag1) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(opticalVo.getRxPower());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey1);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                info.getMaps().put(mapKey1, changeInfo);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
