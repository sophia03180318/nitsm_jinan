package com.jcca.web.ibmMQ.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.ibmMQ.dao.HealthMessageMapper;
import com.jcca.web.ibmMQ.entity.IBMHealthMessage;
import com.jcca.web.ibmMQ.service.HealthMessageService;
import org.springframework.stereotype.Service;

/**
 * @author zhaozheng
 * @date 2020-07-14 16:06
 **/
@Service
public class HealthMessageServiceImpl extends ServiceImpl<HealthMessageMapper, IBMHealthMessage> implements HealthMessageService {


}