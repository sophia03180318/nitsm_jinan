package com.jcca.web.xunjian.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.entity.SysRoomAssetMsgBean;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.auth.vo.SysDictVo;
import com.jcca.web.xunjian.controller.bean.BeginXunJianReq;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.entity.XunjianRecordV2;
import com.jcca.web.xunjian.entity.bean.XunjianLogBean;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;
import com.jcca.web.xunjian.service.XunjianDetailV2Service;
import com.jcca.web.xunjian.service.XunjianRecordV2Service;
import com.jcca.web.xunjian.service.bean.XunjianTab;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 智能巡检V2版本接口
 */
@RestController
@RequestMapping("/api/xunjian/v2")
@Api(tags = "智能巡检V2")
public class XunjianV2Controller {

    public static Integer XUN_JIAN_FLG = 0;
    public static String lokKey = "xunjian_lock";

    @Resource
    private RoomService roomServ;
    @Resource
    private XunjianRecordV2Service xunjianServ;
    @Resource
    private XunjianDetailV2Service detailV2Serv;
    @Resource
    private AssetService assetServ;
    @Resource
    private SysDictService sysDictServ;



    @GetMapping("/exportExcel")
    public void exportExcel(String xunjianRecordId, String assetId, HttpServletResponse response){
        XunjianServerDetailBean result = xunjianServ.queryXunjianDetail(xunjianRecordId, assetId);
        SXSSFWorkbook createExcel = XunjianReportUtil.createExcelV2(result);
        DispatchRecordExcelUtil.responseBody(createExcel, response, "智能巡检报告单");
    }

    @GetMapping("/getLogDetail")
    @ApiOperation(value = "查询巡检详情")
    @RequiresPermissions({"api:xunjian:v2:getLogDetail"})
    ResultVo<?> getLogDetail(String xunjianRecordId, String assetId) {
        XunjianServerDetailBean result = xunjianServ.queryXunjianDetail(xunjianRecordId, assetId);
        return ResultVoUtil.success(result);
    }

    /**
     * 重新巡检
     *
     * @param xunjianRecordId
     * @return
     */
    @GetMapping("/removeXunjian")
    @ApiOperation(value = "删除巡检")
    @ActionLog(name = "删除巡检", title = "智能巡检", key = LogTypeConstant.REMOVEE)
    ResultVo<?> removeXunjian(String xunjianRecordId) {
        if (StrUtil.isEmpty(xunjianRecordId)) {
            return ResultVoUtil.error("缺少巡检记录主键");
        }
        xunjianServ.removeXunjianLog(xunjianRecordId);
        return ResultVoUtil.success();
    }

    @ApiOperation(value = "获取资产巡检项")
    @PostMapping("/queryTarget")
    ResultVo<?> queryTarget(@RequestBody String req) {
        JSONObject jsonObject = JSONUtil.parseObj(req);
        JSONArray modes = jsonObject.getJSONArray("modes");
        List<String> modeList = JSONUtil.toList(modes, String.class);

        List<String> flagList = new ArrayList<>();
        List<SysDict> resp = new ArrayList<>();
        for (String mode : modeList) {
            if (flagList.contains(mode)) {
                continue;
            }
            flagList.add(mode);
            List<SysDict> allLikeName = sysDictServ.getAllLikeName("巡检" + mode);
            resp.addAll(allLikeName);
        }

        Map<String,SysDictVo> voMap = new HashMap<>();
        for (SysDict sysDict : resp) {
            SysDictVo vo = new SysDictVo();
            vo.setKey(sysDict.getName());
            vo.setValue(sysDict.getTitle());
            voMap.put(sysDict.getName(),vo);
        }

        return ResultVoUtil.success(voMap.values());
    }


    @GetMapping("/queryResult")
    ResultVo<?> queryResult() {
        if (XUN_JIAN_FLG == 0) {
            //已结束
            return ResultVoUtil.success("ok");
        } else {
            //进行中
            return ResultVoUtil.success("loading");
        }
    }

    @PostMapping("/editXunjianRemark")
    @ApiOperation(value = "编辑资产项")
    @RequiresPermissions({"api:xunjian:v2:editXunjianRemark"})
    @ActionLog(name = "编辑资产项", title = "智能巡检", key = LogTypeConstant.MODIFY)
    ResultVo<?> editXunjianRemark(@RequestBody String req) {
        JSONObject reqJson = JSONUtil.parseObj(req);
        String id = reqJson.getStr("xunjianRecordId");
        String remark = reqJson.getStr("remark");

        XunjianRecordV2 record = xunjianServ.getById(id);
        if (Objects.isNull(record)) {
            return ResultVoUtil.error("此巡检报告不存在，无法编辑");
        }
        record.setRemark(remark);
        xunjianServ.updateById(record);

        return ResultVoUtil.success();
    }

