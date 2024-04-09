package com.jcca.admin.system.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.TopoAssetPortVlanMapper;
import com.jcca.admin.system.entity.TopoAssetPortVlan;
import com.jcca.admin.system.service.TopoAssetPortVlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yu_chen
 * @date 2020-08-19 18:14
 **/
@Service
public class TopoAssetPortVlanServiceImpl extends ServiceImpl<TopoAssetPortVlanMapper, TopoAssetPortVlan> implements TopoAssetPortVlanService {

    @Resource
    private TopoAssetPortVlanMapper topoAssetPortVlanMapper;

    @Override
    public Boolean deleteAssetPortVlan(String assetId,String pcbId) {
        QueryWrapper<TopoAssetPortVlan> vlanQueryWrapper = new QueryWrapper<>();
        vlanQueryWrapper.eq("ASSET_ID",assetId);
        if(StrUtil.isNotEmpty(pcbId)){
            vlanQueryWrapper.eq("PCB_ID",pcbId);
        }

        topoAssetPortVlanMapper.delete(vlanQueryWrapper);
        return true;
    }

    @Override
    public List<TopoAssetPortVlan> queryAssetPortVlan(String assetId,String pcbId) {
        QueryWrapper<TopoAssetPortVlan> vlanQueryWrapper = new QueryWrapper<>();
        vlanQueryWrapper.eq("ASSET_ID",assetId);
        if(StrUtil.isNotEmpty(pcbId)){
            vlanQueryWrapper.eq("PCB_ID",pcbId);
        }
        return list(vlanQueryWrapper);
    }
}