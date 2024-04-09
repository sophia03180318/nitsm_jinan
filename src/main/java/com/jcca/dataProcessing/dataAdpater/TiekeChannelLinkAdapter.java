package com.jcca.dataProcessing.dataAdpater;

import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO 铁科软件通道适配器
 * @className TiekeChannelLinkAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Component("tiekeChannelLinkAdapter")
public class TiekeChannelLinkAdapter extends AssetIpAdd implements IAdapter<ItsmQueueEntity> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(ItsmQueueEntity data) {
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_CTC.getCode(), "monitor", true);

        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                setAssetIp(data);
                try {
                    dataProcessManager.tiekeChannelLinkHandlerRequest(data);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + data.getAssetIp() + "tiekeChannelLinkHandlerRequest 抛出异常", e);

                }
            }
        });


    }

    @Override
    public String getCode() {
        return CollectConst.TIEKE_CHANNEL_LINK;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
