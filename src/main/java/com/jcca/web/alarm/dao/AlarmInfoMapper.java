package com.jcca.web.alarm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.component.quartz.alarm.bean.UnhealthyAsset;
import com.jcca.web.alarm.controller.bean.AlarmInfoPageQuery;
import com.jcca.web.alarm.controller.bean.AssetAlarmReq;
import com.jcca.web.alarm.dao.bean.QueryExportByTypeReq;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.data.AbnormalAssetQuery;
import com.jcca.web.alarm.service.data.ExportAlarmReportBean;
import com.jcca.web.alarm.vo.AlarmDetailVo;
import com.jcca.web.alarm.vo.AlarmExportVo;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.common.service.bean.ThreeDAlarmReq;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web2.dto.AlarmPageDto;
import com.jcca.web2.dto.CabinetAlarmQueryDto;
import com.jcca.web2.dto.DialogsAlarmListDto;
import com.jcca.web2.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 告警信息
 *
 * @author Lvyp
 */
public interface AlarmInfoMapper extends BaseMapper<AlarmInfo> {

    List<AlarmExportVo> findExportAlarm(@Param("ids") List<String> ids);

    AlarmDetailVo findDetailById(String id);

    AlarmInfo findOneAlarm(Map<String, Object> paramMap);

    List<AlarmUnconfirmVo> findUnconfirmAlarm(Map<String, Object> paramMap);

    List<AlarmExportVo> findExportAllAlarm(AlarmInfoPageQuery query);

    List<AlarmUnconfirmVo> findAssetAlarm(AssetAlarmReq req);

    AlarmInfo getLastPingAlarmInfo(Map<String, Object> paramMap);

    /**
     * 查询未确认或者未恢复的告警
     *
     * @param corrElationId
     * @param assetId
     * @return
     */
    @Select("select * from ALARM_INFO where (STATUS=1 or ALARM_STATE =1) and ASSET_ID=#{assetId} and CORRELATION_ID=#{corrElationId} and ALARM_CODE = #{alarmCode}")
    List<AlarmInfo> selectValidAlarmByCorrElationIdAndAlarmCode(@Param("corrElationId") String corrElationId, @Param("assetId") String assetId, @Param("alarmCode") String alarmCode);

    @Select("select * from ALARM_INFO where (STATUS=1 or ALARM_STATE =1) and ASSET_ID=#{assetId} and CORRELATION_ID=#{corrElationId}")
    List<AlarmInfo> selectValidAlarmByCorrElationId(@Param("corrElationId") String corrElationId, @Param("assetId") String assetId);

    List<String> listTitle(@Param("showJcca") Integer showJcca);

    @Select("select min(ALARM_LEVEL) from ALARM_INFO where (STATUS = 1 OR (STATUS = 2 AND ALARM_STATE = 1)) and BLANK=1 and ASSET_ID=#{assetId}")
    Byte queryMaxAlarmLevel(@Param("assetId") String assetId);

    @Select("select a.ORG_ID,a.OCCUR_TIME,o.TYPE  from SYS_ORG o RIGHT join (select ORG_ID,max(OCCUR_TIME) as OCCUR_TIME from ALARM_INFO a where  STATUS=1 and ALARM_LEVEL =1 and ALARM_STATE=1 and BLANK!=2 group BY ORG_ID) a on a.ORG_ID=o.ID")
    List<AlarmInfo> getOccurTime();

    /**
     * 查询告警下的所有解决方案
     *
     * @param id
     * @return
     */
    @Select("select DISTINCT r.PLAN_STR from ALARM_INFO a LEFT JOIN ALARM_EVENT_REL e ON a.ID = e.ALARM_ID LEFT JOIN ALARM_EVENT t ON e.EVENT_ID = t.id and t.EVENT_LEVEL != 1 LEFT JOIN ALARM_REPOSITORY r ON r.id = t.REPOSITORY_ID where a.ID = #{id} and r.PLAN_STR is not NULL")
    List<String> queryDescriptionList(@Param("id") String id);

    @Select("select min(ALARM_LEVEL) FROM ALARM_INFO WHERE (ALARM_STATE = 1 OR STATUS = 1) AND BLANK = 1 AND ORG_ID = #{orgId}")
    Integer queryMaxAlarmLevelByOrgId(@Param("orgId") String orgId);

    Integer queryMaxAlarmLevelByOrgIds(@Param("orgIds") List<String> orgIds);

    /**
     * 查询类型下设备的所有告警信息
     *
     * @param type
     * @return
     */
    List<ExportAlarmReportBean> selectExportListByType(QueryExportByTypeReq type);

    /**
     * 查询制定机构下设备的所有告警
     *
     * @param req
     * @return
     */
    List<ExportAlarmReportBean> selectExportListByOrgIds(QueryExportByTypeReq req);

    /**
     * 批量恢复未确认或者未恢复的告警
     */
    @Update("<script> update ALARM_INFO a set a.STATUS = 2,a.ALARM_STATE=2,a.IS_SHOW_RECOVER=1,a.CONTENT=(a.CONTENT || '--事件类型被修改，系统自动确认恢复历史告警') where (a.STATUS=1 or a.ALARM_STATE=1) and ID in "
            + "<foreach item='item' index='index' collection='alarmIdListItem' open='(' separator=',' close=')'>"
            + "#{item} "
            + "</foreach></script>")
    int batchRecoverAlarm(@Param("alarmIdListItem") List<String> alarmIdListItem);

