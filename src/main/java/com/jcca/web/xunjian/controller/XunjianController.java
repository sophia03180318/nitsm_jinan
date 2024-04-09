package com.jcca.web.xunjian.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.AssetManufacturerEnum;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.xunjian.adapter.v1.XunJianHandler;
import com.jcca.web.xunjian.controller.bean.XunjianRepoBody;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import com.jcca.web.xunjian.controller.util.bean.XunjianReportTemp;
import com.jcca.web.xunjian.entity.XunjianAlarmMsg;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.entity.XunjianRecord;
import com.jcca.web.xunjian.enums.XunJianTargetEnum;
import com.jcca.web.xunjian.service.XunjianAlarmMsgService;
import com.jcca.web.xunjian.service.XunjianAssetService;
import com.jcca.web.xunjian.service.XunjianDetailService;
import com.jcca.web.xunjian.service.XunjianRecordService;
import com.jcca.web.xunjian.vo.XunjianRecordVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @ClassName XunController
 * @Description 一键巡视V1
 * @Author wone
 * @Date 2021/2/24 10:27
 * @Version ITSM2.0
 **/
@Slf4j
@RestController
@RequestMapping("/api/xunjian")
@Api(tags = "一键巡视")
public class XunjianController {

    @Resource
    private AssetService assetService;
    @Resource
    private XunjianAssetService xunjianAssetService;
    @Resource
    private XunjianRecordService xunjianRecordService;
    @Resource
    private XunjianDetailService xunjianDetailService;
    @Resource
    private XunJianHandler xunjianHandler;
    @Resource
    private XunjianAlarmMsgService xunjianAlarmServ;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private AlarmInfoService alarmInfoService;

    @PostMapping("/asset/list")
    @ApiOperation(value = "待巡视设备列表")
    @RequiresPermissions({"api:xunjian:asset:list"})
    public ResultVo<Object> assetList(Integer page, Integer size) {
        // 待巡检设备列表
        IPage<XunjianAsset> iPage = PagePlugin.startPageT(page, size, XunjianAsset.class);
        QueryWrapper<XunjianAsset> assetQuery = Wrappers.query();
        assetQuery.eq("CREATOR", ShiroUtil.getSubject().getUsername());
        assetQuery.eq("DELETE_FLAH", 1);
        iPage = xunjianAssetService.page(iPage, assetQuery);
        Map<String, Object> resultMap = new HashMap<>();
        List<XunjianAsset> records = iPage.getRecords();
        List<JSONObject> respList = new ArrayList<JSONObject>();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        for (XunjianAsset xunjianAsset : records) {
            Asset asset = assetService.getById(xunjianAsset.getAssetId());
            if(Objects.isNull(asset)){
                continue ;
            }
            JSONObject parseObj = JSONUtil.parseObj(xunjianAsset);
            parseObj.put("id", xunjianAsset.getAssetId());
            parseObj.put("orgId", asset.getOrgId());
            parseObj.put("roomId", assetAttachService.getByAssetId(asset.getId()).getRoomId());
            parseObj.put("name", xunjianAsset.getAssetName());
            parseObj.put("entityId", xunjianAsset.getId());
            parseObj.put("downlineTime",
                    xunjianAsset.getDownlineTime() == null ? "" : sdf1.format(xunjianAsset.getDownlineTime()));
            parseObj.put("onlineTime",
                    xunjianAsset.getOnlineTime() == null ? "" : sdf1.format(xunjianAsset.getOnlineTime()));
            parseObj.put("assetMode", AssetModeEnum.getName(xunjianAsset.getAssetMode()));
            if (Objects.nonNull(xunjianAsset.getManufactoryId())) {
                parseObj.put("manufactoryStr", AssetManufacturerEnum.getName(xunjianAsset.getManufactoryId()));
            }
            respList.add(parseObj);
        }
        resultMap.put("assetList", respList);
        resultMap.put("total", iPage.getTotal());

        return ResultVoUtil.success(resultMap);
    }

