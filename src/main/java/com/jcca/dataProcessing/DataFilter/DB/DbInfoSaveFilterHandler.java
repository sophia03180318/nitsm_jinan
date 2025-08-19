package com.jcca.dataProcessing.DataFilter.DB;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectDBEntity;
import com.jcca.dataProcessing.Entity.CollectTablespaceEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectDB;
import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.entity.CollectTablespace;
import com.jcca.web.collect.service.CollectDBService;
import com.jcca.web.collect.service.CollectDBfileService;
import com.jcca.web.collect.service.CollectTablespaceService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean handler(CollectDBEntity info) {
        Date date = new Date();
        date.setTime(info.getCollectTime());
        CollectDB entity = EntityBeanUtil.copy(info, CollectDB.class);
        List<CollectTablespaceEntity> tableSpace = info.getTablespace();
        List<CollectTablespace> lists = new ArrayList<>();

        String dbId = MyIdUtil.getId();

        List<CollectDBfile> dBfiles = entity.getDbFiles();
        for (CollectDBfile dBfile : dBfiles) {
            dBfile.setCollectTime(date);
        }
        entity.setId(dbId);
        entity.setAssetId(info.getAssetId());
        entity.setCollectCode(info.getCollectTime().toString());
        entity.setCollectTime(date);
        if(StrUtil.isNotEmpty(info.getCacheLibrary())){
            entity.setCacheHitRate(Double.valueOf(info.getCacheLibrary()));
        }
        if(StrUtil.isNotEmpty(info.getDbBusynessRate())){
            entity.setDbBusynessRate(Double.valueOf(info.getDbBusynessRate()));
        }
        if(StrUtil.isNotEmpty(info.getDbSessionUsedRate())){
            entity.setDbSessionUsedRate(Double.valueOf(info.getDbSessionUsedRate()));
        }
        if(StrUtil.isNotEmpty(info.getCacheLibrary())){
            entity.setDbCachePoolhit(Double.valueOf(info.getCacheLibrary()));
        }
        if(StrUtil.isNotEmpty(info.getDbLockUsedRate())){
            entity.setDbLockUsedRate(Double.valueOf(info.getDbLockUsedRate()));
        }
        if(StrUtil.isNotEmpty(info.getDbLockWaitRate())){
            entity.setDbLockWaitRate(Double.valueOf(info.getDbLockWaitRate()));
        }

        dbService.save(entity);

        //处理表空间
        for (CollectTablespaceEntity tablespace : tableSpace) {

            CollectTablespace tablespace1 = EntityBeanUtil.copy(tablespace, CollectTablespace.class);
            tablespace1.setCollectTime(date);
            tablespace1.setCollectDbId(dbId);
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
            dbFile.setCollectDbId(dbId);
        }
        dBfileService.saveBatch(dbFiles);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
