package com.jcca.component.client;

import com.jcca.common.bean.RestBean;
import com.jcca.web.event.entity.AlarmEvent;

/**
 * 车站接口交互客户端
 *
 * @author lyp
 */
public interface StationCollectClient {

    /**
     * 通知车站采集器更新jar
     *
     * @param jarPath 车站存放此jar的路径
     * @param jarSize 此版本jar 的大小
     * @return 返回成功代表已经启动更新，是否成功要等待通知或主动查询
     */
    RestBean updateJar(String jarPath, String stationId, Long jarSize);

    /**
     * 删除车站已经上传的jar
     *
     * @param jarPath
     * @param stationId
     * @return
     */
    RestBean removeJar(String jarPath, String stationId);

    /**
     * 查询车站更新结果
     *
     * @param stationId
     * @return
     */
    RestBean queryUpdateResult(String stationId);

    /**
     * 当ITSM处理完成ping告警的时候需要
     * 将此状态同步回车站保持一致
     *
     * 通知车站ping结果
     */
    void notifyStationPingStatus(String assetId,Boolean status,String uniqueCode);

    /**
     *  当ITSM处理完成车站告警的时候
     *  通知车站变更记录状态，与ITSM保持一致
     * @param assetId
     * @param b
     * @param uniqueCode
     */
    void notifyStationAlarmStatus(String assetId, Boolean b, String uniqueCode);

    /**
     * 车站删除标记位重新推送
     * @param event
     */
    void notifyResetStatus(AlarmEvent event);
}