    @PostMapping("/history")
    @ApiOperation(value = "巡检历史列表")
    @RequiresPermissions({"api:xunjian:history"})
    public ResultVo<Object> history(Integer page, Integer size) {
        // 巡检历史列表
        IPage<XunjianRecord> iPage = PagePlugin.startPageT(page, size, XunjianRecord.class);
        QueryWrapper<XunjianRecord> recordQuery = Wrappers.query();
        recordQuery.eq("CREATOR", ShiroUtil.getSubject().getUsername());
        recordQuery.orderByDesc("CREATE_TIME");
        iPage = xunjianRecordService.page(iPage, recordQuery);
        Map<String, Object> resultMap = new HashMap<>();
        List<XunjianRecord> records = iPage.getRecords();

        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (XunjianRecord xunjianRecord : records) {
            String xunjianTarget = xunjianRecord.getXunjianTarget();
            String[] split = xunjianTarget.split(",");
            ArrayList<String> values = ListUtil.toList(split);
            String nameByValues = XunJianTargetEnum.getNameByValues(values);

            JSONObject respJson = JSONUtil.parseObj(xunjianRecord);
            respJson.put("xunjianTargetStr", nameByValues);
            respJson.put("xunjianTime", DateUtil.format(xunjianRecord.getXunjianTime(), "yyyy-MM-dd HH:mm:ss"));
            jsonList.add(respJson);
        }

        resultMap.put("recordList", jsonList);
        resultMap.put("total", iPage.getTotal());

        return ResultVoUtil.success(resultMap);
    }

    @PostMapping("/history/{id}")
    @ApiOperation(value = "查看巡检历史详情")
    @RequiresPermissions({"api:xunjian:history"})
    public ResultVo<Object> historyList(@PathVariable("id") String id) {
        XunjianRecord xunjianRecord = xunjianRecordService.getById(id);
        //xunjianRecord.setOperator(ShiroUtil.getSubject().getNickname());

        List<String> asList = Arrays.asList(xunjianRecord.getXunjianTarget().split(","));
        String nameByValues = XunJianTargetEnum.getNameByValues(asList);
        xunjianRecord.setXunjianTargetStr(nameByValues);

        IPage<XunjianAsset> iPage = PagePlugin.startPageT(0, 10000, XunjianAsset.class);
        QueryWrapper<XunjianAsset> assetQuery = Wrappers.query();
        assetQuery.eq("CREATOR", ShiroUtil.getSubject().getUsername());
        assetQuery.orderByDesc("CREATE_TIME");
        iPage = xunjianAssetService.page(iPage, assetQuery);

        List<XunjianAsset> records = iPage.getRecords();
        // fatten
        List<XunjianRepoBody> bodyList = new ArrayList<XunjianRepoBody>();

        List<String> filterAssetList = new ArrayList<String>();
        for (XunjianAsset xunjianAsset : records) {
            QueryWrapper<XunjianDetail> queryWrapper = new QueryWrapper<XunjianDetail>();
            queryWrapper.eq("XUNJIAN_RECORD_ID", id);
            queryWrapper.eq("ASSET_ID", xunjianAsset.getAssetId());
            List<XunjianDetail> details = xunjianDetailService.list(queryWrapper);

            if (details.isEmpty() || filterAssetList.contains(xunjianAsset.getAssetId())) {
                continue;
            }
            filterAssetList.add(xunjianAsset.getAssetId());

            XunjianRepoBody body = new XunjianRepoBody();
            for (XunjianDetail detail : details) {
                body.setAssetId(xunjianAsset.getAssetId());
                body.setAssetName(xunjianAsset.getAssetName());
                fatten(detail, body);
            }

            bodyList.add(body);
        }


        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recordList", bodyList);
        resultMap.put("total", bodyList.size());
        resultMap.put("xunjianRecord", xunjianRecord);

        return ResultVoUtil.success(resultMap);
    }

