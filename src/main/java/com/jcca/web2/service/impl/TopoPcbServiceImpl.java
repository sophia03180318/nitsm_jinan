package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.entity.TopoAssetPortPic;
import com.jcca.admin.system.entity.TopoAssetPortVlan;
import com.jcca.admin.system.service.TopoAssetPortPicService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.service.TopoAssetPortVlanService;
import com.jcca.web2.dao.TopoPcbMapper;
import com.jcca.web2.entity.TopoPcb;
import com.jcca.web2.service.TopoPcbService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: 自定义板卡
 * @author: Lvyp
 * @create: 2024/01/10 14:08
 */
@Service
public class TopoPcbServiceImpl extends ServiceImpl<TopoPcbMapper, TopoPcb> implements TopoPcbService  {

    @Resource
    private TopoAssetPortService assetPortServ;
    @Resource
    private TopoAssetPortVlanService topoAssetPortVlanService;
    @Resource
    private TopoAssetPortPicService topoAssetPortPicService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeEntity(TopoPcb pcb) {
        QueryWrapper<TopoAssetPort> portDelQuery = new QueryWrapper<TopoAssetPort>();
        portDelQuery.eq("PCB_ID",pcb.getPcbId());
        assetPortServ.remove(portDelQuery);

        QueryWrapper<TopoAssetPortVlan> vlanDelQuery = new QueryWrapper<TopoAssetPortVlan>();
        vlanDelQuery.eq("PCB_ID",pcb.getPcbId());
        topoAssetPortVlanService.remove(vlanDelQuery);

        QueryWrapper<TopoAssetPortPic> picDelQuery = new QueryWrapper<TopoAssetPortPic>();
        picDelQuery.eq("PCB_ID",pcb.getPcbId());
        topoAssetPortPicService.remove(picDelQuery);

        removeById(pcb.getPcbId());
    }
}
