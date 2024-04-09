package com.jcca.web.xunjian.adapter.v1.impl;

import com.jcca.web.asset.controller.ApiDsController;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectDiskService;
import com.jcca.web.collect.service.CollectDsService;
import com.jcca.web.collect.service.CollectRaidService;
import com.jcca.web.xunjian.adapter.v1.XunJianAdapter;
import com.jcca.web.xunjian.adapter.v1.util.TemplateUtil;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 磁盘使用率巡检
 *
 * @author Lvyp
 */
@Slf4j
@Service
public class DiskUsedRateAdpaterImpl implements XunJianAdapter {

    private static final List<Integer> NEED_MODE = Arrays.asList(183,318);

    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private CollectDiskService collectDiskServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectDsService dsService;
    @Resource
    private CollectRaidService raidService;


    @Override
    public XunjianDetail xunJian(XunjianAsset xunjianAsset, XunjianDetail detail) {

        Asset itsmAsset = assetServ.getById(xunjianAsset.getAssetId());

        if (!NEED_MODE.contains(itsmAsset.getAssetMode())) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("该设备类型无此指标");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            return detail;
        }

        if (itsmAsset.getAssetMode() == 318) {
            detail.setXunJianValue("");
            detail.setThresholdValue("");
            detail.setResultMsg("存储系列未查询到相应值");
            detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);

            if (itsmAsset.getAssetImage().startsWith("DS")) {
                List<CollectDS> ds = dsService.findByType(itsmAsset.getId(), 4);
                if (Objects.nonNull(ds) && !ds.isEmpty()) {
                    CollectDS collectDS = ds.get(0);
                    String capacityStr = ApiDsController.getNetFileSizeDescription(collectDS.getCapacity());
                    String freeCapacityStr = ApiDsController.getNetFileSizeDescription(collectDS.getFreeCapacity());
                    Long freeCapacity = collectDS.getFreeCapacity();
                    String v = "0";
                    if(freeCapacity !=0){
                        v = new BigDecimal(collectDS.getFreeCapacity()).multiply(new BigDecimal(100)).divide(new BigDecimal(collectDS.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
                    }
                    detail.setResultMsg("总容量: " + capacityStr + ";  剩余容量: " + freeCapacityStr + ";  空闲率: " + v + "%");
                }
            } else if (itsmAsset.getAssetImage().startsWith("V") || itsmAsset.getAssetImage().startsWith("v")) {
                List<CollectRaid> raids = raidService.findByType(itsmAsset.getId(), null, 3);
                if (Objects.nonNull(raids) && !raids.isEmpty()) {
                    CollectRaid collectRaid = raids.get(0);
                    String capacityStr = ApiDsController.getNetFileSizeDescription(collectRaid.getCapacity());
                    String usedCapacityStr = ApiDsController.getNetFileSizeDescription(collectRaid.getUsedCapacity());
                    String v = new BigDecimal(collectRaid.getUsedCapacity()).multiply(new BigDecimal(100)).divide(new BigDecimal(collectRaid.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
                    detail.setResultMsg("总容量: " + capacityStr + ";  使用容量: " + usedCapacityStr + ";  使用率: " + v + "%");
                }
            }
            return detail;
        } else {
            ThresholdAssetVo threshold = thresholdAssetService.findAssetThreshold(xunjianAsset.getAssetId());
            List<CollectDisk> diskList = collectDiskServ.getRealTimeData(xunjianAsset.getAssetId());
            Optional<CollectDisk> maxDisk = diskList.stream().max(Comparator.comparingDouble(CollectDisk::getUsedRate));
            CollectDisk disk = null;
            try {
                disk = maxDisk.get();
            }catch (Exception e){
                log.error(e.getMessage());
            }

            if(Objects.isNull(disk)){
                detail.setXunJianValue("");
                detail.setThresholdValue("");
                detail.setResultMsg("未获取到任何数据，请人工巡检");
                detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
                return detail;
            }

            if (Objects.isNull(threshold) || Objects.isNull(threshold.getDisk())) {
                detail.setXunJianValue("");
                detail.setThresholdValue("");
                detail.setResultMsg("设备未设定阈值，当前磁盘最高使用率："+disk.getUsedRate()+" 磁盘："+disk.getMountPoint());
                detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
                return detail;
            }

            detail.setThresholdValue(threshold.getDisk().toString());

            if (disk.getUsedRate() > threshold.getDisk()) {
                detail.setNormalFlag(XunjianDetail.EXCEPTION_FLAG);
            } else {
                detail.setNormalFlag(XunjianDetail.NORMAL_FLAG);
            }

            detail.setXunJianValue(disk.getUsedRate().toString());
            detail.setResultMsg(TemplateUtil.getThresholdTemp(threshold.getDisk(), disk.getUsedRate(), true, true)+"<br/>【所属磁盘】："+disk.getMountPoint());
        }
        return detail;
    }

    @Override
    public String getCode() {
        return XunJianTargetEnum.DISK_RATE.getCode();
    }

}
