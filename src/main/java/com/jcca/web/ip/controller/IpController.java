package com.jcca.web.ip.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.service.BaseService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.ip.controller.bean.*;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.entity.NetWorkAddress;
import com.jcca.web.ip.enums.IpAuthStatusEnum;
import com.jcca.web.ip.enums.IpPingStatusEnum;
import com.jcca.web.ip.enums.IpStatusEnum;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.ip.service.NetWorkAddressService;
import com.jcca.web.ip.util.AppIpUtil;
import com.jcca.web.ip.vo.DetectionVo;
import com.jcca.web.ip.vo.IpInfoVo;
import com.jcca.web.ip.vo.NetWorkAddressVo;
import com.jcca.web.ip.vo.SysOrgVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ip相关接口
 *
 * @author lyp
 */
@Slf4j
@Api(tags = "Ip相关接口")
@RestController
@RequestMapping("/api/ip")
public class IpController {

    public static final String BATCH_PING_FLG = "BATCH_PING:";
    @Resource
    private NetWorkAddressService netService;
    @Resource
    private IpInfoService ipMsgService;
    @Resource
    private AssetService assetService;
    @Resource
    private RedisService redisServ;
    @Resource
    private SysOrgService orgService;
    @Resource
    private BaseService baseService;

    /**
     * 获取登录用户组织树展示
     */

    @GetMapping("/orglist")
    @ApiOperation(value = "用户权限组织列表")
    public ResultVo orgList() {
        SysOrg org = orgService.getDefaultOrg();
        if (Objects.isNull(org)) {
            return ResultVoUtil.warning("请分配组织");
        }
        Map<String, Object> map = new HashMap<>(16);
        List<SysOrg> subjectOrgs = ShiroUtil.getSubjectOrgs();

        List<SysOrgVo> subjectOrgs2 = new ArrayList<>();
        //添加组织下面所有ip段
        for (SysOrg subjectOrg : subjectOrgs) {
            subjectOrgs2.addAll(netService.selectIps(subjectOrg));
        }
        //添加组织
        for (SysOrg subjectOrg : subjectOrgs) {
            subjectOrgs2.add(netService.getSysOrgVo(subjectOrg));
        }
        //查询未分配组织的ip段 (pid为"未知组织")
        subjectOrgs2.addAll(netService.selectOldIp());

        map.put("id", org.getId());
        map.put("title", org.getTitle());
        map.put("orgList", subjectOrgs2);

        return ResultVoUtil.success(ResultEnum.SUCCESS.getMessage(), map);
    }


