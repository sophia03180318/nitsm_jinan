package com.jcca.dataProcessing.DataFilter.DB;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectDBEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 数据库表空间阈值过滤处理类
 * @className DBTableSpaceFilterHandler
 * @date 2023/10/27 9:30
 * @since 2.1.0.0
 */
@Component("dbInfoFilterHandler")
public class DbInfoFilterHandler extends IFilterHandler<CollectDBEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectDBEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_db.getCode();
        String mapKey1 = StatusInfoChangeTypeEnum.status_db_state.getCode();
        ChangeInfo changeInfo1 = this.createChangeInfo(info.getStatus(), redisKey, mapKey1);
        changeInfo1.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey1, changeInfo1);

        String mapKey2=StatusInfoChangeTypeEnum.status_dbVersion.getCode();
        ChangeInfo changeInfo2= this.createChangeInfo(info.getDbVersion(),redisKey,mapKey2);
        changeInfo2.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey2, changeInfo2);


        String mapKey3=StatusInfoChangeTypeEnum.status_dbSysUpTime.getCode();
        ChangeInfo changeInfo3= this.createChangeInfo(info.getSysUpTime(),redisKey,mapKey3);
        changeInfo3.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey3, changeInfo3);

        String mapKey4=StatusInfoChangeTypeEnum.status_dbCacheLibrary.getCode();
        ChangeInfo changeInfo4= this.createChangeInfo(info.getCacheLibrary(),redisKey,mapKey4);
        changeInfo4.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey4, changeInfo4);

        String mapKey5=StatusInfoChangeTypeEnum.status_dbMemTotal.getCode();
        ChangeInfo changeInfo5= this.createChangeInfo(info.getDbMemTotal(),redisKey,mapKey5);
        changeInfo5.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey5, changeInfo5);

        String mapKey6=StatusInfoChangeTypeEnum.status_dbDiskTotal.getCode();
        ChangeInfo changeInfo6= this.createChangeInfo(info.getDbDiskTotal(),redisKey,mapKey6);
        changeInfo6.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey6, changeInfo6);

        String mapKey7=StatusInfoChangeTypeEnum.status_dbCache.getCode();
        ChangeInfo changeInfo7= this.createChangeInfo(info.getDbCache(),redisKey,mapKey7);
        changeInfo7.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey7, changeInfo7);

        String mapKey8=StatusInfoChangeTypeEnum.status_dbBusynessRate.getCode();
        ChangeInfo changeInfo8= this.createChangeInfo(info.getDbBusynessRate(),redisKey,mapKey8);
        changeInfo8.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey8, changeInfo8);

        String mapKey9=StatusInfoChangeTypeEnum.status_dbSessionSize.getCode();
        ChangeInfo changeInfo9= this.createChangeInfo(info.getDbSessionSize(),redisKey,mapKey9);
        changeInfo9.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey9, changeInfo9);

        String mapKey10=StatusInfoChangeTypeEnum.status_dbSessionUsedRate.getCode();
        ChangeInfo changeInfo10= this.createChangeInfo(info.getDbSessionUsedRate(),redisKey,mapKey10);
        changeInfo10.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey10, changeInfo10);

        String mapKey11=StatusInfoChangeTypeEnum.status_dbCachePoolSize.getCode();
        ChangeInfo changeInfo11= this.createChangeInfo(info.getDbCachePoolSize(),redisKey,mapKey11);
        changeInfo11.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey11, changeInfo11);

        String mapKey12=StatusInfoChangeTypeEnum.status_dbSessionUsedRate.getCode();
        ChangeInfo changeInfo12= this.createChangeInfo(info.getDbCachePoolhit(),redisKey,mapKey12);
        changeInfo12.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey12, changeInfo12);

        String mapKey13=StatusInfoChangeTypeEnum.status_dbCachePoolSize.getCode();
        ChangeInfo changeInfo13= this.createChangeInfo(info.getCanUseLockSize(),redisKey,mapKey13);
        changeInfo13.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey13, changeInfo13);

        String mapKey14=StatusInfoChangeTypeEnum.status_dbCachePoolhit.getCode();
        ChangeInfo changeInfo14= this.createChangeInfo(info.getDbLockUsedRate(),redisKey,mapKey14);
        changeInfo14.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey14, changeInfo14);

        String mapKey15=StatusInfoChangeTypeEnum.status_canUseLockSize.getCode();
        ChangeInfo changeInfo15= this.createChangeInfo(info.getDbLockWaitRate(),redisKey,mapKey15);
        changeInfo15.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey15, changeInfo15);

        String mapKey16=StatusInfoChangeTypeEnum.status_dbLockUsedRate.getCode();
        ChangeInfo changeInfo16= this.createChangeInfo(info.getJavaPoolSize(),redisKey,mapKey16);
        changeInfo16.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey16, changeInfo16);

        String mapKey17=StatusInfoChangeTypeEnum.status_dbLockWaitRate.getCode();
        ChangeInfo changeInfo17= this.createChangeInfo(info.getDbLockWaitRate(),redisKey,mapKey17);
        changeInfo17.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey17, changeInfo17);

        String mapKey18=StatusInfoChangeTypeEnum.status_javaPoolSize.getCode();
        ChangeInfo changeInfo18= this.createChangeInfo(info.getJavaPoolSize(),redisKey,mapKey18);
        changeInfo18.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey18, changeInfo18);

        String mapKey19=StatusInfoChangeTypeEnum.status_redoLogBuffer.getCode();
        ChangeInfo changeInfo19= this.createChangeInfo(info.getRedoLogBuffer(),redisKey,mapKey19);
        changeInfo19.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey19, changeInfo19);

        String mapKey20=StatusInfoChangeTypeEnum.status_dbConnection.getCode();
        ChangeInfo changeInfo20= this.createChangeInfo(info.getDbConnection(),redisKey,mapKey20);
        changeInfo20.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey20, changeInfo20);

        String mapKey21=StatusInfoChangeTypeEnum.status_dbActive.getCode();
        ChangeInfo changeInfo21= this.createChangeInfo(info.getDbActive(),redisKey,mapKey21);
        changeInfo21.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey21, changeInfo21);

        String mapKey22=StatusInfoChangeTypeEnum.status_dbLanguage.getCode();
        ChangeInfo changeInfo22= this.createChangeInfo(info.getLanguage(),redisKey,mapKey22);
        changeInfo22.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey22, changeInfo22);

        String mapKey23=StatusInfoChangeTypeEnum.status_alertPath.getCode();
            ChangeInfo changeInfo23= this.createChangeInfo(info.getAlertPath(),redisKey,mapKey23);
        changeInfo23.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey23, changeInfo23);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey){
        ChangeInfo changeInfo=new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }

}
