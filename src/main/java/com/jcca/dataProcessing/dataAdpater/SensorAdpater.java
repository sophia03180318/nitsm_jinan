package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO 传感器适配器
 * @className SensorAdpater
 * @date 2023/10/20 16:28
 * @since 2.1.0.0
 */

@Slf4j
@Component("sensorAdpater")
public class SensorAdpater extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    /**
     * 处理环境传感采集数据
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        //删除空值
        data.removeAll(Arrays.asList(new JSONObject()));

        List<CollectSensorEntity> sensorList = JSONUtil.toList(data, CollectSensorEntity.class);

        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_temp.getCode(), "monitor", true);
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_fan.getCode(), "monitor", true);
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                String collectCode = MyIdUtil.getId();
                for (CollectSensorEntity collectSensorEntity : sensorList) {
                    collectSensorEntity.setCollectCode(collectCode);
                    setAssetIp(collectSensorEntity);
                    try {
                        dataProcessManager.sensorHandlerRequest(collectSensorEntity);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectSensorEntity.getAssetIp() + "sensorHandlerRequest 抛出异常", e);
                    }
                }
            }
        });

    }

    @Override
    public String getCode() {

        return CollectConst.SENSOR;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
