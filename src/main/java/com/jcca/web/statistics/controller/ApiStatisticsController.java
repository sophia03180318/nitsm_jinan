package com.jcca.web.statistics.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.statistics.service.StatisticsService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName ApiStatisticsController
 * @Description 统计分析
 * @Date 2020/7/16 14:25
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/statistics")
@Api(tags = "统计分析")
public class ApiStatisticsController {

    @Resource
    private AssetService assetService;
    @Resource
    private StatisticsService statisticsService;
    @Resource
    private RedisService redisService;

    /**
     * 资产统计
     *
     * @return
     */
    @GetMapping("/asset")
    @ApiOperation(value = "统计资产")
    @RequiresPermissions("api:statistics")
    public ResultVo asset() {
        // 厂商设备数量统计
        List<StatisticsAlarmVo> manufacturerList = assetService.getManufacturerAsset();
        if (CollectionUtil.isEmpty(manufacturerList)) {
            return ResultVoUtil.error("请先添加设备！");
        }

        // 按类型统计设备数量
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        List<StatisticsAlarmVo> assetModeList = assetService.getModeAsset(orgIds);

        // 按组织统计设备数量
        List<StatisticsAlarmVo> orgAssetList = assetService.getOrgAsset();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("manufacturer", manufacturerList);
        resultMap.put("assetMode", assetModeList);
        resultMap.put("orgAsset", orgAssetList);

        return ResultVoUtil.success(resultMap);
    }

    /**
     * 告警统计
     *
     * @return
     */
    @GetMapping("/alarm")
    @ApiOperation(value = "统计告警")
    @RequiresPermissions("api:statistics")
    @ActionLog(name = "查看告警统计分析", title = "统计分析", key = LogTypeConstant.QUERY)
    public ResultVo alarm() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgIds", orgIds);
        Calendar c = Calendar.getInstance();

        // 当天不同告警级别统计 2021-01-05
        paramMap.put("today_", DateUtil.format(c.getTime(), "yyyy-MM-dd"));
        List<StatisticsAlarmVo> levelDayAlarmList = statisticsService.getDayLevelAlarm(paramMap);
        if (levelDayAlarmList.size() != 4) {
            levelDayAlarmList = this.fillData(levelDayAlarmList);
        }

        // 本月一二三级各级别告警总数
        paramMap.put("currentMonth_", DateUtil.format(c.getTime(), "yyyy-MM"));
        List<StatisticsAlarmVo> levelAlarmList = statisticsService.getLevelAlarm(paramMap);

        // 所有未确认告警
        paramMap.put("status_", AlarmStatusEnum.UNCONFIRM.getCode());
        StatisticsAlarmVo unconfirmAlarm = statisticsService.getUnconfirmAlarm(paramMap);

        // 本月告警条数，本月一级告警条数
        List<StatisticsAlarmVo> monthNowAlarmList = statisticsService.getMonthAlarm(paramMap);
        Long allNowAlarm = 0L, firstNowAlarm = 0L;
        for (StatisticsAlarmVo alarmVo : monthNowAlarmList) {
            if ("all".equals(alarmVo.getName())) {
                allNowAlarm = alarmVo.getTotal();
            }
            if ("first".equals(alarmVo.getName())) {
                firstNowAlarm = alarmVo.getTotal();
            }
        }

        // 上月告警条数，上月一级告警条数
        Long allLastAlarm = 0L, firstLastAlarm = 0L;
        c.add(Calendar.MONTH, -1);
        paramMap.put("currentMonth_", DateUtil.format(c.getTime(), "yyyy-MM"));

        Object lastMonth = paramMap.get("currentMonth_");
        Object all = redisService.get(RedisCacheConst.STATISTICS_LAST_MONTH_ALARM_ALL + lastMonth);
        Object first = redisService.get(RedisCacheConst.STATISTICS_LAST_MONTH_ALARM_FIRST + lastMonth);
        if (Objects.nonNull(all) && Objects.nonNull(first)) {
            allLastAlarm = Long.valueOf(all.toString());
            firstLastAlarm = Long.valueOf(first.toString());
        } else {
            List<StatisticsAlarmVo> monthLastAlarmList = statisticsService.getMonthAlarm(paramMap);
            for (StatisticsAlarmVo alarmVo : monthLastAlarmList) {
                if ("all".equals(alarmVo.getName())) {
                    allLastAlarm = alarmVo.getTotal();
                }
                if ("first".equals(alarmVo.getName())) {
                    firstLastAlarm = alarmVo.getTotal();
                }
            }
            redisService.set(RedisCacheConst.STATISTICS_LAST_MONTH_ALARM_ALL + lastMonth, allLastAlarm, 31 * 24 * 60 * 60L);
            redisService.set(RedisCacheConst.STATISTICS_LAST_MONTH_ALARM_FIRST + lastMonth, firstLastAlarm, 31 * 24 * 60 * 60L);
        }

        // 返回结果集
        Map<String, Object> resultMap = new HashMap<>(32);
        resultMap.put("levelDayAlarm", levelDayAlarmList);

        resultMap.put("allNowAlarm", allNowAlarm);
        resultMap.put("firstNowAlarm", firstNowAlarm);
        resultMap.put("allLastAlarm", allLastAlarm);
        resultMap.put("firstLastAlarm", firstLastAlarm);

