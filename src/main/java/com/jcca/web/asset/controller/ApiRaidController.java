package com.jcca.web.asset.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.controller.bean.RaidControllerVo;
import com.jcca.web.asset.controller.bean.RaidDetialVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectRaidService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ Author：sophia
 * @ Date：Created in 0:47 2022/6/9
 * @ Description:
 */
@RestController
@RequestMapping("/api/raid")
@Slf4j
@Api(tags = "磁盘阵列相关接口")
public class ApiRaidController {

    @Resource
    private CollectRaidService raidService;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetAttachService attachService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private RoomService roomService;


    /**
     * 资产详情
     */

    @GetMapping("/detail/{assetId}")
    @ApiOperation(value = "资产详情")
    @ActionLog(name = "查看磁盘阵列详情", title = "监控管理", key = LogTypeConstant.QUERY)
    public ResultVo<RaidDetialVo> detail(@PathVariable("assetId") String assetId) {
        Asset asset = assetService.getById(assetId);
        if (ObjectUtil.isNull(asset)) {
            return ResultVoUtil.error("资产不存在");
        }
        RaidDetialVo raidDetialVo = new RaidDetialVo();
        raidDetialVo.setId(assetId);
        raidDetialVo.setIp(asset.getIp());
        raidDetialVo.setAssetName(asset.getName());
        raidDetialVo.setAssetImage(asset.getAssetImage());
        String manufacturerName = DictUtil.getValue("ASSET_FACTORY", asset.getManufacturerId() + "");
        raidDetialVo.setManufacturerName(manufacturerName);

        String assetModelStr = DictUtil.getValue("ASSET_MODE", asset.getAssetMode() + "");
        raidDetialVo.setAssetModelStr(assetModelStr);
        String OrgName = orgService.getById(asset.getOrgId()).getTitle();
        raidDetialVo.setOrgName(OrgName);
        AssetAttach attach = attachService.getByAssetId(assetId);
        String cabinetName = cabinetService.getById(attach.getCabinetId()).getName();
        raidDetialVo.setCabinetName(cabinetName);

        String roomName = roomService.getById(attach.getRoomId()).getName();
        raidDetialVo.setRoomName(roomName);

        return ResultVoUtil.success(raidDetialVo);
    }

    /**
     * 资产池列表
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/groupList/{assetId}")
    @ApiOperation(value = "池列表")
    public ResultVo<List<CollectRaid>> groupList(@PathVariable("assetId") String assetId) {
        List<CollectRaid> groupList = raidService.findByType(assetId, null, 0);
        for (CollectRaid collectRaid : groupList) {
            collectRaid.setCapacityStr(getNetFileSizeDescription(collectRaid.getCapacity()));
            collectRaid.setUsedCapacityStr(getNetFileSizeDescription(collectRaid.getUsedCapacity()));
            String v = new BigDecimal(collectRaid.getUsedCapacity()).multiply(new BigDecimal(100)).divide(new BigDecimal(collectRaid.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
            collectRaid.setUsedRate(v);
        }
        return ResultVoUtil.success(groupList);
    }


    /**
     * 资产池列表
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/logList/{assetId}")
    @ApiOperation(value = "日志列表")
    public ResultVo<List<String>> logList(@PathVariable("assetId") String assetId) {
        List<CollectRaid> logList = raidService.findByType(assetId, null, 5);
        List<String> collect = logList.stream().map(CollectRaid::getLogInfo).collect(Collectors.toList());
        return ResultVoUtil.success(collect);
    }


    /**
     * 容量
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/capacity/{assetId}")
    @ApiOperation(value = "使用容量")
    public ResultVo<CollectRaid> capacity(@PathVariable("assetId") String assetId) {
        List<CollectRaid> capacitys = raidService.findByType(assetId, null, 3);
        if (ObjectUtil.isNotNull(capacitys) && !capacitys.isEmpty()) {
            CollectRaid capacity = capacitys.get(0);
            capacity.setCollectTimeStr(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(capacity.getCollectTime()));
            long freeCapacity = capacity.getCapacity() - capacity.getUsedCapacity();
            capacity.setFreeCapacityStr(getNetFileSizeDescription(freeCapacity));
            String v = new BigDecimal(capacity.getUsedCapacity()).multiply(new BigDecimal(100)).divide(new BigDecimal(capacity.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
            capacity.setUsedRate(v);

            return ResultVoUtil.success(capacity);
        }
        return ResultVoUtil.success(new CollectRaid());
    }

    /**
     * mdiskList
     *
     * @return
     * @Author: sophia
     */
    @PostMapping("/getMdisk")
    @ApiOperation(value = "mdiskList")
    public ResultVo<List<CollectRaid>> getMdisk(@RequestBody CollectRaid group) {
        if (ObjectUtil.isNull(group)) {
            return ResultVoUtil.error("请传入池信息!");
        }

        if (ObjectUtil.isNull(group.getAssetId()) || ObjectUtil.isNull(group.getRealId())) {
            return ResultVoUtil.error("暂无采集结果,请您稍后");
        }

        List<CollectRaid> mdiskList = raidService.findByType(group.getAssetId(), group.getRealId(), 1);
        return ResultVoUtil.success(mdiskList);
    }

