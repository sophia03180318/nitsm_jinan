package com.jcca.web.ai.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.UnitEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.ai.vo.*;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectDiskService;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.collect.service.bean.AssetDiskVo;
import com.jcca.web.collect.service.bean.DiskVo;
import com.jcca.web.statistics.service.HourCpuService;
import com.jcca.web.statistics.service.HourMemoryService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: ai访问接口
 * @author: sophia
 * @create: 2025/04/16 11:11
 **/
@RestController
@RequestMapping("/api/ai")
@Api(tags = "AI获取分析数据接口")
@Slf4j
public class ApiAiDataController {


    @Resource
    private CollectCpuService collectCpuService;
    @Resource
    private CollectMemoryService collectMemoryService;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectNetworkCardService networkCardService;
    @Resource
    private CollectDiskService collectDiskService;
    @Resource
    private HourCpuService hourCpuService;
    @Resource
    private HourMemoryService hourMemoryService;
    @Resource
    private BrokenRecordService brokenRecordService;

    @Resource
    private SysModuleConfigService moduleConfigService;


    /**
     * 分析CPU
     *
     * @return
     */
    @PostMapping("/getCpu")
    public ResultVo getCpuList(@RequestBody QueryVo queryVo) {
        List<CpuVo> cpuLineList = new ArrayList<>();
        Integer day = queryVo.getDay();
        String assetId = queryVo.getAssetId();
        if (day == 0) {
            IPage cpuPage = PagePlugin.startPage(1, 50);
            QueryWrapper<CollectCpu> cpuQuery = Wrappers.query();
            cpuQuery.orderByDesc("collect_time");
            cpuQuery.eq("asset_id", assetId);
            cpuPage = collectCpuService.page(cpuPage, cpuQuery);
            List<CollectCpu> cpus = cpuPage.getRecords();
            for (CollectCpu record : cpus) {
                CpuVo cpuVo = new CpuVo();
                cpuVo.setCpuUsedRate(record.getCpuUsedRate());
                cpuVo.setCollectDate(record.getCollectTime());
                cpuLineList.add(cpuVo);
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -day);
            Date fiveDaysAgo = calendar.getTime();
            cpuLineList = hourCpuService.findDataByDay(assetId, fiveDaysAgo);
        }
        String collect = cpuLineList.stream().map(CpuVo::toString).collect(Collectors.joining(";"));

        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.cpuStr");

        return ResultVoUtil.success(config.getValue(), config.getValue() + collect);
    }


    /**
     * 分析内存
     *
     * @return
     */
    @PostMapping("/getMemory")
    public ResultVo getMemory(@RequestBody QueryVo queryVo) {
        List<MemoryVo> memoryLineList = new ArrayList<>();
        Integer day = queryVo.getDay();
        String assetId = queryVo.getAssetId();
        if (day == 0) {
            IPage page = PagePlugin.startPage(1, 50);
            QueryWrapper<CollectMemory> query = Wrappers.query();
            query.orderByDesc("collect_time");
            query.eq("asset_id", assetId);
            page = collectMemoryService.page(page, query);
            List<CollectMemory> datas = page.getRecords();
            for (CollectMemory record : datas) {
                MemoryVo memoryVo = new MemoryVo();
                memoryVo.setMemUsedRate(record.getMemUsedRate());
                memoryVo.setCollectDate(record.getCollectTime());
                memoryLineList.add(memoryVo);
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -day);
            Date fiveDaysAgo = calendar.getTime();
            memoryLineList = hourMemoryService.findDataByDay(assetId, fiveDaysAgo);
        }
        String collect = memoryLineList.stream().map(MemoryVo::toString).collect(Collectors.joining(";"));
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.memoryStr");

        return ResultVoUtil.success(config.getValue(), config.getValue() + collect);
    }


    /**
     * 分析交换空间
     *
     * @return
     */
    @PostMapping("/getSwap")
    public ResultVo getSwap(@RequestBody QueryVo queryVo) {
        Integer day = queryVo.getDay();
        String assetId = queryVo.getAssetId();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        Date fiveDaysAgo = calendar.getTime();
        List<SwapVo> swapByDay = hourMemoryService.findSwapByDay(assetId, fiveDaysAgo);
        String collect = swapByDay.stream().map(SwapVo::toString).collect(Collectors.joining(";"));
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.swapStr");

        return ResultVoUtil.success(config.getValue(), config.getValue() + collect);
    }


