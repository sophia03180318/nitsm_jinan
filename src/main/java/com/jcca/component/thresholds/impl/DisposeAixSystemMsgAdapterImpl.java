package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.UnitEnum;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectAixSystemFattenBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 处理AIX系统数据
 *
 * @author lyp
 */
@Slf4j
@Service
public class DisposeAixSystemMsgAdapterImpl implements CollectAdapter {

    @Resource
    private AssetService assetServ;

    @Override
    public void dispose(JSONArray data) {
        List<CollectAixSystemFattenBean> beanList = JSONUtil.toList(data, CollectAixSystemFattenBean.class);

        if (beanList.isEmpty()) {
            log.error("AIX 数据处理失败，空的序列集合");
            return;
        }

        CollectAixSystemFattenBean aixSystemMsg = beanList.get(0);
        String assetId = aixSystemMsg.getAssetId();

        if (StrUtil.isEmpty(assetId)) {
            log.error("AIX 数据处理失败，缺少资产ID");
            return;
        }
        Asset asset = assetServ.getById(assetId);

        if (Objects.isNull(asset)) {
            log.error("AIX 数据处理失败，资产不存在，ID：{}", assetId);
            return;
        }

        if (StrUtil.isEmpty(asset.getSerialNumber())) {
            asset.setSerialNumber(aixSystemMsg.getSerial());
        }
        if (StrUtil.isNotEmpty(aixSystemMsg.getPowerModel())) {
            asset.setPowerModel(aixSystemMsg.getPowerModel());
        }
        if (StrUtil.isNotEmpty(aixSystemMsg.getFrequency())) {
            asset.setCpuFrequency(aixSystemMsg.getFrequency());
        }
        if (Objects.nonNull(aixSystemMsg.getCpuCoreNum())) {
            asset.setCpuCoreNumber(aixSystemMsg.getCpuCoreNum());
        }
        if (Objects.nonNull(aixSystemMsg.getPowerNum())) {
            asset.setPowerTotal(aixSystemMsg.getPowerNum());
        }
        if (Objects.nonNull(aixSystemMsg.getDiskNum())) {
            asset.setDiskTotal(aixSystemMsg.getDiskNum());
        }
        if (Objects.nonNull(aixSystemMsg.getCpuNum())) {
            asset.setCpuNumber(aixSystemMsg.getCpuNum());
        }
        if (StrUtil.isNotEmpty(aixSystemMsg.getCpuMode())) {
            asset.setCpuModel(aixSystemMsg.getCpuMode());
        }
        if (StrUtil.isNotEmpty(aixSystemMsg.getSystemVersion())) {
            asset.setOperationSystem(aixSystemMsg.getSystemVersion());
        }
        if (Objects.nonNull(aixSystemMsg.getMemory())) {
            asset.setMemory(UnitEnum.autoScaleKb(aixSystemMsg.getMemory()));
        }
        if (Objects.nonNull(aixSystemMsg.getDiskCapacityCount())) {
            BigDecimal bigDecimal = new BigDecimal(aixSystemMsg.getDiskCapacityCount());
            BigDecimal divide = bigDecimal.divide(new BigDecimal(1024), 0, BigDecimal.ROUND_HALF_UP);
            asset.setDiskCapacity(divide.longValue() + "G");
        }

        assetServ.updateById(asset);
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.AIX_SYSTEM_MSG;
    }

}
