package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
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
 * @description TODO 通号版本信息适配器
 * @className CrscVersionAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Component("crscVersionAdapter")
public class CrscVersionAdapter extends AssetIpAdd implements IAdapter<String> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    @Override
    public void dispose(String data) {
        ItsmQueueEntity itsmQueueReq = JSONUtil.toBean(data, ItsmQueueEntity.class);
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_CTC.getCode(), "monitor", true);

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                setAssetIp(itsmQueueReq);
                dataProcessManager.crscVersionHandlerRequest(itsmQueueReq);
                return 1;
            }
        });

        if(itsmQueueReq.getInspectRecordId()!=null&&!"".equals(itsmQueueReq.getInspectRecordId())){
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
        return CollectConst.CRSC_VERSION;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
