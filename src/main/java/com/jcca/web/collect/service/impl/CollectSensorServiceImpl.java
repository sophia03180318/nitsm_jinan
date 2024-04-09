package com.jcca.web.collect.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.redis.service.RedisService;
import com.jcca.web.collect.dao.CollectSensorMapper;
import com.jcca.web.collect.entity.CollectSensor;
import com.jcca.web.collect.enums.SensorStatusEnum;
import com.jcca.web.collect.service.CollectSensorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 传感采集信息
 *
 * @author Lvyp
 */
@Service
public class CollectSensorServiceImpl extends ServiceImpl<CollectSensorMapper, CollectSensor>
        implements CollectSensorService {

    private static final String SENSOR_KEY = "SENSOR:TAB:KEY:";

    @Resource
    private RedisService redisService;
    @Resource
    private CollectSensorMapper collectSensorMapper;

    @Override
    public void updateRealTimeData(List<CollectSensor> sensors) {
        CollectSensor sensor = sensors.get(0);
        String key = SENSOR_KEY + sensor.getAssetId();
        redisService.set(key, JSONUtil.parseArray(sensors));
    }

    @Transactional
    @Override
    public void updateBatchByAssetId(List<CollectSensor> entityList) {
        if (entityList.isEmpty()) {
            log.error("updateBatchByAssetId mast be not null");
            return;
        }
        String assetId = entityList.get(0).getAssetId();
        QueryWrapper<CollectSensor> wrapper = new QueryWrapper<CollectSensor>();
        wrapper.eq("ASSET_ID", assetId);
        collectSensorMapper.delete(wrapper);
        saveBatch(entityList);

    }

    @Override
    public Boolean removeBeforeData(Integer removeHour) {
        if (removeHour < 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        QueryWrapper<CollectSensor> query = Wrappers.query();
        query.select("asset_id", "max(collect_time) as collectTime");
        query.groupBy("asset_id");
        List<CollectSensor> list = this.list(query);
        for (CollectSensor collect : list) {
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
    public List<CollectSensor> getRealTimeData(String assetId) {

        List<CollectSensor> selectRealTimeData = collectSensorMapper.selectRealTimeData(assetId);
        if (Objects.isNull(selectRealTimeData)) {
            selectRealTimeData = new ArrayList<CollectSensor>();
        }

        List<String> normalStatus = Arrays.asList("0", "1");
        for (CollectSensor collectSensor : selectRealTimeData) {
            collectSensor.setStatusStr(SensorStatusEnum.getMsgByCode(collectSensor.getStatus()));
            if(normalStatus.contains(collectSensor.getStatus())){
                collectSensor.setStatus(SensorStatusEnum.NORMAL.getCode());
            }
            if(StrUtil.isEmpty(collectSensor.getDescStr())){
                collectSensor.setDescStr(collectSensor.getSerialNumberName());
            }
        }

        return selectRealTimeData;
    }

}
