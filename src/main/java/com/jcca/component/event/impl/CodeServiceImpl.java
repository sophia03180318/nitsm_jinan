package com.jcca.component.event.impl;

import cn.hutool.core.date.DateUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.component.event.CodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 可并发生成唯一ID
 *
 * @author lyp
 */
@Service
public class CodeServiceImpl implements CodeService {

    @Resource
    private RedisService redisServ;

    @Override
    public String getCode(String code, Long num) {
        String seqence = redisServ.getIdSeqence(code, 99999999L);
        return DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + seqence;
    }

}
