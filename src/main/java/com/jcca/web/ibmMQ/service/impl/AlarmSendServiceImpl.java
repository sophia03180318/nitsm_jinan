package com.jcca.web.ibmMQ.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.component.event.EventLogicService;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
import com.jcca.dataProcessing.dataAdpater.MQAdapter;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.ibmMQ.domain.HealthMessage;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.service.AlarmSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/31 16:10
 */
@Slf4j
@Service
public class AlarmSendServiceImpl implements AlarmSendService {

    @Resource
    private AssetService assetServ;
    @Resource
    private EventLogicService eventServ;

    //  private ListenerManager listenerManager;
    @Resource
    private DataProcessManager dataProcessManager;


//    @PostConstruct
//    public void init() {
//        listenerManager = new ListenerManager();
//        listenerManager.addDataSourceListener(new EventInfoListener());
//    }


    @Override
    public void sendAlarm(StatisticalData paramStatisticalData) {
        Monitor monitor = paramStatisticalData.getMonitor();
        String assetIp = monitor.getConnection().getHost();

        Asset asset = assetServ.findOneByIp(assetIp);
        if (Objects.isNull(asset)) {
            log.error("【MQ数据处理失败】：IP：{}的资产不存在！", assetIp);
            return;
        }

        List<HealthMessage> list = new ArrayList<HealthMessage>(paramStatisticalData.getMessages());
        for (HealthMessage healthMessage : list) {
            String messageFormat = "";
            if (monitor.getCategory().getValue().equals("Queue")) {

                MQMonitorEntity monitorEntity = new MQMonitorEntity();
                monitorEntity.setCollectTime(new Date().getTime());
                monitorEntity.setAssetId(asset.getId());
                monitorEntity.setName(monitor.getObjectName());
                if (ObjectUtil.isNotNull(healthMessage.getRule()) && ObjectUtil.isNotNull(healthMessage.getResult())) {
                    monitorEntity.setStatus(EventLevelEnum.ABNORMAL.getCode());
                    String ruleVale = healthMessage.getRule().replace("currentQDepth>", "");
                    String resultVale = healthMessage.getResult().replace("currentQDepth=", "");
                    messageFormat = "MQ队列" + monitor.getName() + "当前队列深度：" + resultVale + "，配置阈值深度：" + ruleVale + "。";
                    monitorEntity.setMessage(messageFormat);
                } else if (Objects.nonNull(healthMessage.getFlag()) && true == healthMessage.getFlag()) {
                    monitorEntity.setStatus(EventLevelEnum.NORMAL.getCode());
                    messageFormat = "MQ队列" + monitor.getName() + "队列深度阈值状态恢复。";
                    monitorEntity.setMessage(messageFormat);
                }
                MQAdapter mqAdapter = (MQAdapter) dataProcessManager.getAdapater("MQAdapter");
                mqAdapter.dispose(monitorEntity);

            }
        }
    }
}
