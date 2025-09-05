package com.jcca.dataProcessing.DataFilter.DB;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.*;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectDB;
import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.entity.CollectTablespace;
import com.jcca.web.collect.service.CollectDBService;
import com.jcca.web.collect.service.CollectDBfileService;
import com.jcca.web.collect.service.CollectTablespaceService;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web2.entity.*;
import com.jcca.web2.service.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 数据库表空间阈值过滤处理类
 * @className DBTableSpaceFilterHandler
 * @date 2023/10/27 9:30
 * @since 2.1.0.0
 */
@Component("dbInfoSaveFilterHandler")
public class DbInfoSaveFilterHandler extends IFilterHandler<CollectDBEntity> {

    @Resource
    private CollectTablespaceService collectTablespaceService;
    @Resource
    private CollectDBService dbService;
    @Resource
    private CollectDBfileService dBfileService;
    @Resource
    private CollectDatabasesInfoService databasesInfoService;
    @Resource
    private CollectDbLogSettingService collectDbLogSettingService;
    @Resource
    private CollectDbProcessLockInfoService processLockInfoService;
    @Resource
    private CollectDbLockInfoService dbLockInfoService;
    @Resource
    private CollectDbSlowSqlService slowSqlService;
    @Resource
    private ManageDbService dbManagerService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean handler(CollectDBEntity info) {
        QueryWrapper<ManageDb> mdbQuery = new QueryWrapper<>();
        mdbQuery.eq("ASSET_ID", info.getAssetId());
        List<ManageDb> list = dbManagerService.list(mdbQuery);
        if(list.size() !=1 ){
            //录入的数据库可能已经被删除 或者存在重复的数据库信息
            return false;
        }


        Date date = new Date();
        date.setTime(info.getCollectTime());
        CollectDB entity = EntityBeanUtil.copy(info, CollectDB.class);
        List<CollectTablespaceEntity> tableSpace = info.getTablespace();
        List<CollectTablespace> lists = new ArrayList<>();


        List<CollectDBfile> dBfiles = entity.getDbFiles();
        for (CollectDBfile dBfile : dBfiles) {
            dBfile.setCollectTime(date);
        }
        entity.setId(list.get(0).getId());
        entity.setAssetId(info.getAssetId());
        entity.setCollectCode(info.getCollectTime().toString());
        entity.setCollectTime(date);
        if (StrUtil.isNotEmpty(info.getCacheHitRate())) {
            entity.setCacheHitRate(Double.valueOf(info.getCacheLibrary()));
        }

        if (StrUtil.isNotEmpty(info.getDbBusynessRate())) {
            entity.setDbBusynessRate(Double.valueOf(info.getDbBusynessRate()));
        }
        if (StrUtil.isNotEmpty(info.getDbSessionUsedRate())) {
            entity.setDbSessionUsedRate(Double.valueOf(info.getDbSessionUsedRate()));
        }
        if (StrUtil.isNotEmpty(info.getCacheLibrary())) {
            entity.setDbCachePoolhit(Double.valueOf(info.getCacheLibrary()));
        }
        if (StrUtil.isNotEmpty(info.getDbLockUsedRate())) {
            entity.setDbLockUsedRate(Double.valueOf(info.getDbLockUsedRate()));
        }
        if (StrUtil.isNotEmpty(info.getDbLockWaitRate())) {
            entity.setDbLockWaitRate(Double.valueOf(info.getDbLockWaitRate()));
        }
        if (Objects.nonNull(info.getBlockedLock())) {
            entity.setBlockedLock(info.getBlockedLock() ? 1 : -1);
        }
        if (StrUtil.isNotEmpty(info.getRedoLogBuffer())) {
            entity.setRedoLogBuffer(info.getRedoLogBuffer());
        }

        //这里必须保证ID不变，不然关联数据在做迭代的时候 界面查询会出问题
        dbService.saveOrUpdate(entity);

        //处理表空间
        for (CollectTablespaceEntity tablespace : tableSpace) {

            CollectTablespace tablespace1 = EntityBeanUtil.copy(tablespace, CollectTablespace.class);
            tablespace1.setCollectTime(date);
            tablespace1.setCollectDbId(entity.getId());
            tablespace1.setAssetId(entity.getAssetId());
            lists.add(tablespace1);
        }
        QueryWrapper<CollectTablespace> query = Wrappers.query();
        query.eq("asset_id", info.getAssetId());
        collectTablespaceService.remove(query);
        collectTablespaceService.saveBatch(lists);

        // 删除文件数据
        QueryWrapper<CollectDBfile> fileQuery = Wrappers.query();
        fileQuery.eq("asset_id", info.getAssetId());
        dBfileService.remove(fileQuery);
        //保存各类文件数据
        List<CollectDBfile> dbFiles = info.getDbFiles();
        for (CollectDBfile dbFile : dbFiles) {
            dbFile.setAssetId(info.getAssetId());
            dbFile.setCollectTime(date);
            dbFile.setCollectDbId(entity.getId());
        }
        dBfileService.saveBatch(dbFiles);

        //处理数据库基础信息
        List<DatabasesBeanEntity> databasesInfoList = info.getDatabasesInfoList();
        if (Objects.nonNull(databasesInfoList) && !databasesInfoList.isEmpty()) {
            List<CollectDatabasesInfo> collectDatabasesInfos = EntityBeanUtil.copyList(databasesInfoList, CollectDatabasesInfo.class);
            for (CollectDatabasesInfo databasesBeanEntity : collectDatabasesInfos) {
                databasesBeanEntity.setPermitAgentLinkStatus(databasesBeanEntity.getPermitAgentLink() ? 1 : -1);
                databasesBeanEntity.setId(MyIdUtil.getId());
                databasesBeanEntity.setCollectDbId(entity.getId());
                databasesBeanEntity.setAssetId(entity.getAssetId());
                databasesBeanEntity.setIsTemplateFlag(databasesBeanEntity.getIsTemplate() ? 1 : -1);
            }
            //更新采集数据
            databasesInfoService.updateCollectData(collectDatabasesInfos, entity.getAssetId());
        }

        //处理日志配置信息
        List<DbLogSettingEntity> logSettingList = info.getLogSettingList();
        if (Objects.nonNull(logSettingList) && !logSettingList.isEmpty()) {
            List<CollectDbLogSetting> collectDbLogSettingList = EntityBeanUtil.copyList(logSettingList, CollectDbLogSetting.class);
            for (CollectDbLogSetting collectDbLogSetting : collectDbLogSettingList) {
                collectDbLogSetting.setId(MyIdUtil.getId());
                collectDbLogSetting.setCollectDbId(entity.getId());
                collectDbLogSetting.setAssetId(entity.getAssetId());
            }

            collectDbLogSettingService.updateCollectData(collectDbLogSettingList, entity.getAssetId());
        }

        //处理数据库进程锁
        List<DbProcessLockEntity> processLockList = info.getProcessLockList();
        if (Objects.nonNull(processLockList) && !processLockList.isEmpty()) {
            List<CollectDbProcessLockInfo> collectProcessLockList = EntityBeanUtil.copyList(processLockList, CollectDbProcessLockInfo.class);
            for (CollectDbProcessLockInfo collectProcessLock : collectProcessLockList) {
                collectProcessLock.setId(MyIdUtil.getId());
                collectProcessLock.setCollectDbId(entity.getId());
                collectProcessLock.setAssetId(entity.getAssetId());
            }

            processLockInfoService.updateCollectData(collectProcessLockList, entity.getAssetId());
        }

        //处理数据库锁信息
        List<DbLockInfoEntity> lockInfoList = info.getLockInfoList();
        if (Objects.nonNull(lockInfoList) && !lockInfoList.isEmpty()) {
            List<CollectDbLockInfo> collectDbLockList = EntityBeanUtil.copyList(lockInfoList, CollectDbLockInfo.class);
            for (CollectDbLockInfo collectDbLock : collectDbLockList) {
                collectDbLock.setId(MyIdUtil.getId());
                collectDbLock.setCollectDbId(entity.getId());
                collectDbLock.setAssetId(entity.getAssetId());
            }

            dbLockInfoService.updateCollectData(collectDbLockList, entity.getAssetId());
        }
        //处理数据库慢sql
        List<CollectDbSlowSql> slowSqlList = info.getSlowSqlList();
        if (Objects.nonNull(slowSqlList) && !slowSqlList.isEmpty()) {
            List<CollectDbSlowSql> collectSlowList = EntityBeanUtil.copyList(slowSqlList, CollectDbSlowSql.class);

            for (CollectDbSlowSql collectDbSlowSql : collectSlowList) {
                collectDbSlowSql.setId(MyIdUtil.getId());
                collectDbSlowSql.setCollectDbId(entity.getId());
                collectDbSlowSql.setAssetId(entity.getAssetId());
            }
            slowSqlService.updateCollectData(collectSlowList, entity.getAssetId());
        }

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
