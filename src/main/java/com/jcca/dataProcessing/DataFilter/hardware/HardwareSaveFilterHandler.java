package com.jcca.dataProcessing.DataFilter.hardware;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectHardwareBean;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.CollectHardware;
import com.jcca.web.asset.service.CollectHardwareService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: hhw
 * @description: HardwareSaveFilterHandler 主要是用来
 * @date: 2025-08-25  14:14
 * @since: 2.1.9.0
 */
@Component("hardwareSaveFilterHandler")
public class HardwareSaveFilterHandler extends IFilterHandler<CollectHardwareBean> {


    @Resource
    private CollectHardwareService collectHardwareService;

    /**
     * 处理方法
     *
     * @param info
     * @return 返回值true表示可以进入下一层处理，放回false
     * 表示整个处理结束，不会进入下层处理，不会保存缓存
     */
    @Override
    public synchronized boolean handler(CollectHardwareBean info) throws ResultException, Exception {
        CollectHardware hardware = collectHardwareService.findByAssetId(info.getAssetId());
        if (hardware == null) {
            hardware = new CollectHardware();
            BeanUtils.copyProperties(info, hardware);
            hardware.setCollectTime(DateUtil.date(info.getCollectTime()));
            collectHardwareService.save(hardware);
        } else {
            BeanUtils.copyProperties(info, hardware);
            hardware.setCollectTime(DateUtil.date(info.getCollectTime()));
            collectHardwareService.updateById(hardware);
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
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
