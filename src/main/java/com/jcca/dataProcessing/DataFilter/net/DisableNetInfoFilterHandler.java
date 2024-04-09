package com.jcca.dataProcessing.DataFilter.net;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
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
        //查询网卡是否配置了禁用
        QueryWrapper<AssetHidConf> queryWrapper = new QueryWrapper<AssetHidConf>();
        queryWrapper.eq("asset_id", info.getAssetId());
        queryWrapper.eq("flag", info.getName());
        queryWrapper.eq("type", AssetHidConf.TypeEnum.NET_CARD.name());
        List<AssetHidConf> list = hidConfServ.list(queryWrapper);
        if (list != null && list.size() >= 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
