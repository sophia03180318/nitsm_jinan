package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectDiskBean;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.service.CollectDiskService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 磁盘信息处理
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeDiskAdapterImpl implements CollectAdapter {

    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private CollectDiskService diskService;
    @Resource
    private EventLogicService eventLogicServ;

    /**
     * 磁盘数据处理
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectDiskBean> disks = JSONUtil.toList(data, CollectDiskBean.class);
        List<CollectDisk> diskList = new ArrayList<CollectDisk>();
        Date createTime = new Date();
        String collectCode = MyIdUtil.getId();
        for (CollectDiskBean disk : disks) {
            if (StrUtil.isEmpty(disk.getAssetId()) || StrUtil.isEmpty(disk.getCollectTime())
                    || StrUtil.isEmpty(disk.getName()) || Objects.isNull(disk.getTotal())
                    || Objects.isNull(disk.getUsed()) || Objects.isNull(disk.getAvailable())) {
                continue;
            }
            if(StrUtil.isNotEmpty(disk.getMountPoint())){
                if(disk.getMountPoint().toLowerCase().contains("/mnt")||disk.getMountPoint().toLowerCase().contains("/media")){
                    //不采集/mnt 路径
                    continue;
                }
            }
            String collectUsedRate = AppMathUtil.percentage(disk.getUsed(), disk.getTotal(), 4);
            String collectFreeRate = AppMathUtil.percentage(disk.getAvailable(), disk.getTotal(), 4);

            Date date = new Date();
            date.setTime(Long.valueOf(disk.getCollectTime()));

            CollectDisk item = new CollectDisk();
            item.setId(MyIdUtil.getId());
            item.setAssetId(disk.getAssetId());
            item.setCollectCode(collectCode);
            item.setCollectTime(date);
            item.setCreateTime(createTime);
            item.setFree(disk.getAvailable());
            item.setMountPoint(disk.getMountPoint());
            item.setTotal(disk.getTotal());
            item.setUsed(disk.getUsed());
            item.setFreeRate(Double.valueOf(collectFreeRate));
            item.setUsedRate(Double.valueOf(collectUsedRate));

            addEvent(item, collectUsedRate);

            diskList.add(item);
        }

        if (diskList.isEmpty()) {
            return;
        }
        // 插入磁盘采集数据
        diskService.updateBatchByAssetId(diskList);
        // 缓存设备信息
        diskService.updateRealTimeData(diskList);
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.DISK;
    }

    /**
     * 触发事件
     *
     * @param item
     */
    private void addEvent(CollectDisk item, String collectUsedRate) {
        String assetId = item.getAssetId();

        String LockId = "Event_ADD_DISK_" + assetId;
        synchronized (LockId.intern()) {
            VerifyThresholdReq req = new VerifyThresholdReq();
            req.setCollectValue(item.getUsedRate().toString());
            req.setAssetId(assetId);
            req.setHaveSection(false);
            req.setType(ThresholdSectionEnum.DSIK);
            VerifyThresholdResp verifyResp = thresholdServ.verifyThreshold(req);

            List<CreateEventReq> reqList = new ArrayList<CreateEventReq>();

            if (Objects.nonNull(verifyResp)) {
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setOriginalMsg(verifyResp.getMsg());
                eventReq.setEventLevel(verifyResp.getAlarmStatus() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode());

                eventReq.setAssetId(assetId);
                eventReq.setBaseValue(verifyResp.getBaseValue());
                eventReq.setCollectValue(item.getUsedRate().toString());
                eventReq.setUniqueCode(EventUniqueCode.DISK_UNIQUE_CODE);
                eventReq.setFlag(item.getMountPoint());
                eventReq.setGroupFlag(EventGroupConstant.DISK);
                eventReq.setCreateTime(item.getCollectTime());
                reqList.add(eventReq);
            }
            //阶段阈值
            VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(item.getUsedRate()), EventUniqueCode.DISK_UNIQUE_CODE);
            List<CreateEventReq> diskEventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.DISK, item.getCollectTime(), assetId, item.getUsedRate().toString(), "磁盘"+item.getMountPoint());

            reqList.addAll(diskEventList);

            try {
                for (CreateEventReq createEventReq : reqList) {
                    eventLogicServ.addEvent(createEventReq);
                }
            } catch (Exception e) {
                log.error("处理磁盘事件异常：{}", e.getMessage(), e);
            }
        }

    }

}
