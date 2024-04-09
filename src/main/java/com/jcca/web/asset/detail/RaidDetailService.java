package com.jcca.web.asset.detail;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.controller.bean.AssetAlarmReq;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.controller.ApiDsController;
import com.jcca.web.asset.controller.ApiRaidController;
import com.jcca.web.asset.controller.bean.DsVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectDsService;
import com.jcca.web.collect.service.CollectRaidService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 磁盘阵列
 */
@Service
public class RaidDetailService  implements DetailAdapter {

    @Resource
    private CollectRaidService raidService;
    @Resource
    private CollectDsService dsService;
    @Resource
    private AlarmInfoService alarmInfoService;

    @Override
    public String getCode() {
        return String.valueOf(AssetModeConst.RAID);
    }

    @Override
    public ResultVo handle(Asset asset) {

        return ResultVoUtil.success();
    }

    @Override
    public ResultVo getAssetGeneralInfo(Asset asset) {
        JSONObject respJson = new JSONObject();
        //查询设备的信息
        AssetAlarmReq req = new AssetAlarmReq();
        req.setAssetId(asset.getId());
        respJson.put("alarmInfo", alarmInfoService.findAssetAlarm(req));

        if(asset.getAssetImage().toUpperCase().startsWith("V")){
            List<CollectRaid> capacitys = raidService.findByType(asset.getId(), null, 3);
            if (ObjectUtil.isNotNull(capacitys) && !capacitys.isEmpty()) {
                CollectRaid capacity = capacitys.get(0);
                capacity.setCollectTimeStr(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(capacity.getCollectTime()));
                long freeCapacity = capacity.getCapacity() - capacity.getUsedCapacity();
                capacity.setFreeCapacityStr(ApiRaidController.getNetFileSizeDescription(freeCapacity));
                String v = new BigDecimal(capacity.getUsedCapacity()).multiply(new BigDecimal(100)).divide(new BigDecimal(capacity.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
                capacity.setUsedRate(v);

                respJson.put("asset",asset);
                respJson.put("collectTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(capacity.getCollectTime()));
                respJson.put("capacity",capacity);
                return ResultVoUtil.success(respJson);
            }
        }else{
            //Ds系列
            List<CollectDS> capacity = dsService.findByType(asset.getId(), 4);
            if (Objects.nonNull(capacity) && !capacity.isEmpty()) {
                CollectDS collectDS = capacity.get(0);
                DsVo dsVo = new DsVo();
                dsVo.setCapacity(collectDS.getCapacity());
                dsVo.setFreeCapacity(collectDS.getFreeCapacity());
                dsVo.setCapacityStr(collectDS.getCapacityStr());
                long usedCapacity = collectDS.getCapacity() - collectDS.getFreeCapacity();
                dsVo.setCapacityStr(ApiDsController.getNetFileSizeDescription(collectDS.getCapacity()));
                dsVo.setUsedCapacityStr(ApiDsController.getNetFileSizeDescription(usedCapacity));
                dsVo.setFreeCapacityStr(ApiDsController.getNetFileSizeDescription(collectDS.getFreeCapacity()));
                if (collectDS.getCapacity()!=0 && Objects.nonNull(collectDS.getFreeCapacity())) {
                    String v = new BigDecimal(usedCapacity).multiply(new BigDecimal(100)).divide(new BigDecimal(collectDS.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
                    dsVo.setUsedRate(v);
                } else {
                    dsVo.setUsedRate("0");
                }
                dsVo.setFreeCapacityStr(ApiDsController.getNetFileSizeDescription(collectDS.getFreeCapacity()));

                respJson.put("asset",asset);
                respJson.put("collectTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(collectDS.getCollectTime()));
                respJson.put("capacity",dsVo);
                return ResultVoUtil.success(respJson);
            }
        }


        CollectRaid capacity = new CollectRaid();
        respJson.put("asset",asset);
        respJson.put("collectTime","----");
        respJson.put("capacity",capacity);

        return ResultVoUtil.success(respJson);
    }
}
