package com.jcca.admin.biz.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.biz.entity.Station;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageModel;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.exception.CollectAgencyException;
import com.jcca.component.quartz.station.QuartzStationNotifyJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName StationController
 * @Description 车站信息配置
 * @Date 2020/6/19 10:32
 * @Author hanwone
 */
@Slf4j
@Controller
@RequestMapping("/biz/station")
public class StationController {

    @Resource
    private StationService stationService;
    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private RedisService redisService;
    @Resource
    private CollectAgent collectAgency;

    private String COLLECT_NODE_KEY = "collectNodeMonitorV2";

    /**
     * 去车站配置首页
     *
     * @return
     */
    @RequestMapping("/index")
    @RequiresPermissions("biz:station:index")
    @ActionLog(name = "查看车站信息列表", title = "车站配置", key = LogTypeConstant.QUERY)
    public String index(Model model, Station station, Integer page, Integer size) {
        IPage<Station> iPage = PagePlugin.startPageT(page, size, Station.class);

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();

        QueryWrapper<Station> stationQuery = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getTitle())) {
            stationQuery.like("title", station.getTitle());
        }
        if (StrUtil.isNotEmpty(station.getIp())) {
            stationQuery.like("ip", station.getIp());
        }
        stationQuery.in("org_id", orgIds);
        List<Station> records = stationService.list(stationQuery);
        for (Station item : records) {
            SysOrg org = orgService.getById(item.getOrgId());
            item.setTitle(org.getTitle());
        }

        List<Station> stations = new ArrayList<Station>();
        if (station.getOrgTreeId() != null) {
            List<Station> OrgRecord = new ArrayList<Station>();
            String orgTreeId = station.getOrgTreeId();
            SysOrg org = orgService.getById(orgTreeId);
            Integer type = org.getType();

            if (type == 3) {//线
                List<String> ids = orgService.getIdByline(orgTreeId);
                for (Station record : records) {
                    if (ids.contains(record.getOrgId())) {
                        OrgRecord.add(record);
                    }
                    stations = OrgRecord;
                }
            } else if (type == 1) {//局
                stations = records;
            } else if (type == 4) {//车站&中心
                for (Station record : records) {
                    if (record.getOrgId().equals(orgTreeId)) {
                        OrgRecord.add(record);
                    }
                }
                stations = OrgRecord;
            }

        } else {
            stations = records;
        }

        iPage.setTotal(stations.size());
        PageModel<String> pm = new PageModel(stations, (int) iPage.getSize());
        List<String> records2 = pm.getObjects((int) iPage.getCurrent());
        model.addAttribute("list", records2);
        model.addAttribute("page", iPage);

        return "/biz/station/index";
    }

    /**
     * 保存车站信息
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping("/save")
    @RequiresPermissions("biz:station:save")
    @ResponseBody
    @ActionLog(name = "保存车站信息", title = "车站配置", key = LogTypeConstant.ADD)
    public ResultVo save(@Validated Station station) {
        QueryWrapper<Station> query = Wrappers.query();
        if (StrUtil.isNotEmpty(station.getOrgId())) {
            query.ne("org_id", station.getOrgId());
        }
        query.eq("ip", station.getIp()).or().eq("ip2", station.getIp2());
        Station one = stationService.getOne(query);
        if (Objects.nonNull(one)) {
            return ResultVoUtil.error("IP[" + station.getIp() + "]和车站" + one.getTitle() + "的IP重复");
        }

        stationService.saveOrUpdate(station);

        this.send2Station(station.getOrgId(), station.getIp(), station.getTitle());

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除车站信息
     *
     * @return
     */
    @GetMapping("/delete")
    @ResponseBody
    @ActionLog(name = "删除车站信息", title = "车站配置", key = LogTypeConstant.REMOVEE)
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


    @Value("${project.syslog_snmp.host}")
    private String itsmIp;

    /**
     * 将车站IP信息发送给车站
     *
     * @return
     */
    private boolean send2Station(String stationId, String stationIp, String stationName) {
        String nitsmUrl = itsmIp.split(",")[0];
        JSONObject param = new JSONObject();
        param.put("stationIp", stationIp);
        param.put("stationName", stationName);
        param.put("nitsmUrl", "http://" + nitsmUrl + ":5501");
        String body = stationService.sendPostToStation("/bg/system/commit", stationId, param.toString());
        return JSONUtil.isJsonArray(body);
    }


    /**
     * 去添加页面
     *
     * @return
     */
    @RequestMapping("/add")
    @RequiresPermissions("biz:station:add")
    public String add() {
        return "/biz/station/add";
    }

    /**
     * 去编辑页面
     *
     * @return
     */
    @RequestMapping("/edit/{id}")
    @RequiresPermissions("biz:station:edit")
    public String edit(@PathVariable("id") String id, Model model) {
        model.addAttribute("station", stationService.getById(id));
        return "/biz/station/add";
    }

    /**
     * 获取车站列表
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping("/list")
    @ResponseBody
    public ResultVo list() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        QueryWrapper<SysOrg> query = Wrappers.query();
        query.in("id", orgIds);
        query.eq("type", OrgTypeConst.STATION);
        List<SysOrg> orgList = sysOrgService.list(query);

        return ResultVoUtil.success(orgList);
    }

    @RequestMapping("/notifyList")
    @ResponseBody
    String notifyList() {
        return JSONUtil.toJsonStr(QuartzStationNotifyJob.NOTIFY_QUEUE);
    }

}
