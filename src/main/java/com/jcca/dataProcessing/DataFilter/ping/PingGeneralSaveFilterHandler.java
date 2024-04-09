package com.jcca.dataProcessing.DataFilter.ping;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetStatusEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 非组ping过滤处理类,普通单个ping保存
 * @className PingNoGroupFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pingGeneralSaveFilterHandler")
public class PingGeneralSaveFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    @Resource
    private AssetService assetServ;

    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        Asset asset = null;
        if(StrUtil.isEmpty(info.getAssetId())){
            asset = assetServ.findOneByIp(info.getAssetIp());
        }else{
            asset = assetServ.getById(info.getAssetId());
        }

        if(Objects.isNull(asset)){
            AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS,info,"资产不存在");
            return true ;
        }
        asset.setStatus(info.getFlag() ? AssetStatusEnum.ASSET_STATUS_ONLINE.getCode() : AssetStatusEnum.ASSET_STATUS_OFFLINE.getCode());
        assetServ.updateById(asset);
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
