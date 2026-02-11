package com.jcca.dataProcessing.DataFilter.PCB;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.bean.constant.PcbStatus;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectPcbEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author lifp
 * @version 1.0
 * @description: pcb 板卡状态采集
 * @date 2026-01-07 星期三 9:39:12
 */
@Component("pCBFilterHandler")
public class PCBFilterHandler extends IFilterHandler<List<CollectPcbEntity>> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource(name = ThreadPoolEnum.thresholdDataDisposePool)
    private ThreadPoolExecutor thresholdDisposePool;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * 处理板卡采集数据
     */
    @Override
    public boolean handler(List<CollectPcbEntity> info) {
        List<CollectPcbEntity> collectPcbEntities = new ArrayList<>();

        for (CollectPcbEntity pcb : info) {

            if (StrUtil.isNotEmpty(pcb.getVersion())) {
                //新版本车站采集器保存数据后直接结束，无需处理告警
                return false;
            }

            if (StrUtil.isEmpty(pcb.getEntPhysicalCardStatusRev()) || StrUtil.isEmpty(pcb.getAssetId())
                    || StrUtil.isEmpty(pcb.getName()) || StrUtil.isEmpty(pcb.getDescStr())
                    || StrUtil.isEmpty(pcb.getModelName()) || StrUtil.isEmpty(pcb.getType())
                    || StrUtil.isEmpty(pcb.getSerialNumber()) || StrUtil.isEmpty(pcb.getSerialNumberName())
                    || StrUtil.isEmpty(pcb.getSoftwareVersion()) || StrUtil.isEmpty(pcb.getHardwareVersion())
                    || StrUtil.isEmpty(pcb.getOsVersion()) || StrUtil.isEmpty(pcb.getPcbIndex())) {
                continue;
            }

            String redisKey = pcb.getAssetIp() + ":" + pcb.getAssetId() + ":" + StatusInfoChangeTypeEnum.PCB_STATUS.getCode() + ":" + pcb.getPcbIndex();
            String mapKey = StatusInfoChangeTypeEnum.PCB_STATUS.getCode();

            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(pcb.getEntPhysicalCardStatusRev());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            Date date = new Date();
            date.setTime(pcb.getCollectTime());
            changeInfo.setCollectTime(date);
            pcb.getMaps().put(mapKey, changeInfo);

            boolean flag = eventInfoChangeManagerService.infoIschange(pcb.getInspectRecordId(), redisKey, mapKey, pcb.getEntPhysicalCardStatusRev());
            if (flag) {

                String eventRedisKey = StatusInfoChangeTypeEnum.event_pcb_state.getCode();
                String eventMapKey = pcb.getAssetIp() + "_" + pcb.getAssetId() + "_" + pcb.getPcbIndex();

                String msg = buildOriginalMsg(pcb.getName(), pcb.getEntPhysicalCardStatusRev());
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg("板卡状态发生变化，请及时关注板卡状态！");
                alarmTempReq.setCollectValue(pcb.getEntPhysicalCardStatusRev());

                alarmTempReq.setFlag(pcb.getPcbIndex());

                int status = (!pcb.getEntPhysicalCardStatusRev().equals(PcbStatus.UP) ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode());
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(pcb.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq, pcb.getInspectRecordId(), pcb.getVersion());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescLog(msg);

                    this.dispatureEvent(event);
                }
            }
            collectPcbEntities.add(pcb);
        }

        // 独立处理 板卡信息 由数组转换对象形式处理信息
        if (collectPcbEntities.isEmpty()) return true;
        Future<Integer> future = excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                CountDownLatch cdh = new CountDownLatch(collectPcbEntities.size());
                String collectCode = MyIdUtil.getId();
                for (CollectPcbEntity item : collectPcbEntities) {
                    thresholdDisposePool.execute(() -> {
                        try {
                            item.setCollectCode(collectCode);
                            dataProcessManager.pcbHandlerRequest(item);
                        } catch (Exception e) {
                            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + item.getAssetName() + "interfaceHandlerRequest 抛出异常", e);
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

        if (collectPcbEntities.get(0).getInspectRecordId() != null && !"".equals(collectPcbEntities.get(0).getInspectRecordId())) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

    /**
     * 状态对应 - 事件描述
     *
     * @param name
     * @param status
     * @return
     */
    private String buildOriginalMsg(String name, String status) {
        switch (status) {
            case PcbStatus.UP:
                return String.format("%s ,板卡状态恢复（up）", name);
            case PcbStatus.UNKNOWN:
                return String.format("%s ,板卡状态未知（unknown），请及时关注板卡状态！", name);
            case PcbStatus.DISABLED:
                return String.format("%s ,板卡状态禁用（disabled），请及时处理。", name);
            case PcbStatus.OK_BUT_DIAG_FAILED:
                return String.format("%s ,板卡基本功能正常，但诊断失败（okButDiagFailed），请及时查看板卡状态！", name);
            default:
                return String.format("%s ,板卡状态异常（状态码：%s）", name, status);
        }
    }
}
