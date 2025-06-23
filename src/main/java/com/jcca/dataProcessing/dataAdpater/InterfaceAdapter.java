package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO 端口信息适配器
 * @className InterfaceAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */

@Slf4j
@Component("interfaceAdapter")
public class InterfaceAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource(name = ThreadPoolEnum.thresholdDataDisposePool)
    private ThreadPoolExecutor thresholdDisposePool;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * 处理硬件端口信息
     *
     * @param
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectInterfaceEntity> interfaces = JSONUtil.toList(data, CollectInterfaceEntity.class);

        if(Objects.isNull(interfaces)||interfaces.isEmpty()){
            return ;
        }
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_port.getCode(), "monitor", true);

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                CountDownLatch cdh = new CountDownLatch(interfaces.size());
                String collectCode = MyIdUtil.getId();
                for (CollectInterfaceEntity item : interfaces) {
                    thresholdDisposePool.execute(() -> {
                        try {
                            item.setCollectCode(collectCode);
                            setAssetIp(item);
                            dataProcessManager.interfaceHandlerRequest(item);
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



        if(interfaces.get(0).getInspectRecordId()!=null&&!"".equals(interfaces.get(0).getInspectRecordId())){
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
        return CollectConst.INTERFACE;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }


}
