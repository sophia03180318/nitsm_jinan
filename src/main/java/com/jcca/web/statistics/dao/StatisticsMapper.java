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
    List<StatisticsVo> findAssetAlarmByAssetModeV2(@Param("day") String day, @Param("showJcca") String showJcca);

    List<StatisticsVo> findAssetCenterAlarmByAssetModeV2();

    /**
     * @description: 近七天告警折线图
     * @author: HanHW
     * @date: 2023/11/8 17:43
     * @param: [paramMap]
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getSevenDaysAlarmLineV2(@Param("params") Map<String, Object> paramMap);

    /**
     * @description: 近七天中心告警折线图
     * @author: sophia
     * @date: 2024/09/10 11:43
     * @param: [paramMap]
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getSevenDaysCenterAlarmLineV2(@Param("startDate") String startDate);

    /**
     * @description: 查找厂商设备数量
     * @author: HanHW
     * @date: 2023/11/7 9:53
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.BusinessAlarmVoV2>
     **/
    Integer getBizAssetCountV2();

    /**
     * @description: 查厂商异常设备数量
     * @author: HanHW
     * @date: 2023/11/7 10:12
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.BusinessAlarmVoV2>
     **/
    Integer getBizAbnormalCountV2();

    /**
     * @description: 未确认未恢复告警数量 用于大屏推送
     * @author: HanHW
     * @date: 2023/11/7 11:35
     * @param: []
     * @return: void
     **/
    List<RollAlarmVo> getRollAlarmV2(String showJcca);


    /**
     * @description: 未确认未恢复中心告警数量 用于大屏推送
     * @author: sophia
     * @date: 2024/09/10 11:35
     * @param: []
     * @return: void
     **/
    List<RollAlarmVo> getRollCenterAlarmV2();

    /**
     * @description: 获取有告警的车站 用于大飞展示
     * @author: HanHW
     * @date: 2023/11/7 13:12
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.RollAlarmVoV2>
     **/
    List<RollAlarmVo> getStationAlarmV2(String showJcca);

    /**
     * @description: 分设备类型查询某天告警数
     * @author: HanHW
     * @date: 2023/11/8 9:41
     * @param: []
     * @return: java.util.List<com.jcca.web.statistics.vo.StatisticsAlarmVo>
     **/
    List<StatisticsAlarmVo> getModeAssetV2();
}
