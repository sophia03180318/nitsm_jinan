package com.jcca.web.xunjian.adapter.v1.impl;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.service.CollectCpuService;
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
public class CpuUsedRateAdpaterImpl implements XunJianAdapter {

    private static final List<Integer> NEED_CPU_MODE = Arrays.asList(183, 42, 201);

    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private CollectCpuService collectCpuServ;
    @Resource
    private AssetService assetServ;

    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        detail.setXunjianTargetItem(getCode());
        ThresholdAssetVo threshold = thresholdAssetService.findAssetThreshold(asset.getAssetId());
        List<CollectCpu> assetCpu = collectCpuServ.getRealTimeData(asset.getAssetId());
        Asset itsmAsset = assetServ.getById(asset.getAssetId());

        if (!NEED_CPU_MODE.contains(itsmAsset.getAssetMode())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备类型无此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        if (Objects.isNull(threshold) || Objects.isNull(threshold.getCpu())) {
            threshold = new ThresholdAssetVo();
            threshold.setCpu(90d);
        }

        Double cpuUsedUate = 10d;
        if (!assetCpu.isEmpty()) {
            CollectCpu collectCpu = assetCpu.get(0);
            cpuUsedUate = collectCpu.getCpuUsedRate();
        }

        detail.setThresholdValue(threshold.getCpu().toString());
        if (cpuUsedUate > threshold.getCpu()) {
            detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
        } else {
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        }

        detail.setXunJianValue(cpuUsedUate.toString());
        detail.setResultMsg(TemplateUtil.getThresholdTemp(threshold.getCpu(), cpuUsedUate, false, true));

        return detail;
    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.CPU_USED_RATE.getCode();
    }

}
