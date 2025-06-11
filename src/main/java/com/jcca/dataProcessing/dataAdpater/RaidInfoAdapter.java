package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.CollectRaidSystemFattenEntity;
import com.jcca.dataProcessing.Entity.DiskEntity;
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
import java.util.concurrent.*;

import static com.jcca.web.asset.controller.ApiRaidController.getNetFileSizeDescription;
import static java.lang.Long.parseLong;

/**
 * @author Zhaozheng
 * @description TODO 存储信息适配器
 * @className RaidInfoAdapter
 * @date 2023/10/20 16:28
 * @since 2.1.0.0
 */
@Slf4j
@Component("raidInfoAdapter")
public class RaidInfoAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

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

        List<CollectRaidSystemFattenEntity> beanList = JSONUtil.toList(data, CollectRaidSystemFattenEntity.class);
        CollectRaidSystemFattenEntity collectRaidSystemFattenEntity = beanList.get(0);

        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_storage.getCode(), "monitor", true);


        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                List<DiskEntity> drivers = collectRaidSystemFattenEntity.getDrives();
                List<DiskEntity> groupList = collectRaidSystemFattenEntity.getGroupList();
                List<DiskEntity> mdiskList = collectRaidSystemFattenEntity.getMdiskList();
                List<DiskEntity> vdiskList = collectRaidSystemFattenEntity.getVdiskList();
                Integer size = drivers.size() + groupList.size() + mdiskList.size() + vdiskList.size() + 1;
                CountDownLatch cdh = new CountDownLatch(size);
                if (true) {
                    Long time = collectRaidSystemFattenEntity.getCollectTime();
                    DiskEntity diskEntity = new DiskEntity();
                    diskEntity.setAssetId(collectRaidSystemFattenEntity.getAssetId());
                    diskEntity.setAssetIp(collectRaidSystemFattenEntity.getAssetIp());
                    diskEntity.setCapacity(collectRaidSystemFattenEntity.getTotalCapacity());
                    diskEntity.setCapacityStr(getNetFileSizeDescription(parseLong(collectRaidSystemFattenEntity.getTotalCapacity())));
                    diskEntity.setUsedCapacity(collectRaidSystemFattenEntity.getUsedCapacity());
                    diskEntity.setUsedCapacityStr(getNetFileSizeDescription(parseLong(collectRaidSystemFattenEntity.getUsedCapacity())));
                    diskEntity.setDiskType(3);
                    diskEntity.setId(MyIdUtil.getId());
                    diskEntity.setCollectTime(time);
                    diskEntity.setCollectCode(collectRaidSystemFattenEntity.getCollectCode());

                    thresholdDisposePool.execute(() -> {
                        try {
                            dataProcessManager.raidInfoHandlerRequest(diskEntity);
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + diskEntity.getAssetIp() + "raidInfoHandlerRequest 抛出异常", e);

                        } finally {
                            cdh.countDown();

                        }
                    });


                }


                for (int i = 0; i < groupList.size(); i++) {
                    DiskEntity diskEntity = groupList.get(i);
                    diskEntity.setAssetId(collectRaidSystemFattenEntity.getAssetId());
                    diskEntity.setAssetIp(collectRaidSystemFattenEntity.getAssetIp());
                    diskEntity.setCollectCode(collectRaidSystemFattenEntity.getCollectCode());
                    thresholdDisposePool.execute(() -> {
                        try {
                            setAssetIp(diskEntity);
                            dataProcessManager.raidInfoHandlerRequest(diskEntity);
                        } catch (Exception e) {
                            log.error("V系列存储处理错误", e);
                        } finally {
                            cdh.countDown();

                        }
                    });


                }
                for (int i = 0; i < mdiskList.size(); i++) {
                    DiskEntity diskEntity = mdiskList.get(i);
                    diskEntity.setAssetId(collectRaidSystemFattenEntity.getAssetId());
                    diskEntity.setAssetIp(collectRaidSystemFattenEntity.getAssetIp());
                    diskEntity.setCollectCode(collectRaidSystemFattenEntity.getCollectCode());
                    thresholdDisposePool.execute(() -> {
                        try {
                            setAssetIp(diskEntity);
                            dataProcessManager.raidInfoHandlerRequest(diskEntity);
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + diskEntity.getAssetIp() + "raidInfoHandlerRequest 抛出异常", e);

                        } finally {
                            cdh.countDown();

                        }
                    });

                }
                for (int i = 0; i < drivers.size(); i++) {
                    DiskEntity diskEntity = drivers.get(i);
                    diskEntity.setAssetId(collectRaidSystemFattenEntity.getAssetId());
                    diskEntity.setAssetIp(collectRaidSystemFattenEntity.getAssetIp());
                    diskEntity.setCollectCode(collectRaidSystemFattenEntity.getCollectCode());
                    thresholdDisposePool.execute(() -> {
                        try {
                            setAssetIp(diskEntity);
                            dataProcessManager.raidInfoHandlerRequest(diskEntity);
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + diskEntity.getAssetIp() + "raidInfoHandlerRequest 抛出异常", e);

                        } finally {
                            cdh.countDown();

                        }
                    });

                }

                for (int i = 0; i < vdiskList.size(); i++) {
                    DiskEntity diskEntity = vdiskList.get(i);
                    diskEntity.setAssetId(collectRaidSystemFattenEntity.getAssetId());
                    diskEntity.setAssetIp(collectRaidSystemFattenEntity.getAssetIp());
                    diskEntity.setCollectCode(collectRaidSystemFattenEntity.getCollectCode());
                    thresholdDisposePool.execute(() -> {
                        try {
                            setAssetIp(diskEntity);
                            dataProcessManager.raidInfoHandlerRequest(diskEntity);
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + diskEntity.getAssetIp() + "raidInfoHandlerRequest 抛出异常", e);

                        } finally {
                            cdh.countDown();

                        }
                    });

                }

                List<String> logs = collectRaidSystemFattenEntity.getLogList();
                if (logs != null && logs.size() > 0) {
                    for (int i = 0; i < logs.size(); i++) {
                        RaidCommonLogEntity entity = new RaidCommonLogEntity();
                        entity.setAssetId(collectRaidSystemFattenEntity.getAssetId());
                        entity.setAssetIp(collectRaidSystemFattenEntity.getAssetIp());
                        entity.setCollectCode(collectRaidSystemFattenEntity.getCollectCode());
                        entity.setLog(logs.get(i));
                        thresholdDisposePool.execute(() -> {
                            try {
                                setAssetIp(entity);
                                dataProcessManager.raidVLogHandlerRequest(entity);
                            } catch (Exception e) {
                                AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + entity.getAssetIp() + "raidInfoHandlerRequest 抛出异常", e);

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
                return 1;
            }
        });

        if(collectRaidSystemFattenEntity.getDrives().get(0).getInspectRecordId()!=null&&!"".equals(collectRaidSystemFattenEntity.getDrives().get(0).getInspectRecordId())){
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
        return CollectConst.RAID_SYSTEM_MSG;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
