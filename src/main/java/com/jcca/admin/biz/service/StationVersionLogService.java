package com.jcca.admin.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.system.controller.bean.BeginUpdateReq;

import java.util.List;

/**
 * 车站升级记录
 *
 * @author lyp
 */
public interface StationVersionLogService extends IService<StationVersionLog> {

    /**
     * 查找车站最后一次的更新记录
     *
     * @param orgId
     * @return
     */
    StationVersionLog findLastLogByStation(String orgId);

    /**
     * 查询当前版本的所有组织
     *
     * @param version
     * @return
     */
    List<String> listorgIdByVersion(String version);

    /**
     * 提交更新
     *
     * @param req
     * @throws Exception
     */
    void beginUpdate(BeginUpdateReq req) throws Exception;

    /**
     * 开始将包上传到车站
     *
     * @param log
     */
    void uploadJarToStation(StationVersionLog log);

    /**
     * 上传成功的包开始更新
     *
     * @param item
     */
    void updateJarToStation(StationVersionLog item);

    /**
     * 查询这个车站需要更新的jar列表
     *
     * @param orgId
     * @return
     */
    List<String> listJarName(String orgId);

    /**
     * 查询
     *
     * @param orgId
     * @param jarName
     * @return
     */
    StationVersionLog lastLog(String orgId, String jarName);

    /**
     * 删除原有JAR并重新上传
     *
     * @param versionLog
     * @throws Exception
     */
    void deleteJarAndAfreshUpload(StationVersionLog versionLog) throws Exception;

    /**
     * 更新结果
     *
     * @param item
     */
    void queryUpdateResult(StationVersionLog item);

    /**
     * 查询车站运行状态
     *
     * @param station
     */
    Boolean queryStationRunStatus(Station station);

    /**
     * 查询车站 监控资产 最后一次cpu采集时间 是否超过2小时
     */
    Integer getBetweenTime(String stationId);

}
