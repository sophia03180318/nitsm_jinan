package com.jcca.web2.controller;

import cn.hutool.core.date.DateUtil;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.statistics.service.StatisticsService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.vo.BizAlarmVo;
import com.jcca.web2.vo.RollAlarmVo;
import com.jcca.web2.vo.StatisticsVo;
import com.jcca.web2.vo.StatistisMapOrg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HanHW
 * @description 数据统计V2
 * @className StatisticsControllerV2
 * @date 2023/10/25 9:41
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/statistics")
@Api(tags = "数据统计V2")
public class StatisticsControllerV2 {

    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private StatisticsService statisticsService;

    // 线路总数 设备总数，车站总数，中心设备数，车站设备数
    @GetMapping("/asset")
    @ApiOperation("监控设备统计")
    public ResultVo<Object> asset() {

        int lineCount =  orgService.getListByOrgType(OrgTypeConst.LINE).size();
        int watchCount = assetService.getAssetCountV2();
        int stationCount = orgService.getListByOrgType(OrgTypeConst.STATION).size();
        int centerAssetCount = assetService.getAssetCountByOrgTypeV2(OrgTypeConst.CENTER);
        int stationAssetCount = assetService.getAssetCountByOrgTypeV2(OrgTypeConst.STATION);
       // int overhaulAssetCount = assetService.getOverhaulListV2().size();

        Map<String, Object> resMap = new HashMap<>();
        resMap.put("lineCount", lineCount);
        resMap.put("watchCount", watchCount);
        resMap.put("stationCount", stationCount);
        resMap.put("centerAssetCount", centerAssetCount);
        resMap.put("stationAssetCount", stationAssetCount);
       // resMap.put("overhaulAssetCount", overhaulAssetCount);

        return ResultVoUtil.success(resMap);
    }

    // 厂商设备数，分中心和车站
    @GetMapping("/manufacturerAsset")
    @ApiOperation("厂商设备统计")
    public ResultVo<Object> manufacturer(@RequestParam Byte orgType) {
        if (Objects.isNull(orgType)) {
            orgType = OrgTypeConst.CENTER;
        }

        List<StatisticsAlarmVo> list = statisticsService.getManufacturerAssetV2(orgType);

        return ResultVoUtil.success(list);
    }

    // 今日设备状态，分类型，分告警级别
    @GetMapping("/assetModeStatus")
    @ApiOperation("设备状态")
    public ResultVo<Object> assetModeStatus() {

        String today = DateUtil.today();
        List<StatisticsVo> resultList = statisticsService.findAssetAlarmByAssetModeV2(today);

        return ResultVoUtil.success(resultList);
    }

    // 七天内设备告警，分告警级别
    @GetMapping("/sevenAlarmLine")
    @ApiOperation("七天内设备告警")
    public ResultVo<Object> sevenAlarm() {

        List<StatisticsAlarmVo> resList = statisticsService.getSevenDaysAlarmLineV2();

        return ResultVoUtil.success(resList);
    }

    // 实时监测 当前存在的告警，监测采集指标
    @GetMapping("/realTime")
    @ApiOperation("实时监测")
    public ResultVo<Object> realTime() {

        List<StatisticsAlarmVo> resList = statisticsService.getCollectTargetAlarmV2();

        return ResultVoUtil.success(resList);
    }

    // 综合告警统计 BizTypeEnum
    @GetMapping("/business")
    @ApiOperation("综合告警统计")
    public ResultVo<Object> business() {

        List<BizAlarmVo> resList = statisticsService.getBusinessAlarmV2();

        return ResultVoUtil.success(resList);
    }

    @GetMapping("/rollAlarm")
    @ApiOperation("大屏实时滚动消息")
    public ResultVo<Object> rollAlarm() {
        // 设备未确认告警实时滚动
        List<RollAlarmVo> alarmVoList = statisticsService.rollAlarmV2();
        return ResultVoUtil.success(alarmVoList);
    }

    // 地图上车站状态统计
    @GetMapping("/stationState")
    @ApiOperation("地图上车站状态统计")
    public ResultVo<Object> stationAlarm() {

        List<RollAlarmVo> list = statisticsService.stationAlarmV2();

        return ResultVoUtil.success(list);
    }

    // 地图上车站数据
    @GetMapping("/stationMapData")
    @ApiOperation("地图上车站数据")
    public ResultVo<Object> stationMapData(@RequestParam String orgTitle) {
        if (StringUtils.isEmpty(orgTitle)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        Map<String, Object> respMap = statisticsService.getStationMapDataV2(orgTitle);

        return ResultVoUtil.success(respMap);
    }

    // 地图上的线路及车站
    @GetMapping("/mapOrg")
    @ApiOperation("地图上的线路及车站")
    public ResultVo<Object> mapOrg() {

        List<StatistisMapOrg> list = statisticsService.getMapOrgListV2();

        return ResultVoUtil.success(list);
    }

    // 保存地图上车站坐标
    @PostMapping("/saveCoords")
    @ApiOperation("保存地图上车站坐标")
    public ResultVo<String> saveCoords(@RequestBody() List<RollAlarmVo> voList) {

        statisticsService.saveCoordsV2(voList);

        return ResultVoUtil.SAVE_SUCCESS;
    }
}