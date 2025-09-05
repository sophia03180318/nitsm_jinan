package com.jcca.dataProcessing.listener.xunjianHandler;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static com.jcca.web2.constant.Web2Const.XUNJIAN_COLLECT_QUEUE;

@Component("eventXunjianHandler")
public class EventXunjianHandler extends IFilterHandler<IEvent> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(IEvent info) throws Exception {


        boolean returnflag = true;


        if (info.getInspectRecordId() != null && !"".equals(info.getInspectRecordId())) {

            info.setXunjianDesc(info.getDescStr());
            if (info.getXunjianDesc().contains("恢复")) {
                String desc = info.getXunjianDesc().replace("恢复", "正常");
                info.setXunjianDesc(desc);
            }


            ChangeInfo changeInfo = info.getInfo();
            //有变化是true，无变化是false，null是第一次
            Boolean flag = eventInfoChangeManagerService.infoChangeStatus(changeInfo.getRedisKey(), changeInfo.getMapKey(), changeInfo.getValue());
            if (!Objects.isNull(flag) && flag) {
                //如果是本身状态有变化，那就是正常流程
                returnflag = true;
            }
            //端口变化需要过滤，未使用的端口。
            if (StatusInfoChangeTypeEnum.event_port_state.getCode().equals(info.getEventRedisKey())) {
                int value = Integer.valueOf(changeInfo.getValue().toString());
                //端口本身是断开的，但是redis中已经有端口状态的缓存
                if ((value == 2) && !Objects.isNull(flag) && (!flag)) {
                    Object object = eventInfoChangeManagerService.getStateValue(info.getEventRedisKey(), info.getMapKey());
                    //如果没有事件说明不是异常的
                    if (Objects.isNull(object)) {
                        //设置巡检正常
                        info.setStatus(EventLevelEnum.NORMAL.getCode());
                        //redis缓存中一直存是的是断的端口信息
                        info.setXunjianDesc("端口 " + info.getAlarmTempReq().getFlag() + " 未启用");
                        returnflag = false;
                    } else {
                        //本身就存在异常
                        returnflag = true;
                    }
                }
                //如果状态是断的，并且缓存为空，说明是第一次采集
                if (value == 2 && Objects.isNull(flag)) {
                    //redis缓存中一直存是的是断的端口信息
                    info.setXunjianDesc("端口 " + info.getAlarmTempReq().getFlag() + " 未启用");
                    info.setStatus(EventLevelEnum.NORMAL.getCode());
                    returnflag = false;
                }
            }else if (StatusInfoChangeTypeEnum.event_net_state.getCode().equals(info.getEventRedisKey())){
                int value = Integer.valueOf(changeInfo.getValue().toString());
                //端口本身是断开的，但是redis中已经有端口状态的缓存
                if (value == 2 && Objects.nonNull(flag) && !flag) {
                    Object object = eventInfoChangeManagerService.getStateValue(info.getEventRedisKey(), info.getMapKey());
                    //如果没有事件说明不是异常的
                    if (Objects.isNull(object)) {
                        //设置巡检正常
                        info.setStatus(EventLevelEnum.NORMAL.getCode());
                        //redis缓存中一直存是的是断的端口信息
                        info.setXunjianDesc("网卡 " + info.getAlarmTempReq().getFlag() + " 未启用");
                        returnflag = false;
                    } else {
                        //本身就存在异常
                        returnflag = true;
                    }
                }
                //如果状态是断的，并且缓存为空，说明是第一次采集
                if (value == 2 && Objects.isNull(flag)) {
                    //redis缓存中一直存是的是断的端口信息
                    info.setXunjianDesc("网卡 " + info.getAlarmTempReq().getFlag() + " 未启用");
                    info.setStatus(EventLevelEnum.NORMAL.getCode());
                    returnflag = false;
                }
            }
            //如果不在往下层提交，那么就在这个地方进行保存
            if(returnflag==false){
                XUNJIAN_COLLECT_QUEUE.put(info);
            }

            return returnflag;
        }


        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