    @Select("select * from ALARM_INFO where STATUS =1 and ALARM_STATE=1 and ALARM_LEVEL=#{level} and OCCUR_TIME < to_date(#{date},'yyyy-mm-dd hh24:mi:ss')")
    List<AlarmInfo> findKeepAlarm(int level, String date);

    @Select("select * from  (select ASSET_ID as assetId,count(*) as num from ALARM_INFO where OCCUR_TIME > to_date(#{date},'yyyy-mm-dd hh24:mi:ss')and  TITLE in (select name from ALARM_EVENT_GROUP where EVENT_TYPE_IDS in (select DISTINCT(EVENT_TYPE_ID) from ALARM_REPOSITORY where ALARM_CODE in ('RESTART','PROCESS_STOP')))  GROUP BY ASSET_ID) where num >#{num}")
    List<UnhealthyAsset> findUnHealthyAsset(int num, String date);

    /**
     * 查询未确认的告警总数
     *
     * @return
     */
    @Select("select * from ALARM_INFO where STATUS = 1")
    List<AlarmInfo> selectUnAscertainAlarm();

    @Update("UPDATE ALARM_INFO  set STATUS=2, ALARM_STATE=2,IS_SHOW_RECOVER=1,CONTENT=concat(CONTENT,'--取消阈值监控默认恢复')  where CONTENT like concat(concat('%进程%',#{processName}),'%') and (STATUS=1 or ALARM_STATE=1)  and asset_id=#{assetId}")
    void recoverProcess(String processName,String assetId);

    List<AlarmUnconfirmVo> findBizAlarm(AssetAlarmReq req);

    @Select("UPDATE ALARM_INFO  set STATUS=2, ALARM_STATE=2,IS_SHOW_RECOVER=1,CONTENT=concat(CONTENT,'--取消阈值监控默认恢复')  where (STATUS = 1 or ALARM_STATE = 1) and DESCRIPTION like concat(concat('%',#{msg}),'%') and asset_id=#{assetId}")
    Integer recoverAlarm(@Param("msg") String msg, @Param("assetId") String assetId);

    List<AlarmInfo> broadcastAlarmList(SysConfig sysConfig);

    @Select("UPDATE ALARM_INFO  set STATUS=2, ALARM_STATE=2,IS_SHOW_RECOVER=1,CONTENT=concat(CONTENT,'--取消阈值监控默认恢复')  where (STATUS = 1 or ALARM_STATE = 1) and DESCRIPTION like concat(concat('%',#{msg}),'%')")
    Integer recoverAlarmByMsg(@Param("msg") String msg);

    /**
     * 查询机柜中的告警信息列表
     *
     * @param query
     * @return
     */
    List<CabinetAlarmInfoVo> selectCabinetAlarmV2(CabinetAlarmQueryDto query);

    /**
     * 查询大屏告警列表
     *
     * @param query
     * @return
     * @author Lvyp
     */
    List<DialogsAlarmListVo> queryDialogsVoListV2(DialogsAlarmListDto query);


    /**
     * 查3D页面告警列表
     *
     * @param query
     * @return
     * @author SOPHIA
     */

    List<DialogsAlarmListVo> queryCenterDialogsVoListV2(@Param("eventCategory") String eventCategory);


    /**
     * 查询告警列表
     *
     * @param alarmCode
     * @param assetId
     * @param alarmFlag
     * @return
     */
    AlarmInfo getAssetAlarmV2(@Param("alarmCode") String alarmCode, @Param("assetId") String assetId, @Param("alarmFlag") String alarmFlag);


    @Select("select i.id as id,I.ASSET_ID AS assetId ,i.ALARM_LEVEL as \"level\" ,i.ALARM_STATE as isRecover,i.STATUS as isVerify from (select asset_id from ASSET_ATTACH where ROOM_ID=#{roomId1} or ROOM_ID =#{roomId2})a join ALARM_INFO i on a.asset_id=i.ASSET_ID where (i.STATUS=1)")
    List<ThreeDAlarmReq> getThreeDAlarm(String roomId1, String roomId2);

    /**
     * 查询数量
     *
     * @param query
     * @return
     */
    Integer queryAbnormalAssetV2(AbnormalAssetQuery query);

    /**
     * 分页查询
     *
     * @param page
     * @param query
     * @return
     */
    IPage<AlarmPageVo> pageV2(Page page, @Param("query") AlarmPageDto query);

    /**
     * 根据等级分类
     *
     * @param query
     * @return
     */
    List<AlarmPageStatisticsVo> statisticsV2(@Param("query") AlarmPageDto query);


    /**
     * 查询数量
     * 将符合条件的告警更新为确定恢复
     *
     * @param alarmCode
     * @param assetId
     * @param flag
     * @param msg
     */
    void recoverAlarmV2(@Param("alarmCode") String alarmCode, @Param("assetId") String assetId, @Param("flag") String flag, @Param("msg") String msg);

    /**
     * 查询未确认或者未恢复的告警
     *
     * @return
     */
    @Select("select * from ALARM_INFO where (STATUS = 1 or ALARM_STATE = 1) and ALARM_CODE =#{alarmCode}")
    AlarmInfo selectUnOverAlarm(@Param("alarmCode") String alarmCode);

    @Select("select min(ALARM_LEVEL) as alarmLevel ,ASSET_ID as assetId from ALARM_INFO where ORG_ID =#{orgId}  group by ASSET_ID")
    List<WebAssetAlarmVo> getAssetAlarmByOrg(String orgId);

}
