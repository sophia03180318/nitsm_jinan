package com.jcca.admin.biz.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.vo.TopoNodePortVo;
import com.jcca.admin.biz.vo.TopoVlanVo;
import com.jcca.admin.system.entity.*;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.TopoAssetPortPicService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.service.TopoAssetPortVlanService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/17 16:50
 */
@Controller
@RequestMapping(value={"/system/graphInterface","/api/v2/graphInterface"})
public class GraphInterfaceController {

    public static final Integer DOWN_STATUS = 2;
    public static final Integer UNKONW_STATUS = 0;

    @Resource
    private TopoAssetPortService topoAssetPortService;
    @Resource
    private TopoAssetPortVlanService topoAssetPortVlanService;
    @Resource
    private CollectInterfacesService intefacesServ;
    @Resource
    private TopoAssetPortPicService topoAssetPortPicService;
    @Resource
    private AssetService assetService;

    @GetMapping("/index")
    @RequiresPermissions("system:graphInterface:index")
    public String index() {
        return "/system/interface/index";
    }

    /**
     * 组织数据列表
     */
    @GetMapping("/list")
    @ResponseBody
    public ResultVo list() {
        List<SysOrg> list = new ArrayList<>(ShiroUtil.getSubjectOrgs());
        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
        SysOrgService sysOrgService = SpringContextUtil.getBean(SysOrgService.class);
        List<SysOrg> listAsset = sysOrgService.getOrgsAsset(user.getId(),"");
        listAsset.addAll(list);
        return ResultVoUtil.success(listAsset);
    }

    /**
     * 获取端口信息
     *
     * @return
     */
    @PostMapping("/listPort")
    @ResponseBody
    public ResultVo listPort(@RequestBody String assetId) {
        return ResultVoUtil.success(portList(assetId,""));
    }



    /**
     * 查询端口
     * @param assetId
     * @return
     */
    public Map<String, Object> portList(String assetId,String pcbId) {
        Map<String, Object> map = new HashMap<>();
        Asset asset = assetService.getById(assetId);
        List<AssetPortVo> port = null;
        if (assetService.isStationAsset(assetId) || AssetModeConst.B24.equals(asset.getAssetImage())) {
            port = topoAssetPortService.selectAssetPort2(assetId,pcbId);
        } else {
            port = topoAssetPortService.selectAssetPort(assetId,pcbId);
            if (Objects.isNull(port) || port.isEmpty()) {
                port = topoAssetPortService.selectAssetPort2(assetId,pcbId);
            }
        }

        if (port.isEmpty()) {
            port = getCachePortData(assetId);
        }


        // 查询状态一小时之前是断则置为灰色
        for (AssetPortVo item : port) {
            Date updateDate = item.getUpdateDate();
            if (DOWN_STATUS.equals(item.getStatus()) && Objects.nonNull(updateDate)) {

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, -1);
                Date time = calendar.getTime();
                long between = DateUtil.between(updateDate, time, DateUnit.MS, false);
                if (between > 0) {
                    item.setStatus(UNKONW_STATUS);
                }
            }

        }

        List<TopoAssetPortVlan> vlan = topoAssetPortVlanService.queryAssetPortVlan(assetId,pcbId);
        for (TopoAssetPortVlan topoAssetPortVlan : vlan) {
            String str = new String(topoAssetPortVlan.getContent());
            topoAssetPortVlan.setContentStr(str);
        }

