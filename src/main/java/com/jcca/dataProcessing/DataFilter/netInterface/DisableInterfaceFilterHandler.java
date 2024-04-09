package com.jcca.dataProcessing.DataFilter.netInterface;

import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zhaozheng
 * @description TODO
 * @className DisableInterfaceFilterHandler
 * @date 2023/12/20 18:49
 * @since 2.1.0.0
 */
@Component("disableInterfaceFilterHandler")
public class DisableInterfaceFilterHandler extends IFilterHandler<CollectInterfaceEntity> {
    @Resource
    private AssetHidConfService hidConfServ;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectInterfaceEntity info) throws ResultException {

        List<String> portNameList = hidConfServ.getFlagListByAsset(info.getAssetId(), AssetHidConf.TypeEnum.PORT.name()).stream().map(AssetHidConf::getFlag).collect(Collectors.toList());
        //如果此端口配置了未监控
        if (portNameList.contains(info.getPortName())) {
            String eventRedisKey = StatusInfoChangeTypeEnum.event_port_state.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getPortName();
            //清楚相关的端口事件信息
            eventInfoChangeManagerService.delStateValue(eventRedisKey, eventMapKey);
            throw new ResultException(ResultEnum.dataProcess_interface_interrupter);
        } else {
            return true;
        }


    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}
