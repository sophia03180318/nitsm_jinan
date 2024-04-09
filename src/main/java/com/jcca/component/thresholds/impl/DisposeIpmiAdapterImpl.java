package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.bean.CpuIpmi;
import com.jcca.common.utils.bean.SensorItsm;
import com.jcca.common.utils.bean.SensorQueryResp;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.enums.SensorStatusEnum;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.CollectSensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 查询
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeIpmiAdapterImpl implements CollectAdapter {

    @Resource
    private AssetService assetServ;
    @Resource
    private CollectSensorService sensorServ;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private EventLogicService eventLogicServ;

    @Override
    public void dispose(JSONArray data) {
        if (log.isDebugEnabled()) {
            log.debug("【ITSM接到IPMI采集数据】：" + data.toString());
        }
        List<SensorQueryResp> ipmiList = JSONUtil.toList(data, SensorQueryResp.class);
        if (ipmiList.isEmpty()) {
            return;
        }
        List<CollectSensor> sensorList = new ArrayList<CollectSensor>();
        for (SensorQueryResp ipmiBody : ipmiList) {
            String collectCode = ipmiBody.getCollectCode();
            Date date = new Date();
            date.setTime(Long.valueOf(ipmiBody.getCollectDate()));

            String assetId = ipmiBody.getAssetId();
            Asset asset = assetServ.getById(assetId);
            if (Objects.isNull(asset)) {
                log.error("【ITSM处理IPMI数据失败】：", "资产主键空");
            }
            List<SensorItsm> sysBrdList = ipmiBody.getSysBrdList();
            List<SensorItsm> fanList = ipmiBody.getFanList();
            List<SensorItsm> temList = ipmiBody.getTemList();
            List<SensorItsm> ledList = ipmiBody.getLedList();
            List<SensorItsm> manuList = ipmiBody.getManuList();
            List<CpuIpmi> cpuList = ipmiBody.getCpuList();
            List<SensorItsm> logList = ipmiBody.getLogList();


            if (Objects.isNull(sysBrdList)) {
                sysBrdList = new ArrayList<SensorItsm>();
            }
            if (Objects.isNull(fanList)) {
                fanList = new ArrayList<SensorItsm>();
            }
            if (Objects.isNull(temList)) {
                temList = new ArrayList<SensorItsm>();
            }
            if (Objects.isNull(ledList)) {
                ledList = new ArrayList<SensorItsm>();
            }
            if (Objects.isNull(logList)) {
                logList = new ArrayList<SensorItsm>();
            }

            List<SensorItsm> logs = logList.stream().map(l -> {
                l.setMsg(l.getValue() + "      " + l.getMsg());
                return l;
            }).collect(Collectors.toList());

/*       CPU 因为没用 暂时不存库
         if (Objects.nonNull(manuList) && Objects.nonNull(cpuList)) {
                for (CpuIpmi cpu : cpuList) {
                    for (SensorItsm sensorItsm : manuList) {
                        if (cpu.getName().equals(sensorItsm.getName())) {
                            cpu.setManu(sensorItsm.getValue());
                        }
                    }
                }
            }*/

            for (SensorItsm sysBrd : sysBrdList) {
                addSensorItsm(sensorList, SensorTypeEnum.POWER, collectCode, date, assetId, sysBrd);
            }
            for (SensorItsm fan : fanList) {
                addSensorItsm(sensorList, SensorTypeEnum.FAN, collectCode, date, assetId, fan);
            }
            for (SensorItsm tem : temList) {
                addSensorItsm(sensorList, SensorTypeEnum.GAUGE, collectCode, date, assetId, tem);
            }
            for (SensorItsm led : ledList) {
                addSensorItsm(sensorList, SensorTypeEnum.LED, collectCode, date, assetId, led);
            }
            for (SensorItsm log : logs) {
                addSensorItsm(sensorList, SensorTypeEnum.LOG, collectCode, date, assetId, log);
            }
        }
        if (!sensorList.isEmpty()) {
            sensorServ.updateRealTimeData(sensorList);
            sensorServ.updateBatchByAssetId(sensorList);
        }
    }

    private void addSensorItsm(List<CollectSensor> sensorList, SensorTypeEnum type, String collectCode, Date date,
                               String assetId, SensorItsm sysBrd) {
        CollectSensor sensor = new CollectSensor();
        sensor.setId(MyIdUtil.getId());
        sensor.setAssetId(assetId);
        sensor.setCollectCode(collectCode);
        sensor.setCollectTime(date);
        sensor.setCreateTime(new Date());
        sensor.setSensorType(type.name());
        sensor.setValue(sysBrd.getValue());
        sensor.setDescStr(sysBrd.getMsg());
        if (StrUtil.isEmpty(sysBrd.getMsg())) {
            sensor.setDescStr(sysBrd.getValue());
        }
        sensor.setSerialNumberName(sysBrd.getName());
        if ("0".equals(sysBrd.getStatus()) || "1".equals(sysBrd.getStatus())) {
            sensor.setStatus(SensorStatusEnum.NORMAL.getCode());
        } else {
            sensor.setStatus(SensorStatusEnum.UNNORMAL.getCode());
        }

        if (SensorTypeEnum.GAUGE == type) {
            //温度类型 阶段阈值
            try {
                VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(sysBrd.getValue()), EventUniqueCode.COLLECTOR_SENSOR_GAUGE);
                List<CreateEventReq> eventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.SENSOR_GAUGE, date, assetId, sysBrd.getValue(), sysBrd.getMsg());
                for (CreateEventReq createEventReq : eventList) {
                    eventLogicServ.addEvent(createEventReq);
                }
            } catch (Exception e) {
                log.error("处理温度事件异常：{}", e.getMessage(), e);
            }
        }

        sensorList.add(sensor);
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.SYS_PORT;
    }

}
