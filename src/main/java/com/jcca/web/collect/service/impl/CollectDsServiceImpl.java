package com.jcca.web.collect.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectDsMapper;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.service.CollectDsService;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.service.bean.PullAlertLogResultReq;
import com.jcca.web.common.service.bean.PullAlertLogResultResp;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * @ Author：sophia
 * @ Date：Created in 0:31 2022/6/9
 * @ Description:
 */
@Service
public class CollectDsServiceImpl extends ServiceImpl<CollectDsMapper, CollectDS> implements CollectDsService {
    @Resource
    private CollectDsMapper dsMapper;
    @Resource
    private RedisService redisService;

    @Override
    public List<CollectDS> findByType(String assetId, int type) {
        return dsMapper.findByType(assetId, type);
    }

    @Override
    public List<CollectDS> findByDrives(String assetId, int index) {
        return dsMapper.findByDrives(assetId, index);
    }

    @Override
    public List<CollectDS> findByArray(String arrayId) {
        return dsMapper.findByArray(arrayId);
    }

    @Override
    public Date findLastTime(String assetId) {
        return dsMapper.findLastTime(assetId);
    }

    @Override
    public Boolean removeBeforeData(Integer removeHour) {
        if (removeHour < 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        QueryWrapper<CollectDS> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectDS> list = this.list(query);
        for (CollectDS collect : list) {
            Date collectTime = collect.getCollectTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR_OF_DAY, -removeHour);
                Date time = calendar.getTime();
                query = Wrappers.query();
                query.eq("asset_id", collect.getAssetId());
                query.lt("collect_time", time);
                this.remove(query);
            }
        }
        return true;
    }

    @Override
    public PullAlertLogResultResp downLogFile(PullAlertLogResultReq req) throws Exception {
        Object collectUrl = redisService.get(OutConst.COLLECT_MASTER_URL);
        if (Objects.isNull(collectUrl)) {
            throw new Exception("获取中心采集器地址失败");
        }
        String url = "http://" + collectUrl.toString() + "/business/downDSfile";

        String post = HttpUtil.post(url, JSONUtil.toJsonStr(req), 60 * 1000);

        PullAlertLogResultResp dbpullAlertLogResultResp = JSONUtil.toBean(JSONUtil.parseObj(post), PullAlertLogResultResp.class);
        if (!PullAlertLogResultResp.SUNNCESS.equals(dbpullAlertLogResultResp.getCode())){
            throw new Exception(dbpullAlertLogResultResp.getMsg());
        }
        return dbpullAlertLogResultResp;

    }

}
