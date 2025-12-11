package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.log.annotation.MyLogback;
import com.jcca.common.log.constant.LogFunctionConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.vo.RoomVo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author sophia
 * @description TODO donghuan信息适配器
 * @className donghuanAdapter
 * @date 2025/12/05 16:17
 * @since 2.1.0.0
 */
@Component("donghuanAdapter")
public class DonghuanAdapter extends AssetIpAdd implements IAdapter<ItsmQueueEntity> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;


    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void dispose(ItsmQueueEntity data) {
        data.setCollectTime(data.getAlarmTime().getTime());
        data.setAssetId(data.getAssetId());

        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_linkQuality.getCode() + ":" + data.getAlarmType(), "monitor", true);

        Future<Integer> future = excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    dataProcessManager.donghuanHandlerRequest(data);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + data.getAssetId() + "congXingHandlerRequest 抛出异常", e);

                }
                return 1;
            }
        });

        if (data.getInspectRecordId() != null && !"".equals(data.getInspectRecordId())) {
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
        return CollectConst.DongHuan;
    }

    @Override
    @MyLogback(code = LogFunctionConstant.DATA_PROCESS)
    public String dataProcess() {
        return "当前剩余处理数量：" + excutorService.getQueue().size();
    }

}
