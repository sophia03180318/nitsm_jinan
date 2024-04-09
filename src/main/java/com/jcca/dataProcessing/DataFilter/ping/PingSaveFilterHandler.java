package com.jcca.dataProcessing.DataFilter.ping;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.component.other.bean.PingAssetStatus;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetStatusEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 非组ping过滤处理类
 * @className PingNoGroupFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pingSaveFilterHandler")
public class PingSaveFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    @Resource
    private AssetService assetServ;

    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        String content = info.getContent();
        if (StrUtil.isEmpty(content) || !JSONUtil.isJsonArray(content)) {
            //异常数据
            return true;
        }
        List<PingAssetStatus> pingAssetStatusList = JSONUtil.toList(JSONUtil.parseArray(content),
                PingAssetStatus.class);
        if (Objects.nonNull(pingAssetStatusList) && pingAssetStatusList.size() > 1) {
            //组
            for (PingAssetStatus pingAssetStatus : pingAssetStatusList) {
                Asset asset = pingAssetStatus.getAsset();
                Asset update = new Asset();
                update.setId(asset.getId());
                update.setStatus(pingAssetStatus.getCurrStatus() ? AssetStatusEnum.ASSET_STATUS_ONLINE.getCode() : AssetStatusEnum.ASSET_STATUS_OFFLINE.getCode());
                assetServ.updateById(update);
            }
            return true;
        }
        if (Objects.nonNull(pingAssetStatusList) && pingAssetStatusList.size() == 1) {
            PingAssetStatus pingAssetStatus = pingAssetStatusList.get(0);
            info.setAssetIp(pingAssetStatus.getAsset().getIp());
            info.setFlag(pingAssetStatus.getCurrStatus());
            info.setAssetId(pingAssetStatus.getAsset().getId());
        }
        Asset asset = assetServ.getById(info.getAssetId());
        Asset update = new Asset();
        update.setId(asset.getId());
        update.setStatus(info.getFlag() ? AssetStatusEnum.ASSET_STATUS_ONLINE.getCode() : AssetStatusEnum.ASSET_STATUS_OFFLINE.getCode());
        assetServ.updateById(update);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
