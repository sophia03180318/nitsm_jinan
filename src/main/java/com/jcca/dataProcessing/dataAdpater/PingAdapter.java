package com.jcca.dataProcessing.dataAdpater;

import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO ping信息适配器
 * @className PingAdapter
 * @date 2023/10/20 16:28
 * @since 2.1.0.0
 */
@Component("pingAdapter")
public class PingAdapter extends AssetIpAdd implements IAdapter<ReceiveAlarmDto> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(ReceiveAlarmDto data) {
        ReceiveAlarmEntity receiveAlarmEntity = EntityBeanUtil.copy(data, ReceiveAlarmEntity.class);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_ping.getCode(), "monitor", true);


        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if (receiveAlarmEntity.getContent() != null) {
                    try {
                        dataProcessManager.pingHandlerRequest(receiveAlarmEntity);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + receiveAlarmEntity.getAssetIp() + "pingHandlerRequest 抛出异常", e);
                    }
                } else {
                    getAssetId(receiveAlarmEntity);
                    try {
                        dataProcessManager.pingGeneralHandlerRequest(receiveAlarmEntity);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + receiveAlarmEntity.getAssetIp() + "pingGeneralHandlerRequest 抛出异常", e);

                    }
                }
                return 1;
            }
        });

        if(receiveAlarmEntity.getInspectRecordId()!=null&&!"".equals(receiveAlarmEntity.getInspectRecordId())){
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
        return CollectConst.ping;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
