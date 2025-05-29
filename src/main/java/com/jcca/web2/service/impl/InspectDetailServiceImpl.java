package com.jcca.web2.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectDetailMapper;
import com.jcca.web2.dto.xunjian.InspectAssetDetailInfo;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfo;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfoVo;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.vo.InspectRecordListVo;
import com.jcca.web2.vo.ItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author HanHW
 * @description 巡检详情
 * @className InspectDetailServiceImpl
 * @date 2024/1/19 11:32
 * @since 2.1.0.0
 */
@Service
public class InspectDetailServiceImpl extends ServiceImpl<InspectDetailMapper, InspectDetail> implements InspectDetailService {

    @Resource
    private InspectDetailMapper inspectDetailMapper;
    @Resource
    private AssetService assetService;
    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private AlarmInfoService alarmInfoService;

    @Override
    public List<InspectRecordListVo> recordList() {
        List<InspectRecordListVo> downList;
        List<InspectRecordListVo> resList = new ArrayList<>();
        List<InspectRecordListVo> upList = inspectDetailMapper.findRecordList();
        for (InspectRecordListVo vvo : upList) {
            InspectRecordListVo ttvo = new InspectRecordListVo();
            downList = inspectDetailMapper.findDownList(vvo.getInspectCode());
            for (InspectRecordListVo vo : downList) {
                vo.setId(MyIdUtil.getId());
            }
            ttvo.setCreateTime(vvo.getCreateTime());
            ttvo.setInspectCode(vvo.getInspectCode());
            ttvo.setResultPath(vvo.getResultPath());
            ttvo.setList(downList);
            resList.add(ttvo);
        }

        return resList;
    }

    @Override
    public List<InspectRecordListVo> findRecordDetail(String inspectCode) {
        return inspectDetailMapper.findRecordDetail(inspectCode);
    }

    @Override
    public List<InspectDetail> findAssetDetail(String assetId, String inspectCode) {
        return inspectDetailMapper.findAssetDetail(assetId, inspectCode);
    }

    @Override
    public void deleteRecord(String inspectCode) {
        inspectDetailMapper.deleteRecord(inspectCode);
    }

    @Override
    public void exportAssetRecord(String inspectCode, String assetId, HttpServletResponse response) {
        List<InspectDetail> assetDetail = this.findAssetDetail(assetId, inspectCode);
        if (StringUtils.isEmpty(assetDetail)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        List<XunjianDetailV2> list = new ArrayList<>();
        XunjianServerDetailBean req = this.creatBean(assetId);
        for (InspectDetail record : assetDetail) {
            XunjianDetailV2 v2 = new XunjianDetailV2();
            v2.setXunjianTargetItem(record.getTargetName());
            v2.setCommand(record.getCommand());
            v2.setNormalFlagStr(record.getInspectState().equals(Web2Const.INSPECTED) ? "正常" : "异常");
            v2.setInputErrorStr(record.getResultMsg());
            list.add(v2);
        }

        req.setDetailList(list);
        SXSSFWorkbook createExcel = XunjianReportUtil.createExcelV2(req);

        DispatchRecordExcelUtil.responseBody(createExcel, response, "智能巡检报告单");
    }

    @Override
    public Map<String, Object> getRecordDetail(String inspectCode) {
        InspectRecord record = inspectRecordService.getById(inspectCode);
        if (Objects.isNull(record)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }
        List<String> list = inspectDetailMapper.totalAsset(inspectCode);
        if (list.isEmpty()) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }

        QueryWrapper<AlarmInfo> query = Wrappers.query();
        query.eq("STATUS", 1);
        query.in("ASSET_ID", list);
        int alarmCount = alarmInfoService.count(query);

        Integer totalAsset = list.size();
        Integer abnormalAsset = inspectDetailMapper.abnormalAsset(inspectCode, Integer.parseInt(Web2Const.INSPECT_ERROR));
        Integer normalAsset = totalAsset - abnormalAsset;
        String header1 = "巡检人：%s，巡检时间：%s，巡检资产总数：%s，正常资产数：%s，异常资产数：%s，告警总数：%s";
        String inspectTime = DateUtil.format(record.getInspectTime(), "yyyy-MM-dd HH:mm:ss");
        header1 = String.format(header1, record.getModeType(), inspectTime, totalAsset, normalAsset, abnormalAsset, alarmCount);

        StringBuilder header2 = new StringBuilder();
        List<ItemVo> deskList = inspectDetailMapper.deskList(inspectCode);
        for (ItemVo vo : deskList) {
            Integer desk = Integer.parseInt(vo.getId());
            Integer totalDesk = inspectDetailMapper.totalDesk(inspectCode, desk);
            header2.append(vo.getName()).append("：").append(totalDesk).append("台，");
            Integer abnormalDesk = inspectDetailMapper.stateDesk(inspectCode, desk, Integer.parseInt(Web2Const.INSPECT_ERROR));
            Integer normalDesk = totalDesk - abnormalDesk;
            header2.append("正常").append(normalDesk).append("台，异常").append(abnormalDesk).append("台。");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("header1", header1);
        resultMap.put("header2", header2);
        List<Map<String, Object>> lllist = new ArrayList<>();
        for (ItemVo itemVo : deskList) {
            Map<String, Object> map = new HashMap<>();
            List<InspectAssetDetailInfo> details = inspectDetailMapper.getAssetDetail(inspectCode, itemVo.getId());
            map.put("id", itemVo.getId());
            map.put("name", itemVo.getName());
            map.put("details", details);
            lllist.add(map);
        }
        resultMap.put("list", lllist);
        return resultMap;
    }

    @Override
    public InspectTargetDetailInfoVo getTargetDetail(String inspectCode, String assetId) {
        List<InspectTargetDetailInfo> targetDetailInfoList = inspectDetailMapper.getTargetDetail(inspectCode, assetId);
        QueryWrapper<AlarmInfo> query = Wrappers.query();
        query.select("TITLE", "OCCUR_TIME");
        query.eq("STATUS", 1);
        query.eq("INSPECT_RECORD_ID", inspectCode);
        query.eq("ASSET_ID", assetId);
        List<AlarmInfo> infos = alarmInfoService.list(query);

        InspectTargetDetailInfoVo vo = new InspectTargetDetailInfoVo();
        vo.setTargetDetailList(targetDetailInfoList);
        vo.setAlarmInfoList(infos);

        return vo;
    }

    private XunjianServerDetailBean creatBean(String assetId) {
        XunjianServerDetailBean req = new XunjianServerDetailBean();
        AssetMsgVo oneMsg = assetService.findMsgById(assetId);
        req.setAppName(oneMsg.getAssetName());
        BeanUtils.copyProperties(oneMsg, req);
        return req;
    }


}
