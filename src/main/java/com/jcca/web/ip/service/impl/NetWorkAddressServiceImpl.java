package com.jcca.web.ip.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.dao.AssetMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.ip.controller.bean.SaveNetWorkReq;
import com.jcca.web.ip.dao.NetWorkAddressMapper;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.entity.NetWorkAddress;
import com.jcca.web.ip.enums.IpAuthStatusEnum;
import com.jcca.web.ip.enums.IpPingStatusEnum;
import com.jcca.web.ip.enums.IpStatusEnum;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.ip.service.NetWorkAddressService;
import com.jcca.web.ip.util.AppIpUtil;
import com.jcca.web.ip.vo.NetWorkAddressVo;
import com.jcca.web.ip.vo.SysOrgVo;
import com.jcca.web2.vo.LineNetWorkVo;
import com.jcca.web2.vo.NetObj;
import com.jcca.web2.vo.StationNetObj;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 网络设备
 *
 * @author
 */
@Service
public class NetWorkAddressServiceImpl extends ServiceImpl<NetWorkAddressMapper, NetWorkAddress> implements NetWorkAddressService {

    @Resource
    private NetWorkAddressMapper netWorkMapper;
    @Resource
    private IpInfoService ipService;
    @Resource
    private AssetMapper assetMapper;
    @Resource
    private BizManageService bizService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private IpInfoService ipMsgService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<String> createNetWork(SaveNetWorkReq req) throws Exception {

        String gateway = req.getGateway();
        String mask = req.getMask();
        //验证IP是否 符合条件
        ResultVo<String> resultVo = this.verificationNet(gateway, mask, null);
        if (resultVo.getCode().equals(ResultEnum.ERROR.getCode())) {
            return resultVo;
        }
        String startIp = AppIpUtil.getBeginIpStr(gateway, mask);
        String endIp = AppIpUtil.getEndIpStr(gateway, mask);
        req.setStartIp(startIp);
        req.setEndIp(endIp);
        NetWorkAddress netWork = BeanUtil.copyProperties(req, NetWorkAddress.class);

        List<String> ipList = AppIpUtil.getIPList(startIp, endIp);

        netWork.setId(MyIdUtil.getId());
        netWork.setIpNumber(ipList.size());

        List<IpInfo> entityList = new ArrayList<>();
        for (String ip : ipList) {
            IpInfo ipInfo = new IpInfo();
            ipInfo.setGateway(gateway);
            ipInfo.setIp(ip);
            ipInfo.setId(MyIdUtil.getId());
            ipInfo.setMask(mask);
            ipInfo.setNetWorkAddressId(netWork.getId());
            ipInfo.setRank(Long.valueOf(ip.replace(".", "")));
            ipInfo.setAuthStatus(IpAuthStatusEnum.NO.getCode());
            Asset asset = assetMapper.selectByIp(ip);
            ipInfo.setPingStatus(IpPingStatusEnum.UNUSED.getCode());
            if (Objects.nonNull(asset)) {
                ipInfo.setStatus(IpStatusEnum.MONITOR.getCode());
                if (asset.getStatus() == (byte) 1) {
                    ipInfo.setPingStatus(IpPingStatusEnum.USED.getCode());
                }
            } else {
                ipInfo.setStatus(IpStatusEnum.ALLOT.getCode());
            }
            entityList.add(ipInfo);

        }
        netWorkMapper.insert(netWork);
        bizService.save(netWork.getId(), BizManageConstant.NET);
        ipService.saveBatch(entityList);

        return ResultVoUtil.success("保存成功~");

    }

