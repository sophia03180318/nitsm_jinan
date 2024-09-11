package com.jcca.component.quartz.dh;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.Entity.DongHuanEntity;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
import com.jcca.dataProcessing.dataAdpater.DongHuanAdapter;
import com.jcca.dataProcessing.dataAdpater.MQAdapter;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.controller.bean.Repository;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.common.entity.Alarm;
import com.jcca.web.common.service.DhAlarmService;
import com.jcca.web.common.service.PropertyService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.entity.IBMConnection;
import com.jcca.web.ibmMQ.entity.IBMMonitor;
import com.jcca.web.ibmMQ.service.ConnectionService;
import com.jcca.web.ibmMQ.service.MonitorService;
import com.jcca.web.ibmMQ.service.StatisticalDataService;
import lombok.extern.log4j.Log4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 动环状态
 *
 * @author sophia
 */
@Log4j
@Service
@DisallowConcurrentExecution
public class QuartzDhStatusJob extends QuartzJobBean {
    @Value("${dhUrl}")
    private String dhUrl;
    @Resource
    private DataProcessManager dataProcessManager;
    @Resource
    private DhAlarmService alarmService;
    @Resource
    private AssetService assetServ;
    @Resource
    private PropertyService propertyService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            String body = HttpRequest.get(dhUrl + ":9993/tcp/getAlarm").setReadTimeout(10000).setConnectionTimeout(10000).execute().body();
            String code = JSONUtil.parseObj(body).getStr("code", "999");
            if (!"200".equals(code)) {
                log.error("动环告警通讯返回失败, code:"+code);
            } else {
                List<Alarm> alarmLists = alarmService.getAlarm();
                for (Alarm alarm : alarmLists) {
                    if (ObjectUtil.isNull(alarm.getDeviceId())){
                        alarm.setDeviceId( propertyService.getById(alarm.getPropertyId()).getParentID());
                    }
                    AssetMsgVo asset = assetServ.findMsgById(alarm.getDeviceId());
                    if (Objects.isNull(asset)) {
                        log.error("动环告警收到未录入数据，资产ID不存在：" + JSONUtil.toJsonStr(alarm));
                    }
                    try {
                        DongHuanEntity dongHuanEntity = new DongHuanEntity();
                        dongHuanEntity.setAssetIp(asset.getIp());
                        dongHuanEntity.setAssetId(alarm.getDeviceId());
                        dongHuanEntity.setFlag(alarm.getPropertyId());
                        dongHuanEntity.setCreateTime(alarm.getCreateTime());
                        dongHuanEntity.setOriginalMsg(alarm.getDescc());
                        dongHuanEntity.setAssetName(asset.getAssetName());
                        DongHuanAdapter dhAdapter = (DongHuanAdapter) dataProcessManager.getAdapater("dongHuanAdapter");
                        dhAdapter.dispose(dongHuanEntity);
                    } catch (Exception e2) {
                        log.error("接收动环推送设备告警失败: "+e2.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("动环告警通讯失败: " + e.toString());

        }
    }
}



