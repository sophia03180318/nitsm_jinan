package com.jcca.admin.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.biz.entity.StationVersionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 车站版本升级记录
 *
 * @author lyp
 */
@Mapper
public interface StationVersionLogMapper extends BaseMapper<StationVersionLog> {

    /**
     * 查询最后一条升级记录
     *
     * @param orgId
     * @return
     */
    StationVersionLog selectLasterLogByStationId(@Param("orgId") String orgId);

    /**
     * 查询车站ID通过版本号
     *
     * @param version
     * @return
     */
    List<String> selectOrgIdByVersion(@Param("version") String version);

    /**
     * 查看车站是否有正在进行的升级任务
     *
     * @param stationId
     * @return
     */
    StationVersionLog selectLoadingJob(@Param("stationId") String stationId);

    /**
     * 查询更新过的jar 的名字
     *
     * @param stationId
     * @return
     */
    List<String> listJarName(@Param("stationId") String stationId);

    /**
     * 查询jar最后一次更新记录
     *
     * @param stationId
     * @param jarName
     * @return
     */
    StationVersionLog lastLog(@Param("stationId") String stationId, @Param("jarName") String jarName);

    /**
     * 查询是否有此版本成功的记录
     *
     * @param stationId
     * @param versionMsgId
     */
    StationVersionLog selectBySuccessLog(@Param("versionMsgId") String versionMsgId, @Param("stationId") String stationId);


}
