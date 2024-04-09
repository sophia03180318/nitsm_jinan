package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.ChannelDataMapper;
import com.jcca.web.ibmMQ.entity.IBMChannelData;
import com.jcca.web.ibmMQ.service.ChannelDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaozheng
 * @date 2020-07-14 15:53
 **/
@Service
public class ChannelDataServiceImpl extends ServiceImpl<ChannelDataMapper, IBMChannelData> implements ChannelDataService {
    @Resource
    private ChannelDataMapper channelDataMapper;

    @Override
    public IBMChannelData queryLastDate(String monitorId) {
        return channelDataMapper.queryLastDate(monitorId);
    }

    @Override
    public List<IBMChannelData> queryDetailData(String monitorId) {
        return channelDataMapper.queryDetailData(monitorId);
    }

    @Override
    public Boolean removeStatistics(String monitorId) {
        return channelDataMapper.removeStatistics(monitorId);
    }

//    @Override
//    public void saveChannelData(Connection paramConnection, List<MQTTChannelData> paramList) {
//
//    }
}
