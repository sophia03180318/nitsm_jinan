package com.jcca.dataProcessing.listener.eventInfoHandler;


import com.jcca.common.redis.service.RedisService;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.listener.alarmHandler.AlarmEventHandler;
import com.jcca.dataProcessing.manager.IDataChangeManagerService;
import com.jcca.dataProcessing.manager.impl.EventInfoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO  事件保存处理类
 * @className EventSaveAlarmHandler
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("eventSaveAlarmHandler")
public class EventSaveAlarmHandler extends IFilterHandler<IEvent> {

    @Resource
    private IDataChangeManagerService dataChangeManagerService;
    @Resource
    private EventInfoManagerService eventInfoManagerService;
    @Resource(name = "redisTransactionTemplate")
    private RedisTemplate redisTransactionTemplate;
    @Resource
    private RedisService redisServ;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handler(IEvent info) {
        synchronized (AlarmEventHandler.obj){
            try {
                redisTransactionTemplate.multi();
                //改缓存(性能数据)
                ChangeInfo changeInfo = (ChangeInfo) info.getInfo();
                if (changeInfo.getRedisKey() != null) {
                    HashOperations<String, Object, Object> hash = redisTransactionTemplate.opsForHash();
                    hash.put(changeInfo.getRedisKey(), changeInfo.getMapKey(), changeInfo.getValue());
                }
                Object obj = eventInfoManagerService.getStateValue(info.getEventRedisKey(), info.getMapKey());
                //推送的是恢复事件或者是异常事件需要保存事件
                if ((obj != null && info.getStatus() != obj) || (obj == null && info.getStatus() == EventLevelEnum.ABNORMAL.getCode())) {
                    dataChangeManagerService.saveEvent(info);
                }
                eventInfoManagerService.saveRedisChange(info,redisTransactionTemplate);
                redisTransactionTemplate.exec();
            }catch (Exception e) {
                redisTransactionTemplate.discard();
                throw e;
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



}
