package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectDBEntity;
import com.jcca.dataProcessing.Entity.CollectTablespaceEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;


/**
 * @author Zhaozheng
 * @description TODO 数据库信息适配器
 * @className DBAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Slf4j
@Component("dbAdapter")
public class DbAdapter extends AssetIpAdd implements  IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * 数据库
     *
     * @param data
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispose(JSONArray data) {
        List<CollectDBEntity> dbList = JSONUtil.toList(data, CollectDBEntity.class);
        CollectDBEntity collectDBEntity = dbList.get(0);
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_db.getCode(), "monitor", true);

        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                getAssetId(collectDBEntity);
                try {
                    dataProcessManager.dbHandlerRequest(collectDBEntity);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectDBEntity.getAssetIp() + "dbHandlerRequest 抛出异常", e);

                }
                List<CollectTablespaceEntity> list = collectDBEntity.getTablespace();
                String collectCode = MyIdUtil.getId();
                for (int i = 0; i < list.size(); i++) {
                    CollectTablespaceEntity collectTablespaceEntity = list.get(i);
                    collectTablespaceEntity.setAssetId(collectDBEntity.getAssetId());
                    collectTablespaceEntity.setAssetIp(collectDBEntity.getAssetIp());
                    collectTablespaceEntity.setCollectCode(collectCode);
                    try {
                        dataProcessManager.dbTableHandlerRequest(collectTablespaceEntity);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + collectTablespaceEntity.getAssetIp() + "dbTableHandlerRequest 抛出异常", e);
                    }
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
        return CollectConst.DB;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
