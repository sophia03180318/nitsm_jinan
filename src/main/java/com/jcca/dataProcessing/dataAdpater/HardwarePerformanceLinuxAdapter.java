package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectPerformanceBean;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: hhw
 * @description: HardwarePerformanceLinuxAdapter 主要是用来
 * @date: 2025-08-25  13:59
 * @since: 2.1.9.0
 */
@Component("hardwarePerformanceLinuxAdapter")
public class HardwarePerformanceLinuxAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    /**
     * 处理数据
     *
     * @param data 接收到的数据
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectPerformanceBean> list = JSONUtil.toList(data, CollectPerformanceBean.class);
        CollectPerformanceBean bean = list.get(0);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_hardware_performance.getCode(), "monitor", true);

        excutorService.submit(() -> {
            setAssetIp(bean);
            try {
                dataProcessManager.hardwarePerformanceHandlerRequest(bean);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + bean.getAssetIp() + "hardwarePerformanceLinuxAdapter 抛出异常", e);
            }
            return 1;
        });
    }

    /**
     * 获取对应的KEY
     *
     * @return
     */
    @Override
    public String getCode() {
        return CollectConst.LINUX_HARDWARE_PERFORMANCE;
    }

    /**
     * 可用户获取一些过程中的处理信息
     *
     * @return
     */
    @Override
    public String dataProcess() {
        return "当前硬件性能信息剩余处理数量：" + excutorService.getQueue().size();
    }
}
