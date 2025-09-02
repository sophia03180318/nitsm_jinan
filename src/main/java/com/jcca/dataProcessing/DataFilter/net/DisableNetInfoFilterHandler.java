package com.jcca.dataProcessing.DataFilter.net;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectNetworkCardEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 过滤网卡
 * @className DisableNetInfoFilterHandler
 * @date 2023/12/21 18:30
 * @since 2.1.0.0
 */
@Component("disableNetInfoFilterHandler")
public class DisableNetInfoFilterHandler extends IFilterHandler<CollectNetworkCardEntity> {

    @Resource
    private AssetHidConfService hidConfServ;

    @Override
    public boolean handler(CollectNetworkCardEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "查询网卡是否配置了禁用", info.getAssetIp());
        //查询网卡是否配置了禁用
        QueryWrapper<AssetHidConf> queryWrapper = new QueryWrapper<AssetHidConf>();
        queryWrapper.eq("asset_id", info.getAssetId());
        queryWrapper.eq("flag", info.getName());
        queryWrapper.eq("type", AssetHidConf.TypeEnum.NET_CARD.name());
        List<AssetHidConf> list = hidConfServ.list(queryWrapper);
        return list == null || list.isEmpty();
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
