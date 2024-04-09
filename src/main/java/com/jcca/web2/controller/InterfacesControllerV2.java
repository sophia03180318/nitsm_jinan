package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.controller.GraphInterfaceController;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web2.dto.PortTempDto;
import com.jcca.web2.entity.PortTemp;
import com.jcca.web2.entity.TopoPcb;
import com.jcca.web2.service.PortTempService;
import com.jcca.web2.service.TopoPcbService;
import com.jcca.web2.vo.InspectVo;
import com.jcca.web2.vo.InterfacesConfigVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 端口相关管理
 * @description: 端口相关管理
 * @author: Lvyp
 * @create: 2024/01/10 10:51
 */
@RestController
@RequestMapping("/api/v2/interfaces")
@Api(tags = "端口管理V2")
public class InterfacesControllerV2 {

    @Resource
    private CollectInterfacesService  collectInterfaceServ;
    @Resource
    private TopoPcbService  topoPcbServ;
    @Resource
    private PortTempService portTempServ;
    @Resource
    private TopoAssetPortService assetPortServ;
    @Resource
    private GraphInterfaceController graphInterfaceController;
    @Resource
    private SysOrgService sysOrgService;



    /**
     * 组织数据列表
     */
    @GetMapping("/orgList")
    @ResponseBody
    public ResultVo list() {
        List<SysOrg> list = new ArrayList<>(ShiroUtil.getSubjectOrgs());
        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
        List<SysOrg> listAsset = sysOrgService.getOrgsAsset(user.getId(), AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()+"");
        listAsset.addAll(list);
        return ResultVoUtil.success(listAsset);
    }

    /**
     * 获取端口信息
     *
     * @return
     */
    @GetMapping("/listPortV2")
    @ResponseBody
    public ResultVo listPortV2(String assetId,String pcbId) {

        return ResultVoUtil.success(graphInterfaceController.portList(assetId,pcbId));
    }

    @GetMapping("/listPcb")
    public ResultVo listPcb(String assetId){
        if(StrUtil.isEmpty(assetId)){
            return ResultVoUtil.success(new ArrayList<>());
        }
        QueryWrapper<TopoPcb> queryWrapper = new QueryWrapper<TopoPcb>();
        queryWrapper.eq("ASSET_ID",assetId);
        List<TopoPcb> list = topoPcbServ.list(queryWrapper);
        for (TopoPcb topoPcb : list) {
            List<AssetPortVo> assetPortVos = assetPortServ.selectAssetPort(topoPcb.getAssetId(), topoPcb.getPcbId());
            topoPcb.setHaveTopo(!assetPortVos.isEmpty());
        }
        return ResultVoUtil.success(list);
    }

    @PostMapping("/addPcb")
    public ResultVo addPcb(@RequestBody TopoPcb pcb){
        pcb.setPcbId(MyIdUtil.getId());
        topoPcbServ.save(pcb);

        return ResultVoUtil.success(pcb);
    }

    @PostMapping("/updatePcb")
    public ResultVo updatePcb(@RequestBody TopoPcb pcb){
        if(StrUtil.isEmpty(pcb.getPcbId())){
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        topoPcbServ.updateById(pcb);

        return ResultVoUtil.success(pcb);
    }

    @PostMapping("/delPcb")
    public ResultVo delPcb(@RequestBody TopoPcb pcb){
        if(StrUtil.isEmpty(pcb.getPcbId())){
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        topoPcbServ.removeEntity(pcb);
        return ResultVoUtil.success();
    }

    @GetMapping("/getPortConf")
    @ApiOperation("获取端口配置")
    public ResultVo<Object> getPortCOnf(Integer pcbSort,String assetId,String tempId) {
        if(StrUtil.isEmpty(assetId)||Objects.isNull(tempId)){
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        if(Objects.isNull(pcbSort)){
            pcbSort = 1;
        }
        List<CollectInterfaces> realTimeData = collectInterfaceServ.getRealTimeData(assetId);

        List<Integer> typeList = Arrays.asList(18, 22, 23, 339, 135, 56, 6);
        List<CollectInterfaces> collect = realTimeData.stream().filter(item ->( 1==item.getPortLinkType() && typeList.contains(item.getPortType()))).collect(Collectors.toList());
        if(collect.isEmpty()){
            //光交
            collect = realTimeData.stream().filter(item ->( 0==item.getPortLinkType() && typeList.contains(item.getPortType()))).collect(Collectors.toList());
        }
        PortTempDto portTempMsg = portTempServ.getPortTempMsg(tempId);
        if(Objects.isNull(portTempMsg)){
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        List<InterfacesConfigVo> voList = new ArrayList<>();
        int portSize = collect.size();
        int count = portTempMsg.getPort1() + portTempMsg.getPort2();
        int needGetNumStart = (pcbSort-1)*count;
        for (int i=1;i<count+1;i++){
            String tmp = "Gi"+i;
            InterfacesConfigVo vo = new InterfacesConfigVo();
            vo.setTempPortName(tmp);

            int needGetIndex = needGetNumStart+(i-1);
            if(portSize>needGetIndex){
                CollectInterfaces collectInterfaces = collect.get(needGetIndex);
                vo.setPortName(collectInterfaces.getPortName());
                vo.setPortIndex(collectInterfaces.getPortIndex());
            }
            voList.add(vo);
        }
        return ResultVoUtil.success(voList);
    }

}
