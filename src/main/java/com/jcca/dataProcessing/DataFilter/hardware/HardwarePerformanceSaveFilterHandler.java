package com.jcca.dataProcessing.DataFilter.hardware;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectPerformanceBean;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.CollectHardwarePerformance;
import com.jcca.web.asset.service.CollectHardwarePerformanceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author: hhw
 * @description: HardwarePerformanceSaveFilterHandler 主要是用来
 * @date: 2025-08-25  14:14
 * @since: 2.1.9.0
 */
@Component("hardwarePerformanceSaveFilterHandler")
public class HardwarePerformanceSaveFilterHandler extends IFilterHandler<CollectPerformanceBean> {


    @Resource
    private CollectHardwarePerformanceService collectHardwarePerformanceService;

    /**
     * 处理方法
     *
     * @param info
     * @return 返回值true表示可以进入下一层处理，放回false
     * 表示整个处理结束，不会进入下层处理，不会保存缓存
     */
    @Override
    public synchronized boolean handler(CollectPerformanceBean info) throws ResultException, Exception {
        List<CollectHardwarePerformance> hardwares = collectHardwarePerformanceService.findByAssetId(info.getAssetId(), info.getThresholdProcessId());
        if (CollectionUtils.isEmpty(hardwares)) {
            CollectHardwarePerformance hardware = new CollectHardwarePerformance();
            BeanUtils.copyProperties(info, hardware);
            hardware.setCollectTime(DateUtil.date(info.getCollectTime()));
            hardware.setCreateTime(DateUtil.date());
            hardware.setId(MyIdUtil.getId());
            collectHardwarePerformanceService.save(hardware);
            return true;
        }
        String nid = info.getThresholdProcessId();
        for (CollectHardwarePerformance hardware : hardwares) {
            String oid = hardware.getThresholdProcessId();
            if ((nid == null && oid == null) || Objects.equals(nid, oid)) {
                BeanUtils.copyProperties(info, hardware);
                hardware.setCollectTime(DateUtil.date(info.getCollectTime()));
                collectHardwarePerformanceService.updateById(hardware);
                break;
            }
        }
        return true;
    }

    /**
     * 直接控制下层处理
     *
     * @param flag
     * @return 返回true则需要下层处理，返回false不需要下层处理，并且不会保存缓存
     */
    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
