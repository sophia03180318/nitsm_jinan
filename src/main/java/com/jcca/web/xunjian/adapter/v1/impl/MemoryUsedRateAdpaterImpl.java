package com.jcca.web.xunjian.adapter.v1.impl;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.adapter.v1.util.TemplateUtil;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * CPU使用率巡检
 *
 * @author Lvyp
 */
@Service
public class MemoryUsedRateAdpaterImpl implements XunJianAdapter {

    private static final List<Integer> NEED_MODE = Arrays.asList(183, 42, 201);

    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private CollectMemoryService collectmemServ;
    @Resource
    private AssetService assetServ;

    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        detail.setXunjianTargetItem(getCode());
        ThresholdAssetVo threshold = thresholdAssetService.findAssetThreshold(asset.getAssetId());
        List<CollectMemory> assetmemory = collectmemServ.getRealTimeData(asset.getAssetId());

        Asset itsmAsset = assetServ.getById(asset.getAssetId());

        if (!NEED_MODE.contains(itsmAsset.getAssetMode())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备类型无此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        if (Objects.isNull(threshold) || Objects.isNull(threshold.getMemory())) {
            threshold = new ThresholdAssetVo();
            threshold.setMemory(90d);
        }
        Double usedRate = 10d;
        if (!assetmemory.isEmpty()) {
            CollectMemory memory = assetmemory.get(0);
            usedRate = memory.getMemUsedRate();

        }

        detail.setThresholdValue(threshold.getMemory().toString());
        if (usedRate > threshold.getMemory()) {
            detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
        } else {
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        }

        detail.setXunJianValue(usedRate.toString());
        detail.setResultMsg(TemplateUtil.getThresholdTemp(threshold.getMemory(), usedRate, false, true));

        return detail;
    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.MEMORY_RATE.getCode();
    }

}