        List<TopoAssetPortPic> pics = topoAssetPortPicService.queryAssetPortPic(assetId,pcbId);
        pics.forEach(
                i -> i.setContentStr(new String(i.getContent()))
        );
        map.put("port", port);
        map.put("vlan", vlan);
        map.put("total", port.size());
        map.put("pic", pics);
        return map;
    }

    /**
     * 获取缓存中的端口数据 防止未上采集数据导致端口面板不显示
     *
     * @param assetId
     */
    private List<AssetPortVo> getCachePortData(String assetId) {
        List<AssetPortVo> portList = new ArrayList<AssetPortVo>();
        List<CollectInterfaces> realTimeData = intefacesServ.filterPort(assetId);
        for (CollectInterfaces item : realTimeData) {
            QueryWrapper<TopoAssetPort> queryWrapper = new QueryWrapper<TopoAssetPort>();
            queryWrapper.eq("ASSET_ID", assetId);
            queryWrapper.eq("PORT_INDEX", item.getPortIndex());
            TopoAssetPort topoPort = topoAssetPortService.getOne(queryWrapper);

            AssetPortVo vo = new AssetPortVo();
            if (Objects.nonNull(topoPort)) {
                vo.setPointX(topoPort.getPointX());
                vo.setPointY(topoPort.getPointY());
                vo.setNodeId(topoPort.getNodeId());
                vo.setParentId(topoPort.getParentId());
                vo.setStatus(topoPort.getStatus());
                vo.setUpdateDate(topoPort.getUpdateDate());
            }
            vo.setAssetId(item.getAssetId());
            vo.setPortIndex(item.getPortIndex());
            vo.setPortName(item.getPortName());
            vo.setPortType(item.getPortType() + "");
            portList.add(vo);
        }

        return portList;
    }

    @PostMapping("/listPortVlan")
    @ResponseBody
    public ResultVo listPortVlan(@RequestBody String assetId) {
        List<AssetPortVo> list = topoAssetPortService.selectAssetPortVlan(assetId);
        return ResultVoUtil.success(list);
    }

    @PostMapping("/save")
    @ResponseBody
    public ResultVo save(@RequestBody TopoVlanVo topoVlanVo) {
        List<TopoAssetPort> listAssetPorts = new ArrayList<>();
        List<TopoAssetPortVlan> topoAssetPortVlans = new ArrayList<>();
        // 面板型号 syt 2021/7/15
        List<TopoAssetPortPic> topoAssetPortPics = new ArrayList<>();
        for (TopoNodePortVo t : topoVlanVo.getTopoNodePortVos()) {
            if (t.getType().equals("net_topo")) {
                TopoAssetPort topoAssetPort = new TopoAssetPort();
                topoAssetPort.setAssetId(t.getAssetId());
                topoAssetPort.setPcbId(topoVlanVo.getPcbId());
                topoAssetPort.setPointX(t.getPointX());
                topoAssetPort.setPointY(t.getPointY());
                topoAssetPort.setPortIndex(t.getPortIndex());
                topoAssetPort.setNodeId(t.getNodeId());
                topoAssetPort.setParentId(t.getParentId());
                topoAssetPort.setUpdateDate(new Date());
                // 旋转角度 syt 2021/9/28
                topoAssetPort.setRotation(t.getRotation());
                topoAssetPort.setPortMode(t.getPortMode());
                listAssetPorts.add(topoAssetPort);
            } else if (t.getType().equals("group")) {
                TopoAssetPortVlan topoAssetPortVlan = new TopoAssetPortVlan();
                topoAssetPortVlan.setAssetId(t.getAssetId());
                topoAssetPortVlan.setPcbId(topoVlanVo.getPcbId());
                topoAssetPortVlan.setContent(t.getContent().getBytes());
                topoAssetPortVlans.add(topoAssetPortVlan);
            } else if (t.getType().equals("picture")) {
                // 面板型号 syt 2021/7/15
                TopoAssetPortPic topoAssetPortPic = new TopoAssetPortPic();
                topoAssetPortPic.setAssetId(t.getAssetId());
                topoAssetPortPic.setContent(t.getContent().getBytes());
                topoAssetPortPic.setCategoryPic(t.getCategoryPic());
                topoAssetPortPic.setNodeId(t.getNodeId());
                topoAssetPortPic.setPointX(t.getPointX());
                topoAssetPortPic.setPointY(t.getPointY());
                topoAssetPortPic.setPictureImg(t.getPictureImg());
                topoAssetPortPic.setHeight(t.getHeight());
                topoAssetPortPic.setWidth(t.getWidth());
                topoAssetPortPic.setPcbId(topoVlanVo.getPcbId());
                topoAssetPortPics.add(topoAssetPortPic);
            }
        }

        topoAssetPortService.deleteAssetPort(topoVlanVo.getAssetId(),topoVlanVo.getPcbId());
        topoAssetPortVlanService.deleteAssetPortVlan(topoVlanVo.getAssetId(),topoVlanVo.getPcbId());
        topoAssetPortPicService.deleteAssetPortPic(topoVlanVo.getAssetId(),topoVlanVo.getPcbId());
        if (listAssetPorts.size() > 0) {
            topoAssetPortService.saveBatch(listAssetPorts);
        }

        if (topoAssetPortVlans.size() > 0) {
            topoAssetPortVlanService.saveBatch(topoAssetPortVlans);
        }

        // 保存面板信息 syt 2021/7/15
        if (topoAssetPortPics.size() > 0) {
            topoAssetPortPicService.saveBatch(topoAssetPortPics);
        }

        return ResultVoUtil.success("保存成功");
    }
}
