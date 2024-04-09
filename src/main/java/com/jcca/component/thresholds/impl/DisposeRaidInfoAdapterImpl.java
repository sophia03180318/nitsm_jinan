package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectRaidSystemFattenBean;
import com.jcca.component.thresholds.bean.DiskBean;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectRaidService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.jcca.web.asset.controller.ApiRaidController.getNetFileSizeDescription;
import static java.lang.Long.parseLong;

/**
 * 处理V5000系统数据
 *
 * @author sophia
 */
@Slf4j
@Service
public class DisposeRaidInfoAdapterImpl implements CollectAdapter {

    @Resource
    private AssetService assetServ;
    @Resource
    private CollectRaidService raidService;

    @Override
    public void dispose(JSONArray data) {
        List<CollectRaidSystemFattenBean> beanList = JSONUtil.toList(data, CollectRaidSystemFattenBean.class);

        if (beanList.isEmpty()) {
            log.error("RAID 数据处理失败，空的序列集合");
            return;
        }

        CollectRaidSystemFattenBean raidSystemMsg = beanList.get(0);
        String assetId = raidSystemMsg.getAssetId();

        if (StrUtil.isEmpty(assetId)) {
            log.error("RAID 数据处理失败，缺少资产ID");
            return;
        }

        if (Objects.isNull(assetServ.getById(assetId))) {
            log.error("RAID 数据处理失败，资产不存在，ID：{}", assetId);
            return;
        }
        Date date = new Date();
        Long time = Long.valueOf(raidSystemMsg.getCollectTime());
        //存储GROUP

        ArrayList<CollectRaid> collectRaids = new ArrayList<>();
        if (ObjectUtil.isNotNull(raidSystemMsg.getGroupList()) && !raidSystemMsg.getGroupList().isEmpty()) {
            List<DiskBean> groupList = raidSystemMsg.getGroupList();
            for (DiskBean diskBean : groupList) {
                CollectRaid collectRaid = new CollectRaid();
                BeanUtils.copyProperties(diskBean, collectRaid);
                collectRaid.setId(MyIdUtil.getId());
                collectRaid.setAssetId(assetId);
                collectRaid.setCapacity(parseLong(diskBean.getCapacity()));
                collectRaid.setCapacityStr(getNetFileSizeDescription(parseLong(diskBean.getCapacity())));
                collectRaid.setUsedCapacity(parseLong(diskBean.getUsedCapacity()));
                collectRaid.setUsedCapacityStr(getNetFileSizeDescription(parseLong(diskBean.getUsedCapacity())));
                date.setTime(time);
                collectRaid.setCollectTime(date);
                collectRaid.setCollectCode(raidSystemMsg.getCollectCode());
                collectRaids.add(collectRaid);
                raidService.save(collectRaid);
            }


        }

              List<DiskBean>  mdiskList=null;
        //存储MDISK
        if (ObjectUtil.isNotNull(raidSystemMsg.getMdiskList()) && !raidSystemMsg.getMdiskList().isEmpty()) {
                mdiskList = raidSystemMsg.getMdiskList();
            for (DiskBean diskBean : mdiskList) {
                CollectRaid collectRaid = new CollectRaid();
                BeanUtils.copyProperties(diskBean, collectRaid);
                collectRaid.setId(MyIdUtil.getId());
                collectRaid.setAssetId(assetId);
                date.setTime(time);
                collectRaid.setCollectTime(date);
                collectRaid.setCollectCode(raidSystemMsg.getCollectCode());
                raidService.save(collectRaid);
            }
        }
        //存储VDISK
        if (ObjectUtil.isNotNull(raidSystemMsg.getVdiskList()) && !raidSystemMsg.getVdiskList().isEmpty()) {
            List<DiskBean> vDisk = raidSystemMsg.getVdiskList();
            for (DiskBean diskBean : vDisk) {
                CollectRaid collectRaid = new CollectRaid();
                BeanUtils.copyProperties(diskBean, collectRaid);
                collectRaid.setId(MyIdUtil.getId());
                collectRaid.setAssetId(assetId);
                date.setTime(time);
                collectRaid.setCollectTime(date);
                collectRaid.setCollectCode(raidSystemMsg.getCollectCode());
                raidService.save(collectRaid);
            }

        }

        //存储CAPACITY
        String totalCapacity = raidSystemMsg.getTotalCapacity();
        String usedCapacity = raidSystemMsg.getUsedCapacity();
        if (ObjectUtil.isNotNull(totalCapacity) && ObjectUtil.isNotNull(usedCapacity)) {
            CollectRaid collectRaid = new CollectRaid();
            collectRaid.setAssetId(assetId);
            collectRaid.setCapacity(parseLong(totalCapacity));
            collectRaid.setCapacityStr(getNetFileSizeDescription(parseLong(totalCapacity)));
            collectRaid.setUsedCapacity(parseLong(usedCapacity));
            collectRaid.setUsedCapacityStr(getNetFileSizeDescription(parseLong(usedCapacity)));
            date.setTime(time);
            collectRaid.setDiskType(3);
            collectRaid.setCollectTime(date);
            collectRaid.setCollectCode(raidSystemMsg.getCollectCode());
            raidService.save(collectRaid);
        }


        //存储Drive
        if (ObjectUtil.isNotNull(raidSystemMsg.getDrives()) && !raidSystemMsg.getDrives().isEmpty()) {
            List<DiskBean> drives = raidSystemMsg.getDrives();

            for (DiskBean drive : drives) {
                CollectRaid collectRaid = new CollectRaid();
                BeanUtils.copyProperties(drive, collectRaid);
                collectRaid.setId(MyIdUtil.getId());
                collectRaid.setAssetId(assetId);
                date.setTime(time);
                collectRaid.setCollectTime(date);
                collectRaid.setCollectCode(raidSystemMsg.getCollectCode());

                if (Objects.nonNull(collectRaids)&&!collectRaids.isEmpty()&&Objects.nonNull(mdiskList)&&Objects.nonNull(collectRaid.getParentOrgId())) {
                    for (DiskBean diskBean : mdiskList) {
                        if(drive.getParentOrgId().equals(diskBean.getRealId())){
                            String grpId = diskBean.getGrpId();
                            for (CollectRaid raid : collectRaids) {
                                if (raid.getRealId().equals(grpId)){
                                    collectRaid.setParentOrgId(raid.getId());
                                }
                            }
                        }
                    }
                }
                raidService.save(collectRaid);
            }
        }


        //存储Log
        if (ObjectUtil.isNotNull(raidSystemMsg.getLogList()) && !raidSystemMsg.getLogList().isEmpty()) {
            List<String> logList = raidSystemMsg.getLogList();
            for (String log : logList) {
                CollectRaid collectRaid = new CollectRaid();
                collectRaid.setDiskType(5);
                collectRaid.setLogInfo(log);
                collectRaid.setId(MyIdUtil.getId());
                collectRaid.setAssetId(assetId);
                date.setTime(time);
                collectRaid.setCollectTime(date);
                collectRaid.setCollectCode(raidSystemMsg.getCollectCode());
                raidService.save(collectRaid);
            }
        }
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.RAID_SYSTEM_MSG;
    }

}