    /**
     * 查询相应组织的IP段
     *
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    @ApiOperation(value = "查询相应组织的IP段")
    @RequiresPermissions({"api:ip:queryNet"})
    @PostMapping("/queryNet")
//    @ActionLog(name = "查看IP管理", title = "IP管理", key = LogTypeConstant.QUERY)
    ResultVo pageQuery(@RequestBody SysOrg org) {
        //是否是指定组织
        List<NetWorkAddress> records = new ArrayList<NetWorkAddress>();
        if (ObjectUtil.isNotNull(org.getType()) && org.getType() != 99) {
            records = netService.selectIpById(org);

        } else if (ObjectUtil.isNotNull(org.getType()) && org.getType() == 99) {
            records.add(netService.getById(org.getId()));

        }


        List<NetWorkAddressVo> netWorkAddressVos = new ArrayList<NetWorkAddressVo>();
        for (NetWorkAddress record : records) {
            NetWorkAddressVo netWorkAddressVo = new NetWorkAddressVo();
            //以往数据的兼容
            String netWorkId = record.getId();
            if (ObjectUtil.isNull(record.getMask())) {
                record.setMask(AppIpUtil.getMaskByMaskBit(record.getMaskNumber()));
            }
            netWorkAddressVo.setId(record.getId());
            netWorkAddressVo.setMask(record.getMask());
            netWorkAddressVo.setGateway(record.getGateway());
            netWorkAddressVo.setName(record.getName());
            try {
                netWorkAddressVo.setOrgName(orgService.getById(record.getOrgId()).getTitle());
            } catch (Exception e) {
                netWorkAddressVo.setOrgName("未分配组织");
            }


            // ip通已申请
            QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
            queryWrapper.eq("PING_STATUS", IpPingStatusEnum.USED.getCode());
            queryWrapper.eq("AUTH_STATUS", IpAuthStatusEnum.YES.getCode());
            queryWrapper.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUsedAndFinish(ipMsgService.count(queryWrapper));

            // ip通未申请
            QueryWrapper<IpInfo> queryWrapper2 = new QueryWrapper<IpInfo>();
            queryWrapper2.eq("PING_STATUS", IpPingStatusEnum.USED.getCode());
            queryWrapper2.eq("AUTH_STATUS", IpAuthStatusEnum.NO.getCode());
            queryWrapper2.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUsedAndunFinish(ipMsgService.count(queryWrapper2));

            // ip不通已申请
            QueryWrapper<IpInfo> queryWrapper3 = new QueryWrapper<IpInfo>();
            queryWrapper3.eq("PING_STATUS", IpPingStatusEnum.UNUSED.getCode());
            queryWrapper3.eq("AUTH_STATUS", IpAuthStatusEnum.YES.getCode());
            queryWrapper3.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUnusedAndFinish(ipMsgService.count(queryWrapper3));

            // ip不通未申请
            QueryWrapper<IpInfo> queryWrapper4 = new QueryWrapper<IpInfo>();
            queryWrapper4.eq("PING_STATUS", IpPingStatusEnum.UNUSED.getCode());
            queryWrapper4.eq("AUTH_STATUS", IpAuthStatusEnum.NO.getCode());
            queryWrapper4.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUnusedAndunFinish(ipMsgService.count(queryWrapper4));


            // ip通
            QueryWrapper<IpInfo> queryWrapper5 = new QueryWrapper<IpInfo>();
            queryWrapper5.eq("PING_STATUS", IpPingStatusEnum.USED.getCode());
            queryWrapper5.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUsed(ipMsgService.count(queryWrapper5));

            // ip不通
            QueryWrapper<IpInfo> queryWrapper6 = new QueryWrapper<IpInfo>();
            queryWrapper6.eq("PING_STATUS", IpPingStatusEnum.UNUSED.getCode());
            queryWrapper6.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUnused(ipMsgService.count(queryWrapper6));

            BigDecimal ipCount = new BigDecimal(netWorkAddressVo.getUnused()).add(new BigDecimal(netWorkAddressVo.getUsed()));

            // 占用百分比
            BigDecimal situationUse = new BigDecimal(netWorkAddressVo.getUsed()).divide(ipCount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            netWorkAddressVo.setSituationUse(situationUse.setScale(2, RoundingMode.HALF_UP).doubleValue());

            netWorkAddressVos.add(netWorkAddressVo);
        }

        return ResultVoUtil.success(netWorkAddressVos);


    }

    /**
     * 查询已配置的IP网络
     *
     * @return
     */
    @ApiOperation(value = "检测网络内的IP是否ping完成 data:1完成，data:0为未完成")
    @PostMapping("/pingOk/{netId}")
    ResultVo<?> pingOk(@PathVariable("netId") String netId) {
        if (StrUtil.isEmpty(netId)) {
            return ResultVoUtil.success(1);
        }
        Object object = redisServ.get(BATCH_PING_FLG + netId);
        if (Objects.isNull(object)) {
            return ResultVoUtil.success(1);
        } else {
            return ResultVoUtil.success(0);
        }
    }

