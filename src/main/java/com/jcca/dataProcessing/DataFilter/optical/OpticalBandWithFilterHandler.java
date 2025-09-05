package com.jcca.dataProcessing.DataFilter.optical;

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
 * @description TODO 光纤交换机光口状态信息过滤处理类
 * @className OpticalInterfaceUpDownFilterHandler
 * @date 2023/10/27 9:47
 * @since 2.1.0.0
 */
@Component("opticalBandWithFilterHandler")
public class OpticalBandWithFilterHandler extends IFilterHandler<OpticalSwitchEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(OpticalSwitchEntity info) {

        Map<String, String> stateMap = info.getBandwidthMap();
        if (Objects.isNull(stateMap)) {
            return true;
        }


        Set<String> list = stateMap.keySet();
        for (String item : list) {
            String portName = "0/" + item;
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_interface.getCode() + ":" + portName;
            String mapKey = StatusInfoChangeTypeEnum.status_interface_optical_bandwith.getCode();
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, stateMap.get(item));
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(stateMap.get(item));
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                info.getMaps().put(portName + "_" + mapKey, changeInfo);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
