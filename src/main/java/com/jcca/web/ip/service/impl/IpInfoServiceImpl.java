package com.jcca.web.ip.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.TestIpUtil;
import com.jcca.common.utils.constants.AppLogHead;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.dao.CollectNetworkCardMapper;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.ip.controller.IpController;
import com.jcca.web.ip.dao.IpInfoMapper;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.enums.IpPingStatusEnum;
import com.jcca.web.ip.enums.IpStatusEnum;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.ip.vo.DetectionVo;
import com.jcca.web2.vo.IpVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static cn.hutool.core.net.NetUtil.ping;

/**
 * ip信息
 *
 * @author lyp
 */
@Slf4j
@Service
public class IpInfoServiceImpl extends ServiceImpl<IpInfoMapper, IpInfo> implements IpInfoService {

    @Resource
    private IpInfoMapper ipMsgMapper;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectNetworkCardMapper networkMapper;
    @Resource
    private RedisService redisServ;
    @Resource
    AssetService assetService;


    @Override
    public List<DetectionVo> detectionIps(List<String> ids) {
        List<DetectionVo> detectionVos = new ArrayList<DetectionVo>();
        for (String id : ids) {
            IpInfo ipMsg = ipMsgMapper.selectById(id);
            DetectionVo detectionVo = new DetectionVo();
            detectionVo.setId(id);
            detectionVo.setPingStatus(IpPingStatusEnum.UNUSED.getMsg());
            if (Objects.isNull(ipMsg)) {
                detectionVo.setMsg("非本系统ip未执行ping");
            } else {
                Asset asset = assetServ.findOneByIp(ipMsg.getIp());
                if (Objects.isNull(asset)) {
                    ipMsg.setStatus(IpStatusEnum.ALLOT.getCode());
                } else {
                    ipMsg.setStatus(IpStatusEnum.MONITOR.getCode());
                }
                detectionVo.setMsg("ok");
                detectionVo.setIp(ipMsg.getIp());
                ipMsg.setPingStatus(IpPingStatusEnum.UNUSED.getCode());
                detectionVo.setPingStatus(IpPingStatusEnum.UNUSED.getMsg());
                if (ping(ipMsg.getIp())) {
                    ipMsg.setPingStatus(IpPingStatusEnum.USED.getCode());
                    detectionVo.setPingStatus(IpPingStatusEnum.USED.getMsg());
                }
                ipMsg.setPingDate(new Date());
            }
            ipMsgMapper.updateById(ipMsg);

            detectionVos.add(detectionVo);
        }
        return detectionVos;
    }

    @Override
    public Boolean examineIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("IP", ip);
        List<IpInfo> ipList = ipMsgMapper.selectList(queryWrapper);

