package com.jcca.component.quartz.mq;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
import com.jcca.dataProcessing.dataAdpater.MQAdapter;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.entity.IBMConnection;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.service.ConnectionService;
import com.jcca.web.ibmMQ.service.MonitorService;
import com.jcca.web.ibmMQ.service.StatisticalDataService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * MQ队列管理器状态
 *
 * @author sophia
 */
@Service
@DisallowConcurrentExecution
public class QuartzMQStatusJob extends QuartzJobBean {

    @Resource
    private MonitorService monitorService;
    @Resource
    ConnectionService connectionService;
    @Resource
    private AssetService assetServ;
    @Resource
    private StatisticalDataService statisticalDataManager;


    @Resource
    private DataProcessManager dataProcessManager;




    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_DATA, "开始巡检MQ连接状态", "");
        List<IBMConnection> list = connectionService.list();
        Asset asset;
        for (IBMConnection connection : list) {
            asset = assetServ.findOneByIp(connection.getConnectHost());
            if (Objects.isNull(asset)) {
                AppLogUtils.buildLogWarn(LogFunctionEnum.CRON_DATA, "MQ添加事件出现异常!：未录入关联资产", connection.getConnectHost());
                continue;
            }

            IBMMonitor ibmMonitor = monitorService.findMonitor(connection.getConnectName(), Monitor.MonitorNameType.QMGR.getValue());
            Monitor monitor = Monitor.getMonitor(ibmMonitor);
            StatisticalData data = this.statisticalDataManager.queryMonitorDetails(monitor);

            boolean oldStatus = false;
            if (Objects.equals(connection.getStatus(), "OK")) {
                oldStatus = true;
            }

            boolean newStatus = true;
            if (ObjectUtil.isNull(data.getHealthState()) || !data.getHealthState().getValue().equals("OK")) {
                newStatus = false;
            }
            if (newStatus) {
                connection.setStatus("OK");
            } else {
                connection.setStatus("NO");
            }
            connectionService.updateById(connection);

            if (ObjectUtil.isNotNull(connection.getStatus())) {


                MQMonitorEntity monitorEntity = new MQMonitorEntity();
                monitorEntity.setCollectTime(new Date().getTime());
                monitorEntity.setAssetId(asset.getId());
                monitorEntity.setAssetIp(asset.getIp());
                monitorEntity.setStatus(connection.getStatus().equals("OK") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode());

                MQAdapter mqAdapter = (MQAdapter) dataProcessManager.getAdapater("MQAdapter");
                mqAdapter.dispose(monitorEntity);
            }

        }
    }
}



