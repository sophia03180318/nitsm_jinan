package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectDBBean;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectDB;
import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.entity.CollectTablespace;
import com.jcca.web.collect.service.CollectDBService;
import com.jcca.web.collect.service.CollectDBfileService;
import com.jcca.web.collect.service.CollectTablespaceService;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 数据库采集
 *
 * @author Lvyp
 */
@Component
@Slf4j
public class DisposeDbAdapterImpl implements CollectAdapter {

    @Resource
    private CollectDBService dbService;
    @Resource
    private ManageDbService managerDbServ;
    @Resource
    private CollectTablespaceService collectTablespaceService;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private CollectDBfileService dBfileService;

    /**
     * 数据库
     *
     * @param data
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispose(JSONArray data) {
        List<CollectDBBean> dbList = JSONUtil.toList(data, CollectDBBean.class);
        String collectCode = MyIdUtil.getId();
        List<CollectDB> entityList = new ArrayList<CollectDB>();
        List<CollectTablespace> tablespaceList = new ArrayList<>();
        List<CollectDBfile> dBfileList = new ArrayList<>();
        Set<String> dbIdSet = new HashSet<>();
        for (CollectDBBean db : dbList) {
            if (StrUtil.isEmpty(db.getAssetId()) || StrUtil.isEmpty(db.getCollectTime())
                    || Objects.isNull(db.getDbType()) || StrUtil.isEmpty(db.getDbVersion())
                    || Objects.isNull(db.getSysUpTime()) || StrUtil.isEmpty(db.getCacheLibrary())
                    || Objects.isNull(db.getStatus()) || Objects.isNull(db.getDbMemTotal())
                    || Objects.isNull(db.getDbCache()) || StrUtil.isEmpty(db.getDbBusynessRate())
                    || Objects.isNull(db.getDbSessionSize()) || StrUtil.isEmpty(db.getDbSessionUsedRate())
                    || Objects.isNull(db.getDbCachePoolSize()) || StrUtil.isEmpty(db.getDbCachePoolhit())
                    || Objects.isNull(db.getCanUseLockSize()) || StrUtil.isEmpty(db.getDbLockUsedRate())
                    || StrUtil.isEmpty(db.getDbLockWaitRate())) {
                log.error("数据库队列值不符合规范：" + JSONUtil.toJsonStr(db));
                continue;
            }

            Date date = new Date();
            date.setTime(Long.parseLong(db.getCollectTime()));

            CollectDB entity = EntityBeanUtil.copy(db, CollectDB.class);
            List<CollectTablespace> tablespaces = entity.getTablespace();
            for (CollectTablespace tablespace : tablespaces) {
                tablespace.setCollectTime(date);
            }

            List<CollectDBfile> dBfiles = entity.getDbFiles();
            for (CollectDBfile dBfile : dBfiles) {
                dBfile.setCollectTime(date);
            }

            entity.setId(MyIdUtil.getId());
            entity.setCollectCode(collectCode);
            entity.setCollectTime(date);
            entity.setCacheHitRate(Double.valueOf(db.getCacheLibrary()));
            entity.setDbBusynessRate(Double.valueOf(db.getDbBusynessRate()));
            entity.setDbSessionUsedRate(Double.valueOf(db.getDbSessionUsedRate()));
            entity.setDbCachePoolhit(Double.valueOf(db.getCacheLibrary()));
            entity.setDbLockUsedRate(Double.valueOf(db.getDbLockUsedRate()));
            entity.setDbLockWaitRate(Double.valueOf(db.getDbLockWaitRate()));
            entityList.add(entity);

            // 保存表空间数据
            tablespaceList.addAll(tablespaces);
            //保存各类文件数据
            dBfileList.addAll(dBfiles);

            dbIdSet.add(db.getAssetId());
        }

        if (entityList.isEmpty()) {
            return;
        }

        // 实时数据更新
        dbService.updateRealTimeData(entityList);
        // 采集记录保存
        dbService.saveBatch(entityList);
        // 删除原表空间数据
        for (String dbId : dbIdSet) {
            QueryWrapper<CollectTablespace> query = Wrappers.query();
            query.eq("asset_id", dbId);
            collectTablespaceService.remove(query);
        }
        // 保存表空间数据
        collectTablespaceService.saveBatch(tablespaceList);

        // 删除文件数据
        for (String dbId : dbIdSet) {
            QueryWrapper<CollectDBfile> query = Wrappers.query();
            query.eq("asset_id", dbId);
            dBfileService.remove(query);
        }
        //保存文件数据
        dBfileService.saveBatch(dBfileList);

        // 添加事件
        addEvent(dbIdSet, tablespaceList);
    }

    private void addEvent(Set<String> dbIdSet, List<CollectTablespace> tablespaceList) {
        List<String> dbIds = new ArrayList<>(dbIdSet);
        String dbId = dbIds.get(0);


        String lockKey = "ADD_EVENT_DBth_" + dbId;
        synchronized (lockKey.intern()) {
            for (CollectTablespace tabSp : tablespaceList) {
                ManageDb db = managerDbServ.getById(dbId);
                String spaceName = tabSp.getName();
                Double collectValue = tabSp.getUsedRate();

                VerifyThresholdReq req = new VerifyThresholdReq();
                req.setCollectValue(collectValue.toString());
                req.setAssetId(dbId);
                req.setHaveSection(false);
                req.setType(ThresholdSectionEnum.DB_SPACE_NAME);
                VerifyThresholdResp verifyResp = thresholdAssetService.verifyThreshold(req);

                List<CreateEventReq> needAddEventList = new ArrayList<CreateEventReq>();
                if (Objects.nonNull(verifyResp)) {
                    CreateEventReq eventReq = new CreateEventReq();
                    eventReq.setOriginalMsg(verifyResp.getMsg());
                    eventReq.setEventLevel(verifyResp.getAlarmStatus() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode());

                    eventReq.setAssetId(db.getAssetId());
                    eventReq.setBaseValue(verifyResp.getBaseValue());
                    eventReq.setCollectValue(collectValue.toString());
                    eventReq.setUniqueCode(EventUniqueCode.TABSP_UNIQUE_CODE);
                    eventReq.setFlag(spaceName);
                    eventReq.setGroupFlag(EventGroupConstant.DB_SPACE_NAME);
                    eventReq.setCreateTime(tabSp.getCollectTime());

                    needAddEventList.add(eventReq);
                }
                //处理表空间阶段阈值
                VerifyThresholdSectionResp thresholdSectionResp = thresholdAssetService.verifySectionThreshold(collectValue, EventUniqueCode.TABSP_UNIQUE_CODE);
                List<CreateEventReq> eventList = thresholdAssetService.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.SENSOR_GAUGE, tabSp.getCollectTime(), dbId, collectValue.toString(), "表空间："+spaceName);

                for (CreateEventReq createEventReq : eventList) {
                    createEventReq.setAssetId(db.getAssetId());
                }

                needAddEventList.addAll(eventList);

                try {
                    for (CreateEventReq createEventReq : needAddEventList) {
                        eventLogicServ.addEvent(createEventReq);
                    }
                } catch (Exception e) {
                    log.error("处理表空间事件异常：{}", e.getMessage(), e);
                }

            }
        }

    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.DB;
    }

}
