package com.jcca.component.client.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.component.client.StationCollectClient;
import com.jcca.component.quartz.station.QuartzStationNotifyJob;
import com.jcca.component.quartz.station.bean.StationNotifyBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.entity.AlarmEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 车站采集器通讯客户端
 *
 * @author lyp
 */
@Slf4j
@Service
public class StationCollectClientImpl implements StationCollectClient {

    private static final String HTTP_HEAD = "http://";
    private static final String REFRESH_URI = "/station/home/refresh";
    private static final String REMOVE_JAR_URI = "/station/home/removeJar";
    private static final String QUERY_UPDATE_RESULT_URI = "/station/home/result";
    private static final String NOTIFY_PING_STATUS = "/station/notify/pingStatus";
    private static final String NOTIFY_ALARM_STATUS = "/station/notify/alarmStatus";
    private static final String NOTIFY_REMOVE_STATUS = "/station/notify/remove";

    private static final List<String> PING_CODE = Arrays.asList("PING_STOP", "PING_ALL_STOP", "PING_OTHER_STOP");
    /**
     * 车站上送的告警信息
     */
    private static final String STATION_UNICODE = "STATION_SEND_ALARM_";

    @Resource
    private AssetService assetServ;
    @Resource
    private StationService stationServ;

    @Override
    public RestBean updateJar(String jarPath, String stationId, Long jarSize) {
        Station station = stationServ.getById(stationId);
        if (Objects.isNull(station)) {
            return RestBean.ofError("车站ID：" + stationId + "不存在");
        }
        JSONObject reqJson = new JSONObject();
        reqJson.put("jarPath", jarPath);
        reqJson.put("rootPath", station.getFilePath());
        reqJson.put("jarSize", jarSize);

        log.info("StationCollectClient发起开始更新命令，REQ：{}", reqJson.toString());
        try {
            String body = stationServ.sendPostToStation(REFRESH_URI, stationId, reqJson.toString());
            log.info("StationCollectClient发起开始更新命令，收到车站采集器响应：{}", body);
            if (!JSONUtil.isJson(body)) {
                return RestBean.ofError("消息响应错误，更新失败，请手动重试："+body);
            }
            JSONObject respJson = JSONUtil.parseObj(body);
            RestBean resp = JSONUtil.toBean(respJson, RestBean.class);

            return resp;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RestBean.ofError("网络问题导致更新失败，请手动重试");
        }
    }

