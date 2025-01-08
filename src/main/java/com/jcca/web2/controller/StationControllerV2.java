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
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.VersionMsg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.VersionMsgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;

import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.web2.dto.StationPageDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: 车站
 * @author: Lvyp
 * @create: 2025/01/07 09:25
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/stationn")
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
            wrapper.like("TITLE",dto.getStationName());
        }

        if(StrUtil.isNotEmpty(dto.getStationIp())){
            wrapper.and(w->w.eq("IP",dto.getStationIp()).or().eq("IP2",dto.getStationIp()
            ));
        }

        if(StrUtil.isNotEmpty(dto.getTagNum())){
            wrapper.like("TARGET_NAME",dto.getTagNum());
        }
        IPage<Station> iPage = PagePlugin.startPageT(dto.getPage(), dto.getSize(), Station.class);
        IPage<Station> page = stationService.page(iPage, wrapper);

        List<Station> records = page.getRecords();
        for (Station record : records) {
            String orgId = record.getOrgId();
            StationVersionLog lastLog = versionLogServ.findLastLogByStation(orgId);
            if(Objects.isNull(lastLog)){
                record.setUploadFlag(-1);
            }else{
                record.setUploadFlag(StationVersionStatusEnum.isFinish(lastLog.getStatus())?-1:1);
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
            return ResultVoUtil.error("没有更新记录");
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
    public ResultVo save(@Validated Station station) {
        QueryWrapper<Station> query = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getOrgId())) {
            query.ne("org_id", station.getOrgId());
        }
        if(StrUtil.isNotEmpty(station.getIp())){
            query.and(w->w.eq("IP",station.getIp()).or().eq("IP2",station.getIp2()
            ));
        }

        Station one = stationService.getOne(query);
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

}
