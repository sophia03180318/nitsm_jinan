package com.jcca.admin.system.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.controller.bean.SortBackShow;
import com.jcca.admin.system.controller.bean.SysOrgException;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysOrgReq;
import com.jcca.admin.system.entity.SysRoleOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.SysRoleOrgService;
import com.jcca.admin.system.service.SysRoleService;
import com.jcca.admin.system.validator.OrgValid;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hanwone
 * @date 2018/12/02
 */
@Controller
@Slf4j
@RequestMapping("/system/org")
public class OrgController {

    @Resource
    private SysOrgService orgService;

    @Resource
    private SysRoleOrgService roleOrgService;

    @Resource
    private SysRoleService roleService;
    @Resource
    private AssetService assetService;
    @Resource
    private  RoomService roomService;

    /**
     * 跳转到列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:org:index")
    public String index(Model model) {
        String search = HttpServletUtil.getRequest().getQueryString();
        model.addAttribute("search", search);
        return "/system/org/index";
    }

    /**
     * 组织数据列表
     */
    @GetMapping("/list")
    @RequiresPermissions(value = {"index", "system:org:index"}, logical = Logical.OR)
    @ResponseBody
    @ActionLog(name = "查看组织列表", title = "组织管理", key = LogTypeConstant.QUERY)
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
     * 获取排序组织列表
     */
    @GetMapping("/sortList/{pid}/{notId}")
    @RequiresPermissions({"system:org:add", "system:org:edit"})
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
     * 跳转到添加页面
     */
    @GetMapping({"/add", "/add/{pid}"})
    @RequiresPermissions("system:org:add")
    public String toAdd(@PathVariable(value = "pid", required = false) String pid, Model model) {
        if (StrUtil.isNotEmpty(pid)) {
            model.addAttribute("pOrg", orgService.getById(pid));
        }

        return "/system/org/add";
    }

    /**
     * 跳转到编辑页面
     */
    @GetMapping("/edit/{id}")
    @RequiresPermissions("system:org:edit")
    public String toEdit(@PathVariable("id") String id, Model model) {
        SysOrg org = orgService.getById(id);
        SysOrg pOrg = orgService.getById(org.getPid());
        if (pOrg == null) {
            pOrg = new SysOrg();
            pOrg.setId("0");
            pOrg.setTitle("顶级");
        }

        // 编辑时排序的回显
        SortBackShow sb = new SortBackShow();
        String pid = org.getPid();
        if (pid.equals("0")) {
            // 首级
            sb.setNowSort(org.getSort());
            sb.setNowName("首位");
        } else if (org.getSort() == 1) {
            // 非首级的首位
            sb.setNowSort(org.getSort());
            sb.setNowName("首位");
        } else {
            // 其他各个位
            List<SysOrg> listByPid = orgService.getListByPid(org.getPid(), org.getId());
            for (SysOrg o : listByPid) {
                int previous = org.getSort() - 1;
                if (o.getSort() == previous) {
                    sb.setNowSort(o.getSort());
                    sb.setNowName(o.getTitle());
                }
            }
        }

        model.addAttribute("org", org);
        model.addAttribute("pOrg", pOrg);
        model.addAttribute("sort", sb);

        return "/system/org/add";
    }

