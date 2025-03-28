package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zhaozheng
 * @description TODO
 * @className AssetIpAddd 资产信息IP、或者管理口IP和资产ip的相关产讯功能
 * @date 2023/11/24 16:16
 * @since 2.1.0.0
 */
@Service
public class AssetIpAdd {
    @Resource
    RedisService redisService;
    @Resource
    AssetService assetService;

    private Map<String, String> ipMap = new ConcurrentHashMap<>(256);

    /**
     * 通用资产Ip的添加功能
     * 由于一些信息不携带资产IP需要，此方法用户添加资产的IP
     * 如果缓存中存在就使用缓存中的资产IP，如果不存在使用数据库中的资产IP
     *
     * @param commonEntity
     */
    public void setAssetIp(CommonEntity commonEntity) {
        try {
            if (ipMap.get(commonEntity.getAssetId()) == null) {
                Asset asset = assetService.getById(commonEntity.getAssetId());
                ipMap.put(commonEntity.getAssetId(), asset.getIp());
            }
            commonEntity.setAssetIp(ipMap.get(commonEntity.getAssetId()));

            if (StrUtil.isEmpty(commonEntity.getCollectCode())) {
                commonEntity.setCollectCode(MyIdUtil.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过管理口IP查询系统中对应的资产信息（用于添加资产的IP和ID信息）
     *
     * @param syslogEventInfoEntity
     */
    public void getAssetIPbyIMM(SyslogEventInfoEntity syslogEventInfoEntity) {
        Asset asset = assetService.findOneByIp(syslogEventInfoEntity.getIp());
        if (asset != null) {
            syslogEventInfoEntity.setAssetId(asset.getId());
            syslogEventInfoEntity.setAssetIp(asset.getIp());
        }

    }

    /**
     * 通过管理口IP查询系统中对应的资产信息（用于添加资产的IP和ID信息）
     *
     * @param commonEntity
     */
    public void getAssetIPbyIMM(CommonEntity commonEntity) {
        Asset asset = assetService.findOneByIp(commonEntity.getIp());
        if (asset != null) {
            commonEntity.setAssetId(asset.getId());
            commonEntity.setAssetIp(asset.getIp());
        }

    }

    /**
     * 通过采集上来的信息，配置资产的ID
     *
     * @param commonEntity
     */
    public void getAssetId(CommonEntity commonEntity) {

        List<String> listKey = redisService.getKeyByPattern("*" + commonEntity.getAssetIp() + ":" + StatusInfoChangeTypeEnum.status.getCode());
        if (!listKey.isEmpty()) {
            for (String key : listKey) {
                String[] keys = key.split(":");
                if (keys[0] != null && !keys[0].equals("null")) {
                    commonEntity.setAssetId(keys[1]);
                    break;
                }
            }
        } else {
            Asset asset = assetService.findOneByIp(commonEntity.getAssetIp());
            commonEntity.setAssetId(asset.getId());
        }


    }
}
