package com.jcca.dataProcessing.listener.alarmHandler;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.manager.IDataChangeManagerService;
import com.jcca.dataProcessing.manager.bean.SaveAlarmResp;
import com.jcca.dataProcessing.manager.impl.EventInfoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 告警变动过滤处理类
 * @className NetTimeAlarmHandler
 * @date 2023/10/20 9:51
 * @since 2.1.0.0
 */
@Slf4j
@Component("alarmEventHandler")
public class AlarmEventHandler extends IFilterHandler<IEvent> {

    private static int THREAD_SIZE = 0;

    @Resource
    private IDataChangeManagerService dataChangeManagerService;
    @Resource
    private EventInfoManagerService eventInfoManagerService;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource(name = "redisTransactionTemplate")
    private RedisTemplate redisTransactionTemplate;
    @Resource
    private RedisService redisServ;


    /**
     * 锁
     * @throws InterruptedException
     */
    private void verifyNum() throws InterruptedException {
        while (true){
            Boolean lock = redisServ.setNx("ALARM_EXE_LOCK", 10);
            if(lock){
                if(THREAD_SIZE>20){
                    log.info("事务已超过限制，进程阻塞中……");
                    Thread.sleep(2 * 1000);
                } else {
                    THREAD_SIZE = THREAD_SIZE + 1;
                    return;
                }
            }else{
                Thread.sleep(1 * 1000);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean handler(IEvent info) {
        if (Objects.isNull(info.getEventAlarmLevelBaseEntity()) || Objects.isNull(info.getEventAlarmLevelBaseEntity().getAlarmLevel())) {
            //未设定告警级别的告警不上报，只存事件
            dataChangeManagerService.saveEvent(info);
            return true;
        }
        if (StrUtil.isEmpty(info.getAssetId()) || StrUtil.isEmpty(info.getMapKey())) {
            return true;
        }
        try {
            //需要限制流量 最大允许开启20个链接
            verifyNum();

            redisTransactionTemplate.multi();

            SaveAlarmResp resp = new SaveAlarmResp();
            resp.setNeedSendToWeb(false);

            //查看是否有历史告警
            AlarmInfo alarmInfo = alarmInfoService.getAssetAlarm(info.getRedisKey(), info.getAssetId(), info.getMapKey());

            //修改性能信息缓存
            ChangeInfo changeInfo = (ChangeInfo) info.getInfo();

            //这个变量是为了全局状态存储用的
            eventInfoManagerService.getStateValue(info.getRedisKey(), info.getMapKey());

            if (changeInfo.getRedisKey() != null) {
                //这里是保存change中的变量数据……
                eventInfoManagerService.setStateValue(changeInfo.getRedisKey(), changeInfo.getMapKey(), changeInfo.getValue());
            }
            //过滤掉 无告警的正常事件
            if (info.getStatus().equals(EventLevelEnum.NORMAL.getCode()) && Objects.isNull(alarmInfo)) {
                redisTransactionTemplate.exec();
                return true;
            }
            //过滤掉 未确认的已恢复告警
            if (info.getStatus().equals(EventLevelEnum.NORMAL.getCode()) && Objects.nonNull(alarmInfo) && AlarmStateEnum.RECOVER.getCode().equals(alarmInfo.getAlarmState())) {
                redisTransactionTemplate.exec();
                return true;
            }
            //过滤 存在告警的 异常事件
            if (info.getStatus().equals(EventLevelEnum.ABNORMAL.getCode()) && Objects.nonNull(alarmInfo) && AlarmStateEnum.ALARM.getCode().equals(alarmInfo.getAlarmState())) {
                redisTransactionTemplate.exec();
                return true;
            }

            // 历史告警为空创建新告警
            if (alarmInfo == null) {
                Asset asset = assetServ.getById(info.getAssetId());
                alarmInfo = dataChangeManagerService.saveAlarm(info, asset);
                resp.setNeedSendToWeb(true);
                resp.setAsset(asset);
                resp.setNewAlarm(alarmInfo);
            } else {
                //更新告警
                boolean abnormal = info.getStatus().equals(EventLevelEnum.ABNORMAL.getCode());
                if (abnormal) {
                    alarmInfo.setContent(alarmInfo.getContent().replace("【-已恢复-】", ""));
                    alarmInfo.setIsShowRecover(-1);
                } else if (!alarmInfo.getContent().contains("【-已恢复-】")) {
                    alarmInfo.setContent(alarmInfo.getContent() + "【-已恢复-】");
                    alarmInfo.setIsShowRecover(1);
                }
                alarmInfo.setAlarmState(abnormal ? AlarmStateEnum.ALARM.getCode() : AlarmStateEnum.RECOVER.getCode());
                alarmInfo.setOccurTime(info.getCollectTime());
                alarmInfoService.updateById(alarmInfo);
            }

            //推送的是恢复事件或者是异常事件需要保存事件
            AlarmEvent alarmEvent = dataChangeManagerService.saveEvent(info);
            if (alarmInfo != null) {
                dataChangeManagerService.linkEvent(alarmInfo.getId(), alarmEvent);
            }

            //弹出通知框
            if (resp.getNeedSendToWeb()) {
                dataChangeManagerService.popup(resp.getAsset(), resp.getNewAlarm());
            }

            //保存事件缓存 使用带事务的 redisTransactionTemplate
            eventInfoManagerService.saveRedisChange(info, redisTransactionTemplate);
            redisTransactionTemplate.exec();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                redisTransactionTemplate.discard();
            } catch (Exception e1) {
                log.error("结束事务失败……");
            }
        } finally {
            THREAD_SIZE = THREAD_SIZE - 1;
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return false;
    }

}
