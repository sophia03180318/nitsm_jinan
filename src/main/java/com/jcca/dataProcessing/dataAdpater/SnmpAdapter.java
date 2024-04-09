package com.jcca.dataProcessing.dataAdpater;

import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.SnmpEventInfoEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhaozheng
 * @description TODO 北洋版本信息适配器
 * @className DisposeBeiyangVersionAdapter
 * @date 2023/10/20 16:07
 * @since 2.1.0.0
 */
@Component("snmpAdapter")
public class SnmpAdapter extends AssetIpAdd implements IAdapter<SnmpEventInfoEntity> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(SnmpEventInfoEntity infoEntity) {
        //事件监控分类
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                getAssetIPbyIMM(infoEntity);
                try {
                    dataProcessManager.snmpEventHandlerRequest(infoEntity);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + infoEntity.getAssetIp() + "snmpEventHandlerRequest 抛出异常", e);

                }
            }
        });


    }

    @Override
    public String getCode() {
        return CollectConst.SNMP;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
