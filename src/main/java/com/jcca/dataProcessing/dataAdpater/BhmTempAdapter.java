package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.dataProcessing.Entity.CollectBhmTempEntity;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.web.collect.enums.SensorTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;


@Slf4j
@Component("bhmTempAdapter")
public class BhmTempAdapter  extends AssetIpAdd implements IAdapter<JSONArray> {


    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource(name = ThreadPoolEnum.thresholdDataDisposePool)
    private ThreadPoolExecutor thresholdDisposePool;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    @Override
    public void dispose(JSONArray data) {
        List<CollectBhmTempEntity> queueList = JSONUtil.toList(data, CollectBhmTempEntity.class);

        if(Objects.isNull(queueList)||queueList.isEmpty()){
            return ;
        }

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                CountDownLatch cdh = new CountDownLatch(queueList.size());
                String collectCode = MyIdUtil.getId();
                for (CollectBhmTempEntity item : queueList) {
                    thresholdDisposePool.execute(() -> {
                        try {
                            item.setCollectCode(collectCode);
                            setAssetIp(item);
                            dataProcessManager.bhmTempHandlerRequest(item);

                            if(Objects.nonNull(item.getReadingCelsius())){
                                CollectSensorEntity copy = EntityBeanUtil.copy(item, CollectSensorEntity.class);
                                copy.setValue(item.getReadingCelsius().toString());
                                copy.setName(item.getName()+"_"+item.getMemberId());
                                copy.setStatus(item.getStatus().getHealth());
                                copy.setSensorType(SensorTypeEnum.GAUGE.name());
                                dataProcessManager.bhmTempThresholdHandlerRequest(copy);
                            }
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + item.getAssetIp() + "interfaceHandlerRequest 抛出异常", e);
                        } finally {
                            cdh.countDown();
                        }
                    });
                }
                try {
                    cdh.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return 1;
            }
        });



        if(queueList.get(0).getInspectRecordId()!=null&&!"".equals(queueList.get(0).getInspectRecordId())){
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
        return CollectConst.BHM_TEMP_MSG;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }






}
