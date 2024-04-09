package com.jcca.web.collect.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.UnitEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.web.collect.dao.CollectDiskMapper;
import com.jcca.web.collect.entity.CollectDisk;
import com.jcca.web.collect.service.CollectDiskService;
import com.jcca.web.collect.service.bean.AssetDiskVo;
import com.jcca.web.collect.service.bean.DiskVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 磁盘信息
 *
 * @author Lvyp
 */
@Service
public class CollectDiskServiceImpl extends ServiceImpl<CollectDiskMapper, CollectDisk> implements CollectDiskService {

    private static final String DISK_KEY = "DISK:TAB:KEY:";
    private static final String DISK_ALARM = "DISK:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectDiskMapper diskMapper;

    @Override
    public void updateRealTimeData(List<CollectDisk> diskList) {
        CollectDisk disk = diskList.get(0);
        String key = DISK_KEY + disk.getAssetId();
        redisService.set(key, JSONUtil.parseArray(diskList));
    }

    @Override
    public List<CollectDisk> getRealTimeData(String assetId) {

        List<CollectDisk> diskMsgList = diskMapper.selectRealTimeData(assetId);
        return diskMsgList;
    }

    @Override
    public String getAlarmCode(CollectDisk disk) {

        return DISK_ALARM + disk.getAssetId() + disk.getMountPoint();
    }

    @Override
    public AssetDiskVo getAssetDiskMsg(String assetId, UnitEnum unit) {
        List<CollectDisk> realTimeData = this.getRealTimeData(assetId);
        if (realTimeData.isEmpty()) {
            return null;
        }
        Long total = realTimeData.stream().collect(Collectors.summingLong(CollectDisk::getTotal));
        Long scale = unit.getScale();

        AssetDiskVo diskVo = new AssetDiskVo();
        if (UnitEnum.AUTO == unit) {
            // 自适应单位
            diskVo.setDiskTotalStr(UnitEnum.AutoScale(total));
        } else {
            String diskTotalStr = AppMathUtil.div(total, scale, 2);
            diskVo.setDiskTotalStr(diskTotalStr + unit.name());
        }

        List<DiskVo> diskList = new ArrayList<DiskVo>();
        for (CollectDisk disk : realTimeData) {
            DiskVo vo = EntityBeanUtil.copy(disk, DiskVo.class);
            if (UnitEnum.AUTO == unit) {
                String autoTotal = UnitEnum.AutoScale(disk.getTotal());
                String autoUsed = UnitEnum.AutoScale(disk.getUsed());
                String autoFree = UnitEnum.AutoScale(disk.getFree());
                vo.setTotalStr(StrUtil.isEmpty(autoTotal) ? "0" : autoTotal);
                vo.setUsedStr(StrUtil.isEmpty(autoUsed) ? "0" : autoUsed);
                vo.setFreeStr(StrUtil.isEmpty(autoFree) ? "0" : autoFree);
            } else {
                vo.setTotalStr(AppMathUtil.div(disk.getTotal(), scale, 2) + unit.name());
                vo.setUsedStr(AppMathUtil.div(disk.getUsed(), scale, 2) + unit.name());
                vo.setFreeStr(AppMathUtil.div(disk.getFree(), scale, 2) + unit.name());
            }
            diskList.add(vo);
        }
        diskVo.setDiskList(diskList);
        return diskVo;
    }

    /**
     * 批量更新
     */
    @Transactional
    @Override
    public void updateBatchByAssetId(List<CollectDisk> diskList) {
        Assert.isTrue(!diskList.isEmpty(), "updateBatchByAssetId mast be not null");
        String assetId = diskList.get(0).getAssetId();
        QueryWrapper<CollectDisk> wrapper = new QueryWrapper<CollectDisk>();
        wrapper.eq("ASSET_ID", assetId);
        diskMapper.delete(wrapper);
        saveBatch(diskList);
    }

}