    /**
     * Vdisk
     *
     * @return
     * @Author: sophia
     */
    @PostMapping("/getVdisk")
    @ApiOperation(value = "vdiskList")
    public ResultVo<List<CollectRaid>> getVdisk(@RequestBody CollectRaid group) {
        List<CollectRaid> vdiskList = new ArrayList<CollectRaid>();
        if (ObjectUtil.isNull(group)) {
            log.error("磁盘阵列-请传入池信息!参数：{}", JSONUtil.toJsonStr(group));
            return ResultVoUtil.success(vdiskList);
        }

        if (ObjectUtil.isNull(group.getAssetId()) || ObjectUtil.isNull(group.getRealId())) {
            log.error("磁盘阵列-缺少资产ID或realId!");
            return ResultVoUtil.success(vdiskList);
        }

        vdiskList = raidService.findByType(group.getAssetId(), group.getRealId(), 2);
        return ResultVoUtil.success(vdiskList);
    }


    /**
     * Drive
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/getDrive/{assetId}")
    @ApiOperation(value = "DriveList")
    public ResultVo<List<RaidControllerVo>> getDrive(@PathVariable("assetId") String assetId) {
        List<CollectRaid> drives = new ArrayList<CollectRaid>();

        if (ObjectUtil.isNull(assetId) || "".equals(assetId)) {
            log.error("磁盘阵列-缺少资产ID!");
            return ResultVoUtil.error("缺少资产ID!");
        }

        drives = raidService.findByType(assetId, null, 4);
        if (ObjectUtil.isNull(drives) || drives.isEmpty()) {
            return ResultVoUtil.success("暂时未采集到相关Drive信息  请稍等!");
        }
        ArrayList<RaidControllerVo> controllerVos = new ArrayList<>();
        Set<Integer> ySet = new HashSet<>();
        ArrayList<String> x_y = new ArrayList<>();
        int max_x = drives.stream().map(ds -> {
            ySet.add(ds.getYindex());
            x_y.add(ds.getYindex() + "," + ds.getXindex());

            if ("online".equals(ds.getStatus())) {
                ds.setHealth(1);
            } else {
                ds.setHealth(2);
            }

            return ds;
        }).max(Comparator.comparing(CollectRaid::getXindex)).get().getXindex();

        ArrayList<String> index = new ArrayList<>();
        if (max_x < 16) {
            max_x = 16;
        } else if (max_x < 24) {
            max_x = 24;
        }
        for (Integer y : ySet) {
            for (int i = 1; i <= max_x; i++) {
                index.add(y + "," + i);
            }
        }
        index.removeAll(x_y);

        for (String s : index) {
            String[] split = s.split(",");
            CollectRaid collectDS = new CollectRaid();
            collectDS.setAssetId(assetId);
            collectDS.setXindex(Integer.valueOf(split[1]));
            collectDS.setYindex(Integer.valueOf(split[0]));
            collectDS.setHealth(3);
            drives.add(collectDS);
        }
        Map<Integer, List<CollectRaid>> collect = drives.stream().collect(Collectors.groupingBy(CollectRaid::getYindex));


        for (Map.Entry<Integer, List<CollectRaid>> kv : collect.entrySet()) {
            List<CollectRaid> value = kv.getValue();
            RaidControllerVo controllerVo = new RaidControllerVo();
            controllerVo.setDrivers(value);
            controllerVo.setName("Enclosure " + kv.getKey());
            controllerVo.setStatus("online");
            controllerVos.add(controllerVo);
        }
        return ResultVoUtil.success(controllerVos);
    }

    public static String getNetFileSizeDescription(long size) {
        BigDecimal bigDecimal = new BigDecimal(size);
        if (size >= 1099511627776L) {
            return bigDecimal.divide(new BigDecimal(1099511627776L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "TB";
        } else if (size >= 1073741824L) {
            return bigDecimal.divide(new BigDecimal(1073741824L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "GB";
        } else if (size >= 11048576L) {
            return bigDecimal.divide(new BigDecimal(1048576L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "MB";
        } else if (size >= 1024L) {
            return bigDecimal.divide(new BigDecimal(1024L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "KB";
        } else {
            return size + "B";
        }

    }

}
