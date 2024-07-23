package com.jcca.web.asset.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.dao.AssetHidConfMapper;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 隐藏配置
 */
@Service
public class AssetHidConfServiceImpl extends ServiceImpl<AssetHidConfMapper, AssetHidConf> implements AssetHidConfService {

    @Resource
    private  AssetHidConfMapper confMapper;
    @Resource
    private AlarmInfoService alarmServ;

    @Override
    public List<AssetHidConf> getFlagListByAsset(String assetId, String type) {
        List<AssetHidConf> flagListByAsset = confMapper.getFlagListByAsset(assetId, type);
        return flagListByAsset;
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetNetCardConf(String assetId, List<AssetHidConf> saveList) {

        List<AlarmInfo> alarmList=new ArrayList<>();
        for (AssetHidConf assetHidConf : saveList) {
            QueryWrapper<AssetHidConf> confQuery = new QueryWrapper<>();
            confQuery.eq("asset_id",assetId);
            confQuery.eq("flag",assetHidConf.getFlag());
            confQuery.eq("type",AssetHidConf.TypeEnum.NET_CARD.name());
            remove(confQuery);

            //查找此网卡产生的未恢复或未确认的告警
            QueryWrapper<AlarmInfo> query = new QueryWrapper<AlarmInfo>();
            query.eq("ASSET_ID",assetId);
            query.and(wq->{wq.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode()).or().eq("STATUS", AlarmStatusEnum.UNCONFIRM.getCode());});
            query.like("CONTENT",assetHidConf.getFlag());
            List<AlarmInfo> list = alarmServ.list(query);
            if (ObjectUtil.isNotNull(list)){
                alarmList .addAll(list);
            }
        }

        saveBatch(saveList);
        //将此网卡产生的未恢复的告警设置为恢复
        for (AlarmInfo alarmInfo : alarmList) {
            alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            alarmInfo.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
            alarmInfo.setContent(alarmInfo.getContent()+"【取消监控此网卡】");
            alarmServ.updateById(alarmInfo);
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetPortConf(String assetId, List<AssetHidConf> saveList) {

        List<AlarmInfo> alarmList=new ArrayList<>();
        for (AssetHidConf assetHidConf : saveList) {
            QueryWrapper<AssetHidConf> confQuery = new QueryWrapper<>();
            confQuery.eq("asset_id",assetId);
            confQuery.eq("flag",assetHidConf.getFlag());
            confQuery.eq("type",AssetHidConf.TypeEnum.PORT.name());
            remove(confQuery);

            //查找此端口产生的未恢复或未确认的告警
            QueryWrapper<AlarmInfo> query = new QueryWrapper<AlarmInfo>();
            query.eq("ASSET_ID",assetId);
            query.and(wq->{wq.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode()).or().eq("STATUS", AlarmStatusEnum.UNCONFIRM.getCode());});
            query.like("CONTENT",assetHidConf.getFlag());
            List<AlarmInfo> list = alarmServ.list(query);
            if (ObjectUtil.isNotNull(list)){
                alarmList .addAll(list);
            }
        }

        saveBatch(saveList);
        //将此端口产生的未恢复的告警设置为恢复
        for (AlarmInfo alarmInfo : alarmList) {
            alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            alarmInfo.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
            alarmInfo.setContent(alarmInfo.getContent()+"【取消监控此端口】");
            alarmServ.updateById(alarmInfo);
        }
    }

    @Override
    public List<AssetHidConf> getHidConfigByAssetAndFlag(String assetId, String flag) {
        if(StrUtil.isEmpty(flag)){
            return new ArrayList<>();
        }
        List<AssetHidConf> assetHidConfList = confMapper.selectListByAssetAndFlag(assetId, flag);
        return assetHidConfList;
    }


}
