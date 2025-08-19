package com.jcca.dataProcessing.dataAdpater;

import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.dataProcessing.Entity.CollectDBEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.*;


/**
 * @author Zhaozheng
 * @description TODO 数据库信息适配器
 * @className DBAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Slf4j
@Component("dbAlarmAdapter")
public class DbAlarmAdapter extends AssetIpAdd implements  IAdapter<ReceiveAlarmDto> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    /**
     * @param receiveAlarmDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispose(ReceiveAlarmDto receiveAlarmDto) {

        getAssetId(receiveAlarmDto);
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_db.getCode(), "monitor", true);
        CollectDBEntity collectDBEntity = new CollectDBEntity();
        collectDBEntity.setAssetId(receiveAlarmDto.getAssetId());
        collectDBEntity.setAssetIp(receiveAlarmDto.getAssetIp());
        collectDBEntity.setStatus((receiveAlarmDto.getFlag() ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode()).toString());

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    dataProcessManager.dbAlarmHandlerRequest(collectDBEntity);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectDBEntity.getAssetIp() + "dbAlarmHandlerRequest 抛出异常", e);

                }
                return 1;
            }
        });

        if(collectDBEntity.getInspectRecordId()!=null&&!"".equals(collectDBEntity.getInspectRecordId())){
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
        return CollectConst.DB_ALARM;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