    /**
     * 查询已配置的IP网络
     *
     * @return
     */
    @ApiOperation(value = "ping网络内所有的IP")
    @PostMapping("/pingNet/{netId}")
    ResultVo<?> pingNet(@PathVariable("netId") String netId) {
        if (ObjectUtil.isNull(netId)) {
            return ResultVoUtil.success("无可检测Ip");
        }
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
        queryWrapper.eq("NET_WORK_ADDRESS_ID", netId);
        List<IpInfo> ipInfoList = ipMsgService.list(queryWrapper);
        if (Objects.isNull(ipInfoList) || ipInfoList.isEmpty()) {
            return ResultVoUtil.success("发起成功");
        }
        List<String> ipList = ipInfoList.stream().map(IpInfo::getIp).collect(Collectors.toList());

        Object pingIpList = redisServ.get(BATCH_PING_FLG + netId);
        if (Objects.nonNull(pingIpList)) {
            return ResultVoUtil.success("发起成功");
        }

        ipMsgService.asyncPing(netId, ipList);
        redisServ.set(BATCH_PING_FLG + netId, ipList, 3600L);

        return ResultVoUtil.success("发起成功");
    }

    /**
     * ip详情
     */
    @ApiOperation(value = "查询Ip详情")
    @PostMapping("/getIp")
    @ActionLog(name = "查看IP详情", title = "IP管理", key = LogTypeConstant.QUERY)
    ResultVo getIp(@RequestBody IpInfoVo req) {
        if (ObjectUtil.isNull(req.getId())) {
            return ResultVoUtil.success();
        }
        IpInfo ipInfo = ipMsgService.getById(req.getId());
        req.setPingStatus(ipInfo.getPingStatus());
        req.setPingDate(ipInfo.getPingDate());
        Asset asset = assetService.findOneByIp(ipInfo.getIp());

        if (Objects.nonNull(asset)) {
//            if (ObjectUtil.isNotNull(asset.getStatus()) && asset.getStatus() == (byte) 1) {
//                req.setPingStatus(IpPingStatusEnum.USED.getCode());
//            }

            req.setStatus(IpStatusEnum.MONITOR.getCode());
            req.setAssetMode(asset.getAssetMode());
            req.setAssetName(asset.getName());
            String macAddress = ipMsgService.getMacAddress(ipInfo);
            req.setMac(macAddress);
        } else {
            req.setStatus(IpStatusEnum.ALLOT.getCode());
        }
        req.setPingStatusStr(IpPingStatusEnum.getMsgByCode(req.getPingStatus()));
        req.setStatusStr(IpStatusEnum.getMsgByCode(req.getStatus()));
        return ResultVoUtil.success(req);
    }

    /**
     * 查询ip
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "查询网络下的Ip")
    @RequiresPermissions({"api:ip:queryIp"})
    @PostMapping("/queryIp")
    @ActionLog(name = "查看IP列表", title = "IP管理", key = LogTypeConstant.QUERY)
    ResultVo pageQueryIp(@RequestBody IpPageReq req) {
        if (ObjectUtil.isNull(req.getNetWorkId())) {
            ResultVoUtil.success();
        }
        IPage<IpInfo> iPage = PagePlugin.startPageT(req.getPage(), req.getSize(), IpInfo.class);
        String ip = req.getIp();
        String netWorkId = req.getNetWorkId();
        NetWorkAddress netWork = netService.getById(netWorkId);
        ip = getReqIp(ip, netWork);
        req.setIp(ip);
        QueryWrapper<IpInfo> queryWrapper = baseService.getEqQueryWrapper(req, new IpInfo());
        queryWrapper.eq("NET_WORK_ADDRESS_ID", netWorkId);
        queryWrapper.orderByAsc("RANK");

        IPage<IpInfo> page = ipMsgService.page(iPage, queryWrapper);
        List<IpInfo> records = page.getRecords();
        List<IpInfoVo> copyList = new ArrayList<IpInfoVo>();
        for (IpInfo ipMsg : records) {
            IpInfoVo copy = EntityBeanUtil.copy(ipMsg, IpInfoVo.class);
            if (ipMsg.getPingStatus() == (byte) 1) {
                copy.setPingStatus(IpPingStatusEnum.USED.getCode());
            }
            Asset asset = assetService.findOneByIp(ipMsg.getIp());
            if (Objects.nonNull(asset)) {
                copy.setStatus(IpStatusEnum.MONITOR.getCode());
                copy.setAssetName(asset.getName());
                copy.setAssetMode(asset.getAssetMode());
//                if (ipMsg.getPingStatus() != (byte) 1 && ObjectUtil.isNotNull(asset.getStatus()) && asset.getStatus() == (byte) 1) {
//                    copy.setPingStatus(IpPingStatusEnum.USED.getCode());
//                    ipMsgService.updatePingStatus(ipMsg.getIp(), IpPingStatusEnum.USED);
//                }
            } else {
                copy.setStatus(IpStatusEnum.ALLOT.getCode());
            }
            copy.setStatusStr(IpStatusEnum.getMsgByCode(copy.getStatus()));
            copy.setAuthStatusStr(IpAuthStatusEnum.getMsgByCode(ipMsg.getAuthStatus()));
            copy.setPingStatusStr(IpPingStatusEnum.getMsgByCode(copy.getPingStatus()));
            copyList.add(copy);
        }

        PageBean<IpInfoVo> pageBean = new PageBean<>();
        pageBean.setContent(copyList);
        pageBean.setTotal(page.getTotal());

        return ResultVoUtil.success(pageBean);
    }

    /**
     * 匹配筛选条件，如果输入10则替换ip最后一位
     *
     * @param ip
     * @param netWork
     * @return
     */
    private String getReqIp(String ip, NetWorkAddress netWork) {
        if (Objects.nonNull(netWork)) {
            String netIp = netWork.getNetIp();
            String[] split = netIp.split("\\.");
            if (StrUtil.isNotEmpty(ip)) {
                if (!ip.contains(".")) {
                    ip = split[0] + "." + split[1] + "." + split[2] + "." + ip;
                }
            }
        }
        return ip;
    }

