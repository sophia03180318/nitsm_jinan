package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.DSBean;
import com.jcca.component.thresholds.bean.DsSystemFattenBean;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.service.CollectDsService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 处理DS系统数据
 *
 * @author sophia
 */
@Slf4j
@Service
public class DisposeDsInfoAdapterImpl implements CollectAdapter {

    @Resource
    private CollectDsService dsService;
    @Resource
    private EventLogicService eventLogicServ;
    private String code = "DSIK_ARRAY_DRIVE";
    private String code2 = "DSIK_ARRAY_DRIVE_PULL_OUT";
    private String code3 = "ARRAY_DRIVE";

    @Override
    public void dispose(JSONArray data) {
        List<DsSystemFattenBean> beanList = JSONUtil.toList(data, DsSystemFattenBean.class);

        if (beanList.isEmpty()) {
            log.error("DS 数据处理失败，空的序列集合");
            return;
        }

        DsSystemFattenBean dsSystemFattenBean = beanList.get(0);
        String assetId = dsSystemFattenBean.getAssetId();

        if (StrUtil.isEmpty(assetId)) {
            log.error("DS 数据处理失败，缺少资产ID");
            return;
        }

/*        if (Objects.isNull(assetServ.getById(assetId))) {
            log.error("DS 数据处理失败，资产不存在，ID：{}", assetId);
            return;
        }*/
        Date date = new Date();
        date.setTime(Long.valueOf(dsSystemFattenBean.getCollectTime()));
        String collectCode = MyIdUtil.getId();

        //存储Controller
        if (ObjectUtil.isNotNull(dsSystemFattenBean.getControllers()) && !dsSystemFattenBean.getControllers().isEmpty()) {
            List<DSBean> controllers = dsSystemFattenBean.getControllers();
            for (DSBean dsbean : controllers) {
                CollectDS collectDS = new CollectDS();
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setId(MyIdUtil.getId());
                collectDS.setAssetId(assetId);
                collectDS.setCollectCode(collectCode);
                collectDS.setCollectTime(date);
                dsService.save(collectDS);
            }
        }


        List<DSBean> arrays = null;
        //存储Array
        if (ObjectUtil.isNotNull(dsSystemFattenBean.getArrays()) && !dsSystemFattenBean.getArrays().isEmpty()) {
            arrays = dsSystemFattenBean.getArrays();
            for (DSBean dsbean : arrays) {
                CollectDS collectDS = new CollectDS();
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setAssetId(assetId);
                collectDS.setCollectCode(collectCode);
                collectDS.setCollectTime(date);
                dsService.save(collectDS);
            }
        }

        //存储Drives

        if (ObjectUtil.isNotNull(dsSystemFattenBean.getDrives()) && !dsSystemFattenBean.getDrives().isEmpty()) {
            List<DSBean> drives = dsSystemFattenBean.getDrives();
            ArrayList<CollectDS> newDrivers = new ArrayList<>();
            for (DSBean dsbean : drives) {
                CollectDS collectDS = new CollectDS();
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setId(MyIdUtil.getId());
                collectDS.setAssetId(assetId);
                if (Objects.nonNull(collectDS.getParentOrgName()) && Objects.nonNull(arrays)) {
                    for (DSBean array : arrays) {
                        if (collectDS.getParentOrgName().equals(array.getName())) {
                            collectDS.setParentOrgId(array.getId());
                        }
                    }
                }
                collectDS.setCollectCode(collectCode);
                collectDS.setCollectTime(date);
                if (Objects.nonNull(dsbean.getStatusInfo()) && dsbean.getStatusInfo().contains("Optimal")) {
                    collectDS.setStatus(1);
                } else {
                    collectDS.setStatus(2);
                }
                newDrivers.add(collectDS);
                dsService.save(collectDS);
            }

            try {
                //对比array状态
                List<CollectDS> oldArrayList = dsService.findByType(assetId, 1);
                if (ObjectUtil.isNotNull(arrays)&&ObjectUtil.isNotNull(oldArrayList)) {
                    for (DSBean newArray : arrays) {
                        for (CollectDS oldArray : oldArrayList) {
                            if (newArray.getName().equals(oldArray.getName())) {
                                if (!newArray.getStatusInfo().equals("Optimal") && oldArray.getStatusInfo().equals("Optimal")) {
                                    //array状态告警
                                    CreateEventReq addEventReq = new CreateEventReq();
                                    addEventReq.setGroupFlag(MyIdUtil.getId());
                                    addEventReq.setAssetId(assetId);
                                    addEventReq.setCreateTime(date);
                                    addEventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                                    addEventReq.setOriginalMsg("名称为" + newArray.getName() + "的Array,状态异常，现状态为:" + newArray.getStatusInfo());
                                    addEventReq.setUniqueCode(code3);
                                    addEventReq.setFlag(newArray.getName());
                                    try {
                                        eventLogicServ.addEvent(addEventReq);
                                    } catch (Exception e) {
                                        log.error("磁盘阵列[DS] 推送Array告警事件失败" + e.getMessage(), e);
                                    }
                                }

                                if (newArray.getStatusInfo().equals("Optimal") && !oldArray.getStatusInfo().equals("Optimal")) {
                                    //array状态恢复
                                    CreateEventReq addEventReq = new CreateEventReq();
                                    addEventReq.setGroupFlag(MyIdUtil.getId());
                                    addEventReq.setAssetId(assetId);
                                    addEventReq.setCreateTime(date);
                                    addEventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                                    addEventReq.setOriginalMsg("名称为" + newArray.getName() + "的Array,状态恢复，现状态为:" + newArray.getStatusInfo());
                                    addEventReq.setUniqueCode(code3);
                                    addEventReq.setFlag(newArray.getName());
                                    try {
                                        eventLogicServ.addEvent(addEventReq);
                                    } catch (Exception e) {
                                        log.error("磁盘阵列[DS] 推送Array恢复事件失败" + e.getMessage(), e);
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (Exception e) {
                log.error("DS:array 对比出错" + e.toString());
            }


            //对比磁盘状态
            List<CollectDS> oldDrives = dsService.findByType(assetId, 3);
            if (Objects.nonNull(oldDrives) && !oldDrives.isEmpty()) {
                for (CollectDS oldDrive : oldDrives) {
                    boolean exits = false;
                    for (CollectDS newDrive : newDrivers) {
                        if (oldDrive.getXindex() == newDrive.getXindex() && oldDrive.getYindex() == oldDrive.getYindex()) {
                            exits = true;
                            if (oldDrive.getStatus() == 1 & newDrive.getStatus() != 1) {
                                //硬盘状态告警
                                CreateEventReq addEventReq = new CreateEventReq();
                                addEventReq.setGroupFlag(MyIdUtil.getId());
                                addEventReq.setAssetId(assetId);
                                addEventReq.setCreateTime(date);
                                addEventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                                addEventReq.setOriginalMsg("存储位置为" + (oldDrive.getYindex()) + " - " + (oldDrive.getXindex()) + "的硬盘发生故障，现状态为:" + newDrive.getStatusInfo());
                                addEventReq.setUniqueCode(code);
                                addEventReq.setFlag((oldDrive.getYindex()) + " - " + (oldDrive.getXindex()));
                                try {
                                    eventLogicServ.addEvent(addEventReq);
                                } catch (Exception e) {
                                    log.error("磁盘阵列[DS] 推送Drive告警事件失败" + e.getMessage(), e);
                                }
                            } else if (oldDrive.getStatus() != 1 & newDrive.getStatus() == 1) {
                                //硬盘状态恢复
                                CreateEventReq addEventReq = new CreateEventReq();
                                addEventReq.setGroupFlag(MyIdUtil.getId());
                                addEventReq.setAssetId(assetId);
                                addEventReq.setCreateTime(date);
                                addEventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                                addEventReq.setOriginalMsg("存储位置为" + (oldDrive.getYindex()) + " - " + (oldDrive.getXindex()) + "的硬盘故障恢复，现状态为:" + newDrive.getStatusInfo());
                                addEventReq.setUniqueCode(code);
                                addEventReq.setFlag((oldDrive.getYindex()) + " - " + (oldDrive.getXindex()));
                                try {
                                    eventLogicServ.addEvent(addEventReq);
                                } catch (Exception e) {
                                    log.error("磁盘阵列[DS] 推送Drive恢复事件失败" + e.getMessage(), e);
                                }
                            }

                        }
                    }

                    if (!exits) {
                        //硬盘拔出 通知告警
                        CreateEventReq addEventReq = new CreateEventReq();
                        addEventReq.setGroupFlag(MyIdUtil.getId());
                        addEventReq.setAssetId(assetId);
                        addEventReq.setCreateTime(date);
                        addEventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                        addEventReq.setOriginalMsg("检测到失去存储位置为" + (oldDrive.getYindex()) + " - " + (oldDrive.getXindex()) + "硬盘的监控，请确认是否被拔出");
                        addEventReq.setUniqueCode(code2);
                        addEventReq.setFlag((oldDrive.getYindex()) + " - " + (oldDrive.getXindex()));
                        try {
                            eventLogicServ.addEvent(addEventReq);
                        } catch (Exception e) {
                            log.error("磁盘阵列[DS] 推送通知事件失败" + e.getMessage(), e);
                        }
                    }
                }
            }
        }


        //存储logical Drives
        if (ObjectUtil.isNotNull(dsSystemFattenBean.getLogicalDrives()) && !dsSystemFattenBean.getLogicalDrives().isEmpty()) {
            List<DSBean> logicalDrives = dsSystemFattenBean.getLogicalDrives();
            for (DSBean dsbean : logicalDrives) {
                CollectDS collectDS = new CollectDS();
                BeanUtils.copyProperties(dsbean, collectDS);
                collectDS.setId(MyIdUtil.getId());
                collectDS.setAssetId(assetId);
                collectDS.setCollectCode(collectCode);
                collectDS.setCollectTime(date);
                collectDS.setFreeCapacityStr(dsbean.getFreeCapacityStr());
                if (ObjectUtil.isNotNull(dsSystemFattenBean.getLogicalDrives2()) && !dsSystemFattenBean.getLogicalDrives2().isEmpty()) {
                    List<DSBean> logicalDrives2 = dsSystemFattenBean.getLogicalDrives2();
                    for (DSBean drives2 : logicalDrives2) {
                        if (drives2.getName().equals(dsbean.getName())) {
                            collectDS.setStatusInfo(drives2.getStatusInfo());
                            //collectDS.setRaidLevel(drives2.getRaidLevel());
                            collectDS.setFreeCapacityStr(drives2.getFreeCapacityStr());
                        }
                    }
                }
                dsService.save(collectDS);
            }
        }

        if (ObjectUtil.isNotNull(dsSystemFattenBean.getLogInfo()) && !dsSystemFattenBean.getLogInfo().isEmpty()) {
            List<String> logInfo = dsSystemFattenBean.getLogInfo();
            for (String log : logInfo) {
                CollectDS collectDS = new CollectDS();
                collectDS.setId(MyIdUtil.getId());
                collectDS.setAssetId(assetId);
                collectDS.setType(5);
                collectDS.setLogInfo(log + "<br/>");
                collectDS.setCollectCode(collectCode);
                collectDS.setCollectTime(date);
                dsService.save(collectDS);
            }

        }

        if (Objects.nonNull(dsSystemFattenBean.getCapacity()) && Objects.nonNull(dsSystemFattenBean.getFreeCapacity())) {
            if (dsSystemFattenBean.getCapacity() != 0 || (ObjectUtil.isNotNull(dsSystemFattenBean.getDrives()) && !dsSystemFattenBean.getDrives().isEmpty())) {
                CollectDS collectDS = new CollectDS();
                collectDS.setType(4);
                collectDS.setCapacity(dsSystemFattenBean.getCapacity());
                collectDS.setFreeCapacity(dsSystemFattenBean.getFreeCapacity());
                collectDS.setId(MyIdUtil.getId());
                collectDS.setAssetId(assetId);
                collectDS.setCollectCode(collectCode);
                collectDS.setCollectTime(date);
                dsService.save(collectDS);
            }

        }

    }


    @Override
    public String getCode() {
        return ReceiveCollectConst.DS_SYSTEM_MSG;
    }

}