    /**
     * fatten
     *
     * @param detail
     * @param body
     */
    private void fatten(XunjianDetail detail, XunjianRepoBody body) {
        String item = detail.getXunjianTargetItem();
        if (XunJianTargetEnum.CPU_USED_RATE.getCode().equals(item)) {
            body.setCpuNormalFlag(detail.getNormalFlag());
            body.setCpuResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.DISK_RATE.getCode().equals(item)) {
            body.setDiskNormalFlag(detail.getNormalFlag());
            body.setDiskResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.MEMORY_RATE.getCode().equals(item)) {
            body.setMemoryNormalFlag(detail.getNormalFlag());
            body.setMemoryResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.INTERFACE_FLOW_IN.getCode().equals(item)) {
            body.setPortInNormalFlag(detail.getNormalFlag());
            body.setPortInResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.INTERFACE_FLOW_OUT.getCode().equals(item)) {
            body.setPortOutNormalFlag(detail.getNormalFlag());
            body.setPortOutResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.PROCESS_RUN_STATUS.getCode().equals(item)) {
            body.setSoftwareNormalFlag(detail.getNormalFlag());
            body.setSoftwareResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.ALARM.getCode().equals(item)) {
            body.setAlarmNormalFlag(detail.getNormalFlag());
            body.setAlarmResultMsg(detail.getResultMsg());
        } else if (XunJianTargetEnum.NET_CARD.getCode().equals(item)) {
            body.setNetCardResultMsg(detail.getResultMsg());
            body.setNetCardNormalFlag(detail.getNormalFlag());
        } else if (XunJianTargetEnum.RUN_TIME.getCode().equals(item)) {
            body.setRunTimelog(detail.getNormalFlag());
            body.setRunTimeMag(detail.getResultMsg());
        }else if (XunJianTargetEnum.ORACLE.getCode().equals(item)) {
            body.setOracleFlag(detail.getNormalFlag());
            body.setOracleMag(detail.getResultMsg());
        }
    }

    @PostMapping("/asset/query")
    @ApiOperation(value = "已选择待巡检设备列表")
    @RequiresPermissions({"api:xunjian:asset:query"})
    public ResultVo<Object> assetQuery() {
        String operator = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianAsset> queryWrapper = Wrappers.query();
        queryWrapper.eq("OPERATOR", operator);
        queryWrapper.eq("DELETE_FLAH", 1);
        List<XunjianAsset> list = xunjianAssetService.list(queryWrapper);

        return ResultVoUtil.success(list.stream().map(XunjianAsset::getId).collect(Collectors.toList()));
    }

