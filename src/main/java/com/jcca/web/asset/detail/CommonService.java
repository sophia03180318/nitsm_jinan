package com.jcca.web.asset.detail;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.AssetManufacturerEnum;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.enums.UnitEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.controller.bean.AssetAlarmReq;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.detail.bean.AssetLine;
import com.jcca.web.asset.detail.bean.DetailCpu;
import com.jcca.web.asset.detail.bean.DetailGeneral;
import com.jcca.web.asset.detail.bean.DetailMemorySwap;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.collect.service.bean.AssetMemoryVo;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/18 10:58
 */
@Service
public class CommonService {
    @Resource
    private CollectCpuService collectCpuService;
    @Resource
    private CollectMemoryService collectMemoryService;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectSystemTimeService collectSystemTimeService;

    /**
     * 获取基本信息
     *
     * @param assetBelong
     * @return
     */
    public DetailGeneral getDetailGeneral(AssetBelong assetBelong, Asset asset) {
        DetailGeneral detailGeneral = new DetailGeneral();
        BeanUtil.copyProperties(asset, detailGeneral);
        detailGeneral.setCabinetName(assetBelong.getCabinetName());
        detailGeneral.setRoomName(assetBelong.getRoomName());
        // 细分资产类型 20210112hanwon
        detailGeneral.setAssetModeStr(AssetModeEnum.getName(asset.getDesk()));
        detailGeneral.setManufacturerStr(AssetManufacturerEnum.getName(asset.getManufacturerId()));
        return detailGeneral;
    }

    /**
     * 获取CPU信息
     *
     * @param asset
     * @return
     */
    public DetailCpu getDetailCPU(Asset asset, String assetId) {
        DetailCpu detailCpu = new DetailCpu();
        BeanUtil.copyProperties(asset, detailCpu);
        List<CollectCpu> cpuList = collectCpuService.getRealTimeData(assetId);
        detailCpu.setCpuUsedRate(0D);
        if (CollectionUtil.isNotEmpty(cpuList)) {
            detailCpu.setCpuUsedRate(cpuList.get(0).getCpuUsedRate());
            detailCpu.setCollectTime(cpuList.get(0).getCollectTime());
        }
        return detailCpu;
    }

    /**
     * 获取内存信息
     *
     * @return
     */
    public DetailMemorySwap getDetailMemorySwap(String assetId) {
        DetailMemorySwap memorySwapVo = new DetailMemorySwap();
        AssetMemoryVo vo = collectMemoryService.getAssetMemoryMsg(assetId, UnitEnum.AUTO);
        BeanUtil.copyProperties(vo, memorySwapVo);
        return memorySwapVo;
    }

    /**
     * 获取CPU折线
     *
     * @return
     */
    public List<AssetLine> getLineCPU(String assetId) {
        List<AssetLine> cpuLineList = new ArrayList<>();
        IPage cpuPage = PagePlugin.startPage(1, 20);
        QueryWrapper<CollectCpu> cpuQuery = Wrappers.query();
        cpuQuery.orderByDesc("collect_time");
        cpuQuery.eq("asset_id", assetId);
        cpuPage = collectCpuService.page(cpuPage, cpuQuery);
        List<CollectCpu> cpus = cpuPage.getRecords();
        Collections.reverse(cpus);
        for (CollectCpu record : cpus) {
            AssetLine line = new AssetLine();
            line.setData(String.valueOf(record.getCpuUsedRate()));
            line.setTime(record.getCollectTime());
            cpuLineList.add(line);
        }
        return cpuLineList;
    }

    /**
     * 获取内存折线
     *
     * @param assetId
     * @return
     */
    public List<AssetLine> getMemLine(String assetId) {
        List<AssetLine> memLineList = new ArrayList<>();
        IPage memPage = PagePlugin.startPage(1, 20);
        QueryWrapper<CollectMemory> memQuery = Wrappers.query();
        memQuery.eq("asset_id", assetId);
        memQuery.orderByDesc("collect_time");
        memPage = collectMemoryService.page(memPage, memQuery);
        List<CollectMemory> memories = memPage.getRecords();
        Collections.reverse(memories);
        for (CollectMemory record : memories) {
            AssetLine line = new AssetLine();
            line.setData(String.valueOf(record.getMemUsedRate()));
            line.setTime(record.getCollectTime());

            memLineList.add(line);
        }
        return memLineList;
    }

    public ResultVo commonInfo(Asset asset) {
        Map<String, Object> resultMap = new HashMap<>();
        String assetId = asset.getId();
        AssetBelong assetBelong = assetService.findAssetBelongById(assetId);
        if (Objects.isNull(assetBelong)) {
            return ResultVoUtil.error("资产[" + assetId + "]附属信息不存在");
        }

        AssetAlarmReq req = new AssetAlarmReq();
        req.setAssetId(assetId);
        resultMap.put("alarmInfo", alarmInfoService.findAssetAlarm(req));

        this.getdetail(resultMap, asset, assetBelong);

        return ResultVoUtil.success(resultMap);
    }

    private void getdetail(Map<String, Object> resultMap, Asset asset, AssetBelong assetBelong) {
        String assetId = asset.getId();
        //获取CPU信息
        DetailCpu detailCPU = getDetailCPU(asset, assetId);
        // 内存信息
        DetailMemorySwap detailMemorySwap = getDetailMemorySwap(assetId);
        //获取通用信息
        DetailGeneral detailGeneral = getDetailGeneral(assetBelong, asset);

        resultMap.put("generalInfo", detailGeneral);
        resultMap.put("cpuInfo", detailCPU);
        resultMap.put("memorySwapInfo", detailMemorySwap);

        //时间偏差
        List<CollectSystemTime> collectSystemTimes = collectSystemTimeService.getRealTimeData(assetId);
        if (collectSystemTimes != null && collectSystemTimes.size() > 0) {
            CollectSystemTime c = collectSystemTimes.get(0);
            Long systemTime = c.getTimeSpan();
            resultMap.put("systemTime", systemTime == null ? 0 : systemTime.doubleValue() / 1000);
        } else {
            resultMap.put("systemTime", 0);
        }
        //设备运行时间
        Long runTime = collectSystemTimeService.getRunTime(assetId);
        if (runTime != 0l) {
            resultMap.put("runTime", XunjianReportUtil.formatDateTime(runTime));
        } else {
            resultMap.put("runTime", "");
        }
    }

    public ResultVo getBizDetail(Asset asset) {
        Map<String, Object> resultMap = new HashMap<>();
        String assetId = asset.getId();
        AssetBelong assetBelong = assetService.findAssetBelongById(assetId);
        if (Objects.isNull(assetBelong)) {
            return ResultVoUtil.error("资产[" + assetId + "]附属信息不存在");
        }

        AssetAlarmReq req = new AssetAlarmReq();
        req.setAssetId(assetId);
        resultMap.put("alarmInfo", alarmInfoService.findBizAlarm(req));

        this.getdetail(resultMap, asset, assetBelong);

        return ResultVoUtil.success(resultMap);
    }
}
