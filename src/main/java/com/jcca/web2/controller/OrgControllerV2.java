package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.controller.bean.SysOrgException;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysOrgReq;
import com.jcca.admin.system.entity.SysRoleOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.SysRoleOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AdminConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.ToolUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.RoomService;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 组织管理V2
 * @className OrgControllerV2
 * @date 2023/11/30 14:18
 * @since 2.1.0.0
 */
@RestController
@RequestMapping("/api/v2/org")
@Api(tags = "组织管理V2")
public class OrgControllerV2 {

    @Resource
    private SysOrgService orgService;
    @Resource
    private RoomService roomService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysRoleOrgService roleOrgService;

    /**
     * 组织数据列表
     */
    @GetMapping("/list")
    @ResponseBody
    public ResultVo<Object> list(SysOrg org) {
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
    @ResponseBody
    public ResultVo<Object> sortList(
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
        return ResultVoUtil.success(sortMap);
    }

    /**
     * 保存添加/修改的数据
     *
     * @param req 表单验证对象
     */
    @PostMapping("/save")
    @RequiresPermissions({"api:v2:org:save"})
    @ResponseBody
    @ActionLog(name = "新增或修改组织信息", title = "组织管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> save(@Validated @RequestBody SysOrgReq req) {
        // 分割名称
        List<String> split = Arrays.asList(ToolUtil.cToe(req.getTitle()).split(","));

        // 排空+倒序
        List<String> afterTreatment = split.stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
        // 查看集合中是否有相同数据
        if (afterTreatment.size() > 1) {
            boolean flag = this.isDup(afterTreatment);
            if (flag) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "所添加的组织名称有重复");
            }
        }

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
        SysOrg sysOrg = new SysOrg();
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
                    sysOrg.setId(MyIdUtil.getId());
                    sysOrg.setTitle(name);
                    sysOrg.setPid(req.getPid());
                    sysOrg.setType(req.getType());
                    sysOrg.setSort(req.getSort());
                    sysOrg.setRemark(req.getRemark());
                    sysOrg.setExistAdd(true);
                    List<SysOrg> levelOrg = orgService.getListByPid(req.getPid(), sysOrg.getId());
                    if (req.getSort() == 0) {
                        sysOrg.setSort((byte) (levelOrg.size() + 1));
                    }

                    this.doSave(sysOrg);
                    //添加组织权限
                    if (req.getType() != 1 && !ShiroUtil.getSubject().getId().equals(AdminConst.ADMIN_ID)) {
                        this.addRole(sysOrg);
                    }

                    if (OrgTypeConst.STATION == req.getType() || OrgTypeConst.CENTER == req.getType()) {
                        req.setId(sysOrg.getId());
                        req.setTitle(name);
                        roomService.createRoomAndCabinet(req);
                        req.setId(null);
                    }

                } else {//编辑
                    SysOrg org = orgService.getById(req.getId());
                    String oldName = org.getTitle();
                    if (!oldName.equals(req.getTitle()) && existNameList.contains(name)) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "检测到" + name + "存在名称重复!");
                    }
                    int type = org.getType();
                    if (type != req.getType()) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "不能修改组织类型");
                    }
                    if (req.getType() == 1 && ObjectUtil.isNotNull(orgId) && !req.getId().equals(orgId)) {
                        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "只能存在一个局组织");
                    }
                    //保存
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
        } catch (SysOrgException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ORG_MANAGE, req, e);
            return ResultVoUtil.error(e.getCode(), e.getMessage());
        }
        return ResultVoUtil.success("成功");
    }

    private boolean isDup(List<String> list) {
        Set<String> set = new HashSet<>();
        for (String str : list) {
            if (set.contains(str)) {
                return true;
            }
            set.add(str);
        }
        return false;
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
        orgService.saveOrUpdateBatch(levelOrg);

    }

    /**
     * 设置一条或者多条数据的状态
     */
    @PostMapping("/del/{id}")
    @RequiresPermissions("api:v2:org:del")
    @ResponseBody
    @ActionLog(name = "删除组织", title = "组织管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> status(@PathVariable(value = "id") String id) {
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
        if (!roomList.isEmpty()) {
            return ResultVoUtil.error("节点下存在机房 不允许删除");
        }

        orgService.updateStatus(StatusEnum.DELETE, id);

        return ResultVoUtil.success("成功");
    }

}
