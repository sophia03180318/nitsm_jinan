package com.jcca.web.statistics.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.statistics.service.StatisticsService;
import com.jcca.web.statistics.service.bean.AssetMinuteMsg;
import com.jcca.web.statistics.service.bean.AssetStatusStatistics;
import com.jcca.web.statistics.vo.AssetInfo;
import com.jcca.web.statistics.vo.AssetStatisticsReq;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web.statistics.vo.StatisticsAssetVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName ApiAssetStatisticsController
 * @Description 设备统计分析
 * @Author wone
 * @Date 2021/1/21 14:19
 * @Version ITSM2.0
 **/
@RestController
@RequestMapping("/api/asset/statistics")
@Api(tags = "设备统计分析")
public class ApiAssetStatisticsController {

    @Resource
    private StatisticsService statisticsService;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmInfoService alarmInfoServ;

    @Resource
    private ManageDbService dbServ;

    /**
     * 查询系统内数据库资产
     * @return
     */
    @GetMapping("/queryDbAsset")
    public ResultVo<Object> queryDbAsset(){
        List<ManageDb> list = dbServ.list();
        List<JSONObject> respList = new ArrayList<>();
        for (ManageDb manageDb : list) {
            QueryWrapper<AlarmInfo> query = new QueryWrapper<>();
            query.eq("ALARM_STATE",1);
            query.eq("ASSET_ID",manageDb.getAssetId());
            query.eq("ALARM_CODE","DB_LINK");
            List<AlarmInfo> list1 = alarmInfoServ.list(query);

            Asset asset = assetServ.getById(manageDb.getAssetId());
            JSONObject respJson = new JSONObject();
            respJson.put("name",manageDb.getDbName());
            respJson.put("id",manageDb.getId());
            respJson.put("desk","Oracle数据库");
            respJson.put("assetMode","Oracle数据库");
            respJson.put("ip",asset.getIp());
            respJson.put("assetImage","oracle");
            respJson.put("jumpType","oracle");

            if(list1.isEmpty()){
                respJson.put("status",1);
                respJson.put("monitorStatus","正常");
            }else{
                respJson.put("status",0);
                respJson.put("monitorStatus","连接异常");
            }
            respList.add(respJson);
        }

        return ResultVoUtil.success(respList);
    }


    /**
     * 设备统计分析页面
     *
     * @param req
     * @return
     */
    @PostMapping("/index")
    @ApiOperation(value = "设备统计分析")
    @ActionLog(name = "查看设备统计分析", title = "统计分析", key = LogTypeConstant.QUERY)
    public ResultVo<Object> index(@RequestBody AssetStatisticsReq req) {

        if (StrUtil.isEmpty(req.getOrgId())) {
            req.setOrgIdList(ShiroUtil.getSubjectOrgIds());
        }

        Integer page = Convert.toInt(req.getPage(), 1);
        Integer size = Convert.toInt(req.getSize(), 10);
        req.setStart((page - 1) * size);
        req.setEnd(page * size + 1);

        // 按查询条件查询设备列表
        List<AssetInfo> infoList = statisticsService.findAssetByPage(req);
        Long totalItem = statisticsService.countAssetInfo(req);

        // 按类型统计设备数量
        List<StatisticsAlarmVo> assetModeList = statisticsService.getDeskAsset(req);

        // 按厂商统计设备数量
        List<StatisticsAlarmVo> manufacturerList = statisticsService.getManufacturerAsset(req);

        // 按型号统计设备数量
        List<StatisticsAlarmVo> assetImageList = statisticsService.getModelAsset(req);

        StatisticsAssetVo vo = new StatisticsAssetVo();
        vo.setAssetImageList(assetImageList);
        vo.setAssetInfoList(infoList);
        vo.setAssetManufacturerList(manufacturerList);
        vo.setAssetModeList(assetModeList);
        vo.setTotalItem(totalItem);

        return ResultVoUtil.success(vo);
    }

    @GetMapping("/getRunTime")
    @ApiOperation(value = "设备运行时长柱状")
    public ResultVo<Object> runTime() {
        return ResultVoUtil.success(statisticsService.queryAssetRunTime());
    }


    @GetMapping("/getHourMsg")
    @ApiOperation(value = "设备CPU、内存、温度")
    public ResultVo<Object> getHourMsg() {
        return ResultVoUtil.success(statisticsService.queryAssetHourMsg());
    }

    @GetMapping("/getMinuteMsg")
    public ResultVo<Object> getMinuteMsg(String assetId){
        if(StrUtil.isEmpty(assetId)){
            return ResultVoUtil.success(new AssetMinuteMsg());
        }
        AssetMinuteMsg assetMinuteMsgs = statisticsService.queryMinuteMsg(assetId);
        return ResultVoUtil.success(assetMinuteMsgs);
    }

    @GetMapping("/getMonitorAsset")
    public ResultVo<Object> getMonitorAsset(){
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("STATUS",1);
        queryWrapper.eq("IS_DEL",1);
        queryWrapper.eq("WATCH",1);
        queryWrapper.in("ASSET_MODE",Arrays.asList("183","42","201","263"));
        List<Asset> assetList = assetServ.list(queryWrapper);
        return ResultVoUtil.success(assetList);
    }

