package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectProcessEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.ThresholdProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Zhaozheng
 * @description TODO 进程信息适配器
 * @className processAdapter
 * @date 2023/10/20 16:28
 * @since 2.1.0.0
 */
@Slf4j
@Component("processAdapter")
public class ProcessAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Resource
    private ThresholdProcessService thresholdService;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * 处理进程信息
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectProcessEntity> processBeans = JSONUtil.toList(data, CollectProcessEntity.class);
        if (CollectionUtils.isEmpty(processBeans)) {
            return;
        }
        QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_ID", processBeans.get(0).getAssetId());
        List<ThresholdProcess> thresholdList = thresholdService.list(queryWrapper);
        if (thresholdList == null || thresholdList.isEmpty()) {
            return;
        }
        List<CollectProcessEntity> disposeList = new ArrayList<>();
        Boolean isStation = assetService.isStationAsset(processBeans.get(0).getAssetId());
        String collectCode = MyIdUtil.getId();
        for (ThresholdProcess thresholdProcess : thresholdList) {
            CollectProcessEntity collectProcess = new CollectProcessEntity();
            collectProcess.setName(thresholdProcess.getProcessName());
            collectProcess.setProcessId(thresholdProcess.getProcessId());
            collectProcess.setThresholdId(thresholdProcess.getId());
            collectProcess.setAssetId(thresholdProcess.getAssetId());
            collectProcess.setStationAsset(isStation);
            collectProcess.setCollectTime(processBeans.get(0).getCollectTime());
            collectProcess.setCollectCode(collectCode);
            setAssetIp(collectProcess);

            List<CollectProcessEntity> collectProcessList = processBeans.stream().filter(item -> item.getName().contains(thresholdProcess.getProcessName())).collect(Collectors.toList());
            if (collectProcessList.isEmpty()) {
                collectProcess.setStatus(false);
            }else {
                CollectProcessEntity collectProcessEntity = collectProcessList.get(0);
                collectProcess.setStatus(true);
                collectProcess.setProcessId(collectProcessEntity.getProcessId());
                collectProcess.setCpuRate(collectProcessEntity.getCpuRate());
                collectProcess.setMemoryRate(collectProcessEntity.getMemoryRate());
            }

            disposeList.add(collectProcess);
        }

        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_process.getCode(), "monitor", true);
        excutorService.execute(() -> {
            for (CollectProcessEntity collectProcessEntity : disposeList) {
                try {
                    dataProcessManager.processHandlerRequest(collectProcessEntity);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectProcessEntity.getAssetIp() + "processHandlerRequest 抛出异常", e);

                }
            }
        });


    }


    @Override
    public String getCode() {
        return CollectConst.PROCESS;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }



}