    /**
     * 新建网络
     *
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @ApiOperation(value = "新建网络")
    @RequiresPermissions({"api:ip:saveNetWork"})
    @PostMapping("/saveNetWork")
    @ActionLog(name = "新建网络", title = "IP管理", key = LogTypeConstant.ADD)
    ResultVo<String> saveNetWork(@Validated @RequestBody SaveNetWorkReq req) {
        req.setNetIp(req.getGateway());
        int num = AppIpUtil.getMaskBitByMask(req.getMask());
        if (num == 0) {
            return ResultVoUtil.paramError("请输入255.255.255.240 到 255.255.255.252之内的正确掩码格式", String.class);
        }
        req.setMaskNumber(num);
        try {
            return netService.createNetWork(req);
        } catch (Exception e) {
            log.error("IP管理-新建网络失败：{}", e.getMessage(), e);
            return ResultVoUtil.error("保存失败：" + e.getMessage());
        }

    }

    /**
     * 编辑网络
     *
     * @param req
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "编辑网络")
    @RequiresPermissions({"api:ip:editNetWork"})
    @PostMapping("/editNetWork")
    @ActionLog(name = "编辑网络", title = "IP管理", key = LogTypeConstant.MODIFY)
    ResultVo editNetWork(@Validated @RequestBody EditNetWorkReq req) {
        NetWorkAddress net = netService.getById(req.getId());
        if (Objects.isNull(net)) {
            return ResultVoUtil.paramError("网络Id不存在", String.class);
        }
        if (ObjectUtil.isNull(req.getOrgId())) {
            return ResultVoUtil.paramError("组织ID不可为空", String.class);
        }
        //net.setMask(AppIpUtil.getMaskByMaskBit(net.getMaskNumber()));

        //组织归属有改动
        if (!req.getOrgId().equals(net.getOrgId())) {
            //验证是否满足
            ResultVo<String> resultVo = netService.verificationNet(net.getGateway(), net.getMask(), req.getId());
            if (resultVo.getCode().equals(ResultEnum.ERROR.getCode())) {
                return resultVo;
            }
        }

        NetWorkAddress netNew = EntityBeanUtil.replaceParameter(req, net, NetWorkAddress.class);
        if (netNew.getOrgId().equals("0")) {
            netNew.setOrgId(null);
        }
        netService.updateById(netNew);
        return ResultVoUtil.SAVE_SUCCESS;
    }

    /**
     * 删除网络
     *
     * @param ids
     * @return
     */
    @ApiOperation(value = "删除网络")
    @RequiresPermissions({"api:ip:removeNetWork"})
    @PostMapping("/removeNetWork/{id}")
    @ActionLog(name = "删除网络", title = "IP管理", key = LogTypeConstant.REMOVEE)
    ResultVo<String> removeNet(@PathVariable("id") String ids) {
        if (StrUtil.isEmpty(ids)) {
            return ResultVoUtil.paramError("请传入需要删除的Id", String.class);
        }
        NetWorkAddress net = netService.getById(ids);
        if (Objects.isNull(net)) {
            return ResultVoUtil.paramError("该网络不存在", String.class);
        }
        netService.removeNet(net);

        return ResultVoUtil.REMOVE_SUCCESS;

    }

