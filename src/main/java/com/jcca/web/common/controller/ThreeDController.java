package com.jcca.web.common.controller;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.common.service.ThreeDService;
import com.jcca.web.common.service.bean.ThreeDResult;
import com.jcca.web.statistics.service.StatisticsService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.dto.DialogsAlarmListDto;
import com.jcca.web2.vo.DialogsAlarmListVo;
import com.jcca.web2.vo.MonitoringItemVo;
import com.jcca.web2.vo.RollAlarmVo;
import com.jcca.web2.vo.StatisticsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 3D机房接口
 * @author: sophia
 * @create: 2023/12/27 11:33
 **/
@RestController
@RequestMapping("/api/v2/threeD")
@Api(tags = "3D机房接口")
public class ThreeDController {

    @Resource
    private ThreeDService threeDService;
    @Resource
    private StatisticsService statisticsService;
    @Resource
    private AlarmInfoService alarmInfoServ;

    @GetMapping("/syncAssetByRoom")
    @ApiOperation("同步3D机房信息")
    public ResultVo syncAssetByRoom() {
        ThreeDResult threeDResult = threeDService.syncAssetByRoom();
        if (threeDResult.isStatus()){
            return ResultVoUtil.success("同步成功~");
        }
        return ResultVoUtil.error(threeDResult.getLog());
    }


    @GetMapping("/sevenCenterAlarmLine")
    @ApiOperation("七天内中心设备告警")
    public ResultVo<Object> sevenAlarm() {
        List<StatisticsAlarmVo> resList = statisticsService.getSevenDaysCenterAlarmLineV2();
        return ResultVoUtil.success(resList);
    }

    @GetMapping("/rollCenterAlarm")
    @ApiOperation("3D机房页面实时滚动消息")
    public ResultVo<Object> rollAlarm() {
        // 设备未确认告警实时滚动
        List<RollAlarmVo> alarmVoList = statisticsService.rollCenterAlarmV2();
        return ResultVoUtil.success(alarmVoList);
    }


    @GetMapping("/getCenterEventCategory")
    @ApiOperation("查询监控项目状态")
    public ResultVo getMonitoringItem(@RequestParam("refuseList") List<String> refuseList) {
        List<MonitoringItemVo> voList = alarmInfoServ.getMonitoringItemV2(refuseList);
        for (MonitoringItemVo monitoringItemVo : voList) {
                DialogsAlarmListDto query = new DialogsAlarmListDto();
                query.setUserName("root");
                List<DialogsAlarmListVo> alarmList = alarmInfoServ.queryCenterDialogsVoListV2(query);
                monitoringItemVo.setTotal(alarmList.size());
        }
        return ResultVoUtil.success(voList);
    }


    // 今日设备状态，分类型，分告警级别
    @GetMapping("/centerAssetModeStatus")
    @ApiOperation("设备状态")
    public ResultVo<Object> assetModeStatus() {
        List<StatisticsVo> resultList = statisticsService.findAssetCenterAlarmByAssetModeV2();
        return ResultVoUtil.success(resultList);
    }


}