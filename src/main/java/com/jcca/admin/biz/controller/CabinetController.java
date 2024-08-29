package com.jcca.admin.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageModel;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.HttpServletUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.vo.RoomVo;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName CabinetController
 * @Description 机柜管理
 * @Date 2020/4/26 15:56
 * @Author hanwone
 */
@Controller
@RequestMapping("/system/cabinet")
public class CabinetController {

    @Resource
    private CabinetService cabinetService;
    @Resource
    private RoomService roomService;
    @Resource
    private SysOrgService orgService;

    /**
     * 组织数据列表
     */
    @GetMapping("/list")
    @ResponseBody
    public ResultVo list(SysOrg org) {
        Map<String, Object> map = new HashMap<>();
        if (!Objects.equals(AdminConst.ADMIN_ID, ShiroUtil.getSubject().getId())) {
            map.put("userId", ShiroUtil.getSubject().getId());
        }
        if (org.getTitle() != null) {
            map.put("title", org.getTitle());
        }
        List<SysOrg> list = orgService.getOrgsByUserId(map);
        return ResultVoUtil.success(list);
    }


    /**
     * 机柜首页
     *
     * @return
     */
    @GetMapping("/index")
    @RequiresPermissions("system:cabinet:index")
    @ActionLog(name = "查看机柜列表", title = "机柜管理", key = LogTypeConstant.QUERY)
    public String index(Model model, Cabinet cabinet, Integer page, Integer size) {
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<Cabinet> wrapper = new QueryWrapper<>();
        if (cabinet.getName() != null) {
            wrapper.like("name", cabinet.getName());
        }

        List<Room> rooms = roomService.getSubjectRooms();
        List<String> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
        roomIds.add("x");
        wrapper.in("room_id", roomIds);
        wrapper.orderByDesc("modify_time", "ID");
        List<Cabinet> records = cabinetService.list(wrapper);
        //List<Cabinet> records = iPage.getRecords();
        // 封装数据
        for (Cabinet record : records) {
            StringBuilder orgName = new StringBuilder();
            Room room = roomService.getById(record.getRoomId());
            //添加组织名称链条    获取机房所属组织
            SysOrg org = orgService.getById(room.getOrgId());
            String pidStr = org.getPids();
            pidStr = pidStr.replace("[", "");
            pidStr = pidStr.replace("]", "");
            String[] pids = pidStr.split(",");
            for (String pid : pids) {
                if (!pid.equals("0")) {
                    try {
                        orgName.append(">").append(orgService.getById(pid).getTitle());
                    } catch (NullPointerException e) {
                        orgName.append(">未知组织");
                    }

                }

            }
            orgName.append(">").append(org.getTitle());

            record.setOrgId(room.getOrgId());
            record.setOrgName(orgName.substring(1));
            record.setRoomId(room.getName());
        }


        if (cabinet.getOrgTreeId() != null) {//筛选组织
            List<Cabinet> OrgRecord = new ArrayList<Cabinet>();
            SysOrg orgNode = orgService.getById(cabinet.getOrgTreeId());
            Integer type = orgNode.getType();
            if (type == 3) {//线
                //获取线下所有车站
                List<String> ids = orgService.getIdByline(orgNode.getId());
                for (Cabinet record : records) {
                    if (ids.contains(record.getOrgId())) {
                        OrgRecord.add(record);
                    }
                }
                records = OrgRecord;
            } else if (type == 2 || type == 4) {//中心或站
                for (Cabinet record : records) {
                    if (record.getOrgId().equals(orgNode.getId())) {
                        OrgRecord.add(record);
                    }
                }
                records = OrgRecord;
            }


        }

        iPage.setTotal(records.size());
        PageModel<String> pm = new PageModel(records, (int) iPage.getSize());
        List<String> records2 = pm.getObjects((int) iPage.getCurrent());
        model.addAttribute("list", records2);
        model.addAttribute("page", iPage);
        String search = HttpServletUtil.getRequest().getQueryString();

        model.addAttribute("search", search);
        return "/biz/cabinet/index";
    }

