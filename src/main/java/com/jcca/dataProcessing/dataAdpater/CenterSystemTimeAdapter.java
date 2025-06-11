package com.jcca.dataProcessing.dataAdpater;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectSystemTimeEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO CPU数据适配器
 * @className CpuAdapter
 * @date 2023/10/20 16:17
 * @since 2.1.0.0
 */
@Slf4j
@Component("centerSystemTimeAdapter")
public class CenterSystemTimeAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    /**
     * cpu 数据处理
     *
     * @param data
     */
    @Override

    public void dispose(JSONArray data) {


        List<CollectSystemTimeEntity> list = JSONUtil.toList(data, CollectSystemTimeEntity.class);
        CollectSystemTimeEntity collectSystemTimeEntity = list.get(0);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_run_time_state.getCode(), "monitor", true);
        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                setAssetIp(collectSystemTimeEntity);
                try {
                    dataProcessManager.systemTimeHandlerRequest(collectSystemTimeEntity);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSystemTimeEntity.getAssetIp() + "systemTimeHandlerRequest 抛出异常", e);
                }
                return 1;
            }
        });

        if(collectSystemTimeEntity.getInspectRecordId()!=null&&!"".equals(collectSystemTimeEntity.getInspectRecordId())){
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
        return CollectConst.SYSTEM_TIME;
    }


    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }


}