    @Override
    public ResultVo<String> verificationNet(String gateway, String mask, String id) {
        String startIp = AppIpUtil.getBeginIpStr(gateway, mask);
        String endIp = AppIpUtil.getEndIpStr(gateway, mask);
        //网关不可设置为 网络地址和广播地址
        if (gateway.equals(startIp)) {
            return ResultVoUtil.error("网关不可设置为此网段的网络地址");
        } else if (gateway.equals(endIp)) {
            return ResultVoUtil.error("网关不可设置为此网段的广播地址");
        }
        //对所有存储网段进行冲突校验
        List<NetWorkAddress> netWorkList = list();

        for (NetWorkAddress netWorkAddress : netWorkList) {
            if (ObjectUtil.isNull(id) || !netWorkAddress.getId().equals(id)) {
                //以往ip段的兼容
                if (ObjectUtil.isNull(netWorkAddress.getStartIp()) || ObjectUtil.isNull(netWorkAddress.getEndIp())) {
                    String gateway2 = netWorkAddress.getGateway();
                    String mask2 = AppIpUtil.getMaskByMaskBit(netWorkAddress.getMaskNumber());
                    netWorkAddress.setStartIp(AppIpUtil.getBeginIpStr(gateway2, mask2));
                    netWorkAddress.setEndIp(AppIpUtil.getEndIpStr(gateway2, mask2));
                    netWorkMapper.updateById(netWorkAddress);
                }
                String start = netWorkAddress.getStartIp();
                String end = netWorkAddress.getEndIp();
                String orgName = "";
                try {
                    orgName = orgService.getById(netWorkAddress.getOrgId()).getTitle();
                } catch (Exception e) {
                    orgName = "未知组织";
                }

                if (startIp == start || startIp == end || endIp == start || endIp == end) {
                    return ResultVoUtil.error("与" + orgName + "已存在的" + netWorkAddress.getName() + "网段冲突，如需添加请删除原有网段");
                } else if (compare(startIp, start) && !compare(startIp, end)) {
                    return ResultVoUtil.error("与" + orgName + "已存在的" + netWorkAddress.getName() + "网段冲突，如需添加请删除原有网段");
                } else if (compare(endIp, start) && !compare(endIp, end)) {
                    return ResultVoUtil.error("与" + orgName + "已存在的" + netWorkAddress.getName() + "网段冲突，如需添加请删除原有网段");
                } else if (!compare(startIp, start) && compare(endIp, end)) {
                    return ResultVoUtil.error("与" + orgName + "已存在的" + netWorkAddress.getName() + "网段冲突，如需添加请删除原有网段");
                }
            }
        }
        return ResultVoUtil.success();
    }

    @Transactional
    @Override
    public void removeNet(NetWorkAddress net) {
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
        queryWrapper.eq("NET_WORK_ADDRESS_ID", net.getId());

        List<IpInfo> ipList = ipService.list(queryWrapper);

        if (ipList.size() > 0) {
            List<String> ids = ipList.stream().map(IpInfo::getId).collect(Collectors.toList());
            List<List<String>> partition = Lists.partition(ids, 999);
            for (List<String> list : partition) {
                ipService.removeByIds(list);
            }
        }

        netWorkMapper.deleteById(net.getId());
        bizService.remove(net.getId(), BizManageConstant.NET);
    }


    // 1>2  返回true
    private boolean compare(String ip1, String ip2) {
        String[] ip1Array = ip1.split("\\.");
        String[] ip2Array = ip2.split("\\.");
        for (int i = 0; i < 4; i++) {
            if (Integer.parseInt(ip1Array[i]) > Integer.parseInt(ip2Array[i])) {
                return true;
            } else if (Integer.parseInt(ip1Array[i]) < Integer.parseInt(ip2Array[i])) {
                return false;
            }
        }
        return false;

    }


    //以组织模式返回指定组织下所有ip段
    public List<SysOrgVo> selectIps(SysOrg org) {
        List<SysOrgVo> ips2 = new ArrayList<SysOrgVo>();
        if (org.getType() == 2 || org.getType() == 4) {
            List<NetWorkAddress> ips = netWorkMapper.selectIps(org.getId());
            for (NetWorkAddress ip : ips) {
                SysOrgVo sysOrg = new SysOrgVo();
                sysOrg.setId(ip.getId());
                sysOrg.setTitle(ip.getName());
                sysOrg.setName(ip.getName());
                sysOrg.setGateway(ip.getGateway());
                sysOrg.setMask(ip.getMask());
                sysOrg.setRemark(ip.getRemark());

                if (ObjectUtil.isNotNull(ip.getOrgId())) {
                    sysOrg.setOrgName(getNameByOrgId(ip.getOrgId()));
                } else {
                    sysOrg.setOrgName("未知组织");
                }

                sysOrg.setType(99);
                sysOrg.setPid(ip.getOrgId());
                ips2.add(sysOrg);
            }
        }
        return ips2;

    }

