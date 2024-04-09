package com.jcca.web.collect.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectDBfileMapper;
import com.jcca.web.collect.entity.CollectDBfile;
import com.jcca.web.collect.service.CollectDBfileService;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.service.bean.PullAlertLogResultResp;
import com.jcca.web.common.service.bean.PullAlertLogResultReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 0:31 2022/6/9
 * @ Description:
 */
@Service
@Slf4j
public class CollectDBfileServiceImpl extends ServiceImpl<CollectDBfileMapper, CollectDBfile> implements CollectDBfileService {
    @Resource
    private RedisService redisService;


    @Override
    public PullAlertLogResultResp verifyDBfile(PullAlertLogResultReq req) throws Exception {
        Object collectUrl = redisService.get(OutConst.COLLECT_MASTER_URL);
        if (Objects.isNull(collectUrl)) {
            throw new Exception("获取中心采集器地址失败");
        }
        String url = "http://" + collectUrl.toString() + "/business/verifyDBfile";

        log.info("中心采集器执行SNMP请求REQ：{}", JSONUtil.toJsonStr(req));
        String post = HttpUtil.post(url, JSONUtil.toJsonStr(req), 60 * 1000);
        log.info("中心采集器执行SNMP请求RESP：{}", post);
        PullAlertLogResultResp PullAlertLogResultResp = JSONUtil.toBean(JSONUtil.parseObj(post), PullAlertLogResultResp.class);
        if (!PullAlertLogResultResp.SUNNCESS.equals(PullAlertLogResultResp.getCode())){
            throw new Exception(PullAlertLogResultResp.getMsg());
        }
        return PullAlertLogResultResp;
    }


    @Override
    public PullAlertLogResultResp downDBfile(PullAlertLogResultReq req) throws Exception {
        Object collectUrl = redisService.get(OutConst.COLLECT_MASTER_URL);
        if (Objects.isNull(collectUrl)) {
            throw new Exception("获取中心采集器地址失败");
        }
        String url = "http://" + collectUrl.toString() + "/business/downDBfile";

        log.info("中心采集器执行SNMP请求REQ：{}", JSONUtil.toJsonStr(req));
        String post = HttpUtil.post(url, JSONUtil.toJsonStr(req), 60 * 1000);
        log.info("中心采集器执行SNMP请求RESP：{}", post);
        PullAlertLogResultResp PullAlertLogResultResp = JSONUtil.toBean(JSONUtil.parseObj(post), PullAlertLogResultResp.class);
        if (!PullAlertLogResultResp.SUNNCESS.equals(PullAlertLogResultResp.getCode())){
            throw new Exception(PullAlertLogResultResp.getMsg());
        }
        return PullAlertLogResultResp;
    }
}
