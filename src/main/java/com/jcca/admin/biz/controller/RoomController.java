package com.jcca.admin.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.biz.entity.RoomReq;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageModel;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.RoomService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RoomController
 * @Description 机房管理
 * @Date 2020/4/26 15:56
 * @Author hanwone
 */
@Controller
@RequestMapping("/system/room")
public class RoomController {

    @Resource
    private RoomService roomService;

    @Resource
    private SysOrgService orgService;

    /**
     * 机房首页
     *
     * @return
     */
    @GetMapping("/index")
    @RequiresPermissions("system:room:index")
    @ActionLog(name = "获取机房列表", title = "机房管理", key = LogTypeConstant.QUERY)
    public String index(Model model, Room room, Integer page, Integer size) {
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        if (room.getName() != null) {
            wrapper.like("name", room.getName());
        }
        List<Room> record2 = new ArrayList<Room>();
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        wrapper.in("org_id", orgIds);
        wrapper.orderByDesc("modify_time");
        List<Room> records = roomService.list(wrapper);
        // 封装数据
        if (room.getOrgTreeId() != null) {
            List<Room> OrgRecord = new ArrayList<Room>();
            String orgTreeId = room.getOrgTreeId();
            SysOrg org = orgService.getById(orgTreeId);
            Integer type = org.getType();
            if (type == 3) {//线
                List<String> ids = orgService.getIdByline(orgTreeId);
                for (Room record : records) {
                    if (ids.contains(record.getOrgId())) {
                        record.setOrgName(orgService.getById(record.getOrgId()).getTitle());
                        OrgRecord.add(record);
                    }
                    record2 = OrgRecord;
                }
            } else if (type == 1) {//局
                for (Room record : records) {
                    record.setOrgName(orgService.getById(record.getOrgId()).getTitle());
                }
                record2 = records;
            } else {//车站&中心
                for (Room record : records) {
                    if (record.getOrgId().equals(orgTreeId)) {
                        record.setOrgName(orgService.getById(record.getOrgId()).getTitle());
                        OrgRecord.add(record);
                    }
                }
                record2 = OrgRecord;
            }

        } else {
            for (Room record : records) {
                record.setOrgName(orgService.getById(record.getOrgId()).getTitle());
            }
            record2 = records;
        }

        iPage.setTotal(record2.size());
        PageModel<String> pm = new PageModel(record2, (int) iPage.getSize());
        List<String> records3 = pm.getObjects((int) iPage.getCurrent());

        model.addAttribute("list", records3);
        model.addAttribute("page", iPage);
        return "/biz/room/index";
    }

    /**
     * 去添加页面
     *
     * @return
     */
    @GetMapping("/add")
    @RequiresPermissions("system:room:add")
    public String toAdd() {
        return "/biz/room/add";
    }


    /**
     * 去编辑页面
     *
     * @return
     */
    @GetMapping("/edit/{ids}")
    @RequiresPermissions("system:room:edit")
    public ModelAndView toEdit(@PathVariable String ids) {
        ModelAndView mv = new ModelAndView();
        Room room = roomService.getById(ids);
        mv.addObject("room", room);
        SysOrg sysOrg = orgService.getById(room.getOrgId());
        sysOrg.setName(sysOrg.getTitle());
        mv.addObject("pOrg", sysOrg);
        mv.setViewName("/biz/room/add");
        return mv;
    }

    /**
     * 保存机房信息
     *
     * @return
     */
    @PostMapping("/save")
    @RequiresPermissions("system:room:save")
    @ResponseBody
    @ActionLog(name = "保存机房信息", title = "机房管理", key = LogTypeConstant.ADD)
    public ResultVo save(@Validated RoomReq req) {
        ResultVo<?> saveRes = roomService.saveRoomsInOrg(req);
//        roomService.saveOrUpdate(room);
        return saveRes;
    }

    /**
     * 删除机房信息
     *
     * @return
     */
    @GetMapping("/del")
    @RequiresPermissions("system:room:del")
    @ResponseBody
    @ActionLog(name = "删除机房信息", title = "机房管理", key = LogTypeConstant.REMOVEE)
    public ResultVo del(@RequestParam(value = "ids", required = false) List<String> ids) {
        return roomService.delById(ids.get(0));
    }

    /**
     * 机房列表
     *
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    @RequiresPermissions(value = {"system:room:index", "system:cabinet:index"}, logical = Logical.OR)
    public ResultVo list() {

        return ResultVoUtil.success(roomService.list());
    }
}