        if (ipList.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public ResultVo<String> allocationIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return ResultVoUtil.paramError("ip不存在", String.class);
        }

        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("IP", ip);
        List<IpInfo> ipList = ipMsgMapper.selectList(queryWrapper);
        if (ipList.size() == 0) {
            return ResultVoUtil.paramError("该ip不可用", String.class);
        }
        String macAddress = TestIpUtil.getMacAddress(ip, true);
        for (IpInfo ipInfo : ipList) {
            ipInfo.setStatus(IpStatusEnum.MONITOR.getCode());
            // 检测mac地址
            if (StrUtil.isNotEmpty(macAddress)) {
                ipInfo.setMac(macAddress);
            }
            ipMsgMapper.updateById(ipInfo);
        }
        return ResultVoUtil.success("");
    }

    @Override
    @Transactional
    public void liberateIp(String ip) {
        log.info(AppLogUtils.logStr(AppLogHead.IP_MANAGER, "req", ip));
        if (StringUtils.isEmpty(ip)) {
            log.info(AppLogUtils.logStr(AppLogHead.IP_MANAGER, "恢复IP失败", ip + "空的IP"));
            return;
        }
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
        queryWrapper.eq("IP", ip);
        queryWrapper.in("STATUS", Arrays.asList(IpStatusEnum.MONITOR.getCode()));
        List<IpInfo> ipList = ipMsgMapper.selectList(queryWrapper);
        if (ipList.isEmpty()) {
            log.info(AppLogUtils.logStr(AppLogHead.IP_MANAGER, "恢复IP失败", ip + "状态已分配并监控的IP不存在"));
            return;
        }
        for (IpInfo ipInfo : ipList) {
            ipInfo.setStatus(IpStatusEnum.ALLOT.getCode());
            ipInfo.setMac("");
            ipMsgMapper.updateById(ipInfo);
        }
    }

    @Override
    public String getMacAddress(IpInfo ip) {
        String macAddress = "";
        List<CollectNetworkCard> findLasterMacAddressByIp = networkMapper.findLasterMacAddressByIp(ip.getIp());
        if (Objects.nonNull(findLasterMacAddressByIp) && !findLasterMacAddressByIp.isEmpty()) {
            macAddress = findLasterMacAddressByIp.get(0).getMacAddress();
        } else {
            // 网络设备通过IP扫描
            macAddress = "";
        }

        ip.setMac(macAddress);
        ipMsgMapper.updateById(ip);

        return macAddress;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMacAddress(String ip, String mac) {
        if (StrUtil.isEmpty(ip) || StrUtil.isEmpty(mac)) {
            return;
        }
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
        queryWrapper.eq("IP", ip);
        List<IpInfo> list = this.list(queryWrapper);
        List<IpInfo> updateList = new ArrayList<IpInfo>();
        for (IpInfo ipInfo : list) {
            if (mac.equals(ipInfo.getMac())) {
                continue;
            }
            ipInfo.setMac(mac);
            updateList.add(ipInfo);
        }
        if (!updateList.isEmpty()) {
            updateBatchById(updateList);
        }

    }

    /**
     * 更新ping状态
     */
    @Override
    public void updatePingStatus(String ip, IpPingStatusEnum pingStatus) {

        if (StrUtil.isEmpty(ip) || Objects.isNull(pingStatus)) {
            return;
        }
        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<IpInfo>();
        queryWrapper.eq("IP", ip);
        List<IpInfo> list = this.list(queryWrapper);
        List<IpInfo> updateList = new ArrayList<IpInfo>();
        for (IpInfo ipInfo : list) {
/*			if (pingStatus.getCode().equals(ipInfo.getPingStatus())) {
				continue;
			}*/
            ipInfo.setPingStatus(pingStatus.getCode());
            ipInfo.setPingDate(new Date());
            updateList.add(ipInfo);
        }
        if (!updateList.isEmpty()) {
            updateBatchById(updateList);
        }
    }

    @Override
    public List<IpInfo> listByIp(String ip) {
        if (StrUtil.isEmpty(ip)) {
            return new ArrayList<>();
        }
        List<IpInfo> selectByIp = ipMsgMapper.selectByIp(ip);
        if (Objects.isNull(selectByIp) || selectByIp.isEmpty()) {
            return new ArrayList<IpInfo>();
        }
        return selectByIp;
    }

    @Async
    @Override
    public void asyncPing(String netId, List<String> ipList) {
        for (String ip : ipList) {
            Boolean ping = null;
            try {
                ping = TestIpUtil.ping(ip, 3);
            } catch (IOException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.IP_MANAGE,ip,e);
                return;
            }
            if (ping) {
                updatePingStatus(ip, IpPingStatusEnum.USED);
            } else {
                updatePingStatus(ip, IpPingStatusEnum.UNUSED);
            }
        }
        if (StrUtil.isNotEmpty(netId)) {
            redisServ.remove(IpController.BATCH_PING_FLG + netId);
        }
    }

    @Override
    public List<IpVo> selectIPVoV2(String netWorkId) {
        return ipMsgMapper.selectIPVoV2(netWorkId);
    }


}
