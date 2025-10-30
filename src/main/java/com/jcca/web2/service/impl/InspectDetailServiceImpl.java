package com.jcca.web2.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.enums.AssetModeEnum;
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
import com.jcca.web2.dto.xunjian.*;
import com.jcca.web2.entity.AssetMode;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.vo.InspectRecordListVo;
import com.jcca.web2.vo.ItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private AssetModeService assetModeService;

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
        return getRecordDetailByParam(inspectCode, null);
    }

    /**
     * 根据 inspectCode 和 type 参数查询详情
     * type 格式：[deskCode]_[totalType]，例如：183_assetTotal
     * 若 type 为空，则查询全部
     */
    @Override
    public Map<String, Object> getRecordDetailByParam(String inspectCode, String type) {
        TypeParseResult result = TypeParseResult.parseTypeParam(type);
        return buildRecordDetail(inspectCode, result.getDeskCode(), result.getTotalType());
    }

    private Map<String, Object> buildRecordDetail(String inspectCode, String deskCode, String totalType) {
        InspectRecord record = inspectRecordService.getById(inspectCode);
        if (Objects.isNull(record)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }
        List<String> assetList = inspectDetailMapper.totalAsset(inspectCode);
        if (assetList.isEmpty()) {
            return new HashMap<>();
        }

        QueryWrapper<InspectDetail> alarmQuery = Wrappers.query();
        alarmQuery.eq("INSPECT_CODE", inspectCode).isNotNull("ALARM_ID");
        int alarmCount = this.count(alarmQuery);

        int totalAsset = assetList.size();
        int abnormalAsset = inspectDetailMapper.abnormalAsset(inspectCode, Integer.parseInt(Web2Const.INSPECT_ERROR));
        int normalAsset = totalAsset - abnormalAsset;
        String inspectTime = DateUtil.format(record.getInspectTime(), "yyyy-MM-dd HH:mm:ss");

        JSONObject header1 = new JSONObject();
        header1.put("xjPeople", record.getModeType());
        header1.put("xjTime", inspectTime);
        header1.put("xjAssetTotal", totalAsset); // 总数
        header1.put("xjAssetNormalTotal", normalAsset); // 正常资产数
        header1.put("xjAssetAbNormalTotal", abnormalAsset); // 异常资产数
        header1.put("xjAssetWarningTotal", alarmCount);  // 告警总数

        List<JSONObject> header2 = new ArrayList<>();
        List<ItemVo> allDeskList = inspectDetailMapper.deskList(inspectCode);
        for (ItemVo vo : allDeskList) {
            int desk = Integer.parseInt(vo.getId());
            int totalDesk = inspectDetailMapper.totalDesk(inspectCode, desk);
            int abnormalDesk = inspectDetailMapper.stateDesk(inspectCode, desk, Integer.parseInt(Web2Const.INSPECT_ERROR));
            int warningDesk = inspectDetailMapper.stateDesk(inspectCode, desk, Integer.parseInt(Web2Const.INSPECT_ALARM));
            int normalDesk = totalDesk - abnormalDesk - warningDesk;

            JSONObject item = new JSONObject();
            item.put("id", vo.getId());
            item.put("name", vo.getName());
            item.put("assetTotal", totalDesk);  // 总数
            item.put("normalTotal", normalDesk); // 正常资产数
            item.put("abNormalTotal", abnormalDesk); // 异常资产数
            item.put("warningTotal", warningDesk); // 告警总数
            header2.add(item);
        }
        List<Map<String, Object>> detailList = new ArrayList<>();
        List<ItemVo> targetDesks = filterDesks(allDeskList, deskCode);

        for (ItemVo itemVo : targetDesks) {
            String status = parseStatusFromTotalType(totalType); // 转换 totalType -> 状态码
            List<InspectAssetDetailInfo> details = inspectDetailMapper.getAssetDetail(inspectCode, itemVo.getId(), status);

            Map<String, Object> map = new HashMap<>();
            map.put("id", itemVo.getId());
            map.put("name", itemVo.getName());
            map.put("details", details);
            detailList.add(map);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("header1", header1);
        resultMap.put("header2", header2);
        resultMap.put("list", detailList);
        return resultMap;
    }

    private List<ItemVo> filterDesks(List<ItemVo> allDesks, String deskCode) {
        if (StringUtils.isEmpty(deskCode) || deskCode.equals(Web2Const.TOTAL_TYPE_ABNORMAL)) {
            return allDesks; // 全部
        }
        return allDesks.stream()
                .filter(vo -> vo.getId().equals(deskCode))
                .collect(Collectors.toList());
    }

    private String parseStatusFromTotalType(String totalType) {
        switch (totalType) {
            case Web2Const.TOTAL_TYPE_ABNORMAL:
                return Web2Const.INSPECT_ERROR;
            case Web2Const.TOTAL_TYPE_WARNING:
                return Web2Const.INSPECT_ALARM;
            default:
                return "-1"; // 查询全部状态
        }
    }

    @Override
    public InspectTargetDetailInfoVo getTargetDetail(String inspectCode, String assetId, String type) {
        TypeParseResult result = TypeParseResult.parseTypeParam(type);
        String queryCode = result.getQueryStatus();
        List<InspectTargetDetailInfo> targetDetailInfoList = inspectDetailMapper.getTargetDetail(
                inspectCode,
                assetId,
                queryCode);
        for (InspectTargetDetailInfo info : targetDetailInfoList) {
            info.setTargetType(info.getTargetItem().substring(0, info.getTargetItem().lastIndexOf(":")));
        }
        InspectTargetDetailInfoVo vo = new InspectTargetDetailInfoVo();
        vo.setTargetDetailList(targetDetailInfoList);
        vo.setAlarmInfoList(new ArrayList<>());

        QueryWrapper<InspectDetail> query1 = Wrappers.query();
        query1.eq("INSPECT_CODE", inspectCode);
        query1.eq("ASSET_ID", assetId);
        query1.isNotNull("ALARM_ID");

        // 添加 deskCode 条件
        if (result.getDeskCode() != null) {
            query1.eq("ASSET_DESK", result.getQueryStatus());
        }

        List<InspectDetail> list = this.list(query1);
        if (!CollectionUtils.isEmpty(list)) {
            QueryWrapper<AlarmInfo> query = Wrappers.query();
            query.select("ID", "TITLE", "OCCUR_TIME", "REMARK", "ALARM_CODE", "DESCRIPTION", "CONTENT");
            query.in("ID", list.stream().map(InspectDetail::getAlarmId).collect(Collectors.toSet()));
            if (!StringUtils.isEmpty(type)) {
                query.eq("ALARM_STATE", 1);
            }
            query.orderByDesc("CREATE_TIME", "ALARM_CODE");
            List<AlarmInfo> infos = alarmInfoService.list(query);
            vo.setAlarmInfoList(infos);
        }
        return vo;
    }

    @Override
    public Map<String, Object> getReport1(String inspectCode) {
        List<InspectReport1> list = inspectDetailMapper.getReport1(inspectCode);
        Map<String, List<String>> map = new HashMap<>();
        int i = 0;
        for (InspectReport1 report1 : list) {
            i++;
            report1.setIndex(i);
            AssetMode mode = assetModeService.getByCode(report1.getAssetDesk());
            if (Objects.nonNull(mode)) {
                report1.setAssetDeskStr(mode.getName());
            } else {
                report1.setAssetDeskStr("为定义的设备类型" + report1.getAssetDesk());
            }

            if (!StringUtils.isEmpty(report1.getAlarmLevel())) {
                report1.setAlarmLevelStr(AlarmLevelEnum.getMsg(report1.getAlarmLevel().intValue()));
            }

            if (!StringUtils.isEmpty(report1.getAlarmStatus())) {
                report1.setAlarmStatusStr(AlarmStatusEnum.getMsg(report1.getAlarmStatus()));
            } else {
                report1.setAlarmStatusStr(AlarmStatusEnum.getMsg((byte) 1));
            }

            if (!StringUtils.isEmpty(report1.getAlarmCode())) {
                List<String> infos = map.get(report1.getAlarmCode());
                if (infos == null) {
                    infos = alarmInfoService.getRemarksByAlarmCode(report1.getAlarmCode());
                    map.put(report1.getAlarmCode(), infos);
                }
                report1.setRemarks(infos);
                if (!infos.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String info : infos) {
                        sb.append(info).append("\r\n");
                    }
                    report1.setRemarkStr(sb.toString());
                }
            }
        }

        QueryWrapper<InspectDetail> query = Wrappers.query();
        query.eq("INSPECT_CODE", inspectCode);
        query.isNotNull("ALARM_ID");
        int alarmCount = this.count(query);

        query = Wrappers.query();
        query.select("ASSET_ID");
        query.eq("INSPECT_CODE", inspectCode);
        query.groupBy("ASSET_ID");
        List<InspectDetail> list1 = this.list(query);
        Integer totalAsset = list1.size();
        Integer abnormalAsset = inspectDetailMapper.abnormalAsset(inspectCode, Integer.parseInt(Web2Const.INSPECT_ERROR));
        Integer normalAsset = totalAsset - abnormalAsset;
        InspectRecord record = inspectRecordService.getById(inspectCode);
        String header1 = "巡检人：%s，巡检时间：%s，巡检资产总数：%s，正常资产数：%s，异常资产数：%s，告警总数：%s";
        String inspectTime = DateUtil.format(record.getInspectTime(), "yyyy-MM-dd HH:mm:ss");
        header1 = String.format(header1, record.getModeType(), inspectTime, totalAsset, normalAsset, abnormalAsset, alarmCount);

        StringBuilder header2 = new StringBuilder();
        List<ItemVo> deskList = inspectDetailMapper.deskList(inspectCode);
        for (ItemVo vo : deskList) {
            Integer desk = Integer.parseInt(vo.getId());
            Integer totalDesk = inspectDetailMapper.totalDesk(inspectCode, desk);
            header2.append(vo.getName()).append("：").append(totalDesk).append("台，");
            Integer abnormalDesk = inspectDetailMapper.stateDesk(inspectCode, desk, Integer.parseInt(Web2Const.INSPECT_ALARM));
            Integer normalDesk = totalDesk - abnormalDesk;
            header2.append("正常").append(normalDesk).append("台，告警").append(abnormalDesk).append("台。");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("header1", header1);
        resultMap.put("header2", header2);
        resultMap.put("list", list);
        return resultMap;
    }

    @Override
    public Map<String, Object> report1Down(String inspectCode) {
        return this.getReport1(inspectCode);
    }

    private XunjianServerDetailBean creatBean(String assetId) {
        XunjianServerDetailBean req = new XunjianServerDetailBean();
        AssetMsgVo oneMsg = assetService.findMsgById(assetId);
        req.setAppName(oneMsg.getAssetName());
        BeanUtils.copyProperties(oneMsg, req);
        return req;
    }

    @Override
    public List<InspectEcharts> getBaseEcharts(String inspectCode) {
        return inspectDetailMapper.getBaseEcharts(inspectCode);
    }

    @Override
    public List<InspectEventDetail> getEchartsDetail(String inspectCode, String eventTypeId) {
        return inspectDetailMapper.getEchartsDetail(inspectCode, eventTypeId);
    }

}
