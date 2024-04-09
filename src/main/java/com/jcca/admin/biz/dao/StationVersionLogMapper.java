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
    @Select("select * from M_STATION_VERSION_LOG  where id = (select max(CAST(id as integer)) from M_STATION_VERSION_LOG where STATION_ID = #{orgId})")
    StationVersionLog selectLasterLogByStationId(@Param("orgId") String orgId);

    /**
     * 查询车站ID通过版本号
     *
     * @param version
     * @return
     */
    @Select("select e.STATION_ID from M_STATION_VERSION_LOG e where e.VERSION=#{version}")
    List<String> selectOrgIdByVersion(@Param("version") String version);

    /**
     * 查看车站是否有正在进行的升级任务
     *
     * @param stationId
     * @return
     */
    @Select("select * from M_STATION_VERSION_LOG e where e.STATION_ID = #{stationId} and e.STATUS != 'UPDATE_SUCCESS' ")
    StationVersionLog selectLoadingJob(@Param("stationId") String stationId);

    /**
     * 查询更新过的jar 的名字
     *
     * @param stationId
     * @return
     */
    @Select("select JAR_NAME from M_STATION_VERSION_LOG e where e.STATION_ID = #{stationId} and e.STATUS = 'UPDATE_SUCCESS' GROUP BY JAR_NAME")
    List<String> listJarName(@Param("stationId") String stationId);

    /**
     * 查询jar最后一次更新记录
     *
     * @param orgId
     * @param jarName
     * @return
     */
    @Select("select * from M_STATION_VERSION_LOG e where id = (select max(CAST(id as integer)) from M_STATION_VERSION_LOG t where t.STATION_ID = #{stationId} and t.STATUS = 'UPDATE_SUCCESS' and t.JAR_NAME=#{jarName})")
    StationVersionLog lastLog(@Param("stationId") String stationId, @Param("jarName") String jarName);

    /**
     * 查询是否有此版本成功的记录
     *
     * @param stationId
     * @param id
     */
    @Select("select * from M_STATION_VERSION_LOG e where id = (select max(CAST(id as integer)) from M_STATION_VERSION_LOG t where t.STATION_ID = #{stationId} and t.STATUS = 'UPDATE_SUCCESS' and t.VERSION_MSG_ID=#{versionMsgId})")
    StationVersionLog selectBySuccessLog(@Param("versionMsgId") String versionMsgId, @Param("stationId") String stationId);


}
