package com.jcca.dataProcessing.dataAdpater;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.CollectBhmCpuEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 管理口主动采集的CPU信息
 */
@Slf4j
@Component("bhmCpuAdapter")
public class BhmCpuAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource(name = ThreadPoolEnum.thresholdDataDisposePool)
    private ThreadPoolExecutor thresholdDisposePool;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    @Override
    public void dispose(JSONArray data) {
        List<CollectBhmCpuEntity> cpuList = JSONUtil.toList(data, CollectBhmCpuEntity.class);

        if(Objects.isNull(cpuList)||cpuList.isEmpty()){
            return ;
        }

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                CountDownLatch cdh = new CountDownLatch(cpuList.size());
                String collectCode = MyIdUtil.getId();
                for (CollectBhmCpuEntity item : cpuList) {
                    thresholdDisposePool.execute(() -> {
                        try {
                            item.setCollectCode(collectCode);
                            setAssetIp(item);
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + item.getAssetIp() + "interfaceHandlerRequest 抛出异常", e);
                        } finally {
                            cdh.countDown();
                        }
                    });
                }
                try {
                    cdh.await();
                    dataProcessManager.bhmCpuHandlerRequest(cpuList);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }

                return 1;
            }
        });



        if(cpuList.get(0).getInspectRecordId()!=null&&!"".equals(cpuList.get(0).getInspectRecordId())){
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
        return CollectConst.BHM_CPU_MSG;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
