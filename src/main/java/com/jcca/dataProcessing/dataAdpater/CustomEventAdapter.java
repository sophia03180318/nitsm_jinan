package com.jcca.dataProcessing.dataAdpater;


import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CustomEvent;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * IAdapter整合整理数据的作用，将数据整理成格式化数据
 */

/**
 * @author Zhaozheng
 * @description TODO 小型机基础信息适配器
 * @className AixSystemMsgAdapter
 * @date 2023/10/20 15:40
 * @since 2.1.0.0
 */

@Slf4j
@Component("customEventAdapter")
public class CustomEventAdapter extends AssetIpAdd implements IAdapter<CustomEvent> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    @Override
    public void dispose(CustomEvent data) {
        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                setAssetIp(data);
                try {
                    dataProcessManager.customEventHandlerRequest(data);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + data.getAssetIp() + "customEventHandlerRequest 抛出异常", e);
                }
                return 1;
            }
        });

        if(data.getInspectRecordId()!=null&&!"".equals(data.getInspectRecordId())){
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getCode() {
        return CollectConst.CUSTOMEVENT;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
