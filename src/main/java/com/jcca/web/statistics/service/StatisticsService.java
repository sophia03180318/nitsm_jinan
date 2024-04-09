package com.jcca.web.statistics.service;

import com.jcca.web.statistics.service.bean.AssetHourMsg;
import com.jcca.web.statistics.service.bean.AssetMinuteMsg;
import com.jcca.web.statistics.service.bean.AssetRunTime;
import com.jcca.web.statistics.vo.AssetInfo;
import com.jcca.web.statistics.vo.AssetStatisticsReq;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.vo.BizAlarmVo;
import com.jcca.web2.vo.RollAlarmVo;
import com.jcca.web2.vo.StatisticsVo;
import com.jcca.web2.vo.StatistisMapOrg;

import java.util.List;
import java.util.Map;

/**
 * CPU汇总相关
 *
 * @author Lvyp
 */
public interface StatisticsService {

    /**
     * 告警最多的组织
     *
     * @return
     */
    List<StatisticsAlarmVo> getOrgAlarm(Map<String, Object> paramMap);

    /**
     * 告警最多的设备
     *
     * @return
     */
    List<StatisticsAlarmVo> getAssetAlarm(Map<String, Object> paramMap);

    /**
     * 告警最多的类别
     *
     * @return
     */
    List<StatisticsAlarmVo> getCategoryAlarm(Map<String, Object> paramMap);

    /**
     * 按告警级别统计
     *
     * @return
     */
    List<StatisticsAlarmVo> getLevelAlarm(Map<String, Object> paramMap);

    /**
     * 本月度硬件更换种类统计
     *
     * @return
     */
    List<StatisticsAlarmVo> getYearCategoryFix();

    /**
     * 本年度告警月统计折线图
     *
     * @return
     */
    List<StatisticsAlarmVo> getYearAlarm(Map<String, Object> paramMap);

    /**
     * 本月告警总次数
     *
     * @param
     * @return
     */
    List<StatisticsAlarmVo> getMonthAlarm(Map<String, Object> paramMap);

    /**
     * 当天告警次数小时统计
     *
     * @return
     */
    List<StatisticsAlarmVo> getToayAlarm(Map<String, Object> paramMap);

    /**
     * 获取所有未确认告警数量
     *
     * @param paramMap
     * @return
     */
    StatisticsAlarmVo getUnconfirmAlarm(Map<String, Object> paramMap);

    /**
     * 统计近一小时端口流量平均值
     *
     * @return
     */
    List<StatisticsAlarmVo> getHourFlow(Map<String, Object> paramMap);

    /**
     * 最近七天告警数据
     *
     * @param paramMap
     * @return
     */
    List<StatisticsAlarmVo> get7Days(Map<String, Object> paramMap);

    /**
     * 当天不同告警级别统计
     *
     * @param paramMap
     * @return
     */
    List<StatisticsAlarmVo> getDayLevelAlarm(Map<String, Object> paramMap);

    /**
     * 按类型统计设备数量
     *
     * @param req
     * @return
     */
    List<StatisticsAlarmVo> getDeskAsset(AssetStatisticsReq req);

    /**
     * 按厂商统计设备数量
     *
     * @param req
     * @return
     */
    List<StatisticsAlarmVo> getManufacturerAsset(AssetStatisticsReq req);

    /**
     * 按型号统计设备数量
     *
     * @param req
     * @return
     */
    List<StatisticsAlarmVo> getModelAsset(AssetStatisticsReq req);

    /**
     * 按查询条件查询设备列表
     *
     * @param req
     * @return
     */
    List<AssetInfo> findAssetByPage(AssetStatisticsReq req);

    /**
     * 获取总条数
     *
     * @param req
     * @return
     */
    Long countAssetInfo(AssetStatisticsReq req);

    /**
     * 获取运行时长统计
     *
     * @return
     */
    List<AssetRunTime> queryAssetRunTime();

    /**
     * 一小时统计
     *
     * @return
     */
    List<AssetHourMsg> queryAssetHourMsg();

    /**
     * 查询分钟统计
     *
     * @return
     */
    AssetMinuteMsg queryMinuteMsg(String assetId);

    /**
     * @description: 按组织类型统计厂商设备数量
     * @author: HanHW
     * @date: 2023/10/25 17:39
     * @param: [orgType]
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getManufacturerAssetV2(Byte orgType);

    /**
     * @description: 分设备类型查询某天告警数
     * @author: HanHW
     * @date: 2023/10/26 16:00
     * @param: [day]
     * @return: java.util.List<com.jcca.web2.vo.StatisticsVoV2>
     **/
    List<StatisticsVo> findAssetAlarmByAssetModeV2(String day);

    /**
     * @description: 查看大屏 地图中车站数据
     * @author: HanHW
     * @date: 2023/10/27 11:33
     * @param: [orgTitle:组织名称]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    Map<String, Object> getStationMapDataV2(String orgTitle);

    /**
     * @description: 获取业务数据相关告警统计
     * @author: HanHW
     * @date: 2023/10/27 16:42
     * @param: []
     * @return: List<BizStatusVoV2>
     **/
    List<BizAlarmVo> getBusinessAlarmV2();

    /**
     * @description: 近七天告警折线图
     * @author: HanHW
     * @date: 2023/10/30 13:54
     * @param: []
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getSevenDaysAlarmLineV2();

    /**
     * @description: 采集指标实时监测
     * @author: HanHW
     * @date: 2023/11/1 10:12
     * @param: []
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getCollectTargetAlarmV2();

    /**
     * @description: 地图上的线路及车站
     * @author: HanHW
     * @date: 2023/11/3 15:07
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.StatistisMapOrgV2>
     **/
    List<StatistisMapOrg> getMapOrgListV2();

    /**
     * @description: 未确认告警数量 用于大屏推送
     * @author: HanHW
     * @date: 2023/11/7 11:35
     * @param: []
     * @return: void
     **/
    List<RollAlarmVo> rollAlarmV2();

    /**
     * @description: 获取有告警的车站
     * @author: HanHW
     * @date: 2023/11/7 13:08
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.RollAlarmVoV2>
     **/
    List<RollAlarmVo> stationAlarmV2();

    /**
     * @description: 保存地图上车站坐标
     * @author: HanHW
     * @date: 2023/11/7 14:22
     * @param: [voList]
     * @return: void
     **/
    void saveCoordsV2(List<RollAlarmVo> voList);
}