    @GetMapping("/getAssetStatus")
    @ApiOperation(value = "linux、windows、aix、交换机、路由器 状态统计")
    public ResultVo<Object> getAssetStatus() {

        int winCount = selectAssetCount(1);
        int linuxCount = selectAssetCount(0);
        int aixCount = selectAssetCount(2);

        int switchCount = selectAssetCountByMode(201);
        int routeCount = selectAssetCountByMode(42);

        //在线
        int linuxOnline = selectAssetStatusCount(0, Arrays.asList(1, 4));
        int winOnline = selectAssetStatusCount(1, Arrays.asList(1, 4));
        int aixOnline = selectAssetStatusCount(2, Arrays.asList(1, 4));
        int switchOnline = selectAssetStatusCountByMode(201, Arrays.asList(1, 4));
        int routeOnline = selectAssetStatusCountByMode(42, Arrays.asList(1, 4));
        //不监控
        int linuxUnmonitoredCount = selectAssetStatusCount(0, Arrays.asList(2));
        int winUnmonitoredCount = selectAssetStatusCount(1, Arrays.asList(2));
        int aixUnmonitoredCount = selectAssetStatusCount(2, Arrays.asList(2));
        int switchUnmonitoredCount = selectAssetStatusCountByMode(201, Arrays.asList(2));
        int routeUnmonitoredCount = selectAssetStatusCountByMode(42, Arrays.asList(2));
        //离线
        int linuxAbnormalCount = selectAssetStatusCount(0, Arrays.asList(0));
        int winAbnormalCount = selectAssetStatusCount(1, Arrays.asList(0));
        int aixAbnormalCount = selectAssetStatusCount(2, Arrays.asList(0));
        int switchAbnormalCount = selectAssetStatusCountByMode(201, Arrays.asList(0));
        int routeAbnormalCount = selectAssetStatusCountByMode(42, Arrays.asList(0));


        List<AssetStatusStatistics> respList = new ArrayList<AssetStatusStatistics>();
        respList.add(getBean("windows设备", winCount, winOnline, winUnmonitoredCount, winAbnormalCount));
        respList.add(getBean("linux设备", linuxCount, linuxOnline, linuxUnmonitoredCount, linuxAbnormalCount));
        respList.add(getBean("aix设备", aixCount, aixOnline, aixUnmonitoredCount, aixAbnormalCount));
        respList.add(getBean("交换机设备", switchCount, switchOnline, switchUnmonitoredCount, switchAbnormalCount));
        respList.add(getBean("路由设备", routeCount, routeOnline, routeUnmonitoredCount, routeAbnormalCount));

        return ResultVoUtil.success(respList);
    }


    public AssetStatusStatistics getBean(String name, int count, int onlineCount, int unmonitoredCount, int abnormalCount) {
        AssetStatusStatistics bean = new AssetStatusStatistics();
        bean.setMoldName(name);
        bean.setMoldAssetCount(count);
        bean.setNormalCount(onlineCount);
        bean.setUnmonitoredCount(unmonitoredCount);
        bean.setAbnormalCount(abnormalCount);

        return bean;
    }

    /**
     * 网络设备类型总数
     *
     * @param
     * @return
     */
    private int selectAssetCountByMode(int assetMode) {
        QueryWrapper<Asset> assetWinQuery = new QueryWrapper<Asset>();
        assetWinQuery.eq("IS_DEL", 1);
        assetWinQuery.eq("ASSET_MODE", assetMode);

        return assetServ.count(assetWinQuery);
    }

    /**
     * 状态类型筛选
     *
     * @return
     */
    private int selectAssetStatusCountByMode(int assetMode, List<Integer> status) {
        QueryWrapper<Asset> assetWinQuery = new QueryWrapper<Asset>();
        assetWinQuery.eq("IS_DEL", 1);
        assetWinQuery.eq("ASSET_MODE", assetMode);
        assetWinQuery.in("STATUS", status);

        return assetServ.count(assetWinQuery);
    }

    /**
     * 类型总数
     *
     * @param collectType
     * @return
     */
    private int selectAssetCount(int collectType) {
        QueryWrapper<Asset> assetWinQuery = new QueryWrapper<Asset>();
        assetWinQuery.eq("IS_DEL", 1);
        assetWinQuery.eq("COLLECTION_TYPE", collectType);

        return assetServ.count(assetWinQuery);
    }

    /**
     * 状态类型筛选
     *
     * @param collectType
     * @return
     */
    private int selectAssetStatusCount(int collectType, List<Integer> status) {
        QueryWrapper<Asset> assetWinQuery = new QueryWrapper<Asset>();
        assetWinQuery.eq("IS_DEL", 1);
        assetWinQuery.eq("COLLECTION_TYPE", collectType);
        assetWinQuery.in("STATUS", status);

        return assetServ.count(assetWinQuery);
    }

}
