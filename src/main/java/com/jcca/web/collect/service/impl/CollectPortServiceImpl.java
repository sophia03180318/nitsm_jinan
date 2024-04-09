package com.jcca.web.collect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.collect.dao.CollectPortMapper;
import com.jcca.web.collect.entity.CollectPort;
import com.jcca.web.collect.service.CollectPortService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class CollectPortServiceImpl extends ServiceImpl<CollectPortMapper, CollectPort> implements CollectPortService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePortList(List<CollectPort> portList,String assetId) {
        QueryWrapper<CollectPort> delWrapper = new QueryWrapper<CollectPort>();
        delWrapper.eq("ASSET_ID",assetId);
        remove(delWrapper);
        saveBatch(portList);
    }
}
