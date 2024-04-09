package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.RestBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.entity.NetWorkAddress;
import com.jcca.web.ip.enums.IpStatusEnum;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.ip.service.NetWorkAddressService;
import com.jcca.web.ip.vo.NetWorkAddressVo;
import com.jcca.web2.vo.IpVo;
import com.jcca.web2.vo.LineNetWorkVo;
import com.jcca.web2.vo.NetWorkVo;
import com.jcca.web2.vo.StationNetObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: IP管理
 * @author: sophia
 * @create: 2023/11/02 14:43
 **/

@RestController
@RequestMapping("/api/v2/ip")
@Api(tags = "IP管理V2版本")
public class IpControllerV2 {
    @Resource
    private NetWorkAddressService netService;
    @Resource
    private IpInfoService ipMsgService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService orgService;



    @GetMapping("/getLineNetMsg")
    @ApiOperation(value = "获取车站列表含车站内网段列表")
    public ResultVo<Object> getLineNetMsg(String lineOnrId) {
        List<StationNetObj> netWorkAddressList =  netService.getLineNetMsgV2(lineOnrId);
        return ResultVoUtil.success(netWorkAddressList);
    }




    /**
     * 获取登录用户组织树展示
     */
    @GetMapping("/orglist")
    @ApiOperation(value = "用户权限组织列表")
    public ResultVo<Object> orgList() {
        SysOrg org = orgService.getDefaultOrg();
        if (Objects.isNull(org)) {
            return ResultVoUtil.warning("请分配组织");
        }
        List<SysOrg> orgList = ShiroUtil.getSubjectOrgs().stream().filter(o -> {
            return o.getType() == OrgTypeEnum.LINE.getCode() || o.getType() == OrgTypeEnum.CENTER.getCode();
        }).sorted(Comparator.comparing(SysOrg::getType)).collect(Collectors.toList());
        return ResultVoUtil.success(orgList);
    }

    /**
     * 获取中心网段地址
     */
    @GetMapping("/getCenterNetWork/{orgId}")
    @ApiOperation(value = "获取中心网段地址")
    public ResultVo<Object> getCenterNetWork(@PathVariable String orgId) {
        if (StringUtils.isEmpty(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        if (OrgTypeEnum.CENTER.getCode() != org.getType()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织不是中心类型");
        }
        List<NetWorkAddressVo> netWorkAddressVos = netService.selectCenterIpByIdV2(org);
        return ResultVoUtil.success(netWorkAddressVos);
    }


    /**
     * 获取中心IP列表
     */
    @GetMapping("/getCenterIpList/{netWorkId}")
    @ApiOperation(value = "获取中心网段的IP地址")
    public ResultVo<Object> getCenterIpList(@PathVariable String netWorkId) {
        if (StringUtils.isEmpty(netWorkId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }

        List<IpVo> ipList = ipMsgService.selectIPVoV2(netWorkId);

        for (IpVo ipVo : ipList) {
            String[] ipArr = ipVo.getIp().split("\\.");
            String ipNumStr = ipArr[ipArr.length - 1];
            ipVo.setIpNumSort(Integer.valueOf(ipNumStr));
            ipVo.setIpNum(ipNumStr);
            if(StrUtil.isNotEmpty(ipVo.getAssetId())){
                ipVo.setStatus(IpStatusEnum.MONITOR.getCode());
            }else{
                ipVo.setStatus(IpStatusEnum.ALLOT.getCode());
            }
        }
        List<IpVo> sortList = ipList.stream().sorted(Comparator.comparing(IpVo::getIpNumSort))
                .collect(Collectors.toList());
        return ResultVoUtil.success(sortList);

    }

    /**
     * 获取线路网段地址
     */
    @GetMapping("/getLineNetWork/{orgId}")
    @ApiOperation(value = "获取线路下网段地址")
    public ResultVo<Object> getLineNetWork(@PathVariable String orgId) {
        if (StringUtils.isEmpty(orgId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        SysOrg org = orgService.getById(orgId);
        if (OrgTypeEnum.LINE.getCode() != org.getType()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "组织不是线类型");
        }
        ArrayList<LineNetWorkVo> lineNetWorkVos = netService.selectLineIpByIdV2(org);
        return ResultVoUtil.success(lineNetWorkVos);
    }


    /**
     * 获取线路网段的IP地址列表
     */
    @GetMapping("/getLineIpList/{netWorkIds}")
    @ApiOperation(value = "获取线路网段的IP地址列表")
    public ResultVo<Object> getIpList(@PathVariable String netWorkIds) {
        if (ObjectUtil.isEmpty(netWorkIds)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        String[] netWorkIdList = netWorkIds.split(",");
        ArrayList<NetWorkVo> netWorkList = new ArrayList<>();
        for (String netWorkId : netWorkIdList) {
            NetWorkAddress netWork = netService.getById(netWorkId);
            NetWorkVo netWorkVo = new NetWorkVo();
            netWorkVo.setId(netWork.getId());
            netWorkVo.setName(netWork.getName());

            QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("NET_WORK_ADDRESS_ID", netWorkId);
            queryWrapper.orderByAsc("RANK");
            List<IpInfo> ips = ipMsgService.list(queryWrapper);
            ArrayList<IpVo> ipList = new ArrayList<>();
            for (IpInfo ipMsg : ips) {
                IpVo ipVo = new IpVo();
                BeanUtils.copyProperties(ipMsg, ipVo);
                String[] ipArr = ipMsg.getIp().split("\\.");
                String ipNumStr = ipArr[ipArr.length - 1];
                ipVo.setIpNumSort(Integer.valueOf(ipNumStr));
                ipVo.setIpNum(ipNumStr);

                if (StringUtils.isEmpty(ipMsg.getRemark())) {
                    ipVo.setStatus(IpStatusEnum.ALLOT.getCode());
                } else {
                    ipVo.setStatus(IpStatusEnum.MONITOR.getCode());
                }
                ipList.add(ipVo);
            }
            List<IpVo> sortList = ipList.stream().sorted(Comparator.comparing(IpVo::getIpNumSort))
                    .collect(Collectors.toList());

            netWorkVo.setIpList(sortList);
            netWorkList.add(netWorkVo);
        }

        return ResultVoUtil.success(netWorkList);
    }


    /**
     * IP绑定设备 信息查询
     */
    @GetMapping("/getIpAssetMsg/{id}")
    @ApiOperation(value = "IP绑定设备信息查询")
    public ResultVo<Object> getIpAssetMsg(@PathVariable("id") String id) {
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "请传入需要查询详情的ip");
        }
        IpInfo ip = ipMsgService.getById(id);
        if (Objects.isNull(ip)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "未查询到此IP信息");
        }
        String assetId = ip.getRemark();
        if (StringUtils.isEmpty(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "此IP上无资产信息");
        }
        Asset asset = assetService.getById(assetId);
        if (ObjectUtil.isNull(asset)) {
            ip.setRemark("");
            ipMsgService.saveOrUpdate(ip);
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "此IP上无资产信息");
        }
        AssetMsgVo msg = assetService.getAssetMsgVo(asset);
        String uIndex = assetService.getUIndex(assetId);
        if (Objects.nonNull(uIndex)) {
            uIndex = uIndex.replace("_", "U_");
            msg.setPositionStr(uIndex + "U");
        }
        return ResultVoUtil.success(msg);
    }
}