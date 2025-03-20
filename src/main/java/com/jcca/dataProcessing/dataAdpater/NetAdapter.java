package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectNetworkCardEntity;
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
 * @description TODO 网卡信息适配器
 * @className MemoryAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Slf4j
@Component("netAdapter")
public class NetAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * 网卡数据处理
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectNetworkCardEntity> netList = JSONUtil.toList(data, CollectNetworkCardEntity.class);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_net.getCode(), "monitor", true);
        excutorService.submit(() -> {
            String collectCode = MyIdUtil.getId();
            for (CollectNetworkCardEntity item : netList) {
                setAssetIp(item);
                item.setCollectCode(collectCode);
                try {
                    dataProcessManager.networkHandlerRequest(item);
                } catch (ResultException e) {
                    AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS, item.getAssetIp(), "networkHandlerRequest 抛出异常:" + e.getMessage());
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + item.getAssetIp() + " networkHandlerRequest 抛出异常", e);
                }
            }
        });

    }

    @Override
    public String getCode() {
        return CollectConst.NET_CARD;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
