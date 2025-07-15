package com.jcca.web.alarm.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
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
import com.jcca.web.asset.controller.bean.Repository;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.service.bean.ThreeDAlarmReq;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.construction.entity.ConstructionRecord;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web2.dto.*;
import com.jcca.web2.vo.*;

import java.util.List;
import java.util.Map;


/**
 * 告警信息
 *
 * @author
 */
public interface AlarmInfoService extends IService<AlarmInfo> {


    /**
     * 查询设备已存在的 告警状态或未确认状态
     *
     * @param alarmCode
     * @param assetId
     * @param alarmFlag
     * @return
     */
    AlarmInfo getAssetAlarm(String alarmCode, String assetId, String alarmFlag);


    /**
     * 获取导出告警数据
     *
     * @param ids
     * @return
     */
    List<AlarmExportVo> findExportAlarm(List<String> ids);

    /**
     * 告警详情
     *
     * @param id
     * @return
     */
    AlarmDetailVo findDetailById(String id);

    /**
     * 查询是否有相同但未恢复告警
     *
     * @param paramMap
     * @return
     */
    AlarmInfo findOneAlarm(Map<String, Object> paramMap);

    /**
     * 查找当前用户管理的组织内的所有未确认告警
     *
     * @return
     */
    List<AlarmUnconfirmVo> findUnconfirmAlarm(Map<String, Object> paramMap);

    /**
     * 按条件导出全部告警
     *
     * @param query
     * @return
     */
    List<AlarmExportVo> findExportAllAlarm(AlarmInfoPageQuery query);

    /**
     * 获取资产 未确认或者已确认未恢复 告警
     *
     * @param req
     * @return
     */
    List<AlarmUnconfirmVo> findAssetAlarm(AssetAlarmReq req);

    /**
     * 查询事件规则组下是否有未确认或者未恢复的告警
     *
     * @param id
     * @param assetId
     * @param alarmCode
     * @return
     */
    List<AlarmInfo> findValidAlarmByCorrElationId(String id, String assetId, String alarmCode);

    /**
     * 查询事件规则组下是否有未确认或者未恢复的告警 无需匹配告警编号
     *
     * @param id
     * @param assetId
     * @return
     */
    List<AlarmInfo> findValidAlarmByCorrElationId(String id, String assetId);

    /**
     * 处理事件告警
     *
     * @return
     */
    String exeEventAlarm(AlarmEventGroup group, AlarmEvent alarmEvent);

    /**
     * 再次处理告警
     * alarmFlag -1告警1恢复
     *
     * @return
     */
    String exeAgainEventAlarm(AlarmEventGroup group, AlarmInfo alarmInfo, Integer alarmFlag, AlarmEvent alarmEvent);

    /**
     * 获取告警信息
     *
     * @param group
     * @param alarmEvent
     * @param asset
     * @return
     */
    String formatAlarmMsg(AlarmEventGroup group, AlarmEvent alarmEvent, Asset asset);

    /**
     * 获取告警编号
     *
     * @param groupId
     * @param eventFlag
     * @return
     */
    String getEventAlarmCode(String groupId, String eventFlag);

    /**
     * 获取格式化后需要展示的告警内容
     *
     * @param content
     * @param alarmStatus
     * @param showRecover
     * @return
     */
    String getContent(String content, byte alarmStatus, Integer showRecover, String time);

    /**
     * 标题
     *
     * @return
     */
    List<String> listTitle();


    /**
     * 查询最大的未确认或者未恢复的告警级别
     *
     * @param assetId
     * @return
     */
    Integer queryMaxLevel(String assetId);


    /**
     * 查询后台是否打开连续告警配置
     */
    boolean keepAlarm();

    /**
     * 组织上 未确认未恢复的一级告警产生的最新时间
     * AlarmInfo:occurTime中放的是此组织最新的告警时间
     * AlarmInfo:type中放的是组织类型
     */
    List<AlarmInfo> getOccurTime();

    /**
     * 查找命中此告警的所有解决方案
     *
     * @param id
     * @return
     */
    List<String> queryDescriptionList(String id);

    /**
     * 查找命中此告警的所有解决方案
     *
     * @param id
     * @return
     */
    List<Repository> queryDescriptionListV2(String id);

    /**
     * 按照单个组织ID查询最高告警级别
     *
     * @param orgId 组织ID
     * @return 告警级别
     */
    Integer getMaxLevelByOrgId(String orgId);