    /**
     * 分析网卡
     *
     * @return
     */
    @GetMapping("/getNetcard/{assetId}")
    public ResultVo getNetcard(@PathVariable String assetId) {
        ArrayList<NetworkVo> networkVos = new ArrayList<>();
        List<CollectNetworkCard> networkCardList = networkCardService.getRealTimeData(assetId);
        for (CollectNetworkCard card : networkCardList) {
            if (StrUtil.isEmpty(card.getIp())) {
                continue;
            }
            NetworkVo networkVo = new NetworkVo();
            networkVo.setIp(card.getIp());
            networkVo.setMacAddress(card.getMacAddress());
            networkVo.setName(card.getName());
            networkVo.setStatus(card.getStatus());
            networkVo.setCollectDate(card.getCollectTime());
            networkVos.add(networkVo);
        }
        String collect = networkVos.stream().map(NetworkVo::toString).collect(Collectors.joining(";"));
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.networkCardStr");

        return ResultVoUtil.success(config.getValue(), config.getValue() + collect);
    }


    /**
     * 分析磁盘
     *
     * @return
     */
    @GetMapping("/getDisk/{assetId}")
    public ResultVo getDisk(@PathVariable String assetId) {
        AssetDiskVo assetDiskVo = collectDiskService.getAssetDiskMsg(assetId, UnitEnum.AUTO);
        if (Objects.nonNull(assetDiskVo)) {
            List<DiskVo> diskVoList = assetDiskVo.getDiskList();
            String collect = diskVoList.stream().map(DiskVo::toString).collect(Collectors.joining(";"));
            SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.diskStr");

            return ResultVoUtil.success(config.getValue(), config.getValue() + collect);
        }
        return ResultVoUtil.warning("未获取到相关硬盘信息");
    }


    /**
     * 分析进程
     *
     * @return
     */
    @PostMapping("/getProcess")
    public ResultVo getInterfaceList(@RequestBody QueryVo queryVo) {
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.processStr");

        return ResultVoUtil.success(config.getValue(), config.getValue() + queryVo.getProcessName());
    }

    /**
     * 查询告警
     *
     * @return
     */
    @GetMapping("/getAlarm/{alarmId}")
    public ResultVo getAlarmInfo(@PathVariable String alarmId) {
        AlarmInfo alarm = alarmInfoService.getById(alarmId);
        AlarmVo alarmVo = new AlarmVo();
        alarmVo.setAlarmState(alarm.getAlarmState());
        alarmVo.setTitle(alarm.getTitle());
        alarmVo.setDescription(alarm.getDescription());
        alarmVo.setOccurTime(alarm.getOccurTime());

        Asset asset2 = assetService.getById(alarm.getAssetId());
        String assetStr = "";
        if (ObjectUtil.isNotNull(asset2)) {
            String manufacturerName = DictUtil.getValue("ASSET_FACTORY", asset2.getManufacturerId() + "");
            String assetModelStr = DictUtil.getValue("ASSET_MODE", asset2.getAssetMode() + "");
            assetStr = "\n设备厂商:" + manufacturerName + ";设备类型:" + assetModelStr + ";设备型号:" + asset2.getAssetImage() + "\n";

        }
        String content = alarm.getContent();
        String linkAsset = "";

        if (content.contains("对端设备")) {
            SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.portStr");
            if (content.contains("Gi1/0/9")) {
                Asset asset = assetService.getById("1327545380090540033");
                linkAsset += "\n对端设备:" + asset.getName() + ";对端IP:" + asset.getIp() + ";对端端口:GigabitEthernet1/0/5;对端供货商:" + asset.getAssetSupplier() + ";供货商联系方式:400-921-9900\n";
                return ResultVoUtil.success(config.getValue(), assetStr + linkAsset + config.getValue() + alarmVo);

            } else if (content.contains("Gi1/0/17")) {
                Asset asset = assetService.getById("1333467992012427266");
                linkAsset += "\n对端设备:" + asset.getName() + ";对端IP:" + asset.getIp() + ";对端供货商:" + asset.getAssetSupplier() + ";供货商联系方式:400-921-9900;影响进程:[oraclebhm (LOCAL=NO)]、[nginx: worker process]\n";
                return ResultVoUtil.success(config.getValue(), assetStr + linkAsset + config.getValue() + alarmVo);
            }
        }
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.alarmStr");
        return ResultVoUtil.success(config.getValue(), assetStr + linkAsset + config.getValue() + alarmVo);
    }

