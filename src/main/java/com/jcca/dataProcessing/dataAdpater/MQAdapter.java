package com.jcca.dataProcessing.dataAdpater;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO
 * @className MQAdapter
 * @date 2024/1/6 10:27
 * @since 2.1.0.0
 */
@Slf4j
@Component("MQAdapter")
public class MQAdapter extends AssetIpAdd implements IAdapter<MQMonitorEntity> {


    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(MQMonitorEntity data) {
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_memory.getCode(), "monitor", true);
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    dataProcessManager.mqHandlerRequest(data);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + data.getAssetIp() + "mqHandlerRequest 抛出异常", e);

                }
            }
        });
    }

    @Override
    public String getCode() {
        return CollectConst.MQ;
    }

    @Override
    public String dataProcess() {
        return null;
    }
}