    /**
     * 组织数据列表
     */
    @GetMapping("/roomList")
    @ResponseBody
    public ResultVo roomList() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        QueryWrapper<SysOrg> wq = Wrappers.query();
        wq.in("id", orgIds);
        List<SysOrg> list = orgService.list(wq);
        ArrayList<SysOrg> sysOrgs = new ArrayList<>(list);
        for (SysOrg sysOrg : list) {
            List<RoomVo> roomVos = roomService.listByOrgId(sysOrg.getId());
            for (RoomVo roomVo : roomVos) {
                SysOrg sysOrg1 = new SysOrg();
                sysOrg1.setId(roomVo.getId());
                sysOrg1.setPid(sysOrg.getId());
                sysOrg1.setTitle(roomVo.getName());
                sysOrgs.add(sysOrg1);
            }
        }
        return ResultVoUtil.success(sysOrgs);
    }

    /**
     * 获取排序组织列表
     */
    @GetMapping("/sortList/{pid}/{notId}")
    @ResponseBody
    public Map<Integer, String> sortList(
            @PathVariable(value = "pid", required = false) String pid,
            @PathVariable(value = "notId", required = false) String notId) {
        // 本级排序组织列表
        notId = notId != null ? notId : "0";
        Map<String, Object> map = new HashMap<>();
        if (!Objects.equals(AdminConst.ADMIN_ID, ShiroUtil.getSubject().getId())) {
            map.put("userId", ShiroUtil.getSubject().getId());
        }
        map.put("pid", pid);
        map.put("notId", notId);
        List<SysOrg> levelOrg = orgService.getOrgsByUserId(map);
        Map<Integer, String> sortMap = new TreeMap<>();
        for (int i = 1; i <= levelOrg.size(); i++) {
            sortMap.put(i, levelOrg.get(i - 1).getTitle());
        }
        return sortMap;
    }

    /**
     * 去添加页面
     *
     * @return
     */
    @GetMapping("/add")
    @RequiresPermissions("system:cabinet:add")
    @ActionLog(name = "准备添加机柜", title = "机柜管理", key = LogTypeConstant.QUERY)
    public ModelAndView toAdd() {
        ModelAndView mv = new ModelAndView();
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        QueryWrapper<SysOrg> wq = Wrappers.query();
        wq.in("id", orgIds);
        wq.orderByAsc("type");
        SysOrg org = orgService.list(wq).get(0);
        mv.addObject("org", org);
        mv.setViewName("/biz/cabinet/add");
        return mv;
    }


    /**
     * 去编辑页面
     *
     * @return
     */
    @GetMapping("/edit/{ids}")
    @RequiresPermissions("system:cabinet:edit")
    @ActionLog(name = "准备编辑机柜", title = "机柜管理", key = LogTypeConstant.QUERY)
    public ModelAndView toEdit(@PathVariable String ids) {
        ModelAndView mv = new ModelAndView();
        Cabinet cabinet = cabinetService.getById(ids);
        mv.addObject("cabinet", cabinet);
        mv.addObject("room", roomService.getById(cabinet.getRoomId()));

        mv.addObject("roomList", roomService.getSubjectRooms());
        mv.setViewName("/biz/cabinet/add");
        return mv;
    }

    /**
     * 保存机柜信息
     *
     * @return
     */
    @PostMapping("/save")
    @RequiresPermissions("system:cabinet:save")
    @ActionLog(name = "保存机柜信息", title = "机柜管理", key = LogTypeConstant.ADD)
    @ResponseBody
    public ResultVo save(@Validated Cabinet cabinet) {

        cabinetService.saveCabinetV2(cabinet);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除机柜信息
     *
     * @return
     */
    @GetMapping("/del")
    @RequiresPermissions("system:cabinet:del")
    @ActionLog(name = "删除机柜", title = "机柜管理", key = LogTypeConstant.REMOVEE)
    @ResponseBody
    public ResultVo del(@RequestParam(value = "ids", required = false) List<String> ids) {

        return cabinetService.delById(ids.get(0));
    }

    /**
     * 机柜拓扑
     *
     * @return
     */
    @GetMapping("/topo")
    @ApiOperation(value = "获取机柜拓扑")
    @ActionLog(name = "获取机柜拓扑", title = "拓扑", key = LogTypeConstant.QUERY)
    @RequiresPermissions("api:asset:topo")
    public ResultVo topo() {

        List<Cabinet> cabinetList = cabinetService.list();

        return ResultVoUtil.success(cabinetList);
    }


}
