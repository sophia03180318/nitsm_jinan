package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectDiskEntity;
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
 * @description TODO 磁盘信息适配器
 * @className DiskAdapter
 * @date 2023/10/20 16:25
 * @since 2.1.0.0
 */
@Slf4j
@Component("diskAdapter")
public class DiskAdapter extends AssetIpAdd implements IAdapter<JSONArray> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    /**
     * 磁盘数据处理
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectDiskEntity> disks = JSONUtil.toList(data, CollectDiskEntity.class);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_disk.getCode(), "monitor", true);
        excutorService.submit(new Runnable() {
            @Override
            public void run() {
                String collectCode = MyIdUtil.getId();
                for (CollectDiskEntity disk : disks) {
                    setAssetIp(disk);
                    disk.setCollectCode(collectCode);
                    try {
                        dataProcessManager.diskHandlerRequest(disk);
                    } catch (Exception e) {
                        AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + disk.getAssetIp() + "diskHandlerRequest 抛出异常", e);
                    }
                }
            }
        });

    }

    @Override
    public String getCode() {
        return CollectConst.DISK;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess(){
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
