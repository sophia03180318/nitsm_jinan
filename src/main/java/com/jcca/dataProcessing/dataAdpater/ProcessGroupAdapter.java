package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.dataProcessing.Entity.ProcessAlarmQueueEntity;
import com.jcca.dataProcessing.Entity.ProcessGroupEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.ThresholdProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO 进程信息适配器
 * @className processAdapter
 * @date 2023/10/20 16:28
 * @since 2.1.0.0
 */
@Slf4j
@Component("processGroupAdapter")
public class ProcessGroupAdapter extends AssetIpAdd implements IAdapter<ReceiveAlarmDto> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private ThresholdProcessService processServ;

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * 处理进程信息
     *
     * @param alarmDto
     */

    @Override
    public void dispose(ReceiveAlarmDto alarmDto) {

        ProcessGroupEntity processGroupEntity = new ProcessGroupEntity();
        List<ProcessAlarmQueueEntity> queueObj = JSONUtil.toList(JSONUtil.parseArray(alarmDto.getContent()), ProcessAlarmQueueEntity.class);
        ProcessAlarmQueueEntity entity = queueObj.get(0);
        processGroupEntity.setAssetIp(entity.getAssetIp());


        for (ProcessAlarmQueueEntity processAlarmQueueEntity : queueObj) {
            ThresholdProcess process = processServ.getOneByAssetIpAndName(processAlarmQueueEntity.getAssetIp(), entity.getProcessName());
            if (Objects.isNull(process)) {
                return;
            }
            getAssetId(processAlarmQueueEntity);

            if (StrUtil.isEmpty(process.getProcessId())) {
                processAlarmQueueEntity.setProcessId(process.getProcessId());
            }
            processAlarmQueueEntity.setProcessName(process.getProcessName());
            processAlarmQueueEntity.setThresholdId(process.getId());
        }

        processGroupEntity.setQueueObj(queueObj);

        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                getAssetId(processGroupEntity);
                try {
                    dataProcessManager.processGroupHandlerRequest(processGroupEntity);
                } catch (Exception e) {
                    String message = "设备" + processGroupEntity.getAssetIp() + "processGroupHandlerRequest 抛出异常【%s】";
                    String format = String.format(message, JSONUtil.parse(alarmDto).toString());
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, format, e);

                }
            }
        });


    }



    @Override
    public String getCode() {
        return CollectConst.PROCESS_GROUP;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
