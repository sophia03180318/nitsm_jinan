package com.jcca.dataProcessing.DataFilter.aix;


import cn.hutool.core.util.StrUtil;
import com.jcca.common.enums.UnitEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectAixSystemFattenEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * IFilterHandler处理流程作为数据处理类
 * IFilterHandler-------》IFilterHandler-------》IFilterHandler-------》支持组合式处理。
 * handler返回true，则到下一个IFilterHandler处理，返回false则无需下一个IFilterHandler进行处理
 * 如果有事件变动 ，使用this.dispatureEvent(event)将事件抛出，有其他监听器进行处理
 */

/**
 * @author Zhaozheng
 * @description TODO 小机信息存储
 * @className AixSystemMsgSaveIFilterHandlerHandler
 * @date 2023/10/27 9:21
 * @since 2.1.0.0
 */
@Slf4j
@Component("aixSystemMsgSaveIFilterHandlerHandler")
public class AixSystemMsgSaveIFilterHandlerHandler extends IFilterHandler<CollectAixSystemFattenEntity> {

    @Resource
    private AssetService assetServ;

    @Override
    public boolean handler(CollectAixSystemFattenEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "小型机基础信息数据保存", info.getAssetIp());
        String assetId = info.getAssetId();

        if (StrUtil.isEmpty(assetId)) {
            log.error("AIX 数据处理失败，缺少资产ID");
            return true;
        }
        Asset asset = assetServ.getById(assetId);

        if (Objects.isNull(asset)) {
            log.error("AIX 数据处理失败，资产不存在，ID：{}", assetId);
            return true;
        }

        if (StrUtil.isNotEmpty(info.getSerial())) {
            asset.setSerialNumber(info.getSerial());
        }
        if (StrUtil.isNotEmpty(info.getPowerModel())) {
            asset.setPowerModel(info.getPowerModel());
        }
        if (StrUtil.isNotEmpty(info.getFrequency())) {
            asset.setCpuFrequency(info.getFrequency());
        }
        if (Objects.nonNull(info.getCpuCoreNum())) {
            asset.setCpuCoreNumber(info.getCpuCoreNum());
        }
        if (Objects.nonNull(info.getPowerNum())) {
            asset.setPowerTotal(info.getPowerNum());
        }
        if (Objects.nonNull(info.getDiskNum())) {
            asset.setDiskTotal(info.getDiskNum());
        }
        if (Objects.nonNull(info.getCpuNum())) {
            asset.setCpuNumber(info.getCpuNum());
        }
        if (StrUtil.isNotEmpty(info.getCpuMode())) {
            asset.setCpuModel(info.getCpuMode());
        }
        if (StrUtil.isNotEmpty(info.getSystemVersion())) {
            asset.setOperationSystem(info.getSystemVersion());
        }
        if (Objects.nonNull(info.getMemory())) {
            asset.setMemory(UnitEnum.autoScaleKb(info.getMemory()));
        }
        if (Objects.nonNull(info.getDiskCapacityCount())) {
            BigDecimal bigDecimal = new BigDecimal(info.getDiskCapacityCount());
            BigDecimal divide = bigDecimal.divide(new BigDecimal(1024), 0, BigDecimal.ROUND_HALF_UP);
            asset.setDiskCapacity(divide.longValue() + "G");
        }

        assetServ.updateById(asset);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