    @Override
    public RestBean removeJar(String jarPath, String stationId) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("jarPath", jarPath);
        if(LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_STATION)){
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_STATION, stationId,"StationCollectClient发起删除车站JAR命令，"+ reqJson.toString()));
        }
        try {
            String body =  stationServ.sendPostToStation(REMOVE_JAR_URI,stationId,reqJson.toString());
            if(!JSONUtil.isJson(body)){
                return RestBean.ofError("消息响应错误，删除JAR失败，请手动重试："+body);
            }
            if(LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_STATION)){
                log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_STATION, stationId,"StationCollectClient发起删除车站JAR命令，收到车站采集器响应："+ body));
            }


            JSONObject respJson = JSONUtil.parseObj(body);
            RestBean resp = JSONUtil.toBean(respJson, RestBean.class);

            return resp;
        } catch (Exception e) {
            if(LogInputUtils.inputError(ServerTypeEnum.SYSTEM_STATION)){
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_STATION, ErrorCodeEnum.SYSTEM_STATION_DELETE,"","网络问题导致删除JAR失败，请排查网络问题后重试"+e.getMessage()));
            }
            return RestBean.ofError("网络问题导致删除JAR失败，请排查网络问题后重试");
        }

    }

    @Override
    public RestBean queryUpdateResult(String stationId) {
        log.info("StationCollectClient发起查询更新结果，REQ：{}", stationId);

        try {
            String body =  stationServ.sendPostToStation(QUERY_UPDATE_RESULT_URI,stationId,"");
            if(!JSONUtil.isJson(body)){
                return RestBean.ofError("查询失败");
            }

            if(LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_STATION)){
                log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_STATION, stationId,"StationCollectClient发起查询更新结果，"+ body));
            }
            JSONObject respJson = JSONUtil.parseObj(body);
            RestBean resp = JSONUtil.toBean(respJson, RestBean.class);

            return resp;
        } catch (Exception e) {
            if(LogInputUtils.inputError(ServerTypeEnum.SYSTEM_STATION)){
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_STATION, ErrorCodeEnum.SYSTEM_STATION_ADD,"","查询失败"+e.getMessage()));
            }
            return RestBean.ofError("查询失败");
        }
    }

    @Override
    public void notifyStationPingStatus(String assetId, Boolean status,String uniqueCode) {
        if(!PING_CODE.contains(uniqueCode)){
            return ;
        }

        Asset asset = assetServ.getById(assetId);
        if (Objects.isNull(asset)) {
            log.error("警告：设备可能已经被删除,通知车站采集器Ping状态失败。设备ID：" + assetId);
            return;
        }
        Station station = stationServ.getById(asset.getOrgId());
        if (Objects.isNull(station)) {
            return;
        }

        StationNotifyBean reqJson = new StationNotifyBean();
        reqJson.setAssetId(assetId);
        reqJson.setStatus(status);
        log.info("通知车站Ping状态请求：地址【" + station.getIp() + "】" + "参数：" + JSONUtil.toJsonStr(reqJson));
        String body = stationServ.sendPostToStation(NOTIFY_PING_STATUS, station.getOrgId(), JSONUtil.toJsonStr(reqJson));
        log.info("通知车站Ping状态返回：" + body);
        String errorFlag = "通讯失败";
        if (body.contains(errorFlag)) {
            //计入发送列表重新发送
            reqJson.setUrl(NOTIFY_PING_STATUS);
            reqJson.setOrgId(station.getOrgId());
            if (!QuartzStationNotifyJob.NOTIFY_QUEUE.contains(reqJson)) {
                QuartzStationNotifyJob.NOTIFY_QUEUE.add(reqJson);
            }
        }


    }


    @Override
    public void notifyStationAlarmStatus(String assetId, Boolean status,String uniqueCode) {
        if(!uniqueCode.contains(STATION_UNICODE)){
            return ;
        }

        Asset asset = assetServ.getById(assetId);
        if (Objects.isNull(asset)) {
            log.error("警告：设备可能已经被删除,通知车站采集器告警状态失败。设备ID：" + assetId);
            return;
        }
        Station station = stationServ.getById(asset.getOrgId());
        if (Objects.isNull(station)) {
            //不是车站的设备或者没配置车站
            return;
        }

        StationNotifyBean reqJson = new StationNotifyBean();
        reqJson.setFlag(uniqueCode + asset.getIp());
        reqJson.setStatus(status);
        log.info("通知车站告警状态请求：地址【" + station.getIp() + "】" + "参数：" + JSONUtil.toJsonStr(reqJson));
        String body = stationServ.sendPostToStation(NOTIFY_ALARM_STATUS, station.getOrgId(), JSONUtil.toJsonStr(reqJson));
        log.info("通知车站告警状态返回：" + body);

        String errorFlag = "通讯失败";
        if (body.contains(errorFlag)) {
            //计入发送列表重新发送
            reqJson.setUrl(NOTIFY_ALARM_STATUS);
            reqJson.setOrgId(station.getOrgId());
            if (!QuartzStationNotifyJob.NOTIFY_QUEUE.contains(reqJson)) {
                QuartzStationNotifyJob.NOTIFY_QUEUE.add(reqJson);
            }
        }
    }

    @Override
    public void notifyResetStatus(AlarmEvent event) {
        Asset asset = assetServ.getById(event.getAssetId());
        if(Objects.isNull(asset)){
            log.error("警告：设备可能已经被删除,通知车站删除状态位失败。设备ID："+event.getAssetId());
            return ;
        }
        Station station = stationServ.getById(asset.getOrgId());
        if(Objects.isNull(station)){
            return ;
        }
        String uniqueCode = event.getUniqueCode();
        JSONObject reqJson = new JSONObject();
        try {
            if(PING_CODE.contains(uniqueCode)){
                reqJson.put("assetId",asset.getId());
                String body =  stationServ.sendPostToStation(NOTIFY_REMOVE_STATUS,station.getOrgId(),reqJson.toString());
                log.info("信息记录：通知车站"+station.getIp()+"采集器删除标记位车站响应："+body);
            }else if(uniqueCode.startsWith(STATION_UNICODE)){
                reqJson.put("flag",uniqueCode);
                String body =  stationServ.sendPostToStation(NOTIFY_REMOVE_STATUS,station.getOrgId(),reqJson.toString());
                log.info("信息记录：通知车站"+station.getIp()+"采集器删除标记位车站响应："+body);
            }
        }catch (Exception e){
            log.error("通知站采集器"+station.getIp()+"删除告警状态发生异常："+e.toString());
        }

    }

}
