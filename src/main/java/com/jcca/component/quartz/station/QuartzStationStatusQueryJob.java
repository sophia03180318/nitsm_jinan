package com.jcca.component.quartz.station;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.UrlUtil;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.bean.CollectNodesMsg;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.listener.EventInfoListener;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 定时查询车站状态 10分钟检测一次采集节点状态，主要完成的任务：更新车站当前的运行状态
 *
 * @author lyp
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzStationStatusQueryJob extends QuartzJobBean {

    @Resource
    private StationService stationServ;
    @Resource
    private CollectAgent collectAgency;
    @Resource
    private AssetService assetService;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    private ListenerManager listenerManager;
    /**
     * 系统启动第一次任务跳过
     */
    private static boolean isOnce = true;

    @PostConstruct
    public void init() {
        if(Objects.isNull(listenerManager)){
            listenerManager = new ListenerManager();
            listenerManager.addDataSourceListener(new EventInfoListener());
        }
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (isOnce) {
            isOnce = false;
            return;
        }

        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_COLLECT_STATUS, DateUtil.formatLocalDateTime(LocalDateTime.now()), "开始巡检中心、车站采集器状态~");
        List<CollectNodesMsg> nodeList = null;
        try {
            nodeList = collectAgency.getCollectNodeMsg();
        } catch (CollectAgencyException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CRON_COLLECT_STATUS, "检测采集节点状态失败", e.getMessage());
            return;
        }

        if (nodeList.isEmpty()) {
            AppLogUtils.buildLogError(LogFunctionEnum.CRON_COLLECT_STATUS, "检测采集节点状态失败", "通过中心采集器获取节点状态信息失败:未获取到任何节点信息");
            return;
        }

        List<Station> stationList = stationServ.list();

        for (Station station : stationList) {
            String nodeUrl = UrlUtil.getStationUrlPrefix(station);
            List<CollectNodesMsg> stationNodeList = nodeList.stream().filter(item -> nodeUrl.equals(item.getNodeUrl()))
                    .collect(Collectors.toList());

            if (stationNodeList.isEmpty()) {
                AppLogUtils.buildLogError(LogFunctionEnum.CRON_COLLECT_STATUS, station, String.format("定时任务-中心采集器缺少车站：%s 的节点信息！", station.getIp()));
                continue;
            }
            if (stationNodeList.size() != 1) {
                AppLogUtils.buildLogError(LogFunctionEnum.CRON_COLLECT_STATUS, stationNodeList, "定时任务-中心采集器存在多个节点信息！");
                continue;
            }

            CollectNodesMsg nodeMsg = stationNodeList.get(0);
            // 车站采集器离线
            if (CollectNodesMsg.DOWN.equals(nodeMsg.getNodeState())) {
                station.setRunStatus("异常");
                continue;
            }

            // 正常
            station.setRunStatus("正常");
        }

        if (!stationList.isEmpty()) {
            stationServ.updateBatchById(stationList);
        }

        //
        for (CollectNodesMsg node : nodeList) {
            if (StrUtil.isEmpty(node.getNodeIp())) {
                continue;
            }
            Asset asset = assetService.findOneByIp(node.getNodeIp());

            if (Objects.isNull(asset)) {
                continue;
            }

            String eventMapKey = asset.getIp() + "_" + asset.getId();
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setIsEvent(true);
            changeInfo.setCollectTime(new Date());
            String originalMsg = "";
            int status = -1;
            if (CollectNodesMsg.IS_CENTER.equals(node.getNodeType())) {
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                if (CollectNodesMsg.DOWN.equals(node.getNodeState())) {
                    originalMsg = String.format("中心采集器:%s,已掉线，请及时处理，IP：%s", node.getNodeName(), node.getNodeIp());
                    alarmTempReq.setCollectValue("掉线");
                } else {
                    originalMsg = String.format("中心采集器:%s,状态正常！", node.getNodeName());
                    alarmTempReq.setCollectValue("正常");
                    status = 1;
                }

                alarmTempReq.setOrgMsg(originalMsg);

                IEvent event = eventInfoChangeManagerService.creatChangeEvent(asset.getId(), changeInfo, StatusInfoChangeTypeEnum.event_jcca_center.getCode(), eventMapKey, status, alarmTempReq,null);
                event.setDescStr(originalMsg);
                listenerManager.dispatureEvent(event);
            } else {
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                if (CollectNodesMsg.DOWN.equals(node.getNodeState())) {
                    originalMsg = String.format("车站采集器:%s,已掉线，请及时处理，IP：%s", node.getNodeName(), node.getNodeIp());
                    alarmTempReq.setCollectValue("掉线");
                } else {
                    originalMsg = String.format("车站采集器:%s,状态正常！", node.getNodeName());
                    alarmTempReq.setCollectValue("正常");
                    status = 1;
                }

                alarmTempReq.setOrgMsg(originalMsg);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(asset.getId(), changeInfo, StatusInfoChangeTypeEnum.event_jcca_station.getCode(), eventMapKey, status, alarmTempReq,null);
                event.setDescStr(originalMsg);
                listenerManager.dispatureEvent(event);
            }
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.CRON_COLLECT_STATUS, DateUtil.formatLocalDateTime(LocalDateTime.now()), "巡检中心、车站采集器状态结束~");
    }

}
