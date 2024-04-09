package com.jcca.web.collect.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectDBMapper;
import com.jcca.web.collect.entity.CollectDB;
import com.jcca.web.collect.service.CollectDBService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 业务处理
 *
 * @author Lvyp
 */
@Service
public class CollectDBServiceImpl extends ServiceImpl<CollectDBMapper, CollectDB> implements CollectDBService {

    private static final String CACHE_KEY = "DB:TAB:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectDBMapper dbMapper;

    @Override
    public void updateRealTimeData(List<CollectDB> entityList) {
        CollectDB db = entityList.get(0);
        String key = CACHE_KEY + db.getAssetId();
        redisService.set(key, JSONUtil.parseArray(entityList));
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectDB> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectDB> list = this.list(query);
        for (CollectDB collect : list) {
            Date collectTime = collect.getCollectTime();
            if (Objects.nonNull(collectTime)) {
                calendar.setTime(collectTime);
                calendar.add(Calendar.HOUR_OF_DAY, -hour);
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
    public List<CollectDB> getRealTimeData(String assetId) {
        List<CollectDB> dbData = dbMapper.selectRealTimeData(assetId);
        return dbData;
    }

}
