package com.jcca.web.asset.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyDateUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.controller.bean.AssetHardwareFixReq;
import com.jcca.web.asset.controller.bean.AssetProcessTreeVo;
import com.jcca.web.asset.controller.bean.OrgProcessTreeVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHardwareFix;
import com.jcca.web.asset.service.AssetHardwareFixService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.asset.vo.AssetHardwareFixExportVo;
import com.jcca.web.asset.vo.AssetHardwareFixVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ApiHardwareController
 * @Description 设备硬件相关
 * @Date 2020/7/16 16:39
 * @Author hanwone
 */
@RestController
@RequestMapping("/api/hardware")
@Api(tags = "资产硬件更换记录")
@Slf4j
public class ApiHardwareController {

    @Resource
    private AssetHardwareFixService assetHardwareFixService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService orgService;

    /**
     * 硬件更换记录列表
     *
     * @param req
     * @return
     */
    @PostMapping("/index")
    @RequiresPermissions("api:hardware:index")
    @ApiOperation(value = "硬件更换记录首页")
    @ActionLog(name = "查看硬件更换记录", title = "硬件维护记录", key = LogTypeConstant.QUERY)
    public ResultVo index(@RequestBody AssetHardwareFixReq req) {
        // 返回
        PageBean<AssetHardwareFixVo> pageBean = new PageBean<>();
        // 通过组织查询资产ID syt
        List<String> allOrgs;
        QueryWrapper<Asset> query = Wrappers.query();
        if (StrUtil.isEmpty(req.getOrgId())) {
            // 页面初始化默认展示第一级组织下的数据 syt
            List<SysOrg> subjectOrgs = ShiroUtil.getSubjectOrgs();
            allOrgs = subjectOrgs.stream().map(SysOrg::getId).collect(Collectors.toList());
            allOrgs.add("x");
        } else {
            allOrgs = allOrgs(req.getOrgId());
        }
        req.setOrgIdList(allOrgs);

        Integer page = Convert.toInt(req.getPage(), 1);
        Integer size = Convert.toInt(req.getSize(), 10);
        req.setStart((page - 1) * size);
        req.setEnd(page * size + 1);

        String month = req.getMonth();
        if (StrUtil.isNotEmpty(month)) {
            month += "-01";
            req.setMonth(month);
            try {
                req.setStartTime(MyDateUtil.getMinMonthDateToString(month));
                req.setEndTime(MyDateUtil.getMaxMonthDateToString(month));
            } catch (ParseException e) {
                return ResultVoUtil.error("查询硬件更换记录日期解析失败,请检查日期格式");
            }
        }
        List<AssetHardwareFixVo> list = assetHardwareFixService.findByPage(req);
        for (AssetHardwareFixVo assetHardwareFixVo : list) {
            AssetBelong assetBelong = assetService.findAssetBelongById(assetHardwareFixVo.getAssetId());
            if (Objects.nonNull(assetBelong)) {
                assetHardwareFixVo.setOrgName(assetBelong.getOrgName());
            }
        }

        Long count = assetHardwareFixService.countItem(req);

        pageBean.setTotal(count);
        pageBean.setContent(list);

        return ResultVoUtil.success(pageBean);
    }

    private List<String> allOrgs(String id) {
        List<String> defaultAllOrg = new ArrayList<>();
        // 查询组织ID
        QueryWrapper<SysOrg> query = Wrappers.query();
        query.like("PIDS", id);

        SysOrg org = orgService.getById(id);
        if (ObjectUtil.isEmpty(org)) {
            defaultAllOrg.add(id);
            return defaultAllOrg;
        }
        if (orgService.getById(id).getType() == OrgTypeConst.CENTER) {
            // 组织级别为中心时只展示中心的数据
        } else {
            defaultAllOrg = orgService.list(query).stream().map(SysOrg::getId).collect(Collectors.toList());
        }
        defaultAllOrg.add(id);
        defaultAllOrg.add("x");
        return defaultAllOrg;
    }

