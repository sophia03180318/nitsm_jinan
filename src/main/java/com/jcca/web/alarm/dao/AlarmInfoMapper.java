package com.jcca.web.alarm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcca.component.quartz.alarm.bean.UnhealthyAsset;
import com.jcca.web.ai.vo.AlarmVo;
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
import com.jcca.web2.dto.*;
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
    List<AlarmInfo> selectValidAlarmByCorrElationIdAndAlarmCode(@Param("corrElationId") String corrElationId, @Param("assetId") String assetId, @Param("alarmCode") String alarmCode);

    List<AlarmInfo> selectValidAlarmByCorrElationId(@Param("corrElationId") String corrElationId, @Param("assetId") String assetId);

    List<String> listTitle(@Param("showJcca") Integer showJcca);

    Byte queryMaxAlarmLevel(@Param("assetId") String assetId);

    List<AlarmInfo> getOccurTime();

    /**
     * 查询告警下的所有解决方案
     *
     * @param id
     * @return
     */
    List<String> queryDescriptionList(@Param("id") String id);

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
    int batchRecoverAlarm(@Param("alarmIdListItem") List<String> alarmIdListItem);

    List<AlarmInfo> findKeepAlarm(int level, String date);

    List<UnhealthyAsset> findUnHealthyAsset(int num, String date);

    /**
     * 查询未确认的告警总数
     *
     * @return
     */
    List<AlarmInfo> selectUnAscertainAlarm();

    void recoverProcess(String processName,String assetId);

    List<AlarmUnconfirmVo> findBizAlarm(AssetAlarmReq req);

    Integer recoverAlarm(@Param("msg") String msg, @Param("assetId") String assetId);

    List<AlarmInfo> broadcastAlarmList(SysConfig sysConfig);

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
    AlarmInfo selectUnOverAlarm(@Param("alarmCode") String alarmCode);

    List<WebAssetAlarmVo> getAssetAlarmByOrg(String orgId);

    AlarmCountDto countAlarm(AlarmPageDto req);

    List<AlarmUnhandledDto> findUnhandledAlarm(AlarmPageDto req);

    List<AlarmUnhandledDto> find5TimesUp(AlarmPageDto req);

    List<String> getRemarksByAlarmCode(String alarmCode);

    List<AlarmVo> findAiAlarm(String assetId);
}
