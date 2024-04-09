package com.jcca.web.xunjian.adapter.v1.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


/**
 * 软件进程运行状态
 *
 * @author Lvyp
 */
@Service
public class SoftwareRunnerStatusApaterImpl implements XunJianAdapter {

    private static final List<Integer> NEED_MODE = Arrays.asList(183);

    @Resource
    private AssetService assetServ;
    @Resource
    private ThresholdProcessService thresholdProcessServ;


    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        Asset itsmAsset = assetServ.getById(asset.getAssetId());

        if (!NEED_MODE.contains(itsmAsset.getAssetMode())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备类型无此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        Byte no = 0;
        QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
        queryWrapper.eq("ASSET_ID", asset.getAssetId());
        queryWrapper.eq("COLLECT_STATUS", no);
        List<ThresholdProcess> list = thresholdProcessServ.list(queryWrapper);

        QueryWrapper<ThresholdProcess> queryWrapper2 = new QueryWrapper<ThresholdProcess>();
        queryWrapper2.eq("ASSET_ID", asset.getAssetId());
        List<ThresholdProcess> list2 = thresholdProcessServ.list(queryWrapper2);

        detail.setXunJianValue("");
        detail.setThresholdValue("");
        if (list.isEmpty()) {
            if (list2.isEmpty()) {
                detail.setResultMsg("该设备未配置被监控进程");
            } else {
                detail.setResultMsg("该设备进程状态正常");
            }
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        } else {
            detail.setResultMsg("设备存在异常状态进程");
            detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
        }

        return detail;
    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.PROCESS_RUN_STATUS.getCode();
    }

}