    /**
     * 保存/更新硬件更换记录
     *
     * @param vo
     * @return
     */
    @PostMapping("/save")
    @RequiresPermissions("api:hardware:save")
    @ApiOperation(value = "保存硬件更换记录")
    @ActionLog(name = "保存硬件更换记录", title = "硬件维护记录", key = LogTypeConstant.ADD)
    public ResultVo save(@Validated @RequestBody AssetHardwareFixVo vo) {
        AssetHardwareFix assetHardwareFix = BeanUtil.copyProperties(vo, AssetHardwareFix.class);

        assetHardwareFixService.saveEntity(assetHardwareFix);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @PostMapping("/remove/{id}")
    @RequiresPermissions("api:hardware:remove")
    @ApiOperation(value = "删除硬件更换记录")
    @ActionLog(name = "删除硬件更换记录", title = "硬件维护记录", key = LogTypeConstant.REMOVEE)
    public ResultVo remove(@PathVariable("id") String id) {
        assetHardwareFixService.removeById(id);
        return ResultVoUtil.REMOVE_SUCCESS;
    }


    @GetMapping("/export/verify")
    @ApiOperation(value = "导出硬件更换记录校验")
    public ResultVo exportVerify(AssetHardwareFixReq req) {
        String month = req.getMonth();
        if (StrUtil.isNotEmpty(month)) {
            String y = month.split("-")[0];
            String m = month.split("-")[1];
            if (m.length() == 1) {
                m = "-0" + m;
            }
            req.setMonth(y + m);
        }

        req.setStart(0);
        req.setEnd(20000);
        req.setAssetIdList(ShiroUtil.getSubjectAssetIds());
        List<AssetHardwareFixExportVo> list = assetHardwareFixService.findExport(req);
        if (Objects.isNull(list) || list.isEmpty()) {
            return ResultVoUtil.warning("没有符合条件的记录");
        }
        return ResultVoUtil.success();
    }


    /**
     * 硬件更换记录
     */
    @GetMapping("/export")
    @RequiresPermissions("api:hardware:export")
    @ApiOperation(value = "导出硬件更换记录")
    @ActionLog(name = "导出硬件更换记录", title = "硬件维护记录", key = LogTypeConstant.DOWNLOAD)
    public void export(AssetHardwareFixReq req, HttpServletResponse response) {
        String month = req.getMonth();
        if (StrUtil.isNotEmpty(month)) {
            String y = month.split("-")[0];
            String m = month.split("-")[1];
            if (m.length() == 1) {
                m = "-0" + m;
            }
            req.setMonth(y + m);
        }
        req.setStart(0);
        req.setEnd(20000);
        req.setAssetIdList(ShiroUtil.getSubjectAssetIds());
        List<AssetHardwareFixExportVo> list = assetHardwareFixService.findExport(req);
        for (AssetHardwareFixExportVo assetHardwareFixVo : list) {
            AssetBelong assetBelong = assetService.findAssetBelongById(assetHardwareFixVo.getAssetId());
            if (Objects.nonNull(assetBelong)) {
                assetHardwareFixVo.setOrgName(assetBelong.getOrgName());
            }
        }


        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet("更换记录");
        writer.addHeaderAlias("assetId", "设备ID");
        writer.addHeaderAlias("assetName", "设备名称");
        writer.addHeaderAlias("orgName", "所属组织");
        writer.addHeaderAlias("assetIp", "设备IP");
        writer.addHeaderAlias("assetModeStr", "设备类型");
        writer.addHeaderAlias("hardwareTypeStr", "硬件类型");
        writer.addHeaderAlias("manufacturer", "设备厂商");
        writer.addHeaderAlias("fruBefore", "更换前FRU");
        writer.addHeaderAlias("fruNow", "更换后FRU");
        writer.addHeaderAlias("reason", "故障原因");
        writer.addHeaderAlias("description", "故障描述");
        writer.addHeaderAlias("fixTime", "更换时间");

        writer.write(list, true);

        String fileName = DateUtil.formatDate(new Date());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename= " + fileName + ".xlsx");
        try {
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
        } catch (IOException e) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_HARDWARE_MANAGER)) {
                String errorMsg = LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_HARDWARE_MANAGER, ErrorCodeEnum.COMMON_EXPORT_ERROR, req.getAssetName(), e.getMessage());
                log.error("导出硬件更换亡灵失败：{}", errorMsg, e);
            }
        }
    }

    /**
     * 组织资产树
     *
     * @return com.jcca.common.vo.ResultVo<?>
     * @Author syt
     * @Date 2021/11/10 17:49
     */
    @GetMapping("/tree")
    @RequiresPermissions("api:hardware:tree")
    @ApiOperation(value = "组织资产树")
    public ResultVo<?> tree() {
        // 默认展示的第一个组织
        SysOrg defOrg = orgService.getDefaultOrg();
        // 当前用户所有组织
        List<SysOrg> subjectOrgs = ShiroUtil.getSubjectOrgs();
        List<String> ids = subjectOrgs.stream().map(SysOrg::getId).collect(Collectors.toList());
        // 组织下的资产
        QueryWrapper<Asset> query = Wrappers.query();
        query.eq("IS_DEL", 1);
        query.in("ORG_ID", ids);
        List<Asset> assets = assetService.list(query);
        // 返回结果
        ArrayList<Object> res = new ArrayList<>();
        for (SysOrg org : subjectOrgs) {
            OrgProcessTreeVo vo = new OrgProcessTreeVo();
            vo.setId(org.getId());
            vo.setPid(org.getPid());
            vo.setPIds(org.getPids());
            vo.setTitle(org.getTitle());
            vo.setType(org.getType());
            res.add(vo);
        }
        for (Asset asset : assets) {
            AssetProcessTreeVo vo = new AssetProcessTreeVo();
            vo.setId(asset.getId());
            vo.setPId(asset.getOrgId());
            vo.setTitle(asset.getName());
            vo.setType(asset.getDesk());
            vo.setOrgName(orgService.getById(asset.getOrgId()).getTitle());
            res.add(0, vo);
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("id", defOrg.getId());
        map.put("title", defOrg.getTitle());
        map.put("res", res);
        return ResultVoUtil.success(map);
    }

}
