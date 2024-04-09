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
 * @description TODO 铁科软件工作状态适配器
 * @className TiekeWorkstateAdapter
 * @date 2023/10/20 16:33
 * @since 2.1.0.0
 */
@Component("tiekeWorkstateAdapter")
public class TiekeWorkstateAdapter extends AssetIpAdd implements IAdapter<ItsmQueueEntity> {
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
                    dataProcessManager.tiekeWorkStateHandlerRequest(data);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + data.getAssetIp() + "tiekeWorkStateHandlerRequest 抛出异常", e);
                }

            }
        });



    }

    @Override
    public String getCode() {
        return CollectConst.TIEKE_WORKSTATE;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