    @Override
    public List<SysOrgVo> selectOldIp() {
        QueryWrapper<NetWorkAddress> queryWrapper = new QueryWrapper<NetWorkAddress>();
        queryWrapper.isNull("ORG_ID");
        List<NetWorkAddress> netWorkAddresses = netWorkMapper.selectList(queryWrapper);
        ArrayList<SysOrgVo> sysOrgs = new ArrayList<SysOrgVo>();
        for (NetWorkAddress netWorkAddress : netWorkAddresses) {
            SysOrgVo sysOrg = new SysOrgVo();
            sysOrg.setId(netWorkAddress.getId());
            sysOrg.setTitle(netWorkAddress.getName());
            sysOrg.setName(netWorkAddress.getName());
            sysOrg.setGateway(netWorkAddress.getGateway());
            sysOrg.setMask(netWorkAddress.getMask());
            sysOrg.setRemark(netWorkAddress.getRemark());
            sysOrg.setOrgName("未知组织");

            sysOrg.setPid("0");
            sysOrg.setType(99);
            sysOrgs.add(sysOrg);
        }
        return sysOrgs;
    }

    @Override
    public List<NetWorkAddress> selectIpById(SysOrg org) {

        List<NetWorkAddress> ips = new ArrayList<>();
        if (org.getType() == 2 || org.getType() == 4) {
            return netWorkMapper.selectIps(org.getId());
        } else if (org.getType() == 3) {//线
            List<SysOrg> Orgs = ShiroUtil.getSubjectOrgs();
            List<String> ids = Orgs.stream().filter(o -> o.getPid().equals(org.getId())).map(SysOrg::getId).collect(Collectors.toList());
            if (!ids.isEmpty()) {
                QueryWrapper<NetWorkAddress> qw = new QueryWrapper<>();
                qw.in("ORG_ID", ids);
                ips = netWorkMapper.selectList(qw);
            }
        }
        return ips;

    }

    @Override
    public List<NetWorkAddressVo> selectCenterIpByIdV2(SysOrg org) {
        List<NetWorkAddress> netWorkAddresses = netWorkMapper.selectIps(org.getId());
        List<NetWorkAddressVo> netWorkAddressVos = setNetWorkInfo(netWorkAddresses);
        return netWorkAddressVos;

    }

    @Override
    public ArrayList<LineNetWorkVo> selectLineIpByIdV2(SysOrg org) {
        List<SysOrg> Orgs = ShiroUtil.getSubjectOrgs();
        List<String> ids = Orgs.stream().filter(o -> o.getPid().equals(org.getId())).map(SysOrg::getId).collect(Collectors.toList());
        ArrayList<LineNetWorkVo> lineNetWorkVos = new ArrayList<>();
        if (!ids.isEmpty()) {
            QueryWrapper<NetWorkAddress> qw = new QueryWrapper<>();
            qw.in("ORG_ID", ids);
            List<NetWorkAddress> netWorkAddresses = netWorkMapper.selectList(qw);
            List<NetWorkAddressVo> netWorkAddressVos = setNetWorkInfo(netWorkAddresses);
            Map<String, List<NetWorkAddressVo>> collect = netWorkAddressVos.stream().collect(Collectors.groupingBy(NetWorkAddressVo::getOrgId));
            for (Map.Entry<String, List<NetWorkAddressVo>> kv : collect.entrySet()) {
                LineNetWorkVo lineNetWorkVo = new LineNetWorkVo();
                lineNetWorkVo.setOrgId(kv.getKey());
                lineNetWorkVo.setOrgName(kv.getValue().get(0).getOrgName());
                lineNetWorkVo.setNetWorkAddressList(kv.getValue());
                lineNetWorkVos.add(lineNetWorkVo);
            }
            return lineNetWorkVos;
        }
        return lineNetWorkVos;
    }


