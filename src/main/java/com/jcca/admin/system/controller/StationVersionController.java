package com.jcca.admin.system.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.enums.StationVersionStatusEnum;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.biz.service.StationVersionLogService;
import com.jcca.admin.system.controller.bean.BeginSetPathReq;
import com.jcca.admin.system.controller.bean.BeginUpdateReq;
import com.jcca.admin.system.controller.bean.StationUpdateDetail;
import com.jcca.admin.system.controller.bean.StationVersionBody;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.VersionMsg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.VersionMsgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 车站升级控制
 *
 * @author lyp
 */
@Slf4j
@Controller
@RequestMapping("/system/version/station")
public class StationVersionController {

    @Resource
    private StationService stationService;
    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private StationVersionLogService versionLogServ;
    @Resource
    private VersionMsgService msgServ;


    /**
     * 车站升级控制
     *
     * @return
     */
    @GetMapping("/index")
    @ActionLog(name = "版本控制列表", title = "车站升级控制", key = LogTypeConstant.QUERY)
    String station(Model model, StationVersionBody station, Integer page, Integer size) {
        IPage<Station> iPage = PagePlugin.startPageT(page, size, Station.class);

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();

        QueryWrapper<Station> stationQuery = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getTitle())) {
            stationQuery.like("title", station.getTitle());
        }
        if (StrUtil.isNotEmpty(station.getIp())) {
            stationQuery.like("ip", station.getIp());
        }
        if (StrUtil.isNotEmpty(station.getVersion())) {
            versionLogServ.listorgIdByVersion(station.getVersion());

            stationQuery.eq("TARGET_NAME", station.getVersion());
        }
        if (StrUtil.isNotEmpty(station.getOrgId())) {
            Set<SysOrg> childrenById = sysOrgService.getChildrenById(station.getOrgId());
            List<String> orgIdList = childrenById.stream().map(SysOrg::getId).collect(Collectors.toList());
            orgIdList.add(station.getOrgId());

            List<String> collect = orgIds.stream().filter(item -> orgIdList.contains(item))
                    .collect(Collectors.toList());

            stationQuery.in("org_id", collect);
        } else {
            stationQuery.in("org_id", orgIds);
        }

        iPage = stationService.page(iPage, stationQuery);
        List<Station> records = iPage.getRecords();

        List<StationVersionBody> respList = new ArrayList<StationVersionBody>();

        List<StationVersionBody> bodyList = EntityBeanUtil.copyList(records, StationVersionBody.class);
        for (StationVersionBody item : bodyList) {
            String stationUrl = "http://" + UrlUtil.getStationUrlPrefix(item);
            item.setStationUrl(stationUrl);
            StationVersionLog lastLog = versionLogServ.findLastLogByStation(item.getOrgId());

            if (Objects.nonNull(lastLog)) {
                StationVersionBody body = EntityBeanUtil.replaceParameter(lastLog, item, StationVersionBody.class);
                body.setStatusStr(StationVersionStatusEnum.getMsgByName(body.getStatus()));
                respList.add(body);
                continue;
            }

            respList.add(item);
        }

        model.addAttribute("list", respList);
        model.addAttribute("page", iPage);
        return "/system/version/stationVersion";
    }

    @PostMapping("/refreshStationStatus")
    @ResponseBody
    @ActionLog(name = "刷新车站更新状态", title = "车站升级控制", key = LogTypeConstant.QUERY)
    ResultVo<?> refreshStationStatus(String stationId) {
        if (StrUtil.isEmpty(stationId)) {
            return ResultVoUtil.error("未传需要刷新的车站ID");
        }

        Station station = stationService.getById(stationId);

        versionLogServ.queryStationRunStatus(station);

        return ResultVoUtil.success("更新成功");
    }

    @PostMapping("/removeUpdateLog")
    @ResponseBody
    @ActionLog(name = "删除升级日志", title = "车站升级控制", key = LogTypeConstant.REMOVEE)
    ResultVo<?> removeUpdateLog(String stationId) {
        if (StrUtil.isEmpty(stationId)) {
            return ResultVoUtil.error("未传车站ID");
        }

        Station station = stationService.getById(stationId);

        StationVersionLog versionLog = versionLogServ.findLastLogByStation(station.getOrgId());

        List<String> statusList = Arrays.asList(StationVersionStatusEnum.UPDATE_FAIL.name(), StationVersionStatusEnum.UPLOAD_FAIL.name());
        if (statusList.contains(versionLog.getStatus())) {
            versionLogServ.removeById(versionLog);
        }

        return ResultVoUtil.success("处理成功");
    }

    @GetMapping("/add")
    @RequiresPermissions("system:version:station:add")
    @ActionLog(name = "添加车站", title = "车站升级控制", key = LogTypeConstant.QUERY)
    String add(Model model, @RequestParam List<String> ids) {
        QueryWrapper<VersionMsg> queryWrapper = new QueryWrapper<VersionMsg>();
        queryWrapper.orderByDesc("CREATE_DATE");
        List<VersionMsg> versionList = msgServ.list(queryWrapper);
        VersionMsg version = msgServ.queryLastVersion();
        if (Objects.nonNull(version)) {
            model.addAttribute("maxVersion", version.getVersion());
        } else {
            model.addAttribute("maxVersion", "系统未上传版本文件");
        }

        StringBuilder nameStr = new StringBuilder();
        for (String orgId : ids) {
            SysOrg organ = sysOrgService.getById(orgId);
            nameStr.append("【");
            nameStr.append(organ.getTitle());
            nameStr.append("】");
        }

        model.addAttribute("versionList", versionList);
        model.addAttribute("orgIdList", ids.toString());
        model.addAttribute("orgNameStr", nameStr.toString());

        return "/system/version/station/add";
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/beginUpdate")
    @ResponseBody
    @ActionLog(name = "开始更新", title = "车站升级控制", key = LogTypeConstant.CONFIRM)
    ResultVo beginUpdate(@Validated BeginUpdateReq req) {
        String stationIdListStr = req.getStationIdListStr();
        String replace = stationIdListStr.replace(",+", ",");

        JSONArray parseArray = JSONUtil.parseArray(replace);
        List<String> list = JSONUtil.toList(parseArray, String.class);

        for (String id : list) {
            Station station = stationService.getById(id);
            if (StrUtil.isEmpty(station.getFilePath())) {
                return ResultVoUtil.error("车站：" + station.getTitle() + "未配置文件存储路径");
            }

        }

        req.setStationIdList(list);
        try {
            versionLogServ.beginUpdate(req);
        } catch (Exception e) {

            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_STATION)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_STATION, ErrorCodeEnum.SYSTEM_STATION_ADD, "", "更新车站" + e.getMessage()));
            }
            return ResultVoUtil.error(e.getMessage());
        }
        return ResultVoUtil.success("已提交");
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/beginSetPath")
    @ResponseBody
    ResultVo beginUpdate(@Validated BeginSetPathReq req) {
        Station station = stationService.getById(req.getSetPathStationId());
        if (Objects.nonNull(station)) {
            station.setFilePath(req.getSavePath());
            stationService.updateById(station);
        }
        return ResultVoUtil.success("保存成功");
    }

    /**
     * 设置文件存储路径
     *
     * @return
     */
    @GetMapping("/setFilePath/{stationId}")
    @RequiresPermissions("system:version:station:setFilePath")
    @ActionLog(name = "设置保存文件路径", title = "车站升级控制", key = LogTypeConstant.ADD)
    String setFilePath(Model model, @PathVariable("stationId") String stationId) {
        model.addAttribute("setPathStationId", stationId);

        return "/system/version/station/setPath";
    }

    /**
     * 获取更新详情列表
     *
     * @param model
     * @param stationId
     * @return
     */
    @GetMapping("/detail/{stationId}")
    @RequiresPermissions("system:version:station:detail")
    String detail(Model model, @PathVariable("stationId") String stationId) {
        // scheduleList
        StationVersionLog versionLog = versionLogServ.findLastLogByStation(stationId);
        List<StationUpdateDetail> detail = getDetail(stationId, versionLog, model);
        model.addAttribute("scheduleList", detail);
        model.addAttribute("stationId", stationId);
        return "/system/version/station/detail";
    }

    /**
     * 查询是否需要定时刷新
     */
    @PostMapping("/getStatus")
    @ResponseBody
    ResultVo<?> getStatus() {
        Integer status = msgServ.getStatus();
        return ResultVoUtil.success(status);
    }

    /**
     * 处理错误，继续向下执行
     *
     * @param stationId
     * @return
     */
    @SuppressWarnings("rawtypes")
    @PostMapping("/disposeError")
    @ResponseBody
    ResultVo disposeError(String stationId) {
        StationVersionLog versionLog = versionLogServ.findLastLogByStation(stationId);
        if (Objects.isNull(versionLog)) {
            return ResultVoUtil.error("升级记录不存在");
        }

        String status = versionLog.getStatus();
        String remark = versionLog.getRemark();
        if (Objects.isNull(remark)) {
            remark = "";
        }

        if (StationVersionStatusEnum.UPLOAD_FAIL.name().equals(status)
                || (StationVersionStatusEnum.UPDATE_FAIL.name().equals(status) && remark.contains("文件损坏"))) {
            // 需要删除原jar 并重新排队发起上传
            try {
                versionLogServ.deleteJarAndAfreshUpload(versionLog);
                return ResultVoUtil.success("处理已提交");
            } catch (Exception e) {
                if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_STATION)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_STATION, ErrorCodeEnum.SYSTEM_STATION_DELETE, "", "更新车站" + e.getMessage()));
                }
                return ResultVoUtil.error(e.getMessage());
            }
        } else if (StationVersionStatusEnum.UPDATE_FAIL.name().equals(status)) {
            // 重新尝试调用更新接口
            versionLog.setStatus(StationVersionStatusEnum.UPLOAD_OK.name());
            versionLog.setRemark("等待系统发起更新指令");
            versionLogServ.updateById(versionLog);

            return ResultVoUtil.success("处理已提交");
        }

        return ResultVoUtil.success("处理已提交");
    }

    private List<StationUpdateDetail> getDetail(String stationId, StationVersionLog versionLog, Model model) {
        List<StationUpdateDetail> detailList = new ArrayList<StationUpdateDetail>();

        if (Objects.isNull(versionLog)) {
            StationUpdateDetail detail = getItem("无更新任务", "该车站无更新记录", false, 1);
            detailList.add(detail);
            model.addAttribute("showButton", "hidden");
            return detailList;
        }

        String status = versionLog.getStatus();

        if (StationVersionStatusEnum.AWAIT_UPLOADING.name().equals(status)) {
            // 等待更新
            StationUpdateDetail detail = getItem("正在排队等待上传JAR", "任务已提交系统！正在等待上传JAR", false, 1);
            detailList.add(detail);
            model.addAttribute("showButton", "hidden");

        } else if (StationVersionStatusEnum.UPLOADING.name().equals(status)) {
            StationUpdateDetail detail = getItem("JAR正在上传中", "JAR上传中,稍等片刻~", false, 1);
            detailList.add(detail);
            model.addAttribute("showButton", "hidden");

        } else if (StationVersionStatusEnum.UPLOAD_OK.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("等待更新", "等待系统向车站发起更新指令", false, 2);
            detailList.add(detail1);
            detailList.add(detail2);

            model.addAttribute("showButton", "hidden");

        } else if (StationVersionStatusEnum.UPLOAD_FAIL.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传失败", "文件上传失败：" + versionLog.getRemark(), true, 1);
            detail1.setButtonName("重新上传");

            model.addAttribute("showButton", "button");
            model.addAttribute("buttonName", "重新上传");

        } else if (StationVersionStatusEnum.UPDATEING.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("更新中", "系统已向车站发起更新指令，等待反馈更新结果", false, 2);
            detailList.add(detail1);
            detailList.add(detail2);

            model.addAttribute("showButton", "hidden");

        } else if (StationVersionStatusEnum.UPDATE_FAIL.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("更新失败", "系统更新失败：" + versionLog.getRemark(), true, 2);
            detail2.setButtonName("重新下发指令");
            detailList.add(detail1);
            detailList.add(detail2);

            model.addAttribute("showButton", "button");
            model.addAttribute("buttonName", "重新下发指令");

        } else if (StationVersionStatusEnum.UPDATE_SUCCESS.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("更新成功", "JAR更新成功：" + versionLog.getJarName(), false, 2);
            detailList.add(detail1);
            detailList.add(detail2);

            model.addAttribute("showButton", "hidden");

        }

        return detailList;

    }

    private StationUpdateDetail getItem(String name, String remark, boolean showButton, Integer rank) {
        StationUpdateDetail detail = new StationUpdateDetail();
        detail.setRank(rank);
        detail.setShowButton(showButton);
        detail.setName(name);
        detail.setRemark(remark);
        return detail;
    }


}
