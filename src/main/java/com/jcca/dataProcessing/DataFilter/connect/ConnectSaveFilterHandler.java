package com.jcca.dataProcessing.DataFilter.connect;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectConnectEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectConnect;
import com.jcca.web.collect.service.CollectConnectService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 连接数过滤处理类
 * @className ConnectFitlerHandler
 * @date 2023/10/27 9:26
 * @since 2.1.0.0
 */
@Component("connectSaveFilterHandler")
public class ConnectSaveFilterHandler extends IFilterHandler<CollectConnectEntity> {

    @Resource
    private CollectConnectService collectConnectService;

    @Override
    public boolean handler(CollectConnectEntity info) {
        if (info.getEstablishedNum() == null) {
            return true;
        }

        Date date = new Date();
        date.setTime(Long.valueOf(info.getCollectTime()));
        CollectConnect connect = EntityBeanUtil.copy(info, CollectConnect.class);
        connect.setCollectTime(date);
        connect.setId(MyIdUtil.getId());
        connect.setEstablishedNum(info.getEstablishedNum());
        QueryWrapper<CollectConnect> wrapper = new QueryWrapper<>();
        wrapper.eq("ASSET_ID", info.getAssetId());
        collectConnectService.remove(wrapper);
        collectConnectService.save(connect);
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
