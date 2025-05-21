package com.jcca.component.quartz.inspect;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.service.XunjianScheduleService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: hhw
 * @description: JobStart主要是用来做智能巡检定时任务启动
 * @date: 2025-02-25  14:19
 * @since: 2.0.11.0
 */
@Component
public class JobStart implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        XunjianScheduleService xunjianScheduleService = applicationContext.getBean(XunjianScheduleService.class);
        UpdateWrapper<XunjianSchedule> update = Wrappers.update();
        update.eq("JOB_STATE", 2);
        update.set("JOB_STATE", 1);
        xunjianScheduleService.update(update);

        InspectAssetService inspectAssetService = applicationContext.getBean(InspectAssetService.class);
        List<InspectAsset> assetList = inspectAssetService.list();
        for (InspectAsset asset : assetList) {
            asset.setInspectState(Web2Const.INSPECT);
        }
        if (!assetList.isEmpty()) {
            inspectAssetService.updateBatchById(assetList, 900);
        }

        xunjianScheduleService.joinScheduleJob();
    }
}