    /**
     * 删除IP
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "删除IP")
    @RequiresPermissions({"api:ip:removeIp"})
    @PostMapping("/removeIp/{id}")
    @ActionLog(name = "删除IP", title = "IP管理", key = LogTypeConstant.REMOVEE)
    ResultVo removeIp(@PathVariable("id") String ids) {
        if (StrUtil.isEmpty(ids)) {
            return ResultVoUtil.paramError("请传入需要删除的Id", String.class);
        }
        IpInfo ipMsg = ipMsgService.getById(ids);
        if (Objects.isNull(ipMsg)) {
            return ResultVoUtil.paramError("该IP不存在", String.class);
        }

        ipMsgService.removeById(ids);

        return ResultVoUtil.REMOVE_SUCCESS;
    }

    /**
     * IP绑定设备 信息查询
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "IP绑定设备信息查询")
    @PostMapping("/getIpMsg/{ip}")
    ResultVo getIpMsg(@PathVariable("ip") String ip) {
        if (StrUtil.isEmpty(ip)) {
            return ResultVoUtil.paramError("请传入需要查询详情的ip", String.class);
        }
        AssetMsgVo msg = assetService.findMsgByIp(ip);
        if (Objects.isNull(msg)) {
            return ResultVoUtil.paramError("没有发现该Ip下的资产", String.class);
        }
        String uIndex = assetService.getUIndex(msg.getId());
        if (Objects.nonNull(uIndex)) {
            uIndex = uIndex.replace("_", "U_");
            msg.setPositionStr(uIndex + "U");
        }

        return ResultVoUtil.success(msg);
    }

    /**
     * ip检测
     *
     * @param req
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "IP检测")
    @RequiresPermissions({"api:ip:detectionIp"})
    @PostMapping("/detectionIp")
    @ActionLog(name = "IP检测", title = "IP管理", key = LogTypeConstant.QUERY)
    ResultVo detectionIp(@Validated @RequestBody DetectionIpReq req) {
        List<DetectionVo> detectionIps = ipMsgService.detectionIps(req.getIds());
        return ResultVoUtil.success(detectionIps);
    }


    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "编辑IP信息")
    @RequiresPermissions({"api:ip:updateIp"})
    @PostMapping("/updateIp")
    @ActionLog(name = "修改IP信息", title = "IP管理", key = LogTypeConstant.MODIFY)
    ResultVo updateIp(@Validated @RequestBody UpdateIpReq req) {
        IpInfo ipInfo = ipMsgService.getById(req.getId());
        if (Objects.isNull(ipInfo)) {
            return ResultVoUtil.error("该Id对应的IP不存在");
        }
        IpInfo updateEntity = EntityBeanUtil.replaceParameter(req, ipInfo, IpInfo.class);
        if (IpAuthStatusEnum.YES.getCode().equals(req.getAuthStatus())) {
            SysUser user = ShiroUtil.getSubject();
            updateEntity.setAuthDate(new Date());
            updateEntity.setAuthUserId(user.getId());
        } else {
            updateEntity.setAuthDate(null);
        }

        ipMsgService.updateById(updateEntity);

        return ResultVoUtil.success("处理成功");
    }


}
