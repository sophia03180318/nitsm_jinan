package com.jcca.dataProcessing.DataFilter.net;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectNetworkCardEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.CollectNetworkCardService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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
    private CollectNetworkCardService networkService;

    @Override
    public synchronized boolean handler(CollectNetworkCardEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存网卡信息", info.getAssetIp());
        //过滤掉IP地址为空的网卡 同时组的还要上
        String ip = info.getIp();

        // 断网后有的网卡采集不到IP 使用原有IP
        if (DEFAULT_VALUE_STR.equals(ip)) {
            QueryWrapper<CollectNetworkCard> query = Wrappers.query();
            query.eq("ASSET_ID", info.getAssetId());
            query.eq("NAME", info.getName());
            List<CollectNetworkCard> list = networkService.list(query);
            if (!list.isEmpty()) {
                info.setIp(list.get(0).getIp());
            }
        }

        if (StrUtil.isEmpty(ip) || DEFAULT_VALUE_STR.equals(ip)) {
            if (!StringUtils.isEmpty(info.getBondType()) && !info.getName().contains("WFP") && !info.getName().contains("QoS")) {
                // 标记行
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