    /**
     * 按照批量组织ID查询最高告警级别
     *
     * @param orgIds 组织ID列表
     * @return 告警级别
     */
    Integer getMaxLevelByOrgIds(List<String> orgIds);

    /**
     * 通过组织类型查询出所有需要导出的记录集合
     *
     * @param type
     * @return
     */
    List<ExportAlarmReportBean> getReportListByOrgType(QueryExportByTypeReq type);

    /**
     * 通过制定组织ID查询所有列表
     *
     * @param queryByTypeReq
     * @return
     */
    List<ExportAlarmReportBean> getReportListByOrgIds(QueryExportByTypeReq queryByTypeReq);

    /**
     * 查询APP需要的告警信息
     * 未确认的告警
     */
    List<JSONObject> selectAppAlarmInfo(Integer pageSize, Integer pageIndex);

    /**
     * 删除相关告警信息
     *
     * @param id
     */
    void delAlarm(String id);

    /**
     * 查询X天未确认的Y级告警
     */
    List<AlarmInfo> findUnconfirmAlarm(int level, int days);

    /**
     * 查询不健康设备
     */
    List<UnhealthyAsset> findUnHealthyAsset(int num, int days);

    /**
     * 系统中未确定的告警总数
     *
     * @return
     */
    List<AlarmInfo> selectUnAscertainAlarm();

    /**
     * 删除资产上的数据库告警信息
     *
     * @param assetId
     */
    void delDbAlarm(String assetId);


    /**
     * 恢复并确认指定进程相关告警
     */
    void recoverProcess(String processName, String assetId);

    /**
     * 查询设备业务告警信息
     *
     * @param req
     * @return
     */
    Object findBizAlarm(AssetAlarmReq req);

    /**
     * 查询设备未确认未恢复的告警信息
     *
     * @param msg
     * @return
     */
    void recoverAlarm(String msg, String assetId);

    List<AlarmInfo> broadcastAlarmList(SysConfig sysConfig);

    void blankAlarm(ConstructionRecord constructionRecordco);

    /**
     * 查询大屏告警列表
     * 此接口会过滤用户权限内的告警数据全选
     *
     * @param query
     * @return
     * @author Lvyp
     */
    List<DialogsAlarmListVo> queryDialogsVoListV2(DialogsAlarmListDto query);


    /**
     * 查询3D告警列表
     */
    List<DialogsAlarmListVo> queryCenterDialogsVoListV2(String eventCategory);


    /**
     * 处理告警V2
     *
     * @param dto
     */
    void disposeAlarmV2(DisposeAlarmDto dto);


    /**
     * 查询项
     *
     * @param refuseList
     * @return
     */
    List<MonitoringItemVo> getMonitoringItemV2(List<String> refuseList);

    /**
     * 大类型筛选告警数量
     *
     * @param eventCategory
     * @return
     */
    Integer queryAbnormalAssetV2(AbnormalAssetQuery query);

    /**
     * 查询机柜中的告警列表
     *
     * @param query
     * @return
     * @author Lvyp
     */
    List<CabinetAlarmInfoVo> selectCabinetAlarmV2(CabinetAlarmQueryDto query);

    /**
     * 分页查询 V2版本
     *
     * @param query
     * @return
     */
    IPage<AlarmPageVo> pageV2(AlarmPageDto query);

    /**
     * 按照条件汇总
     *
     * @param query
     * @return
     */
    List<AlarmPageStatisticsVo> statisticsV2(AlarmPageDto query);


    /**
     * 符合条件的告警状态更新为恢复
     *
     * @param alarmCode
     * @param assetId
     * @param flag
     */
    void recoverAlarmV2(String alarmCode, String assetId, String flag, String msg);


    /**
     * 查询3D机房相关告警
     */
    List<ThreeDAlarmReq> getThreeDAlarm(String roomId1, String roomId2);

    /**
     * 查找未确定或者未恢复告警通过告警码
     *
     * @param alarmCode
     * @return
     */
    AlarmInfo selectUnOverAlarm(String alarmCode);

    /**
    * 获取组织下资产的最大告警级别
    * */
    List<WebAssetAlarmVo> getAssetAlarmByOrg(String orgId);

    AlarmCountDto countAlarm(AlarmPageDto req);

    List<AlarmUnhandledDto> findUnhandledAlarm(AlarmPageDto req);

    List<AlarmUnhandledDto> find5TimesUp(AlarmPageDto req);

    List<String> getRemarksByAlarmCode(String alarmCode);
}
