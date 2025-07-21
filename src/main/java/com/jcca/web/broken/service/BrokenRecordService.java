package com.jcca.web.broken.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.broken.controller.bean.RecordHandleReq;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web2.vo.AssetLifeLineVo;
import com.jcca.web2.vo.DialogsAlarmListVo;

import java.util.List;

/**
 * 故障记录
 *
 * @author lyp
 */
public interface BrokenRecordService extends IService<BrokenRecord> {

    /**
     * 创建故障记录
     *
     * @param broken
     * @return
     */
    void createBroken(BrokenRecord broken);

    /**
     * 通过id 删除故障记录
     *
     * @param id
     * @return
     */
    void removeBroken(String id);

    /**
     * 告警转故障记录
     *
     * @param alarmInfo
     * @return
     */
    int transforRecord(AlarmInfo alarmInfo);

    /**
     * 处理故障记录
     *
     * @param recordHandleReq
     */
    void handle(RecordHandleReq recordHandleReq);

    /**
     * 删除设备的故障记录
     *
     * @param assetId
     */
    void removeAssetLog(String assetId);

    /**
     * 查询生命周期中的故障记录
     *
     * @param assetId
     * @return
     */
    List<AssetLifeLineVo> getLifeLineV2(String assetId);

    /**
     * 故障记录相关事件
     *
     * @param alarmId 告警ID
     * @return
     */
    List<DialogsAlarmListVo> listByAlarmId(String alarmId);

    /**
     *
     * 获取指定资产的最新记录
     * */
    BrokenRecord getByAssetId(String assetId);
}
