package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.entity.StationVersionLog;
import com.jcca.admin.biz.enums.StationVersionStatusEnum;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.biz.service.StationVersionLogService;
import com.jcca.admin.system.controller.bean.BeginUpdateReq;
import com.jcca.admin.system.controller.bean.StationUpdateDetail;
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
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;

import com.jcca.common.utils.SqlInjectionUtils;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.web2.dto.StationPageDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @description: 车站
 * @author: Lvyp
 * @create: 2025/01/07 09:25
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/station")
@Api(tags = "车站管理V2")
public class StationControllerV2 {

    private String COLLECT_NODE_KEY = "collectNodeMonitorV2";

    @Resource
    private StationService stationService;
    @Resource
    private StationVersionLogService versionLogServ;
    @Resource
    private RedisService redisService;
    @Resource
    private CollectAgent collectAgency;
    @Resource
    private SysOrgService orgService;
    @Resource
    private VersionMsgService versionMsgService;


    @GetMapping("/list")
    @ApiOperation("获取车站列表")
    public ResultVo<Object> getList(StationPageDto dto) {
        QueryWrapper<Station> wrapper = new QueryWrapper<>();

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        List<String> stationIdList = new ArrayList<>();

        String orgTreeId = dto.getOrgTreeId();
        if (StrUtil.isNotEmpty(orgTreeId)) {
            SysOrg org = orgService.getById(orgTreeId);
            Integer type = org.getType();
            if (type == 3) {
                //线
                stationIdList = orgService.getStationOrgIdByLineId(orgTreeId);
            } else if (type == 4) {
                stationIdList.add(orgTreeId);
            }
        }

        if(!stationIdList.isEmpty()){
            wrapper.in("org_id", stationIdList);
        }else{
            wrapper.in("org_id", orgIds);
        }

        if(StrUtil.isNotEmpty(dto.getStationName())){
            SqlInjectionUtils.formattingQueryWrapper(wrapper,"TITLE",dto.getStationName());
        }

        if(StrUtil.isNotEmpty(dto.getStationIp())){
            wrapper.and(w->w.eq("IP",dto.getStationIp()).or().eq("IP2",dto.getStationIp()
            ));
        }

        if(StrUtil.isNotEmpty(dto.getTagNum())){
            SqlInjectionUtils.formattingQueryWrapper(wrapper,"TARGET_NAME",dto.getTagNum());
        }
        IPage<Station> iPage = PagePlugin.startPageT(dto.getPage(), dto.getSize(), Station.class);
        wrapper.orderByDesc("MODIFY_TIME");
        IPage<Station> page = stationService.page(iPage, wrapper);

        List<Station> records = page.getRecords();
        for (Station record : records) {
            String orgId = record.getOrgId();
            StationVersionLog lastLog = versionLogServ.findLastLogByStation(orgId);
            if(Objects.isNull(lastLog)){
                record.setUploadFlag(-1);
            }else{
                String updateRate = lastLog.getUpdateRate();
                record.setUploadFlag(StationVersionStatusEnum.isFinish(lastLog.getStatus())?-1:1);
                record.setUploadStatus(lastLog.getStatus());
                record.setUpdateRate(updateRate);
            }
        }

        page.setRecords(records);
        return ResultVoUtil.success(page);
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


    @GetMapping("/queryUpdateRate")
    @ApiOperation("查询更新进度")
    ResultVo<?> queryUpdateRate(String stationOrgId){
        StationVersionLog lastLog = versionLogServ.findLastLogByStation(stationOrgId);
        if(Objects.isNull(lastLog)){
            return ResultVoUtil.success("没有更新记录");
        }

        String updateRate = lastLog.getUpdateRate();

        return ResultVoUtil.success("",updateRate);
    }

    /**
     * 保存车站信息
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    @PostMapping("/save")
    @ApiOperation("保存车站信息")
    public ResultVo save(@Validated @RequestBody Station station) {
        QueryWrapper<Station> query = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getOrgId())) {
            query.ne("org_id", station.getOrgId());
        }
        if(StrUtil.isEmpty(station.getIp2())){
            station.setIp2("");
        }
        if(StrUtil.isNotEmpty(station.getIp())){
            query.and(w->w.eq("IP",station.getIp()).or().eq("IP2",station.getIp2()
            ));
        }
        Integer flag = station.getFlag();
        if(Objects.isNull(flag)){
            return ResultVoUtil.error("接口缺少操作标识");
        }

        Station one = stationService.getOne(query);

        if(Objects.isNull(one)  &&  flag==2){
            return ResultVoUtil.error("数据不存在，无法更新");
        }
        if(Objects.nonNull(one)  &&  flag==1){
            return ResultVoUtil.error("此车站已存在绑定数据！");
        }

        if (Objects.nonNull(one)) {
            return ResultVoUtil.error("IP[" + station.getIp() + "]和车站" + one.getTitle() + "的IP重复");
        }

        stationService.saveOrUpdate(station);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除车站信息
     *
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation("删除车站信息")
    public ResultVo delete(@RequestParam("id") String id) {
        if (Objects.isNull(id) || id.length() == 0) {
            return ResultVoUtil.error("车站ID不可为空~");
        }
        Station one = stationService.getById(id);
        if (ObjectUtil.isNotNull(one)) {
            redisService.deleteHashMap(COLLECT_NODE_KEY, "\"" + one.getIp() + ":" + one.getPort() + "\"");
            //向采集告知删除车站采集器节点
            String url = "{\"url\":\"" + one.getIp() + ":" + one.getPort() + "\"}";
            try {
                collectAgency.sendDeleteStaion(url);
            } catch (CollectAgencyException e) {
                log.error("向主采集器告知车站采集器变动报错：{}", e.getMsg(), e);
            }
            stationService.removeById(id);
        }
        return ResultVoUtil.REMOVE_SUCCESS;
    }

    /**
     * 查询版本列表
     *
     * @return
     */
    @GetMapping("/getVersionList")
    @ApiOperation("查询版本列表")
    public ResultVo getVersionList() {
        List<VersionMsg> list = versionMsgService.queryList();

        return ResultVoUtil.success(list);
    }


    @PostMapping("/beginUpdate")
    @ApiOperation("开始更新")
    ResultVo beginUpdate(@Validated @RequestBody BeginUpdateReq req) {

        List<String> list =  req.getStationIdList();
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


    /**
     * 获取详情列表
     *
     * @param stationId
     * @return
     */
    @GetMapping("/detail/{stationId}")
    @ApiOperation("获取升级详情")
    ResultVo detail(@PathVariable("stationId") String stationId) {
        // scheduleList
        StationVersionLog versionLog = versionLogServ.findLastLogByStation(stationId);
        List<StationUpdateDetail> detail = getDetail(versionLog);
        for (StationUpdateDetail stationUpdateDetail : detail) {
            stationUpdateDetail.setStationId(stationId);
        }

        return ResultVoUtil.success(detail);
    }

    @PostMapping("/removeUpdateLog")
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


    /**
     * 处理错误，继续向下执行
     *
     * @param stationId
     * @return
     */
    @SuppressWarnings("rawtypes")
    @PostMapping("/disposeError")
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


    private List<StationUpdateDetail> getDetail(StationVersionLog versionLog) {
        List<StationUpdateDetail> detailList = new ArrayList<StationUpdateDetail>();

        if (Objects.isNull(versionLog)) {
            StationUpdateDetail detail = getItem("无更新任务", "该车站无更新记录", false, 1);
            detailList.add(detail);
            return detailList;
        }

        String status = versionLog.getStatus();

        if (StationVersionStatusEnum.AWAIT_UPLOADING.name().equals(status)) {
            // 等待更新
            StationUpdateDetail detail = getItem("正在排队等待上传JAR", "任务已提交系统！正在等待上传JAR", false, 1);
            detailList.add(detail);
        } else if (StationVersionStatusEnum.UPLOADING.name().equals(status)) {
            StationUpdateDetail detail = getItem("JAR正在上传中", "JAR上传中，让JAR飞一会~", false, 1);
            detailList.add(detail);
        } else if (StationVersionStatusEnum.UPLOAD_OK.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("等待更新", "等待系统向车站发起更新指令", false, 2);
            detailList.add(detail1);
            detailList.add(detail2);
        } else if (StationVersionStatusEnum.UPLOAD_FAIL.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传失败",  versionLog.getRemark(), true, 1);
            detail1.setButtonName("重新上传");
            detailList.add(detail1);
        } else if (StationVersionStatusEnum.UPDATEING.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("更新中", "系统已向车站发起更新指令，等待反馈更新结果", false, 2);
            detailList.add(detail1);
            detailList.add(detail2);
        } else if (StationVersionStatusEnum.UPDATE_FAIL.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("更新失败", "系统更新失败：" + versionLog.getRemark(), true, 2);
            detail2.setButtonName("重新下发指令");
            detailList.add(detail1);
            detailList.add(detail2);
        } else if (StationVersionStatusEnum.UPDATE_SUCCESS.name().equals(status)) {
            StationUpdateDetail detail1 = getItem("上传完成",
                    "JAR已上传至" + versionLog.getSavePath() + "下的" + versionLog.getFutureVersion() + "文件夹内", false, 1);
            StationUpdateDetail detail2 = getItem("更新成功", "JAR更新成功：" + versionLog.getJarName(), false, 2);
            detailList.add(detail1);
            detailList.add(detail2);
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
