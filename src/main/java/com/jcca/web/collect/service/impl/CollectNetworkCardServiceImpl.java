package com.jcca.web.collect.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.dao.CollectNetworkCardMapper;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 采集网卡信息
 *
 * @author Lvyp
 */
@Service
public class CollectNetworkCardServiceImpl extends ServiceImpl<CollectNetworkCardMapper, CollectNetworkCard>
        implements CollectNetworkCardService {

    private static final String CACHE_KEY = "NETWORK:TAB:KEY:";
    private static final String ALARM_KEY = "NETWORK:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectNetworkCardMapper collectNetMapper;
    @Resource
    private AssetService assetServ;
    @Resource
    private CollectInterfacesService interfaceServ;

    @Override
    public void updateRealTimeData(List<CollectNetworkCard> entityList) {
        CollectNetworkCard net = entityList.get(0);
        String key = CACHE_KEY + net.getAssetId();
        redisService.set(key, JSONUtil.parseArray(entityList));
    }

    @Override
    public String getAlarmCode(CollectNetworkCard net) {
        return ALARM_KEY + net.toString() + "_OFF_ON";
    }

    @Override
    public List<CollectNetworkCard> getRealTimeData(String assetId) {
        List<CollectNetworkCard> realTimeData = collectNetMapper.selectRealTimeData(assetId);
        return realTimeData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchByAssetId(List<CollectNetworkCard> entityList) {
        Assert.isTrue(!entityList.isEmpty(), "updateBatchByAssetId mast be not null");
        String assetId = entityList.get(0).getAssetId();
        QueryWrapper<CollectNetworkCard> wrapper = new QueryWrapper<CollectNetworkCard>();
        wrapper.eq("ASSET_ID", assetId);
        collectNetMapper.delete(wrapper);
        saveBatch(entityList);
    }

    @Override
    public CollectNetworkCard findByMacAddress(String atPhysAddress) {
        if (StrUtil.isEmpty(atPhysAddress)) {
            return null;
        }
        CollectNetworkCard card = collectNetMapper.selectByMacAddress(atPhysAddress);

        return card;
    }

    @Override
    public CollectNetworkCard findByNetworkName(String networkName,String assetId) {
        return collectNetMapper.findByNetworkName(networkName,assetId);
    }

    @Override
    public List<CollectNetworkCard> selectByMacAddressAndAssetId(String macAddr, String assetId) {

        return collectNetMapper.selectByMacAddressAndAssetId(macAddr, assetId);
    }

    @Override
    public Integer updateNetCardStatus(String assetId, String ip, CollectNetCardStatus status) {

        if (CollectNetCardStatus.UP == status) {
            return collectNetMapper.updateNetCardStatus(assetId, ip, status.getCode(), CollectNetCardStatus.DOWN.getCode());
        } else if (CollectNetCardStatus.DOWN == status) {
            return collectNetMapper.updateNetCardStatus(assetId, ip, status.getCode(), CollectNetCardStatus.UP.getCode());
        }

        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByAssetId(CollectNetworkCard net) {
        QueryWrapper<CollectNetworkCard> query = new QueryWrapper<CollectNetworkCard>();
        query.eq("ASSET_ID", net.getAssetId());
        query.eq("NAME", net.getName());
        this.remove(query);
        save(net);
    }

    @Override
    public CollectNetworkCard getOneByIp(String linkAssetIp) {
        QueryWrapper<CollectNetworkCard> query = new QueryWrapper<CollectNetworkCard>();
        query.eq("IP", linkAssetIp);
        List<CollectNetworkCard> list = list(query);
        if(Objects.nonNull(list) && !list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

}
