package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectConnectEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO 连接信息适配器
 * @className ConnectAdapter
 * @date 2023/10/20 16:17
 * @since 2.1.0.0
 */
@Component("connectAdapter")
public class ConnectAdapter extends AssetIpAdd implements IAdapter<JSONArray> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    /**
     * 处理采集的连接
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectConnectEntity> connBeanList = JSONUtil.toList(data, CollectConnectEntity.class);
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                for (CollectConnectEntity collectConnectEntity : connBeanList) {
                    setAssetIp(collectConnectEntity);
                    try {
                        dataProcessManager.connectHandlerRequest(collectConnectEntity);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectConnectEntity.getAssetIp() + "connectHandlerRequest 抛出异常", e);
                    }
                }
            }
        });

    }

    @Override
    public String getCode() {
        return CollectConst.CONNECT;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }


}
