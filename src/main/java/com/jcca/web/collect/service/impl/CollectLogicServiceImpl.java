package com.jcca.web.collect.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.*;
import com.jcca.web.collect.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 采集查询
 *
 * @author lyp
 */
@Service
public class CollectLogicServiceImpl implements CollectLogicService {

    @Resource
    private AssetService assetServ;
    @Resource
    private CollectCpuService cpuServ;
    @Resource
    private CollectMemoryService menoryServ;
    @Resource
    private CollectDiskService diskServ;
    @Resource
    private CollectDBService dbServ;
    @Resource
    private CollectInterfacesService interfaceServ;
    @Resource
    private CollectRaidService raidService;
    @Resource
    private CollectDsService dsService;

    @Override
    public Boolean getCollectStatus(String assetId, Integer minute) throws Exception {
        if (StrUtil.isEmpty(assetId) || Objects.isNull(minute)) {
            throw new Exception("资产ID和验证时常不能空");
        }
        Asset asset = assetServ.getById(assetId);
        if (Objects.isNull(asset)) {
            throw new Exception("资产不存在");
        }
        Integer assetMode = asset.getAssetMode();

        if (AssetModeEnum.ROUTER.getCode() == assetMode || AssetModeEnum.SWITCH.getCode() == assetMode) {
            // 网络设备验证
            if (cpuCollectIsOk(assetId, minute)) {
                return true;
            }
            if (meneoryCollectIsOk(assetId, minute)) {
                return true;
            }
            return false;
        } else if (AssetModeEnum.SMALL_SERVER.getCode() == assetMode || AssetModeEnum.SERVER.getCode() == assetMode
                || AssetModeEnum.TERMINAL.getCode() == assetMode || AssetModeEnum.IPC.getCode() == assetMode) {
            // 服务器类型验证
            if (cpuCollectIsOk(assetId, minute)) {
                return true;
            }
            if (meneoryCollectIsOk(assetId, minute)) {
                return true;
            }
            return false;
        } else if (AssetModeEnum.DB.getCode() == assetMode) {
            if (cpuCollectIsOk(assetId, minute)) {
                return true;
            }
            if (meneoryCollectIsOk(assetId, minute)) {
                return true;
            }
            if (dbCollectIsOk(assetId, minute)) {
                return true;
            }
            return false;
        } else if (AssetModeEnum.RAID.getCode() == assetMode) {
            if (raidCollectIsOk(assetId, minute)) {
                return true;
            }
            return false;
        }

        throw new Exception("未知的设备类型：" + assetMode);
    }

    /**
     * cpu采集是否正常
     *
     * @param minute
     * @return
     */
    private Boolean cpuCollectIsOk(String assetId, int minute) {
        List<CollectCpu> realTimeData = cpuServ.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return false;
        }
        CollectCpu collectCpu = realTimeData.get(0);
        Date collectTime = collectCpu.getCreateTime();

        return verdict(minute, collectTime);
    }

    /**
     * 判断磁盘阵列是否正常
     *
     * @param assetId
     * @param minute
     * @return
     */
    private Boolean raidCollectIsOk(String assetId, int minute) {

        Date lastTime = raidService.findLastTime(assetId);
        if (Objects.isNull(lastTime)) {
            lastTime = dsService.findLastTime(assetId);
            if (Objects.isNull(lastTime)) {
                return false;
            }
        }
        return verdict(minute, lastTime);
    }

    /**
     * 判断内存是否正常
     *
     * @param assetId
     * @param minute
     * @return
     */
    private Boolean meneoryCollectIsOk(String assetId, int minute) {
        List<CollectMemory> realTimeData = menoryServ.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return false;
        }
        CollectMemory item = realTimeData.get(0);
        Date collectTime = item.getCreateTime();

        return verdict(minute, collectTime);
    }

    /**
     * 判断内存是否正常
     *
     * @param assetId
     * @param minute
     * @return
     */
    private Boolean diskCollectIsOk(String assetId, int minute) {
        List<CollectDisk> realTimeData = diskServ.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return false;
        }
        CollectDisk item = realTimeData.get(0);
        Date collectTime = item.getCreateTime();

        return verdict(minute, collectTime);
    }

    /**
     * 判断数据库是否正常
     *
     * @param assetId
     * @param minute
     * @return
     */
    private Boolean dbCollectIsOk(String assetId, int minute) {
        List<CollectDB> realTimeData = dbServ.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return false;
        }
        CollectDB item = realTimeData.get(0);
        Date collectTime = item.getCreateTime();

        return verdict(minute, collectTime);
    }

    /**
     * 判断数据库是否正常
     *
     * @param assetId
     * @param minute
     * @return
     */
    private Boolean interfaceCollectIsOk(String assetId, int minute) {
        List<CollectInterfaces> realTimeData = interfaceServ.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return false;
        }
        CollectInterfaces item = realTimeData.get(0);
        Date collectTime = item.getCreateTime();

        return verdict(minute, collectTime);
    }

    /**
     * 判定采集时间是否在有效期内
     *
     * @param minute
     * @param collectTime
     * @return
     */
    private Boolean verdict(int minute, Date collectTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -minute);

        Date beginDate = calendar.getTime();
        //最后一次采集时间如果大于失效时间 则采集结果有效，采集服务处于正常状态
        return beginDate.getTime() < collectTime.getTime();
    }

}
