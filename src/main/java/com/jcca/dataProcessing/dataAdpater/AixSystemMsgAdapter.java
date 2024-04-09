package com.jcca.dataProcessing.dataAdpater;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectAixSystemFattenEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * IAdapter整合整理数据的作用，将数据整理成格式化数据
 */

/**
 * @author Zhaozheng
 * @description TODO 小型机基础信息适配器
 *
 * @className AixSystemMsgAdapter
 * @date 2023/10/20 15:40
 * @since 2.1.0.0
 */

@Slf4j
@Component("aixSystemMsgAdapter")
public class AixSystemMsgAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());





    @Override
    public void dispose(JSONArray data) {

        List<CollectAixSystemFattenEntity> beanList = JSONUtil.toList(data, CollectAixSystemFattenEntity.class);

        if (beanList.isEmpty()) {
            log.error("AIX 数据处理失败，空的序列集合");
            return;
        }




        CollectAixSystemFattenEntity aixSystemMsg = beanList.get(0);

        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                setAssetIp(aixSystemMsg);
                try {
                    dataProcessManager.aixSystemMsgHandlerRequest(aixSystemMsg);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + aixSystemMsg.getAssetIp() + "aixSystemMsgHandlerRequest 抛出异常", e);
                }
            }
        });


    }

    @Override
    public String getCode() {
        return CollectConst.AIX_SYSTEM_MSG;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
