package com.jcca.dataProcessing.DataFilter.net;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectNetworkCardEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.CollectNetworkCardService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 网卡信息保存类
 * @className NetStateFilterHandler
 * @date 2023/10/27 9:45
 * @since 2.1.0.0
 */
@Component("netSaveFilterHandler")
public class NetSaveFilterHandler extends IFilterHandler<CollectNetworkCardEntity> {

    public static final String DEFAULT_VALUE_STR = "--";

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private CollectNetworkCardService networkService;

    @Override
    public boolean handler(CollectNetworkCardEntity info) {
        //过滤掉IP地址为空的网卡 同时组的还要上
        if (StrUtil.isEmpty(info.getIp()) || DEFAULT_VALUE_STR.equals(info.getIp())) {
            Object stateValue = eventInfoChangeManagerService.getStateValue(info.getAssetIp() + ":" + info.getAssetId() + ":"
                    + StatusInfoChangeTypeEnum.status_net.getCode() + ":" + info.getName(), StatusInfoChangeTypeEnum.status_net_ip.getCode());
            if (!StringUtils.isEmpty(stateValue)) {
                info.setIp(stateValue.toString());
                info.setStatus((byte) 2);
            } else if (!StringUtils.isEmpty(info.getBondType())) {
                // 双网卡绑定的网卡没有IP
                info.setName(info.getName() + info.getBondType());
            } else if (!info.getName().contains("组") || info.getName().contains("WFP") || info.getName().contains("QoS")) {
                //过滤掉名字不包含组，或包含组 含有WFP、QoS的
                return false;
            }
        }

        Date date = new Date();
        date.setTime(info.getCollectTime());

        CollectNetworkCard net = EntityBeanUtil.copy(info, CollectNetworkCard.class);
        net.setId(MyIdUtil.getId());
        net.setCollectTime(date);
        net.setCollectCode(info.getCollectCode());
        if (StrUtil.isNotEmpty(net.getMacAddress())) {
            net.setMacAddress(net.getMacAddress().toLowerCase());
        }
        // 批量更新采集数据
        networkService.updateByAssetId(net);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
