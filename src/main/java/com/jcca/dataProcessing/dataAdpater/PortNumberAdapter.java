package com.jcca.dataProcessing.dataAdpater;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectPortUsedNumberEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO 端口使用数量适配器
 * @className portNumberAdapter
 * @date 2023/10/20 16:28
 * @since 2.1.0.0
 */
@Slf4j
@Component("portNumberAdapter")
public class PortNumberAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(JSONArray data) {
        List<CollectPortUsedNumberEntity> portNumbers = JSONUtil.toList(data, CollectPortUsedNumberEntity.class);
        CollectPortUsedNumberEntity collectPortUsedNumberEntity = portNumbers.get(0);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_system_port.getCode(), "monitor", true);
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                setAssetIp(collectPortUsedNumberEntity);
                try {
                    dataProcessManager.portNumberHandlerRequest(collectPortUsedNumberEntity);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectPortUsedNumberEntity.getAssetIp() + "portNumberHandlerRequest 抛出异常", e);

                }
            }
        });

    }

    @Override
    public String getCode() {
        return CollectConst.PORT_NUMBER;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
