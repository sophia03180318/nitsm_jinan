package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.DSEntity;
import com.jcca.dataProcessing.Entity.DsSystemFattenEntity;
import com.jcca.dataProcessing.Entity.RaidCommonLogEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO DS存储信息适配器
 * @className DsInfoAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Slf4j
@Component("dsInfoAdapter")
public class DsInfoAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Resource(name = ThreadPoolEnum.thresholdDataDisposePool)
    private ThreadPoolExecutor thresholdDisposePool;


    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    @Override
    public void dispose(JSONArray data) {

        List<DsSystemFattenEntity> beanList = JSONUtil.toList(data, DsSystemFattenEntity.class);
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_storage.getCode(), "monitor", true);
        DsSystemFattenEntity ds = beanList.get(0);
        if (ObjectUtil.isNull(ds.getControllers())) {
            return;
        }
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                List<DSEntity> controllers = ds.getControllers();
                List<DSEntity> arrays = ds.getArrays();
                List<DSEntity> logicalDrivers = ds.getLogicalDrives();
                List<DSEntity> drivers = ds.getDrives();
                List<String> logs = ds.getLogInfo();
                Integer size = controllers.size() + arrays.size() + logicalDrivers.size() + drivers.size() + logs.size() + 1;
                CountDownLatch cdh = new CountDownLatch(size);

                thresholdDisposePool.execute(() -> {

                    try {
                        dataProcessManager.raidDsBaseInfoHandlerRequest(ds);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + ds.getAssetIp() + "raidDsBaseInfoHandlerRequest 抛出异常", e);
                    } finally {
                        cdh.countDown();

                    }
                });


                if (controllers != null && controllers.size() > 0) {
                    for (int i = 0; i < controllers.size(); i++) {
                        DSEntity dsEntity = controllers.get(i);
                        thresholdDisposePool.execute(() -> {
                            try {
                                dsEntity.setAssetId(ds.getAssetId());
                                dsEntity.setAssetIp(ds.getAssetIp());
                                setAssetIp(dsEntity);
                                dataProcessManager.raidDsInfoHandlerRequest(dsEntity);
                            } catch (Exception e) {
                                log.error("ds存储处理错误", e);
                            } finally {
                                cdh.countDown();

                            }
                        });
                    }
                }


                if (arrays != null && arrays.size() > 0) {
                    for (int i = 0; i < arrays.size(); i++) {
                        DSEntity dsEntity = arrays.get(i);
                        thresholdDisposePool.execute(() -> {
                            try {
                                dsEntity.setAssetId(ds.getAssetId());
                                dsEntity.setAssetIp(ds.getAssetIp());
                                setAssetIp(dsEntity);
                                dataProcessManager.raidDsInfoHandlerRequest(dsEntity);
                            } catch (Exception e) {
                                log.error("ds存储处理错误", e);
                            } finally {
                                cdh.countDown();

                            }
                        });
                    }
                }


                if (logicalDrivers != null && logicalDrivers.size() > 0) {
                    for (int i = 0; i < logicalDrivers.size(); i++) {
                        DSEntity dsEntity = logicalDrivers.get(i);
                        thresholdDisposePool.execute(() -> {
                            try {
                                dsEntity.setAssetId(ds.getAssetId());
                                dsEntity.setAssetIp(ds.getAssetIp());
                                setAssetIp(dsEntity);
                                dataProcessManager.raidDsInfoHandlerRequest(dsEntity);
                            } catch (Exception e) {
                                log.error("ds存储处理错误", e);
                            } finally {
                                cdh.countDown();

                            }
                        });
                    }
                }

                if (drivers != null && drivers.size() > 0) {
                    for (int i = 0; i < drivers.size(); i++) {

                        DSEntity dsEntity = drivers.get(i);
                        thresholdDisposePool.execute(() -> {
                            try {
                                dsEntity.setAssetId(ds.getAssetId());
                                dsEntity.setAssetIp(ds.getAssetIp());
                                setAssetIp(dsEntity);
                                dataProcessManager.raidDsInfoHandlerRequest(dsEntity);
                            } catch (Exception e) {
                                log.error("ds存储处理错误", e);
                            } finally {
                                cdh.countDown();

                            }
                        });
                    }
                }

                if (logs != null && logs.size() > 0) {
                    for (int i = 0; i < logs.size(); i++) {
                        RaidCommonLogEntity dsEntity = new RaidCommonLogEntity();
                        dsEntity.setAssetId(ds.getAssetId());
                        dsEntity.setAssetIp(ds.getAssetIp());
                        setAssetIp(dsEntity);
                        dsEntity.setLog(logs.get(i));
                        thresholdDisposePool.execute(() -> {
                            try {
                                dataProcessManager.raidDsLogHandlerRequest(dsEntity);
                            } catch (Exception e) {
                                log.error("ds存储处理错误", e);
                            } finally {
                                cdh.countDown();

                            }
                        });

                    }
                }
                try {
                    cdh.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

    }


    @Override
    public String getCode() {
        return CollectConst.DS_SYSTEM_MSG;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
