package com.jcca.dataProcessing.DataFilter.net;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
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
import java.util.Objects;

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
    @Resource
    private RedisService redisService;

    @Override
    public synchronized boolean handler(CollectNetworkCardEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "保存网卡信息", info.getAssetIp());

        Date date = new Date();
        date.setTime(info.getCollectTime());

        CollectNetworkCard net = EntityBeanUtil.copy(info, CollectNetworkCard.class);

        if(StrUtil.isEmpty(net.getIp())||net.getIp().equals(DEFAULT_VALUE_STR)){
            String key =info.getAssetIp()+":"+info.getAssetId()+":status:net:"+info.getName();
            Object ip = redisService.hmGet(key, "ip");
            if(Objects.nonNull(ip) && !DEFAULT_VALUE_STR.equals(ip.toString())){
                net.setIp(ip.toString());
            }
        }

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
