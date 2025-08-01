package com.jcca.dataProcessing.dataAdpater;

import ch.qos.logback.classic.Level;
import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO
 * @className DisposeBeiyangVersionAdapter
 * @date 2023/10/20 16:07
 * @since 2.1.0.0
 */
@Component("syslogAdapter")
public class SyslogAdapter extends AssetIpAdd implements IAdapter<SyslogEventInfoEntity> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(SyslogEventInfoEntity infoEntity) {
        //事件监控分类
        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                //获取资产信息
                getAssetIPbyIMM(infoEntity);
                if(StrUtil.isEmpty(infoEntity.getAssetId())){
                    AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS, infoEntity.getIp() , "此IP在系统中不存在");
                    return 1;
                }
                try {
                    dataProcessManager.syslogEventHandlerRequest(infoEntity);
                } catch (ResultException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, infoEntity.getIp(), " syslogEventHandlerRequest 抛出异常:" + e.getMessage());
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + infoEntity.getIp() + " syslogEventHandlerRequest 抛出异常", e);

                }
                return 1;
            }
        });

        if(infoEntity.getInspectRecordId()!=null&&!"".equals(infoEntity.getInspectRecordId())){
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
        return CollectConst.SYSLOG;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }
}
