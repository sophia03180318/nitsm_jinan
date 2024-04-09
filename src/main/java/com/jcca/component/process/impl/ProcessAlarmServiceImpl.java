package com.jcca.component.process.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.process.ProcessAlarmService;
import com.jcca.component.process.bean.ProcessAlarmQueueBean;
import com.jcca.web.asset.controller.bean.AlarmVerifyBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ProcessAlarmServiceImpl implements ProcessAlarmService {

    private static Map<String, Object> PROCESS_LOCK_UNGROUP_POOL = new ConcurrentHashMap<>();
    private static Map<String, Object> PROCESS_LOCK_GROUP_POOL = new ConcurrentHashMap<>();
    public static Map<String, Integer> alarmCounterMap = new HashMap<String, Integer>();

    @Resource
    private EventLogicService eventServ;
    @Resource
    private ThresholdProcessService processThServ;
    @Resource
    private SysModuleConfigService sysModuleServ;
    @Resource
    private RedisService redisServ;

    @Override
    public void disposeGroupAlarm(List<ProcessAlarmQueueBean> queueObj, Date time) {
        log.info("组进程告警处理参数：{}", JSONUtil.toJsonStr(queueObj));
        ProcessAlarmQueueBean item = queueObj.get(0);
        Asset asset = item.getAsset();
        String lockCode = ReceiveAlarmTypeEnum.PROCESS.getName() + "_" + asset.getAssetCode();

        PROCESS_LOCK_GROUP_POOL.putIfAbsent(lockCode, new Object());
        Object lock = PROCESS_LOCK_GROUP_POOL.get(lockCode);

        synchronized (lock) {

            String processName = item.getProcessName();
            String alarmCode = lockCode + "_" + processName;

            List<ProcessAlarmQueueBean> normalAsset = new ArrayList<ProcessAlarmQueueBean>();
            List<ProcessAlarmQueueBean> errorAsset = new ArrayList<ProcessAlarmQueueBean>();

            for (ProcessAlarmQueueBean processAlarmQueueBean : queueObj) {
                if (processAlarmQueueBean.getProcessStatus()) {
                    normalAsset.add(processAlarmQueueBean);
                } else {
                    errorAsset.add(processAlarmQueueBean);
                }
                // 分别更新单个进程的状态,用于进程管理中的显示 syt
                processThServ.updateByAssetIdAndProcessName(processName, processAlarmQueueBean.getAsset().getId(), processAlarmQueueBean.getProcessStatus(),processAlarmQueueBean.getProcessId());
            }

            if (errorAsset.size() == queueObj.size()) {
                // 组内进程组全部丢失-添加进程全部丢失事件
                addAllDownEvent(time, errorAsset, alarmCode);
                return;
            } else if (errorAsset.size() > 0) {
                // 部分进程丢失
                addOtherDownEvent(time, alarmCode, queueObj, normalAsset, errorAsset);
                return;
            }

            // 组内所有进程都正常
            for (ProcessAlarmQueueBean queue : queueObj) {
                Asset assetItsm = queue.getAsset();

                String msg = String.format("设备组内所有%s进程状态正常.进程号:%s", queue.getProcessName(), queue.getProcessId());
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                eventReq.setAssetId(assetItsm.getId());
                eventReq.setUniqueCode(EventUniqueCode.PROCESS_OTHER_STOP);
                eventReq.setFlag(queue.getProcessName());
                eventReq.setCreateTime(time);
                eventReq.setOriginalMsg(msg);
                eventReq.setGroupFlag(EventGroupConstant.PROCESS);

                CreateEventReq copy = EntityBeanUtil.copy(eventReq, CreateEventReq.class);
                copy.setUniqueCode(EventUniqueCode.PROCESS_ALL_STOP);

                try {
                    eventServ.addEvent(eventReq);
                    eventServ.addEvent(copy);
                } catch (Exception e) {
                    log.error("组进程告警处理ERROR-->添加事件系统异常", e);
                }

            }

        }

    }

    private void addOtherDownEvent(Date time, String alarmCode, List<ProcessAlarmQueueBean> allList, List<ProcessAlarmQueueBean> nornalList, List<ProcessAlarmQueueBean> errorAsset) {
        for (ProcessAlarmQueueBean queue : errorAsset) {
            Asset downAsset = queue.getAsset();
            String msg = String.format("设备组内设备%s,进程%s丢失!", downAsset.getName(), queue.getProcessName());

            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            eventReq.setAssetId(downAsset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PROCESS_OTHER_STOP);
            eventReq.setFlag(queue.getProcessName());
            eventReq.setCreateTime(time);
            eventReq.setGroupFlag(EventGroupConstant.PROCESS);

            eventReq.setOriginalMsg(msg);

            try {
                eventServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("组进程告警处理ERROR-->添加事件系统异常", e);
            }
        }

        //正常的上恢复
        for (ProcessAlarmQueueBean queue : nornalList) {
            Asset downAsset = queue.getAsset();
            String msg = String.format("设备组内设备%s,进程%s恢复!进程状态正常！", downAsset.getName(), queue.getProcessName());

            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            eventReq.setAssetId(downAsset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PROCESS_OTHER_STOP);
            eventReq.setFlag(queue.getProcessName());
            eventReq.setCreateTime(time);
            eventReq.setGroupFlag(EventGroupConstant.PROCESS);

            eventReq.setOriginalMsg(msg);

            try {
                eventServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("组进程告警处理ERROR-->添加事件系统异常", e);
            }
        }


        // 全部丢失的一级进程告警恢复
        for (ProcessAlarmQueueBean queue : allList) {
            Asset downAsset = queue.getAsset();
            String msg = String.format("设备所在组的进程%s已恢复，状态正常!", queue.getProcessName());

            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            eventReq.setAssetId(downAsset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PROCESS_ALL_STOP);
            eventReq.setFlag(queue.getProcessName());
            eventReq.setCreateTime(time);

            eventReq.setOriginalMsg(msg);

            try {
                eventReq.setGroupFlag(EventGroupConstant.PROCESS);
                eventServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("组进程告警处理ERROR-->添加事件系统异常", e);
            }
        }
    }

    /**
     * 组进程全部丢失
     *
     * @param time
     * @param alarmCode
     */
    private void addAllDownEvent(Date time, List<ProcessAlarmQueueBean> errorAsset, String alarmCode) {

        for (ProcessAlarmQueueBean item : errorAsset) {
            Asset assetItem = item.getAsset();
            String processName = item.getProcessName();

            String msg = String.format("设备组内进程%s全部丢失", processName);

            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            eventReq.setAssetId(assetItem.getId());
            eventReq.setUniqueCode(EventUniqueCode.PROCESS_ALL_STOP);
            eventReq.setFlag(processName);
            eventReq.setCreateTime(time);

            eventReq.setOriginalMsg(msg);

            try {
                eventReq.setGroupFlag(EventGroupConstant.PROCESS);
                eventServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("无组进程告警处理ERROR-->添加事件系统异常", e);
            }

        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disposeNoGroupAlarm(ProcessAlarmQueueBean queueObj, Date time) {
        log.info("无分组进程告警处理参数：{}", JSONUtil.toJsonStr(queueObj));
        Asset asset = queueObj.getAsset();

        String lockCode = ReceiveAlarmTypeEnum.PROCESS.getName() + "_" + asset.getId();

        PROCESS_LOCK_UNGROUP_POOL.putIfAbsent(lockCode, new Object());
        Object lock = PROCESS_LOCK_UNGROUP_POOL.get(lockCode);

        synchronized (lock) {
            Boolean flag = queueObj.getProcessStatus();
            String processName = queueObj.getProcessName();
            String processId = queueObj.getProcessId();
            ThresholdProcess processTh = processThServ.getOneByAssetIpAndName(asset.getId(), processName);

            if (Objects.isNull(processTh)) {
                log.error("无组进程告警处理ERROR-->该进程已取消配置 IP：{}，PROCESS_NAME：{}", asset.getIp(), processName);
                return;
            }
            AlarmVerifyBean alarmVerifyConf = sysModuleServ.getAlarmVerifyValue();
            String cacheKey = String.format("%s_%s", asset.getId(), processName);
            CreateEventReq addReq = new CreateEventReq();
            addReq.setAssetId(asset.getId());
            addReq.setCreateTime(time);
            addReq.setFlag(queueObj.getProcessName());
            addReq.setUniqueCode(EventUniqueCode.PROCESS_STOP);

            String originalMsg = "";

            // 告警
            if (!flag) {
                Integer alarmCount = alarmCounterMap.get(cacheKey);
                if (Objects.isNull(alarmCount)) {
                    alarmCount = 0;
                }
                originalMsg = String.format("采集到进程：%s,进程号:%s，在%s时丢失", processName,processId,
                        DateUtil.format(time, "yyyy-MM-dd HH:mm:ss"));

                if(LogInputUtils.inputInfo(ServerTypeEnum.NO_GROUP_PROCESS_STATUS)){
                    log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.NO_GROUP_PROCESS_STATUS,asset.getIp(),String.format("进程: %s 设定上告警累计异常次数：%s，key:%s",processName, alarmVerifyConf.getProcessSize(),cacheKey)));
                    log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.NO_GROUP_PROCESS_STATUS,asset.getIp(),String.format("进程:%s累计异常次数：%s，Key:%s",processName, alarmCount,cacheKey)));
                }


                if (alarmCount >= alarmVerifyConf.getProcessSize()) {
                    //次数大于等于设定 上告警
                    addReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                    processTh.setCollectStatus(StatusEnum.NO.getCode());
                } else {
                    //清空状态再来一次
                    redisServ.delProcess(asset.getId(),asset.getAssetCode());
                    addReq.setEventLevel(EventLevelEnum.WARNING.getCode());
                    originalMsg = String.format("采集到进程：%s，进程号：%s 在%s时丢失，%s", processName,processId, DateUtil.format(time, "yyyy-MM-dd HH:mm:ss"), "由于设定了检查周期此次事件仅记录，不上告警！");
                    alarmCounterMap.put(cacheKey, alarmCount + 1);
                }

                addReq.setOriginalMsg(originalMsg);
            } else {
                if(LogInputUtils.inputInfo(ServerTypeEnum.NO_GROUP_PROCESS_STATUS)){
                    log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.NO_GROUP_PROCESS_STATUS,asset.getIp(),String.format("进程：%s 状态恢复正常清空累计次数",processName)));
                }

                // 恢复
                alarmCounterMap.remove(cacheKey);

                originalMsg = String.format("进程%s 进程ID：%s，在%s时恢复，状态正常", processName,processId,
                        DateUtil.format(time, "yyyy-MM-dd HH:mm:ss"));
                // 更新进程状态
                processTh.setCollectStatus(StatusEnum.OK.getCode());

                // 生成事件
                addReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                addReq.setOriginalMsg(originalMsg);
            }

            processThServ.updateById(processTh);

            try {
                addReq.setGroupFlag(EventGroupConstant.PROCESS);
                eventServ.addEvent(addReq);
            } catch (Exception e) {
                log.error("无组进程告警处理ERROR-->添加进程事件系统错误。 IP：{}，PROCESS_NAME：{}", asset.getIp(), queueObj.getProcessName(),
                        e);
            }

        }

    }

}