        resultMap.put("unconfirmAlarm", unconfirmAlarm);

        resultMap.put("levelAlarm", levelAlarmList);

        return ResultVoUtil.success(resultMap);
    }

    private List<StatisticsAlarmVo> fillData(List<StatisticsAlarmVo> levelDayAlarmList) {
        List<StatisticsAlarmVo> list = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            boolean flag = true;
            for (StatisticsAlarmVo vo : levelDayAlarmList) {
                String name = vo.getName();
                if (i == Integer.parseInt(name)) {
                    list.add(vo);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                StatisticsAlarmVo vo = new StatisticsAlarmVo();
                vo.setTotal(0L);
                vo.setName(i + "");
                list.add(vo);
            }
        }
        return list;
    }

    /**
     * 告警统计柱状图
     *
     * @return
     */
    @GetMapping("/alarmBar")
    @ApiOperation(value = "告警统计柱状图-组织和设备告警")
    @RequiresPermissions("api:statistics")
    public ResultVo alarmBar() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgIds", orgIds);
        Calendar c = Calendar.getInstance();
        paramMap.put("currentMonth_", DateUtil.format(c.getTime(), "yyyy-MM"));

        // 本月告警条数最多的组织
        List<StatisticsAlarmVo> orgAlarmList = statisticsService.getOrgAlarm(paramMap);

        // 本月告警条数最多的设备
        List<StatisticsAlarmVo> assetAlarmList = statisticsService.getAssetAlarm(paramMap);

        // 返回结果集
        Map<String, Object> resultMap = new HashMap<>(32);
        resultMap.put("orgAlarm", orgAlarmList);
        resultMap.put("assetAlarm", assetAlarmList);

        return ResultVoUtil.success(resultMap);
    }


    /**
     * 告警统计折线图
     *
     * @return
     */
    @GetMapping("/alarmLine")
    @ApiOperation(value = "告警统计折线图-年度月统计和当天小时统计")
    @RequiresPermissions("api:statistics")
    public ResultVo alarmLine() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return ResultVoUtil.success();
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgIds", orgIds);
        Calendar c = Calendar.getInstance();
        paramMap.put("year_", c.get(Calendar.YEAR));

        // 本年度告警条数月统计折线图
        List<StatisticsAlarmVo> yearAlarmList = statisticsService.getYearAlarm(paramMap);
        List<StatisticsAlarmVo> yearAlarm = new ArrayList<>();
        int month = c.get(Calendar.MONTH) + 2;
        if (yearAlarmList.size() != month) {
            yearAlarm = this.fillMonth(month, yearAlarmList);
        }

        // 最近七天告警拆线图
        c.add(Calendar.DAY_OF_MONTH, -6);
        paramMap.put("startDate", DateUtil.formatDate(c.getTime()));
        List<StatisticsAlarmVo> latest7DaysList = statisticsService.get7Days(paramMap);

        // 返回结果集
        Map<String, Object> resultMap = new HashMap<>(32);
        resultMap.put("yearAlarm", yearAlarm);
        resultMap.put("latest7Days", latest7DaysList);

        return ResultVoUtil.success(resultMap);
    }

    private List<StatisticsAlarmVo> fillMonth(int month, List<StatisticsAlarmVo> yearAlarmList) {
        List<StatisticsAlarmVo> yearAlarm = new ArrayList<>();
        for (int i = 1; i < month; i++) {
            boolean flag = true;
            for (StatisticsAlarmVo statisticsAlarmVo : yearAlarmList) {
                if (i == Integer.parseInt(statisticsAlarmVo.getName())) {
                    yearAlarm.add(statisticsAlarmVo);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                StatisticsAlarmVo result = new StatisticsAlarmVo();
                result.setTotal(0L);
                result.setName(String.valueOf(i));
                yearAlarm.add(result);
            }
        }
        return yearAlarm;
    }

    /**
     * 硬件更换统计
     *
     * @return
     */
    @GetMapping("/hardware")
    @ApiOperation(value = "硬件更换统计")
    @RequiresPermissions("api:statistics")
    public ResultVo hardware() {
        // 本月度硬件更换种类统计
        List<StatisticsAlarmVo> yearHardwareList = statisticsService.getYearCategoryFix();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("yearHardware", yearHardwareList);

        return ResultVoUtil.success(resultMap);
    }

    /**
     * 设备端口流量统计
     *
     * @return
     */
    @GetMapping("/flow")
    @ApiOperation(value = "设备端口流量统计")
    @RequiresPermissions("api:statistics")
    public ResultVo flow() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orgIds", orgIds);
        Calendar c = Calendar.getInstance();
        paramMap.put("year_", c.get(Calendar.YEAR));
        paramMap.put("month_", c.get(Calendar.MONTH) + 1);
        paramMap.put("day_", c.get(Calendar.DAY_OF_MONTH));
        paramMap.put("hour_", c.get(Calendar.HOUR_OF_DAY) - 1);

        // 设备端口流量一小时平均值
        List<StatisticsAlarmVo> hourFlowList = statisticsService.getHourFlow(paramMap);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("hourFlow", hourFlowList);
        return ResultVoUtil.success(resultMap);
    }
}