    /**
     * 保存添加/修改的数据
     *
     * @param valid 表单验证对象
     */
    @PostMapping("/save")
    @RequiresPermissions({"system:org:add", "system:org:edit"})
    @ResponseBody
    @ActionLog(name = "新增或修改组织信息", title = "组织管理", key = LogTypeConstant.MODIFY)
    public ResultVo save(@Validated OrgValid valid, SysOrgReq req) {
        // 分割名称
        List<String> split = Arrays.asList(ToolUtil.cToe(req.getTitle()).split(","));

        // 排空+倒序
        List<String> afterTreatment = split.stream().filter(s -> StrUtil.isNotBlank(s)).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        // 获取所有名称
        List<String> existNameList = orgService.getOrgByName();

        String orgId = null;
        if (req.getType() == 1) {
            QueryWrapper<SysOrg> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("TYPE", 1);
            queryWrapper.eq("STATUS", 1);
            List<SysOrg> list = orgService.list(queryWrapper);
            if (ObjectUtil.isNotNull(list) && !list.isEmpty()) {
                orgId = list.get(0).getId();
            }
        }
        try {
            for (String name : afterTreatment) {
                // 新增
                if (StrUtil.isEmpty(req.getId())) {
                    if (existNameList.contains(name)) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "检测到" + name + "存在名称重复!");
                    }
                    if (req.getType() == 1 && ObjectUtil.isNotNull(orgId)) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "只能存在一个局组织");
                    }
                    //保存
                    SysOrg sysOrg = new SysOrg();
                    sysOrg.setId(MyIdUtil.getId());
                    sysOrg.setTitle(name);
                    sysOrg.setPid(req.getPid());
                    sysOrg.setType(req.getType());
                    sysOrg.setSort(req.getSort());
                    sysOrg.setRemark(req.getRemark());
                    sysOrg.setExistAdd(true);
                    this.doSave(sysOrg);
                    //添加组织权限
                    if (req.getType() != 1 && !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
                        this.addRole(sysOrg);
                    }
                } else {//编辑
                    String oldName = orgService.getById(req.getId()).getTitle();
                    if (!oldName.equals(req.getTitle()) && existNameList.contains(name)) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "检测到" + name + "存在名称重复!");
                    }
                    if (req.getType() == 1 && ObjectUtil.isNotNull(orgId) && !req.getId().equals(orgId)) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "只能存在一个局组织");
                    }
                    //保存
                    SysOrg sysOrg = new SysOrg();
                    sysOrg.setId(req.getId());
                    sysOrg.setTitle(name);
                    sysOrg.setPid(req.getPid());
                    sysOrg.setType(req.getType());
                    sysOrg.setSort(req.getSort());
                    sysOrg.setRemark(req.getRemark());
                    sysOrg.setExistAdd(false);
                    this.doSave(sysOrg);
                }
            }
        } catch (SysOrgException soe) {
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_ORG)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_ORG, ErrorCodeEnum.SYSTEM_ORG_ADD, "", "添加/编辑组织异常" + soe.getMessage()));
            }
            return ResultVoUtil.error(soe.getCode(), soe.getMessage());
        }
        return ResultVoUtil.success("成功");
    }


    private void addRole(SysOrg org) {
        List<String> rolesIds = ShiroUtil.getSubjectRoleIds();
        String orgId = org.getId();
        String pid = org.getPid();
        for (String roleId : rolesIds) {
            //判断是否有父级组织
            if (roleOrgService.getRoleByOrg(roleId, pid)) {
                SysRoleOrg ro = new SysRoleOrg();
                ro.setRoleId(roleId);
                ro.setOrgId(orgId);
                roleOrgService.save(ro);
            }
        }

    }


    private void doSave(SysOrg orgParam) throws SysOrgException {
        if (Objects.equals(orgParam.getId(), orgParam.getPid())) {
            throw new SysOrgException(ResultEnum.PARAM_ERROR.getCode(), "父级组织不能是自己！");
        }
        // 确定组织等级
        SysOrg pOrg = orgService.getById(orgParam.getPid());
        if (Objects.isNull(pOrg) && orgParam.getType() != 1) {
            throw new SysOrgException(ResultEnum.PARAM_ERROR.getCode(), "组织等级不正确！");
        }
        if (Objects.nonNull(pOrg) && orgParam.getType() - pOrg.getType() != 1) {
            throw new SysOrgException(ResultEnum.PARAM_ERROR.getCode(), "组织等级不正确！");
        }

        if (orgParam.getExistAdd()) {
            // 排序为空时，添加到最后
            if (orgParam.getSort() == null) {
                Byte sortMax = orgService.getSortMax(orgParam.getPid());
                orgParam.setSort(sortMax != null ? (byte) (sortMax - 1) : 0);
            }
        }

        // 添加/更新全部上级序号
        if ("0".equals(orgParam.getPid())) {
            orgParam.setPids("[0]");
        } else {
            orgParam.setPids(pOrg.getPids() + ",[" + orgParam.getPid() + "]");
        }

        // 排序功能
//        Byte existSort = null;
//        if (StrUtil.isNotEmpty(orgParam.getId())) {
//            existSort = orgService.getById(orgParam.getId()).getSort();
//        }
        Byte sort = orgParam.getSort() == null ? (byte) 0 : orgParam.getSort();
        String notId = !orgParam.getExistAdd() ? orgParam.getId() : "0";
        List<SysOrg> levelOrg = orgService.getListByPid(orgParam.getPid(), notId);
        orgParam.setStatus(StatusEnum.OK.getCode());
        try {
            levelOrg.add(sort, orgParam);
        } catch (IndexOutOfBoundsException e) {
            levelOrg.add(sort - 1, orgParam);
        }

        for (int i = 1; i <= levelOrg.size(); i++) {
            levelOrg.get(i - 1).setSort((byte) i);
        }

        // 保存数据
        boolean batch = orgService.saveOrUpdateBatch(levelOrg);

    }

    /**
     * 跳转到详细页面
     */
    @GetMapping("/detail/{id}")
    @RequiresPermissions("system:org:detail")
    public String toDetail(@PathVariable("id") String id, Model model) {
        model.addAttribute("org", orgService.getById(id));
        return "/system/org/detail";
    }

    /**
     * 设置一条或者多条数据的状态
     */
    @RequestMapping("/status/{param}")
    @RequiresPermissions("system:org:status")
    @ResponseBody
    @ActionLog(name = "修改组织状态", title = "组织管理", key = LogTypeConstant.MODIFY)
    public ResultVo status(
            @PathVariable("param") String param,
            @RequestParam(value = "id", required = false) String id) {
        // 判断要删除的节点有无孩子节点
        List<SysOrg> children = orgService.getListByPid(id, id);
        if (children.size() > 0) {
            return ResultVoUtil.error("有子节点的节点不允许删除");
        }
        //判断要删除的节点之下 有没有资产
        QueryWrapper<Asset> assetQueryWrapper = new QueryWrapper<Asset>();
        assetQueryWrapper.eq("ORG_ID", id);
        assetQueryWrapper.eq("IS_DEL", 1);

        if (!assetService.list(assetQueryWrapper).isEmpty()) {
            return ResultVoUtil.error("节点下存在登记资产 不允许删除");
        }
        //判断要删除的节点之下有没有机房
        QueryWrapper<Room> roomQueryWrapper = new QueryWrapper<>();
        roomQueryWrapper.eq("ORG_ID", id);
        List<Room> roomList = roomService.list(roomQueryWrapper);
        if(!roomList.isEmpty()){
            return ResultVoUtil.error("节点下存在机房 不允许删除");
        }

        // 更新状态
        StatusEnum statusEnum = StatusUtil.getStatusEnum(param);
        if (orgService.updateStatus(statusEnum, id)) {
            return ResultVoUtil.success(statusEnum.getMessage() + "成功");
        } else {
            return ResultVoUtil.error(statusEnum.getMessage() + "失败，请重新操作");
        }
    }

    /**
     * 查询管理该组织的所有角色
     *
     * @param id
     * @return
     */
    @RequestMapping("/roleList/{id}")
    @RequiresPermissions("system:org:roleList")
    public String roleList(@PathVariable String id, Model model) {
        QueryWrapper<SysRoleOrg> wrapper = new QueryWrapper<>();
        wrapper.eq("org_id", id);
        List<SysRoleOrg> roleOrgList = roleOrgService.list(wrapper);
        Set<String> roleIdSet = new HashSet<>();
        for (SysRoleOrg roleOrg : roleOrgList) {
            roleIdSet.add(roleOrg.getRoleId());
        }

        model.addAttribute("list", Collections.emptyList());
        if (roleIdSet.size() > 0) {
            model.addAttribute("list", roleService.listByIds(roleIdSet));
        }

        return "/system/org/roleList";
    }

}
