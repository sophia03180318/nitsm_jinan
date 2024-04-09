package com.jcca.web.statistics.controller;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import io.swagger.annotations.Api;
import lombok.Data;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康度统计
 *
 * @author lyp
 */
@RestController
@RequestMapping("/api/health/statistics")
@Api(tags = "设备统计分析")
public class ApiHealthController {

    @Data
    private class StatisticsAlarmSize{
        private String assetName;
        private String assetIp;
        private String assetId;
        private Integer alarmSize;
    }

    @Autowired
    private AssetService assetServ;
    @Autowired
    private AlarmInfoService alarmInfoServ;


    @GetMapping("/queryAlarmSize")
    @RequiresPermissions("api:statistics")
    ResultVo<?> queryAlarmSize(){
        JSONObject resp = new JSONObject();
        List<AlarmInfo> alarmInfos = alarmInfoServ.selectUnAscertainAlarm();
        List<StatisticsAlarmSize> alarmSizeList = new ArrayList<>();
        if(alarmInfos.size() == 0){
            IPage<Asset> startPage = PagePlugin.startPageT(1, 10, Asset.class);
            QueryWrapper<Asset> wrapper = new QueryWrapper<>();
            wrapper.eq("WATCH",1);
            IPage<Asset> page = assetServ.page(startPage, wrapper);
            List<Asset> records = page.getRecords();
            for (Asset record : records) {
                StatisticsAlarmSize item = new StatisticsAlarmSize();
                item.setAssetId(record.getId());
                item.setAssetName(record.getName());
                item.setAssetIp(record.getIp());
                item.setAlarmSize(0);
                alarmSizeList.add(item);
            }
        }else{
            //分组
            Map<String, List<AlarmInfo>> alarmMap = alarmInfos.stream().collect(Collectors.groupingBy(AlarmInfo::getAssetId, Collectors.toList()));
            Set<String> assetIds = alarmMap.keySet();
            for (String assetId : assetIds) {
                Asset asset = assetServ.getById(assetId);
                if(Objects.isNull(asset)){
                    continue;
                }
                StatisticsAlarmSize item = new StatisticsAlarmSize();
                item.setAssetId(asset.getId());
                item.setAssetName(asset.getName());
                item.setAssetIp(asset.getIp());
                item.setAlarmSize(alarmMap.get(assetId).size());
                alarmSizeList.add(item);
            }
        }
        alarmSizeList.sort(Comparator.comparing(StatisticsAlarmSize::getAlarmSize).reversed());

        resp.put("alarmCount",alarmInfos.size());
        resp.put("alarmList",alarmSizeList);
        return ResultVoUtil.success(resp);
    }


    @GetMapping("/queryHealth")
    @RequiresPermissions("api:statistics")
    ResultVo<?> queryHealth() {
        Double queryHealth = assetServ.queryHealth();
        return ResultVoUtil.success(queryHealth);
    }

    @GetMapping("/top")
    @RequiresPermissions("api:statistics")
    ResultVo<?> queryHealth(Integer page, Integer size) {
        IPage<Asset> startPage = PagePlugin.startPageT(page, size, Asset.class);
        QueryWrapper<Asset> wrapper = new QueryWrapper<>();
        wrapper.select("id", "NAME", "IP", "ASSET_MODE", "STATUS", "COLLECTION_TYPE", "HEALTH_DEGREE");
        wrapper.isNotNull("HEALTH_DEGREE");
        wrapper.eq("IS_DEL", 1);
        wrapper.eq("WATCH", 1);
        wrapper.orderByAsc("HEALTH_DEGREE");

        IPage<Asset> pageResult = assetServ.page(startPage, wrapper);

        return ResultVoUtil.success(pageResult);
    }

}
