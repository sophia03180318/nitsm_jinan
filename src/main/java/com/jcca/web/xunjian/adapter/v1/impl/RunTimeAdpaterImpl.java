package com.jcca.web.xunjian.adapter.v1.impl;

import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ Author：sophia
 * @ Date：Created in 13:51 2021/11/23
 * @ Description:
 */
@Service
public class RunTimeAdpaterImpl implements XunJianAdapter {
    @Resource
    CollectSystemTimeService collectSystemTimeService;

    @Override
    public XunjianDetail xunJian(XunjianAsset asset, XunjianDetail detail) {
        detail.setXunjianTargetItem(getCode());
        detail.setXunJianValue("");
        detail.setThresholdValue("");
        detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
        Long runTime = collectSystemTimeService.getRunTime(asset.getAssetId());
        if (runTime != 0l) {
            detail.setXunJianValue(XunjianReportUtil.formatDateTime(runTime));
            detail.setResultMsg(XunjianReportUtil.formatDateTime(runTime));
        } else {
            detail.setXunJianValue("");
            detail.setResultMsg("此设备无法获取运行时长");
        }

        return detail;
    }

    @Override

    public String getCode() {
        return XunJianTargetEnum.RUN_TIME.getCode();

    }
}
