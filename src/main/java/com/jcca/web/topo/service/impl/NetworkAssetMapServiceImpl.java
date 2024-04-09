package com.jcca.web.topo.service.impl;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ValidatorUtils;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.topo.controller.TopoFandMapController;
import com.jcca.web.topo.dao.NetworkAssetMapMapper;
import com.jcca.web.topo.entity.NetworkAssetMap;
import com.jcca.web.topo.service.NetworkAssetMapService;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * topo自动发现
 *
 * @author lyp
 */
@Slf4j
@Service
public class NetworkAssetMapServiceImpl extends ServiceImpl<NetworkAssetMapMapper, NetworkAssetMap>
        implements NetworkAssetMapService {

    private static final List<String> MIB_LIST = Arrays.asList(".1.3.6.1.4.1.9.9.23.1.2.1.1.4",
            ".1.3.6.1.4.1.9.9.23.1.2.1.1.7", ".1.3.6.1.4.1.9.9.23.1.2.1.1.6");
    private static final String GET_MIB = ".1.3.6.1.4.1.9.9.23.1.3.4";

    @Resource
    private OutService outServ;
    @Resource
    private NetworkAssetMapMapper mapMapper;
    @Resource
    private AssetService assetServ;
    @Resource
    private RedisService redisServ;
    @Resource
    private CollectInterfacesService collectInterfaceServ;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beginSeach(BusinessGetSnmpResultReq req) throws Exception {
        beginFind(req);
    }

    private void beginFind(BusinessGetSnmpResultReq req) throws Exception {
        String validateReq = ValidatorUtils.validateReq(req);
        if (StrUtil.isNotEmpty(validateReq)) {
            log.error("TOPO自动发现校验错误：" + validateReq);
            return;
        }

        // 查询这个IP 2分钟内是否发现过
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -2);
        Date time = calendar.getTime();
        QueryWrapper<NetworkAssetMap> queryWrapper = new QueryWrapper<NetworkAssetMap>();
        queryWrapper.eq("LOCALHOST_IP", req.getIp());
        queryWrapper.ge("UPDATE_DATE", time);
        List<NetworkAssetMap> selectList = mapMapper.selectList(queryWrapper);

        if (Objects.nonNull(selectList) && selectList.size() > 0) {
            log.info("该设备近30分钟内存在TOPO发现的记录：{}", req.getIp());
            return;
        }

        // 采集设备ID
        String localDectiveId = "";
        req.setMib(GET_MIB);
        req.setType(BusinessGetSnmpResultReq.WALK);

        // 收到中断信号退出程序
        if (TopoFandMapController.END_TOPO_FAND) {
            log.info("强制结束TOPO发现");
            return;
        }

        try {
            BusinessGetSnmpResultResp snmpResult = outServ.getSnmpResult(req);

            if (BusinessGetSnmpResultResp.ERROR.equals(snmpResult.getCode())) {
                log.error("TOPO发现设备ID采集失败" + snmpResult.getMsg());
                return;
            }

            List<SnmpExecuteResult> resultList = snmpResult.getResultList();
            if (resultList.isEmpty()) {
                log.error("TOPO发现设备ID采集返回空列表：" + JSONUtil.toJsonStr(req));
                return;
            }
            SnmpExecuteResult snmpExecuteResult = resultList.get(0);
            localDectiveId = snmpExecuteResult.getValue();
        } catch (Exception e) {
            log.error("TOPO发现设备ID采集失败" + e.getMessage(), e);
            return;
        }

        Map<String, NetworkAssetMap> cacheMap = new HashedMap<String, NetworkAssetMap>();

        Date date = new Date();

        for (int i = 0; i < MIB_LIST.size(); i++) {
            String mib = MIB_LIST.get(i);
            req.setMib(mib);
            req.setType(BusinessGetSnmpResultReq.WALK);

            // 收到中断信号退出程序
            if (TopoFandMapController.END_TOPO_FAND) {
                log.info("强制结束TOPO发现");
                return;
            }

            try {
                BusinessGetSnmpResultResp snmpResult = outServ.getSnmpResult(req);
                if (BusinessGetSnmpResultResp.ERROR.equals(snmpResult.getCode())) {
                    continue;
                }
                List<SnmpExecuteResult> resultList = snmpResult.getResultList();

                for (SnmpExecuteResult item : resultList) {
                    String oid = item.getOid();
                    String value = item.getValue();

                    String key = oid.replace(mib.replaceFirst(".", ""), "").trim();

                    NetworkAssetMap cacheItem = cacheMap.get(key);
                    if (i == 0 && Objects.isNull(cacheItem)) {
                        String ip = getIp(value);
                        if (StrUtil.isEmpty(ip)) {
                            continue;
                        }
                        String assetIp = this.findAssetIpByRemoteIp(ip);
                        if (StrUtil.isNotEmpty(assetIp)) {
                            ip = assetIp;
                        }

                        cacheItem = new NetworkAssetMap();
                        cacheItem.setId(MyIdUtil.getId());
                        cacheItem.setOidIndex(key);
                        cacheItem.setLocalhostIp(req.getIp());
                        cacheItem.setRemoteIp(ip);
                        cacheItem.setLocalDeviceId(localDectiveId);
                        cacheMap.put(key, cacheItem);
                    } else if (i == 1 && Objects.nonNull(cacheItem)) {
                        cacheItem.setRemotePort(value);
                        cacheItem.setUpdateDate(date);
                        cacheMap.put(key, cacheItem);
                    } else if (i == 2 && Objects.nonNull(cacheItem)) {
                        cacheItem.setRemoteDeviceId(value);
                        cacheMap.put(key, cacheItem);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        Collection<NetworkAssetMap> values = cacheMap.values();
        if (Objects.nonNull(values) && values.size() > 0) {
            QueryWrapper<NetworkAssetMap> removeWrapper = new QueryWrapper<NetworkAssetMap>();
            removeWrapper.eq("LOCALHOST_IP", req.getIp());
            remove(removeWrapper);

            saveBatch(values);
        }
    }

    private String getIp(String value) {
        StrBuilder ip = new StrBuilder("");
        String[] split2 = value.split(":");
        for (int j = 0; j < split2.length; j++) {
            String ipStr = split2[j];
            try {
                Integer valueOf = Integer.valueOf(ipStr, 16);
                ip.append(valueOf);
                if (j != split2.length - 1) {
                    ip.append(".");
                }
            } catch (Exception e) {
                return "";
            }
        }

        return ip.toString();
    }

    @Override
    public void beginSeachAll() throws Exception {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.in("ASSET_MODE", Arrays.asList("42", "201"));
        queryWrapper.eq("IS_DEL", 1);
        queryWrapper.eq("WATCH", 1);
        List<Asset> list = assetServ.list(queryWrapper);
        for (Asset asset : list) {
            BusinessGetSnmpResultReq req = new BusinessGetSnmpResultReq();
            req.setCommunity(asset.getOsUser());
            req.setIp(asset.getIp());
            beginFind(req);
        }

        redisServ.remove(TopoFandMapController.TOPO_FAND_FLAG);

    }

    @Override
    public String findAssetIpByRemoteIp(String remoteIp) {
        Asset asset = assetServ.findOneByIp(remoteIp);
        if (Objects.nonNull(asset)) {
            return asset.getIp();
        }

        CollectInterfaces collectItem = collectInterfaceServ.findInterfaceByLinkIp(remoteIp);
        if (Objects.isNull(collectItem)) {
            return "";
        }

        Asset assetInfo = assetServ.getById(collectItem.getAssetId());
        if (Objects.isNull(assetInfo)) {
            return "";
        }
        return assetInfo.getIp();
    }

    @Override
    public Boolean verifyTopoFindStatus(String assetId) {
        Integer countByAssetId = mapMapper.countByAssetId(assetId);
        return 0 < countByAssetId;
    }

}
