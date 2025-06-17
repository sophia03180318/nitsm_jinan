package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.OpticalSwitchEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO 光纤交换机适配器
 * @className OpticalAdapter
 * @date 2023/10/20 16:08
 * @since 2.1.0.0
 */
@Slf4j
@Component("opticalAdapter")
public class OpticalAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    /**
     * 处理数据
     *
     * @param data 采集到的数据
     */
    @Override
    public void dispose(JSONArray data) {
        List<OpticalSwitchEntity> beanList = JSONUtil.toList(data, OpticalSwitchEntity.class);
        if (CollectionUtil.isEmpty(beanList)) {
            log.error("光交换机采集数据处理失败，空的序列集合");
            return;
        }


        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                String collectCode = MyIdUtil.getId();
                for (OpticalSwitchEntity opticalSwitchEntity : beanList) {
                    setAssetIp(opticalSwitchEntity);
                    opticalSwitchEntity.setCollectCode(collectCode);
                    try {
                        dataProcessManager.opticalHandlerRequest(opticalSwitchEntity);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + opticalSwitchEntity.getAssetIp() + "opticalHandlerRequest 抛出异常", e);
                    }

                }
                return 1;
            }
        });

        if(beanList.get(0).getInspectRecordId()!=null&&!"".equals(beanList.get(0).getInspectRecordId())){
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }


    }

    /**
     * 获取对应的KEY
     *
     * @return
     */
    @Override
    public String getCode() {
        return CollectConst.OPTICAL_SWITCH;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
