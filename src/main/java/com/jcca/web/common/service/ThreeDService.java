package com.jcca.web.common.service;

import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.service.bean.ThreeDResult;

/**
 * @description: 3D机房相关接口
 * @author: sophia
 * @create: 2023/12/04 14:06
 **/
public interface ThreeDService {
    /**
     * 一键同步所有设备
     */
    ThreeDResult syncAssetByRoom();

    /**
     * 设备上架
     */
    ThreeDResult pushAssetAdd(Asset asset);

    /**
     * 设备修改
     */
    ThreeDResult pushAssetChange(Asset asset);

    /**
     * 设备下架
     */
    ThreeDResult pushAssetRemove(String id);

    /**
     * 告警推送  定时推送系统内所有告警
     */
    ThreeDResult pushAlarm();

    /**
     * 告警变动信息  取消告警状态时推送一次
     */
    ThreeDResult cancelAlarm(AlarmInfo alarmInfo);

    /**
     * 链路信息  定时推送所有链路
     */
    ThreeDResult pushLink();

    /**
     * 动环监测点信息
     */
    ThreeDResult pushProperty();
}