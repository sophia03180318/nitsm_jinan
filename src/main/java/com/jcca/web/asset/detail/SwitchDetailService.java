package com.jcca.web.asset.detail;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.entity.Asset;
import org.springframework.stereotype.Service;

/**
 * @ClassName SwitchDetailService
 * @Description 交换机详情处理器
 * @Date 2020/6/22 13:56
 * @Author hanwone
 */
@Service
public class SwitchDetailService implements DetailAdapter {

    @Override
    public String getCode() {
        return String.valueOf(AssetModeConst.SWITCH);
    }

    @Override
    public ResultVo handle(Asset asset) {
        /**
         * 交换机详情使用路由器详情处理器 RouterDetailService
         */
        RouterDetailService routerDetailService = SpringContextUtil.getBean(RouterDetailService.class);
        return ResultVoUtil.success(routerDetailService.handle(asset).getData());
    }

    @Override
    public ResultVo getAssetGeneralInfo(Asset asset) {
        RouterDetailService routerDetailService = SpringContextUtil.getBean(RouterDetailService.class);
        return routerDetailService.getAssetGeneralInfo(asset);
    }
}
