package com.jcca.dataProcessing.manager.impl;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * 初始化事件配置信息到内存
 *
 * @author Zhaozheng
 * @description TODO 事件信息管理
 * @className EventInfoChangeManagerService
 * @date 2023/10/26 19:06
 * @since 2.1.0.0
 */
@Service
public class EventInfoManagerService implements IEventInfoManagerService {

    @Resource
    private RedisService redisService;


    @Override
    public boolean infoIschange(String inspectRecordId, String redisKey, String mapKey, Object changeValue) {
        //判断是否是巡检过来的数据
        if (inspectRecordId != null && !inspectRecordId.isEmpty()) {
            return true;
        }
        Object obj = redisService.hmGet(redisKey, mapKey);
        if (obj == null) {
            return true;
        }

        return this.judgeChange(obj, changeValue);
    }

    @Override
    public synchronized Boolean infoIschangeFirst(String inspectRecordId, String redisKey, String mapKey, Object changeValue) {
        //判断是否是巡检过来的数据
        if (inspectRecordId != null && !"".equals(inspectRecordId)) {
            return true;
        }
        Object obj = redisService.hmGet(redisKey, mapKey);
        if (obj == null) {
            return null;
        }
        return this.judgeChange(obj, changeValue);
    }


    public  Boolean infoChangeStatus(String redisKey, String mapKey, Object changeValue) {

        Object obj = redisService.hmGet(redisKey, mapKey);
        if (obj == null) {
            return null;
        }
        return this.judgeChange(obj, changeValue);
    }



    /**
     * 与缓存中的信息对比是否有变动
     *
     * @param obj
     * @param changeValue
     * @return
     */
    private boolean judgeChange(Object obj, Object changeValue) {
        //Byte类型对比
        if (changeValue instanceof Byte && obj instanceof Integer) {
            if (((Number) changeValue).intValue() == (Integer) obj) {
                return false;
            }
        }
        //Integer类型对比
        if (changeValue instanceof Integer && obj instanceof Integer) {
            if (((Number) changeValue).intValue() == (Integer) obj) {
                return false;
            }
        }
        //Long类型对比
        if (changeValue instanceof Long && obj instanceof Integer) {
            if (((Number) changeValue).longValue() == (Integer) obj) {
                return false;
            }
        }
        if (changeValue instanceof Long && obj instanceof Long) {
            if (((Number) changeValue).longValue() == (Long) obj) {
                return false;
            }
        }

        // 字符串对比
        if (changeValue instanceof String && obj instanceof String) {
            if (changeValue.equals(obj)) {
                return false;
            }
        }

        //double类型对比
        if (changeValue instanceof Double && obj instanceof Double) {
            if (((Number) changeValue).doubleValue() == (Double) obj) {
                return false;
            }
        }

        if (changeValue instanceof Boolean && obj instanceof Boolean) {
            return (Boolean) changeValue ^ (Boolean) obj;
        }
        return true;
    }

    @Override
    public IEvent creatChangeEvent(String assetId, ChangeInfo changeInfo, String redisKey, String mapKey, Integer status, AlarmTempReq alarmTempReq, String inspectRecordId) {
        IEvent event = new IEvent(assetId, changeInfo, redisKey, mapKey, status, alarmTempReq, inspectRecordId);
        return event;
    }

    @Override
    public IEvent creatRecoveryThresholdEvent(String assetId, ChangeInfo changeInfo, String eventRedisKey, String eventMapKey, String redisThresholdKey, String thresholdMapKey, String inspectRecordId) {
        Map<String, Object> map = redisService.getHashMap(redisThresholdKey);
        boolean redisThreshold = map.containsKey(thresholdMapKey);
        if (redisThreshold) {//如果存储阈值配置信息
            Set<String> set = map.keySet();
            for (String key : set) {
                if (key.contains(thresholdMapKey)) {
                    this.delStateValue(redisThresholdKey, key);//删除阈值配置
                }
            }
            IEvent event = new IEvent(assetId, changeInfo, eventRedisKey, eventMapKey, EventLevelEnum.NORMAL.getCode(), inspectRecordId);
            event.setDescStr("阈值设置被清除,恢复阈值事件。");//恢复阈值告警
            changeInfo.setIsEvent(true);
            return event;
        }


        return null;
    }

    /**
     * 保存缓存的事件信息，如果事件信息为正常，则缓存是不存的
     *
     * @param info
     */
    public void saveRedisChange(IEvent info, RedisTemplate redisTemplate) {
        if (info.getStatus() == EventLevelEnum.NORMAL.getCode()) {
            //如果事件正常删除事件缓存
            if(StrUtil.isNotEmpty(info.getEventRedisKey()) && StrUtil.isNotEmpty(info.getMapKey())){
                HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
                hash.delete(info.getEventRedisKey(), info.getMapKey());
            }
        } else if(StrUtil.isNotEmpty(info.getEventRedisKey()) && StrUtil.isNotEmpty(info.getMapKey())){
            //如果事件缓存事件信息
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            hash.put(info.getEventRedisKey(), info.getMapKey(), info.getStatus());
        }
    }


    @Override
    public Object getStateValue(String redisKey, String mapKey) {
        Object obj = redisService.hmGet(redisKey, mapKey);
        return obj;
    }

    @Override
    public void setStateValue(String redisKey, String mapKey, Object value) {
        redisService.hmSet(redisKey, mapKey, value);
    }

    @Override
    public void delStateValue(String redisKey, String mapKey) {
        redisService.hmDel(redisKey, mapKey);
    }

    @Override
    public void delRedisKey(String redisKey) {
        redisService.remove(redisKey);
    }

    @Override
    public void delAndsetStateValue(String redisKey, String mapKey, Object value) {
        redisService.hmDel(redisKey, mapKey);
        redisService.hmSet(redisKey, mapKey, value);
    }


}
