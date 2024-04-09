package com.jcca.admin.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.TopoAssetPortPicMapper;
import com.jcca.admin.system.entity.TopoAssetPortPic;
import com.jcca.admin.system.service.TopoAssetPortPicService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author syt
 * @date 2021-07-15 13:53
 **/
@Service
public class TopoAssetPortPicServiceImpl extends ServiceImpl<TopoAssetPortPicMapper, TopoAssetPortPic>
        implements TopoAssetPortPicService {

    @Resource
    private TopoAssetPortPicMapper topoAssetPortPicMapper;

    @Override
    public Boolean deleteAssetPortPic(String assetId,String pcbId) {
        QueryWrapper<TopoAssetPortPic> queryWrapper = new QueryWrapper<TopoAssetPortPic>();
        queryWrapper.eq("ASSET_ID",assetId);
        if(StrUtil.isNotEmpty(pcbId)){
            queryWrapper.eq("PCB_ID",pcbId);
        }
        topoAssetPortPicMapper.delete(queryWrapper);
        return true;
    }

    @Override
    public List<TopoAssetPortPic> queryAssetPortPic(String assetId,String pcbId) {
        QueryWrapper<TopoAssetPortPic> queryWrapper = new QueryWrapper<TopoAssetPortPic>();
        queryWrapper.eq("ASSET_ID",assetId);
        if(StrUtil.isNotEmpty(pcbId)){
            queryWrapper.eq("PCB_ID",pcbId);
        }
        return list(queryWrapper);
    }

}