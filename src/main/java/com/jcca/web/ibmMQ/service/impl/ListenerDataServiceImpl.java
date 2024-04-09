package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.ListenerDataMapper;
import com.jcca.web.ibmMQ.entity.IBMListenerData;
import com.jcca.web.ibmMQ.service.ListenerDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 16:09
 **/
@Service
public class ListenerDataServiceImpl extends ServiceImpl<ListenerDataMapper, IBMListenerData> implements ListenerDataService {
    @Resource
    ListenerDataMapper listenerDataMapper;

    @Override
    public IBMListenerData queryLastDate(String monitorId) {
        return listenerDataMapper.queryLastDate(monitorId);
    }

    @Override
    public List<IBMListenerData> queryDetailData(String monitorId) {
        return listenerDataMapper.queryDetailData(monitorId);
    }

    @Override
    public Boolean removeStatistics(String monitorId) {
        return listenerDataMapper.removeStatistics(monitorId);
    }
}