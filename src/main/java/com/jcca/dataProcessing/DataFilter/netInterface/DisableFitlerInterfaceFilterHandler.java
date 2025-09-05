package com.jcca.dataProcessing.DataFilter.netInterface;

import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.service.AssetHidConfService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO
 * @className disableFitlerInterfaceFilterHandler 过滤掉虚拟口，其他系统没有用到的端口
 * @date 2023/12/20 18:49
 * @since 2.1.0.0
 */
@Component("disableFitlerInterfaceFilterHandler")
public class DisableFitlerInterfaceFilterHandler extends IFilterHandler<CollectInterfaceEntity> {
    @Resource
    private AssetHidConfService hidConfServ;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    /**
     * 6 以太网的接口
     * 18 	是Ethernet接口，叫以太网接口，
     * 22  专有的串行,路由器和路由器连接时候用的
     * 23   	ppp
     * 339   	rs232
     * 56         Fibre Channel
     *
     * @param info
     * @return
     * @throws ResultException
     */
    @Override
    public boolean handler(CollectInterfaceEntity info) throws ResultException {
        switch (info.getPortType()) {
            case 6:
            case 18:
            case 22:
            case 23:
            case 339:
            case 56:
                return true;
            default:
                throw new ResultException(ResultEnum.dataProcess_interface_interrupter,String.format("设备【%s】,端口【%s】",info.getAssetIp(),info.getPortName()));
        }
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