    /**
     * 分析告警列表
     *
     * @return
     */
    @GetMapping("/getAlarmList/{assetId}")
    public ResultVo getAlarmList(@PathVariable String assetId) {
        List<AlarmVo> aiAlarm = alarmInfoService.findAiAlarm(assetId);
        Asset asset = assetService.getById(assetId);
        String assetStr = "";
        if (ObjectUtil.isNotNull(asset)) {
            String manufacturerName = DictUtil.getValue("ASSET_FACTORY", asset.getManufacturerId() + "");
            String assetModelStr = DictUtil.getValue("ASSET_MODE", asset.getAssetMode() + "");
            assetStr = "\n设备厂商:" + manufacturerName + ";设备类型:" + assetModelStr + ";设备型号:" + asset.getAssetImage() + "\n";
        }
        String collect = aiAlarm.stream().map(AlarmVo::toString).collect(Collectors.joining(";"));
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.alarmListStr");
        return ResultVoUtil.success(config.getValue(), config.getValue() + assetStr + collect);
    }


    /**
     * 查询故障记录
     *
     * @return
     */
    @GetMapping("/getBrokenRecord/{brokenRecordId}")
    public ResultVo getbrokenRecord(@PathVariable String brokenRecordId) {
        BrokenRecord brokenRecord = brokenRecordService.getById(brokenRecordId);
        Asset asset = assetService.getById(brokenRecord.getAssetId());
        SysModuleConfig config = moduleConfigService.getSysModuleConfig("aiConfig.brokenRecordStr");
        String str = config.getValue() + "\n";
        if (ObjectUtil.isNotNull(asset)) {
            String manufacturerName = DictUtil.getValue("ASSET_FACTORY", asset.getManufacturerId() + "");
            String assetModelStr = DictUtil.getValue("ASSET_MODE", asset.getAssetMode() + "");
            str += "设备厂商:" + manufacturerName + ";设备类型:" + assetModelStr + ";设备型号:" + asset.getAssetImage() + "\n";
        }
        if (brokenRecord.getOrigin() == 2) {
            AlarmInfo alarm = alarmInfoService.getById(brokenRecord.getAlarmId());
            if (ObjectUtil.isNotNull(alarm)) {
                str += "告警内容:" + alarm.getDescription() + "\n";

            }
        }
        str += "故障现象:" + brokenRecord.getDescription() + "\n";
        return ResultVoUtil.success(config.getValue(), str);
    }


    /**
     * 获取当前时间往前推N秒的Date对象
     *
     * @param seconds 需要往前推的秒数
     * @return 往前推N秒后的Date对象
     */
    public Date getDateBeforeSeconds(int seconds) {
        // 获取当前时间的Calendar实例
        Calendar calendar = Calendar.getInstance();
        // 将当前时间减去指定的秒数
        calendar.add(Calendar.SECOND, -seconds);
        // 返回计算后的Date对象
        return calendar.getTime();
    }

    public String getAssetId(String assetStr) {
        if (ObjectUtil.isNotNull(assetService.getById(assetStr))) {
            return assetStr;
        }
        Asset asset = assetService.getOneByAllIp(assetStr);
        if (ObjectUtil.isNotNull(asset)) {
            return asset.getId();
        }

        QueryWrapper<Asset> qw = new QueryWrapper<>();
        qw.eq("NAME", assetStr);
        qw.eq("IS_DEL", 1);
        List<Asset> list = assetService.list(qw);
        if (ObjectUtil.isNotNull(list) && !list.isEmpty()) {
            return list.get(0).getId();
        }
        return "";
    }

    private Calendar getReqStartDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - day);
        return calendar;
    }
}