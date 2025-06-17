package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.Entity.SensorEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.web.collect.enums.SensorTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO 管理口信息适配器
 * @className InterfaceAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Slf4j
@Component("ipmiAdapter")
public class IpmiAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource(name = ThreadPoolEnum.thresholdDataDisposePool)
    private ThreadPoolExecutor thresholdDisposePool;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(JSONArray data) {

        List<SensorEntity> ipmiList = JSONUtil.toList(data, SensorEntity.class);


        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                //CountDownLatch cdh = new CountDownLatch(ipmiList.size());
                String collectCode = MyIdUtil.getId();
                for (int j = 0; j < ipmiList.size(); j++) {
                    SensorEntity sensorEntity = ipmiList.get(j);
                    setAssetIp(sensorEntity);
                    List<CollectSensorEntity> fanList = sensorEntity.getFanList();
                    if (fanList != null) {
                        for (int i = 0; i < fanList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = fanList.get(i);
                            thresholdDisposePool.execute(() -> {

                                collectSensorEntity.setSensorType(SensorTypeEnum.FAN.name());
                                collectSensorEntity.setCollectCode(collectCode);
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);

                                }
                            });

                        }
                    }


                    List<CollectSensorEntity> temList = sensorEntity.getTemList();
                    if (temList != null) {
                        for (int i = 0; i < temList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = temList.get(i);
                            if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                            }
                            collectSensorEntity.setSensorType(SensorTypeEnum.GAUGE.name());
                            collectSensorEntity.setCollectCode(collectCode);
                            collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                            collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                            try {
                                dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                            } catch (Exception e) {
                                AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);
                            }

                        }
                    }


                    List<CollectSensorEntity> sysBrdList = sensorEntity.getSysBrdList();
                    if (sysBrdList != null) {
                        for (int i = 0; i < sysBrdList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = sysBrdList.get(i);
                            thresholdDisposePool.execute(() -> {
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setSensorType(SensorTypeEnum.POWER.name());
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setCollectCode(collectCode);
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);

                                }
                            });

                        }
                    }


                    List<CollectSensorEntity> planarList = sensorEntity.getSysPlanarList();
                    if (planarList != null) {
                        for (int i = 0; i < planarList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = planarList.get(i);

                            thresholdDisposePool.execute(() -> {
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setSensorType(SensorTypeEnum.VOLTAGE.name());
                                collectSensorEntity.setCollectCode(collectCode);
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);

                                }
                            });
                        }

                    }

                    List<CollectSensorEntity> logList = sensorEntity.getLogList();
                    if (logList != null) {
                        for (int i = 0; i < logList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = logList.get(i);
                            thresholdDisposePool.execute(() -> {
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setSensorType(SensorTypeEnum.LOG.name());
                                collectSensorEntity.setCollectCode(collectCode);
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);

                                }
                            });

                        }
                    }


                    List<CollectSensorEntity> manuList = sensorEntity.getManuList();
                    if (manuList != null) {
                        for (int i = 0; i < manuList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = manuList.get(i);
                            thresholdDisposePool.execute(() -> {
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setSensorType(SensorTypeEnum.MANU.name());
                                collectSensorEntity.setCollectCode(collectCode);
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);
                                }
                            });
                        }
                    }


                    List<CollectSensorEntity> ledList = sensorEntity.getLedList();
                    if (ledList != null) {
                        for (int i = 0; i < ledList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = ledList.get(i);
                            thresholdDisposePool.execute(() -> {
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setSensorType(SensorTypeEnum.LED.name());
                                collectSensorEntity.setCollectCode(collectCode);
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);
                                }
                            });
                        }
                    }


                    List<CollectSensorEntity> cpuList = sensorEntity.getCpuList();
                    if (cpuList != null) {
                        for (int i = 0; i < cpuList.size(); i++) {
                            CollectSensorEntity collectSensorEntity = cpuList.get(i);
                            thresholdDisposePool.execute(() -> {
                                if (StrUtil.isEmpty(collectSensorEntity.getSerialNumberName())) {
                                    collectSensorEntity.setSerialNumberName(collectSensorEntity.getName());
                                }
                                collectSensorEntity.setSensorType(SensorTypeEnum.CPU.name());
                                collectSensorEntity.setCollectCode(collectCode);
                                collectSensorEntity.setAssetId(sensorEntity.getAssetId());
                                collectSensorEntity.setAssetIp(sensorEntity.getAssetIp());
                                try {
                                    dataProcessManager.ipmiHandlerRequest(collectSensorEntity);
                                } catch (Exception e) {
                                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "ipmiHandlerRequest 抛出异常", e);
                                }
                            });
                        }

                    }
                    //cdh.countDown();
                }
/*                try {
                cdh.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
                return 1;
            }
        });

        if(ipmiList.get(0).getInspectRecordId()!=null&&!"".equals(ipmiList.get(0).getInspectRecordId())){
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
        return CollectConst.SYS_PORT;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