    /**
     * 重新巡检
     *
     * @param xunjianRecordId
     * @return
     */
    @GetMapping("/reloadXunjian")
    @ApiOperation(value = "编辑检查项")
    @RequiresPermissions({"api:xunjian:v2:reloadXunjian"})
    ResultVo<?> reloadXunjian(String xunjianRecordId) {
        if (StrUtil.isEmpty(xunjianRecordId)) {
            return ResultVoUtil.error("缺少主键");
        }

        QueryWrapper<XunjianDetailV2> queryWrapper = new QueryWrapper<XunjianDetailV2>();
        queryWrapper.eq("XUNJIAN_RECORD_ID", xunjianRecordId);
        List<XunjianDetailV2> list = detailV2Serv.list(queryWrapper);
        if (list.isEmpty()) {
            return ResultVoUtil.error("此巡检项内无任何记录");
        }

        List<String> assetIds = new ArrayList<>();
        List<String> xunjianAdapters = new ArrayList<>();

        for (XunjianDetailV2 xunjianDetailV2 : list) {
            String assetId = xunjianDetailV2.getAssetId();
            String xunjianAdapter = xunjianDetailV2.getXunjianAdapter();
            if (!assetIds.contains(assetId)) {
                assetIds.add(assetId);
            }
            if (!xunjianAdapters.contains(xunjianAdapter)) {
                xunjianAdapters.add(xunjianAdapter);
            }
        }
        if(xunjianAdapters.isEmpty()){
            return ResultVoUtil.error("此记录中没有任何可用的巡检项，不可重新发起，请手动发起");
        }
        BeginXunJianReq req = new BeginXunJianReq();
        req.setTargetList(xunjianAdapters);
        req.setAssetIdList(assetIds);

        synchronized (lokKey.intern()) {
            if (XUN_JIAN_FLG == 1) {
                return ResultVoUtil.warning("系统内有您或其他用户正在执行的巡检任务，请稍后再试");
            }
            XUN_JIAN_FLG = 1;
            xunjianServ.startXunJian(req);
        }
        return ResultVoUtil.success();
    }

    @PostMapping("/editAsset")
    @ApiOperation(value = "编辑资产项")
    @RequiresPermissions({"api:xunjian:v2:editAsset"})
    ResultVo<?> editDetail(@RequestBody Asset req) {
        if (StrUtil.isEmpty(req.getId())) {
            return ResultVoUtil.error("缺少设备主键");
        }
        Asset asset = assetServ.getById(req.getId());

        Asset assetNew = EntityBeanUtil.replaceParameter(req, asset, Asset.class);
        assetServ.updateById(assetNew);

        return ResultVoUtil.success();
    }

    @PostMapping("/editDetail")
    @ApiOperation(value = "编辑检查项")
    @RequiresPermissions({"api:xunjian:v2:editDetail"})
    ResultVo<?> editDetail(@RequestBody XunjianDetailV2 detail) {
        String id = detail.getId();
        String xunjianTargetItem = detail.getXunjianTargetItem();
        String command = detail.getCommand();
        String inputErrorStr = detail.getInputErrorStr();
        Integer normalFlag = detail.getNormalFlag();

        String normalFlagStr = XunjianDetailV2.getNormalFlagStr(normalFlag);
        if (StrUtil.isEmpty(normalFlagStr)) {
            return ResultVoUtil.error("请选择正确的参考结果");
        }
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error("缺少主键");
        }
        XunjianDetailV2 detailObj = detailV2Serv.getById(id);
        if (Objects.isNull(detailObj)) {
            return ResultVoUtil.error("记录不存在");
        }
        if (StrUtil.isNotEmpty(xunjianTargetItem)) {
            detailObj.setXunjianTargetItem(xunjianTargetItem);
        }
        if (StrUtil.isNotEmpty(command)) {
            detailObj.setCommand(command);
        }
        detailObj.setNormalFlag(detail.getNormalFlag());
        detailObj.setNormalFlagStr(normalFlagStr);
        //可清空
        detailObj.setInputErrorStr(inputErrorStr);

        detailV2Serv.updateById(detailObj);
        return ResultVoUtil.success();
    }

    @GetMapping("/getLogTab")
    @ApiOperation(value = "获取巡检记录中的总览报表")
    @RequiresPermissions({"api:xunjian:v2:getRoomAsset"})
    ResultVo<?> getLogTab(String xunjianRecordId) {
        Collection<XunjianTab> logTab = xunjianServ.getLogTab(xunjianRecordId);
        return ResultVoUtil.success(logTab);
    }

    /**
     * 查询巡检历史跟登录用户挂钩
     * 只能查询此用户发起的巡检记录
     *
     * @return
     */
    @GetMapping("/getLog")
    @ApiOperation(value = "查询巡检历史记录")
    @RequiresPermissions({"api:xunjian:v2:getLog"})
    ResultVo<?> getLog() {
        String username = ShiroUtil.getSubject().getUsername();
        List<XunjianLogBean> xunjianLogBeans = xunjianServ.queryXunjianLog(username);
        return ResultVoUtil.success(xunjianLogBeans);
    }



    @GetMapping("/getRoomAsset")
    @ApiOperation(value = "获取机房中的机柜和设备信息")
    @RequiresPermissions({"api:xunjian:v2:getRoomAsset"})
    ResultVo<?> getRoomAsset(String roomId) {
        List<SysRoomAssetMsgBean> assetMsgList = roomServ.queryRoomAssets(roomId);
        return ResultVoUtil.success(assetMsgList);
    }

    @PostMapping("/beginXunjian")
    @ApiOperation(value = "开始巡检")
    @RequiresPermissions({"api:xunjian:v2:beginXunjian"})
    @ActionLog(name = "开始巡检", title = "智能巡检", key = LogTypeConstant.ADD)
    ResultVo<?> beginXunjian(@RequestBody @Validated BeginXunJianReq req) {
        synchronized (lokKey.intern()) {
            if (XUN_JIAN_FLG == 1) {
                return ResultVoUtil.warning("系统内有您或其他用户正在执行的巡检任务，请稍后再试");
            }
            XUN_JIAN_FLG = 1;
            xunjianServ.startXunJian(req);
        }
        return ResultVoUtil.success();
    }

}