    @PostMapping("/asset/delete")
    @ApiOperation(value = "删除待巡检设备")
    @RequiresPermissions({"api:xunjian:asset:delete"})
    public ResultVo<?> assetDelete(@RequestBody List<String> assetIds) {
        // 删除原有待巡检设备
        if (Objects.isNull(assetIds) || assetIds.isEmpty()) {
            return ResultVoUtil.error("请输入待删除设备");
        }
        String operator = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianAsset> queryWrapper = Wrappers.query();
        queryWrapper.eq("OPERATOR", operator);

        List<List<String>> inSplit = AppListUtils.inSplit(assetIds, 900);

        Consumer<QueryWrapper<XunjianAsset>> consumer = null;
        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = wrapper -> wrapper.in("ASSET_ID", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<XunjianAsset>> after = wrapper -> wrapper.or().in("ASSET_ID", list);
                consumer = consumer.andThen(after);
            }
        }
        if (Objects.nonNull(consumer)) {
            queryWrapper.and(consumer);
        }
        List<XunjianAsset> list = xunjianAssetService.list(queryWrapper);
        for (XunjianAsset xunjianAsset : list) {
            xunjianAsset.setDeleteFlag(0);
        }
        if (!list.isEmpty()) {
            xunjianAssetService.updateBatchById(list);
        }
        return ResultVoUtil.success("删除成功");
    }

    @PostMapping("/asset/save")
    @ApiOperation(value = "保存待巡检设备")
    @RequiresPermissions({"api:xunjian:asset:save"})
    public ResultVo<Object> assetSave(@RequestBody List<String> assetIds) {
        if (Objects.isNull(assetIds) || assetIds.isEmpty()) {
            return ResultVoUtil.error("请至少选择一个资产");
        }
        // 删除原有待巡检设备
        String operator = ShiroUtil.getSubject().getUsername();
        QueryWrapper<XunjianAsset> queryWrapper = Wrappers.query();
        queryWrapper.eq("OPERATOR", operator);
        List<XunjianAsset> list = xunjianAssetService.list(queryWrapper);
        for (XunjianAsset xunjianAsset : list) {
            xunjianAsset.setDeleteFlag(0);
        }
        if (CollUtil.isNotEmpty(list)) {
            xunjianAssetService.updateBatchById(list);
        }

        // 保存巡检设备
        QueryWrapper<Asset> queryAsset = new QueryWrapper<Asset>();
        queryAsset.in("id", assetIds);
        List<Asset> assetList = assetService.list(queryAsset);

        List<XunjianAsset> xunjianAssetList = new ArrayList<>();
        StringBuilder position;
        for (Asset asset : assetList) {
            position = new StringBuilder("");
            XunjianAsset xunjianAsset = new XunjianAsset();
            xunjianAsset.setAssetId(asset.getId());
            xunjianAsset.setAssetName(asset.getName());
            xunjianAsset.setAssetIp(asset.getIp());
            xunjianAsset.setAssetIp2(asset.getIp2());
            xunjianAsset.setAssetMode(asset.getDesk());
            xunjianAsset.setOperator(operator);
            xunjianAsset.setManufactoryId(asset.getManufacturerId());
            xunjianAsset.setOnlineTime(asset.getOnlineTime());
            xunjianAsset.setDownlineTime(asset.getDownlineTime());
            xunjianAsset.setDeleteFlag(1);

            AssetMsgVo msgVo = assetService.findMsgByIp(asset.getIp());
            position.append(msgVo.getOrgName()).append("--");
            position.append(msgVo.getRoomName()).append("--");
            position.append(msgVo.getCabinetName());
            xunjianAsset.setAssetPosition(position.toString());

            xunjianAssetList.add(xunjianAsset);
        }

        if (!xunjianAssetList.isEmpty()) {
            xunjianAssetService.saveBatch(xunjianAssetList);
        }

        return ResultVoUtil.success("成功");
    }

    @PostMapping("/begin")
    @ApiOperation(value = "开始巡检")
    @RequiresPermissions({"api:xunjian:begin"})
    public ResultVo<?> begin(@Validated @RequestBody XunjianRecordVo recordVo) {

        String operator = ShiroUtil.getSubject().getUsername();
        XunjianRecord record = BeanUtil.copyProperties(recordVo, XunjianRecord.class);
        // 巡检指标
        String[] split = recordVo.getXunjianTarget().split(",");
        ArrayList<String> targetList = ListUtil.toList(split);
        // 保存巡检记录
        String id = MyIdUtil.getId();
        // 检查是否已添加要巡检设备
        QueryWrapper<XunjianAsset> assetQuery = Wrappers.query();
        assetQuery.eq("OPERATOR", operator);
        assetQuery.eq("DELETE_FLAH", 1);

        List<XunjianAsset> assetList = xunjianAssetService.list(assetQuery);
        if (CollUtil.isEmpty(assetList)) {
            return ResultVoUtil.error("请先选择要巡检的设备");
        }
        if (targetList.isEmpty()) {
            return ResultVoUtil.error("请至少选择一个巡检指标");
        }

        // 异常设备数量
        int exceptionNum = 0;
        // 正常设备数量
        int nomarlNum = 0;

        List<XunjianDetail> detailList = new ArrayList<>();

        for (XunjianAsset asset : assetList) {
            List<XunjianDetail> resultList = this.gatherDetail(asset, targetList, id);
            // 异常列表
            List<XunjianDetail> exceptionList = resultList.stream()
                    .filter(item -> !XunjianDetail.NORMAL_FLAG.equals(item.getNormalFlag()))
                    .collect(Collectors.toList());

            if (exceptionList.isEmpty()) {
                nomarlNum++;
            } else {
                exceptionNum++;
            }
            detailList.addAll(resultList);
        }

        record.setId(id);
        record.setXunjianTime(new Date());
        record.setExceptionNum(exceptionNum);
        record.setNormalNum(nomarlNum);
        xunjianRecordService.save(record);
        xunjianDetailService.saveBatch(detailList);

        return ResultVoUtil.success("成功");
    }

    private List<XunjianDetail> gatherDetail(XunjianAsset asset, List<String> targetList, String xunjianRecordId) {
        List<XunjianDetail> detailList = new ArrayList<XunjianDetail>();

        String assetId = asset.getAssetId();

        for (String targetItem : targetList) {
            XunjianDetail detail = new XunjianDetail();
            detail.setAssetId(assetId);
            detail.setAssetMode(asset.getAssetMode());
            detail.setAssetName(asset.getAssetName());
            detail.setNormalFlag(1);
            detail.setXunjianTargetItem(targetItem);

            XunjianDetail xunjian = xunjianHandler.xunjian(targetItem, asset, xunjianRecordId);
            if (Objects.nonNull(xunjian)) {
                detailList.add(xunjian);
            }

        }
        return detailList;
    }

    @GetMapping("/export")
    @ApiOperation(value = "导出巡检报告")
    @RequiresPermissions({"api:xunjian:export"})
    public void export(String historyId, HttpServletResponse response) {
        if (StrUtil.isEmpty(historyId)) {
            log.error("巡检报告导出V1--巡检历史ID 空");
            return;
        }

        XunjianRecord xunjianRecord = xunjianRecordService.getById(historyId);
        if (ObjectUtil.isNull(xunjianRecord)) {
            log.error("巡检报告导出--找不到指定巡检报告 id为" + historyId);
            return;
        }
        List<String> asList = Arrays.asList(xunjianRecord.getXunjianTarget().split(","));
        String nameByValues = XunJianTargetEnum.getNameByValues(asList);
        xunjianRecord.setXunjianTargetStr(nameByValues);

        QueryWrapper<XunjianAsset> assetQuery = new QueryWrapper<XunjianAsset>();
        assetQuery.eq("CREATOR", ShiroUtil.getSubject().getUsername());
        assetQuery.orderByDesc("CREATE_TIME");

        List<XunjianAsset> records = xunjianAssetService.list(assetQuery);
        List<String> assetIdList = new ArrayList<String>();

        // fatten
        // 调度台总数
        int displayerNum = 0;
        // 网络设备总数
        int netServerNum = 0;
        // 服务器总数
        int serverNum = 0;

        List<XunjianRepoBody> bodyList = new ArrayList<XunjianRepoBody>();
        List<String> filterAssetList = new ArrayList<String>();
        for (XunjianAsset xunjianAsset : records) {
            QueryWrapper<XunjianDetail> queryWrapper = new QueryWrapper<XunjianDetail>();
            queryWrapper.eq("XUNJIAN_RECORD_ID", historyId);
            queryWrapper.eq("ASSET_ID", xunjianAsset.getAssetId());
            List<XunjianDetail> details = xunjianDetailService.list(queryWrapper);

            if (details.isEmpty()) {
                continue;
            }
            if (filterAssetList.contains(xunjianAsset.getAssetId())) {
                continue;
            }

            Asset asset = assetService.getById(xunjianAsset.getAssetId());

            List<Integer> netServList = Arrays.asList(42, 201);
            Integer server = 183;
            Integer disk = 1831;

            if (server.equals(asset.getAssetMode())) {
                if (disk.equals(asset.getDesk())) {
                    displayerNum++;
                } else {
                    serverNum++;
                }
            } else if (netServList.contains(asset.getAssetMode())) {
                netServerNum++;
            }

            assetIdList.add(xunjianAsset.getAssetId());

            XunjianRepoBody body = new XunjianRepoBody();
            for (XunjianDetail detail : details) {
                body.setAssetId(xunjianAsset.getAssetId());
                body.setAssetName(xunjianAsset.getAssetName());
                fatten(detail, body);
            }
            bodyList.add(body);
            filterAssetList.add(xunjianAsset.getAssetId());
        }

        XunjianReportTemp req = new XunjianReportTemp();

        QueryWrapper<XunjianAlarmMsg> queryWrapper = new QueryWrapper<XunjianAlarmMsg>();
        queryWrapper.eq("XUNJIAN_RECORD_ID", historyId);
        List<XunjianAlarmMsg> alarmList = xunjianAlarmServ.list(queryWrapper);
        ArrayList<XunjianAlarmMsg> alarmList2 = new ArrayList<>();
        for (XunjianAlarmMsg alarmMsg : alarmList) {
            List<String> descriptionList = alarmInfoService.queryDescriptionList(alarmMsg.getAlarmInfoId());
            alarmMsg.setOpinion(descriptionList.toString());
            alarmList2.add(alarmMsg);
        }
        req.setAlarmList(alarmList2);
        req.setReportList(bodyList);
        req.setOperator(ShiroUtil.getSubject().getNickname());
        req.setXunjianShift(xunjianRecord.getXunjianShift());
        req.setXunjianTarget(xunjianRecord.getXunjianTargetStr());
        req.setExceptionNum(xunjianRecord.getExceptionNum());
        req.setNormalNum(xunjianRecord.getNormalNum());
        req.setXunjianTime(xunjianRecord.getXunjianTime());

        req.setDisplayerNum(displayerNum);
        req.setNetServerNum(netServerNum);
        req.setServerNum(serverNum);

        SXSSFWorkbook createExcel = XunjianReportUtil.createExcel(req);

        DispatchRecordExcelUtil.responseBody(createExcel, response, "智能巡检报告单");

    }
}
