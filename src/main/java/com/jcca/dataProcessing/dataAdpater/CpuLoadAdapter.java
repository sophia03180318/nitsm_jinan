package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectCpuLoadBean;
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
 * @description: CpuLoadAdapter 主要是用来
 * @date: 2025-07-17  15:25
 * @since: 2.1.8.0
 */
@Component("cpuLoadAdapter")
public class CpuLoadAdapter extends AssetIpAdd implements IAdapter<JSONArray> {


    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    /**
     * 处理数据
     *
     * @param data
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectCpuLoadBean> cpuLoadBeanList = JSONUtil.toList(data, CollectCpuLoadBean.class);
        CollectCpuLoadBean collectCpuLoadBean = cpuLoadBeanList.get(0);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_cpuLoad.getCode(), "monitor", true);

        excutorService.submit(() -> {
            setAssetIp(collectCpuLoadBean);
            try {
                dataProcessManager.cpuLoadHandlerRequest(collectCpuLoadBean);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectCpuLoadBean.getAssetIp() + "CpuLoadAdapter 抛出异常", e);
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
        return CollectConst.CPU_LINUX_LOAD_AVG;
    }

    /**
     * 可用户获取一些过程中的处理信息
     *
     * @return
     */
    @Override
    public String dataProcess() {
        return "当前CPU负载剩余处理数量：" + excutorService.getQueue().size();
    }
}
