package com.jcca.web.broken.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.constant.BrokenRecordConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.BrokenOriginEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.broken.controller.bean.RecordHandleReq;
import com.jcca.web.broken.dao.BrokenRecordFileMapper;
import com.jcca.web.broken.dao.BrokenRecordMapper;
import com.jcca.web.broken.dao.BrokenRecordOpinionMapper;
import com.jcca.web.broken.dao.BrokenRecordWordMapper;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.broken.entity.BrokenRecordFile;
import com.jcca.web.broken.entity.BrokenRecordOpinion;
import com.jcca.web.broken.service.BrokenRecordOpinionService;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web2.vo.AssetLifeLineVo;
import com.jcca.web2.vo.DialogsAlarmListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 故障记录
 *
 * @author lyp
 */
@Service
public class BrokenRecordServiceImpl extends ServiceImpl<BrokenRecordMapper, BrokenRecord>
        implements BrokenRecordService {

    @Resource
    private BrokenRecordMapper brokenMapper;
    @Resource
    private BrokenRecordFileMapper fileMapper;
    @Resource
    private BrokenRecordOpinionMapper opinionMapper;
    @Resource
    private BrokenRecordWordMapper brokenRecordWordMapper;
    @Resource
    private BizManageService bizService;
    @Resource
    private AssetService assetService;
    @Resource
    private BrokenRecordOpinionService opinionService;

    /**
     * 保存
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createBroken(BrokenRecord broken) {
        String id = MyIdUtil.getId();
        broken.setId(id);
        brokenMapper.insert(broken);
        bizService.saveByBizAndOrg(id, BizManageConstant.BROKEN);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBroken(String id) {
        brokenMapper.deleteById(id);
        bizService.remove(id, BizManageConstant.BROKEN);
    }

    /**
     * 告警转故障记录
     *
     * @param alarmInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int transforRecord(AlarmInfo alarmInfo) {
        Asset asset = assetService.getById(alarmInfo.getAssetId());

        String id = MyIdUtil.getId();

        BrokenRecord record = new BrokenRecord();
        record.setId(id);
        record.setAssetId(alarmInfo.getAssetId());
        record.setAlarmId(alarmInfo.getId());
        record.setAlarmTitle(alarmInfo.getTitle());
        record.setOccurTime(alarmInfo.getOccurTime());
        record.setAlarmLevel(alarmInfo.getAlarmLevel());
        record.setOrigin(BrokenOriginEnum.SYSTEM_ALARM.getCode());
        record.setDescription(alarmInfo.getOpinion());
        record.setInfluence(asset.getIp());
        record.setReason("");
        record.setHandleResult(alarmInfo.getOpinion());
        record.setHandleWay(alarmInfo.getOpinion());
        record.setStatus(BrokenRecordConst.UNPROCESSED);
        brokenMapper.insert(record);

        bizService.saveByBizAndOrg(id, BizManageConstant.BROKEN);

        return 1;
    }

    /**
     * 处理故障记录
     *
     * @param recordHandleReq
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(RecordHandleReq recordHandleReq) {
        BrokenRecord brokenRecord = this.getById(recordHandleReq.getId());
        if (brokenRecord.getStatus() == BrokenRecordConst.PROCESSED) {
            throw new ResultException(ResultEnum.DUPLICATE.getCode(), "不能重复处理");
        }
        // 处理
        if (1 == recordHandleReq.getFlag()) {
            // 修改状态
            brokenRecord.setStatus(BrokenRecordConst.PROCESSED);
            brokenRecord.setCompleteTime(new Date());
            this.updateById(brokenRecord);
            // 记录意见
            this.saveOpinion(recordHandleReq);
        }
        // 记录意见
        if (0 == recordHandleReq.getFlag()) {
            this.saveOpinion(recordHandleReq);
        }
    }

    @Override
    public void removeAssetLog(String assetId) {
        QueryWrapper<BrokenRecord> brokenQuery = new QueryWrapper<BrokenRecord>();
        brokenQuery.eq("ASSET_ID", assetId);
        List<BrokenRecord> brokenList = brokenMapper.selectList(brokenQuery);

        List<String> brokenIdList = brokenList.stream().map(BrokenRecord::getId).collect(Collectors.toList());
        if (!brokenIdList.isEmpty()) {
            QueryWrapper<BrokenRecordFile> fileQuery = new QueryWrapper<>();
            fileQuery.in("BROKEN_RECORD_ID", brokenIdList);
            fileMapper.delete(fileQuery);

            QueryWrapper<BrokenRecordOpinion> optionWrapper = new QueryWrapper<>();
            optionWrapper.in("BROKEN_RECORD_ID", brokenIdList);
            opinionMapper.delete(optionWrapper);

            brokenMapper.deleteBatchIds(brokenIdList);
        }

    }

    @Override
    public List<AssetLifeLineVo> getLifeLineV2(String assetId) {
        QueryWrapper<BrokenRecord> brokenQuery = new QueryWrapper<BrokenRecord>();
        brokenQuery.eq("ASSET_ID", assetId);
        List<BrokenRecord> brokenList = brokenMapper.selectList(brokenQuery);

        List<AssetLifeLineVo> voList = new ArrayList<>();
        for (BrokenRecord brokenRecord : brokenList) {
            AssetLifeLineVo vo = new AssetLifeLineVo();
            vo.setTitle(brokenRecord.getDescription());
            vo.setLinkId(brokenRecord.getId());
            vo.setType(AssetLifeLineVo.LifeLineType.MALFUNCTION.getCode());
            vo.setCreateTime(brokenRecord.getOccurTime());
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 故障记录相关事件
     *
     * @param alarmId 告警ID
     * @return
     */
    @Override
    public List<DialogsAlarmListVo> listByAlarmId(String alarmId) {

        return brokenRecordWordMapper.listByAlarmId(alarmId);
    }

    @Override
    public BrokenRecord getByAssetId(String assetId) {
        List<BrokenRecord> list = brokenMapper.getListByAssetId(assetId);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    private void saveOpinion(RecordHandleReq recordHandleReq) {
        BrokenRecordOpinion opinion = new BrokenRecordOpinion();
        opinion.setOpinion(recordHandleReq.getOpinion());
        opinion.setBrokenRecordId(recordHandleReq.getId());
        opinion.setStatus(StatusConst.OK);
        opinionService.save(opinion);
    }

}