    private List<NetWorkAddressVo> setNetWorkInfo(List<NetWorkAddress> netWorkAddresses) {
        ArrayList<NetWorkAddressVo> netWorkAddressVos = new ArrayList<>();
        for (NetWorkAddress netWorkAddress : netWorkAddresses) {
            String netWorkId = netWorkAddress.getId();
            NetWorkAddressVo netWorkAddressVo = new NetWorkAddressVo();
            BeanUtils.copyProperties(netWorkAddress, netWorkAddressVo);
            SysOrg org = orgService.getById(netWorkAddress.getOrgId());
            netWorkAddressVo.setOrgName(org.getTitle());
            netWorkAddressVo.setOrgId(org.getId());
/*            // ip通已申请
            QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
            queryWrapper.eq("PING_STATUS", IpPingStatusEnum.USED.getCode());
            queryWrapper.eq("AUTH_STATUS", IpAuthStatusEnum.YES.getCode());
            queryWrapper.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUsedAndFinish(ipMsgService.count(queryWrapper));*/

            // ip通未申请
            QueryWrapper<IpInfo> queryWrapper2 = new QueryWrapper<IpInfo>();
            queryWrapper2.eq("PING_STATUS", IpPingStatusEnum.USED.getCode());
            queryWrapper2.eq("AUTH_STATUS", IpAuthStatusEnum.NO.getCode());
            queryWrapper2.eq("NET_WORK_ADDRESS_ID", netWorkId);
            netWorkAddressVo.setUsedAndunFinish(ipMsgService.count(queryWrapper2));

/*            // ip不通已申请
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
            netWorkAddressVo.setUnusedAndunFinish(ipMsgService.count(queryWrapper4));*/


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

            BigDecimal ipCount = new BigDecimal(netWorkAddressVo.getIpNumber());

            // 占用百分比
            BigDecimal situationUse = new BigDecimal(netWorkAddressVo.getUsed()).divide(ipCount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            netWorkAddressVo.setSituationUse(situationUse.setScale(2, RoundingMode.HALF_UP).doubleValue());
            netWorkAddressVos.add(netWorkAddressVo);
        }
        return netWorkAddressVos;

    }

    @Override
    public String getNameByOrgId(String id) {
        try {
            return orgService.getById(id).getTitle();
        } catch (Exception e) {
            return "未知组织";
        }
    }

    @Override
    public SysOrgVo getSysOrgVo(SysOrg sysOrg) {
        SysOrgVo sysOrgVo = new SysOrgVo();
        sysOrgVo.setTitle(sysOrg.getTitle());
        sysOrgVo.setType(sysOrg.getType());
        sysOrgVo.setId(sysOrg.getId());
        sysOrgVo.setPid(sysOrg.getPid());
        return sysOrgVo;
    }

    @Override
    public List<StationNetObj> getLineNetMsgV2(String lineOrgId) {
        List<StationNetObj> lineNetMsgV2 = netWorkMapper.getLineNetMsgV2(lineOrgId);
        for (StationNetObj stationNetObj : lineNetMsgV2) {
            List<NetObj> netWorkAddressList = stationNetObj.getNetWorkAddressList();
            if(Objects.isNull(netWorkAddressList)){
                continue ;
            }
            for (NetObj netObj : netWorkAddressList) {
                String netId = netObj.getNetId();
                // ip通
                QueryWrapper<IpInfo> queryWrapper5 = new QueryWrapper<IpInfo>();
                queryWrapper5.eq("PING_STATUS", IpPingStatusEnum.USED.getCode());
                queryWrapper5.eq("NET_WORK_ADDRESS_ID", netId);
                netObj.setUsed(ipMsgService.count(queryWrapper5));

                // ip不通
                QueryWrapper<IpInfo> queryWrapper6 = new QueryWrapper<IpInfo>();
                queryWrapper6.eq("PING_STATUS", IpPingStatusEnum.UNUSED.getCode());
                queryWrapper6.eq("NET_WORK_ADDRESS_ID", netId);
                netObj.setUnused(ipMsgService.count(queryWrapper6));
            }
        }
        return lineNetMsgV2;
    }

}
