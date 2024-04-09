package com.jcca.web.statistics.dao;

import com.jcca.web.statistics.entity.HourCpu;
import com.jcca.web.statistics.entity.HourMemory;
import com.jcca.web.statistics.entity.HourTemp;
import com.jcca.web.statistics.service.bean.AssetRunTime;
import com.jcca.web.statistics.vo.AssetInfo;
import com.jcca.web.statistics.vo.AssetStatisticsReq;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.vo.RollAlarmVo;
import com.jcca.web2.vo.StatisticsVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @ClassName StatisticsMapper
 * @Description 统计分析mapper
 * @Date 2020/7/20 10:12
 * @Author hanwone
 */
public interface StatisticsMapper {
    List<StatisticsAlarmVo> getOrgAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getAssetAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getCategoryAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getLevelAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getYearCategoryFix(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getYearAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getMonthAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getToayAlarm(@Param("params") Map<String, Object> paramMap);

    StatisticsAlarmVo getUnconfirmAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getHourFlow(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> get7Days(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getDayLevelAlarm(@Param("params") Map<String, Object> paramMap);

    List<StatisticsAlarmVo> getDeskAsset(AssetStatisticsReq req);

    List<StatisticsAlarmVo> getManufacturerAsset(AssetStatisticsReq req);

    List<StatisticsAlarmVo> getModelAsset(AssetStatisticsReq req);

    List<AssetInfo> findAssetByPage(AssetStatisticsReq req);

    Long countAssetInfo(AssetStatisticsReq req);

    /**
     * 获取运行时长统计
     *
     * @return
     */
    List<AssetRunTime> selectAssetRunTimeList();

    /**
     * 获取CPU一小时
     *
     * @return
     */
    List<HourCpu> cpuHourList(@Param("params") Map<String, String> paramMap);

    /**
     * 获取内存一小时
     *
     * @return
     */
    List<HourMemory> memoryHourList(@Param("params") Map<String, String> paramMap);

    /**
     * 温度一小时统计
     *
     * @return
     */
    List<HourTemp> tempHourList(@Param("params") Map<String, String> paramMap);

    /**
     * @description: 分设备类型查询某天告警数
     * @author: HanHW
     * @date: 2023/11/8 17:44
     * @param: [day]
     * @return: java.util.List<com.jcca.web2.vo.StatisticsVo>
     **/
    List<StatisticsVo> findAssetAlarmByAssetModeV2(@Param("day") String day);

    /**
     * @description: 近七天告警折线图
     * @author: HanHW
     * @date: 2023/11/8 17:43
     * @param: [paramMap]
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getSevenDaysAlarmLineV2(@Param("params") Map<String, Object> paramMap);

    /**
     * @description: 查找厂商设备数量
     * @author: HanHW
     * @date: 2023/11/7 9:53
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.BusinessAlarmVoV2>
     **/
    @Select("select count(*) assetCount from casco_devices")
    Integer getBizAssetCountV2();

    /**
     * @description: 查厂商异常设备数量
     * @author: HanHW
     * @date: 2023/11/7 10:12
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.BusinessAlarmVoV2>
     **/
    @Select("select count(*) abnormalCount from (select business_type from broker_topo_business where alarm_status = 1 group by business_type, device_id)")
    Integer getBizAbnormalCountV2();

    /**
     * @description: 未确认未恢复告警数量 用于大屏推送
     * @author: HanHW
     * @date: 2023/11/7 11:35
     * @param: []
     * @return: void
     **/
    List<RollAlarmVo> getRollAlarmV2();

    /**
     * @description: 获取有告警的车站 用于大飞展示
     * @author: HanHW
     * @date: 2023/11/7 13:12
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.RollAlarmVoV2>
     **/
    List<RollAlarmVo> getStationAlarmV2();

    /**
     * @description: 分设备类型查询某天告警数
     * @author: HanHW
     * @date: 2023/11/8 9:41
     * @param: []
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    @Select("select t.total, t.desk as id, m.name from (select count(*) total, desk from asset where is_del = 1 and desk != 0 and desk not like '7%' group by desk) t " +
            "left join asset_mode m on t.desk = m.code group by t.total, t.desk, m.name")
    List<StatisticsAlarmVo> getModeAssetV2();
}
