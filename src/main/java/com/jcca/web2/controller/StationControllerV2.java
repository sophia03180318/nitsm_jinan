package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * @author HW
 * @description StationControllerV2
 * @className StationControllerV2
 * @date 2024/12/30 15:16
 * @since 2.1.2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/station")
@Api(tags = "车站管理V2")
public class StationControllerV2 {

    @Resource
    private StationService stationService;
    @Resource
    private RedisService redisService;
    @Resource
    private CollectAgent collectAgency;

    @PostMapping("/list")
    @ApiOperation("获取车站列表V2")
    public ResultVo<Object> list(@RequestBody Station station, Integer page, Integer size) {

        Map<String, Object> map = stationService.listV2(station, page, size);

        return ResultVoUtil.success(map);
    }

    @PostMapping("/save")
    @ApiOperation("保存车站信息V2")
    @RequiresPermissions("api:v2:station:save")
    @ActionLog(name = "保存车站信息V2", title = "车站配置", key = LogTypeConstant.ADD)
    public ResultVo<String> save(Station station) {
        QueryWrapper<Station> query = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getOrgId())) {
            query.ne("org_id", station.getOrgId());
        }
        query.and(wq -> wq.eq("ip", station.getIp())
                .or()
                .eq("ip2", station.getIp()));
        Station one = stationService.getOne(query);
        if (Objects.nonNull(one)) {
            return ResultVoUtil.error("IP[" + station.getIp() + "]和车站" + one.getTitle() + "的IP重复");
        }

        stationService.saveOrUpdate(station);
        return ResultVoUtil.SAVE_SUCCESS;
    }

    @GetMapping("/remove")
    @ApiOperation("删除车站信息V2")
    @RequiresPermissions("api:v2:station:remove")
    @ActionLog(name = "删除车站信息V2", title = "车站配置", key = LogTypeConstant.REMOVEE)
    public ResultVo<String> remove(String orgId) {

        if (Objects.isNull(orgId) || orgId.length() == 0) {
            return ResultVoUtil.error("车站ID不可为空~");
        }
        Station one = stationService.getById(orgId);
        if (ObjectUtil.isNotNull(one)) {
            redisService.deleteHashMap("collectNodeMonitorV2", "\"" + one.getIp() + ":" + one.getPort() + "\"");
            //向采集告知删除车站采集器节点

            String url = "{\"url\":\"" + one.getIp() + ":" + one.getPort() + "\"}";
            try {
                collectAgency.sendDeleteStaion(url);
            } catch (CollectAgencyException e) {
                log.error("向主采集器告知车站采集器变动报错V2：{}", e.getMsg(), e);
            }
            stationService.removeById(orgId);
        }
        return ResultVoUtil.REMOVE_SUCCESS;
    }
}
