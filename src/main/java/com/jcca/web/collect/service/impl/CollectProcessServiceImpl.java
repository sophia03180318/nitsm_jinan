package com.jcca.web.collect.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.web.collect.dao.CollectProcessMapper;
import com.jcca.web.collect.entity.CollectProcess;
import com.jcca.web.collect.service.CollectProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 进程
 *
 * @author Lvyp
 */
@Service
public class CollectProcessServiceImpl extends ServiceImpl<CollectProcessMapper, CollectProcess>
        implements CollectProcessService {

    public static final String PROCESS_KEY = "PROCESS:TAB:KEY:";
    private static final String PROCESS_ALARM = "PROCESS:ALARM:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectProcessMapper processMapper;

    @Override
    public void updateRealTimeData(List<CollectProcess> datas) {
        CollectProcess process = datas.get(0);
        String key = PROCESS_KEY + process.getAssetId();
        redisService.set(key, JSONUtil.parseArray(datas));
    }

    @Override
    public List<CollectProcessBean> getRealTimeData(String assetId) {
        // 数据库扫描历史
        List<CollectProcess> newProcess = processMapper.selectAssetNewProcess(assetId);
        if (Objects.isNull(newProcess)) {
            return new ArrayList<CollectProcessBean>();
        }
        return EntityBeanUtil.copyList(newProcess, CollectProcessBean.class);
    }

    @Override
    public String getAlarmCode(CollectProcess process, String type) {
        return PROCESS_ALARM + process.getName() + "ASSETID" + process.getAssetId() + ":" + type;
    }

    @Override
    public void removeByAssetId(String assetId) {
        String key = PROCESS_KEY + assetId;
        redisService.remove(key);

        QueryWrapper<CollectProcess> queryWrapper = new QueryWrapper<CollectProcess>();
        queryWrapper.eq("ASSET_ID", assetId);
        remove(queryWrapper);
    }

    @Override
    public void removeByAssetIdAndProcessName(String assetId, String processName) {
        String key = PROCESS_KEY + assetId;
        redisService.remove(key);

        QueryWrapper<CollectProcess> queryWrapper = new QueryWrapper<CollectProcess>();
        queryWrapper.eq("ASSET_ID", assetId);
        queryWrapper.eq("NAME", processName);
        remove(queryWrapper);
    }

    @Override
    public Boolean removeBeforeData(Integer hour) {
        Calendar calendar = Calendar.getInstance();
        // 修改为保留资产最近两个小时的数据,而不是删除当前时间两小时前的数据 20220812 hanwone
        QueryWrapper<CollectProcess> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectProcess> list = this.list(query);
        for (CollectProcess collect : list) {
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

}
