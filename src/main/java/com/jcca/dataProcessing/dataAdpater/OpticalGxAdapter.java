package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.thresholds.bean.OpticalSwitchV2Bean;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * 光纤交换机适配器
 */
@Slf4j
@Component("opticalGxAdapter")
public class OpticalGxAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

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
        List<OpticalSwitchV2Bean> beanList = JSONUtil.toList(data, OpticalSwitchV2Bean.class);
        if (CollectionUtil.isEmpty(beanList)) {
            log.error("光交换机采集数据处理失败，空的序列集合");
            return;
        }


        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                String collectCode = MyIdUtil.getId();
                for (OpticalSwitchV2Bean OpticalSwitchV2Bean : beanList) {
                    setAssetIp(OpticalSwitchV2Bean);
                    OpticalSwitchV2Bean.setCollectCode(collectCode);
                    try {
                        dataProcessManager.opticalGxHandlerRequest(OpticalSwitchV2Bean);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + OpticalSwitchV2Bean.getAssetIp() + "opticalHandlerRequest 抛出异常", e);
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
        return ReceiveCollectConst.OPTICAL_SWITCH_H3C;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
