package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.CollectVlanEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Zhaozheng
 * @description TODO vlan信息适配器
 * @className VlanAdapter
 * @date 2023/10/20 16:33
 * @since 2.1.0.0
 */
@Component("vlanAdapter")
public class VlanAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    /**
     * 处理vlan数据
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectVlanEntity> vlanList = JSONUtil.toList(data, CollectVlanEntity.class);


        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                dataProcessManager.vlanHandlerRequest(vlanList);
                return 1;
            }
        });

        if(vlanList.get(0).getInspectRecordId()!=null&&!"".equals(vlanList.get(0).getInspectRecordId())){
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
        return CollectConst.VLAN;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
