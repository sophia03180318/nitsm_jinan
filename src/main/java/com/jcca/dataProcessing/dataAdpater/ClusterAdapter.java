package com.jcca.dataProcessing.dataAdpater;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectClusterEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO 集群状态信息适配器
 * @className ClusterAdapter
 * @date 2023/10/20 16:16
 * @since 2.1.0.0
 */
@Slf4j
@Component("clusterAdapter")
public class ClusterAdapter extends AssetIpAdd implements IAdapter<JSONArray> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    @Override
    public void dispose(JSONArray data) {



        List<CollectClusterEntity> collectClusters = JSONUtil.toList(data, CollectClusterEntity.class);
        if (Objects.isNull(collectClusters) || collectClusters.isEmpty()) {
            return;
        }
        excutorService.submit(new Runnable() {
            @Override
            public void run() {

            }
        });

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                for (CollectClusterEntity collectCluster : collectClusters) {
                    setAssetIp(collectCluster);
                    try {
                        dataProcessManager.collectClusterHandlerRequest(collectCluster);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectCluster.getAssetIp() + "collectClusterHandlerRequest 抛出异常", e);

                    }
                }
                return 1;
            }
        });

        if(collectClusters.get(0).getInspectRecordId()!=null&&!"".equals(collectClusters.get(0).getInspectRecordId())){
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
        return CollectConst.CLUSTER;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}



