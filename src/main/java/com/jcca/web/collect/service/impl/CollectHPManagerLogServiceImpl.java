package com.jcca.web.collect.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.SnmpTimeUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.collect.service.CollectHPManagerLogService;
import com.jcca.web.collect.service.bean.HPManagerLogVo;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultResp;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


/**
 * 惠普管理口日志采集
 */
@Slf4j
@Service
public class CollectHPManagerLogServiceImpl implements CollectHPManagerLogService {

    private static List<String> MIB_LIST = Arrays.asList(".1.3.6.1.4.1.232.6.2.11.3.1.2",
            ".1.3.6.1.4.1.232.6.2.11.3.1.3", ".1.3.6.1.4.1.232.6.2.11.3.1.4", ".1.3.6.1.4.1.232.6.2.11.3.1.5",
            ".1.3.6.1.4.1.232.6.2.11.3.1.6", ".1.3.6.1.4.1.232.6.2.11.3.1.7", ".1.3.6.1.4.1.232.6.2.11.3.1.8");
    public static final String CACHE_KEY = "HP_SYS_LOG:";

    @Resource
    private RedisService redisServ;

    @Resource
    private OutService outServ;

    @Override
    public List<HPManagerLogVo> queryHPManagerLog(Asset asset) throws Exception {
        String ipmiIp = asset.getIpmiIp();
        String ipmiUser = asset.getIpmiUser();

        if(StrUtil.isEmpty(ipmiIp)||StrUtil.isEmpty(ipmiUser)){
            return new ArrayList<HPManagerLogVo>();
        }

        BusinessGetSnmpResultReq req = new BusinessGetSnmpResultReq();
        req.setCommunity(ipmiUser);
        req.setIp(ipmiIp);
        req.setType("WALK");

        Map<String, HPManagerLogVo> cacheMap = new HashMap<String, HPManagerLogVo>();

        for (int i = 0; i < MIB_LIST.size(); i++) {
            req.setMib(MIB_LIST.get(i));
            BusinessGetSnmpResultResp snmpResult = outServ.getSnmpResult(req);

            String code = snmpResult.getCode();
            if (!BusinessGetSnmpResultResp.SUNNCESS.equals(code)) {
                throw new Exception("获取日志失败：" + snmpResult.getMsg());
            }

            List<SnmpExecuteResult> resultList = snmpResult.getResultList();
            for (SnmpExecuteResult result : resultList) {
                String oid = result.getOid();
                String value = result.getValue();

                if (Objects.isNull(value)) {
                    continue;
                }
                value = value.trim();

                String[] split = oid.split("\\.");
                String id = split[split.length - 1];

                updateCacheMap(cacheMap, value, id, i);

            }

        }

        Collection<HPManagerLogVo> values = cacheMap.values();
        List<HPManagerLogVo> hpManagerLogVos = Lists.newArrayList(values);

        if(Objects.nonNull(hpManagerLogVos)&&hpManagerLogVos.isEmpty()){
            Object logList = redisServ.get(CollectHPManagerLogServiceImpl.CACHE_KEY+asset.getId());
            if(Objects.nonNull(logList)){
                JSONArray logArray = JSONUtil.parseArray(logList);
                hpManagerLogVos = JSONUtil.toList(logArray, HPManagerLogVo.class);
            }
        }else{
            redisServ.set(CollectHPManagerLogServiceImpl.CACHE_KEY+asset.getId(),hpManagerLogVos);
        }

        return hpManagerLogVos;
    }

    /**
     * 更新缓存MAP
     *
     * @param cacheMap
     * @param value
     * @param id
     * @param i
     */
    private void updateCacheMap(Map<String, HPManagerLogVo> cacheMap, String value, String id, int i) throws Exception {
        HPManagerLogVo hpManagerLogVo = cacheMap.get(id);
        if (Objects.isNull(hpManagerLogVo)) {
            hpManagerLogVo = new HPManagerLogVo();
        }
        if (i == 0) {
            hpManagerLogVo.setId(id);
            hpManagerLogVo.setSeverity(value);
        } else if (i == 1) {
            hpManagerLogVo.setClassStr(value);
        } else if (i == 2) {
            hpManagerLogVo.setEntryCode(value);
        } else if (i == 3) {
            hpManagerLogVo.setCount(value);
        } else if (i == 4) {
            hpManagerLogVo.setInitialUpdate(SnmpTimeUtil.getTimeStr2(value));
        } else if (i == 5) {
            hpManagerLogVo.setLastUpdate(SnmpTimeUtil.getTimeStr2(value));
        } else if (i == 6) {
            hpManagerLogVo.setDescription(SnmpTimeUtil.asciiToString(value));
        }

        cacheMap.put(id, hpManagerLogVo);
    }


}
