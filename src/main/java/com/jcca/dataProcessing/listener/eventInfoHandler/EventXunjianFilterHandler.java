package com.jcca.dataProcessing.listener.eventInfoHandler;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 巡检信息过滤
 * @author Zhaozheng
 * @description TODO 巡检信息过滤
 * @className EventIsConfigAlarmHandler
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("eventXunjianFilterHandler")
public class EventXunjianFilterHandler extends IFilterHandler<IEvent> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Override
    public boolean handler(IEvent info) {

        info.setXunjianDesc(info.getDescStr());
        ChangeInfo changeInfo=info.getInfo();
        Boolean flag= eventInfoChangeManagerService.infoChangeStatus(changeInfo.getRedisKey(),changeInfo.getMapKey(),changeInfo.getValue());
        if(flag==true){
            //如果是本身状态有变化，那就是正常流程
            return true;
        }
        //端口变化需要过滤，未使用的端口。
        if(StatusInfoChangeTypeEnum.event_port_state.getCode().equals(info.getEventRedisKey())){
             Integer value  =Integer.valueOf(changeInfo.getValue().toString());
             //端口本身是断开的，但是redis中已经有端口状态的缓存
            if(((int)changeInfo.getValue()==2)&&(flag==false)){
              Object object=  eventInfoChangeManagerService.getStateValue(info.getEventRedisKey(),info.getMapKey());
              //如果没有事件说明不是异常的
              if(Objects.isNull(object)){
                  return false;
              }else{
                  //本身就存在异常
                  return true;
              }
            }
             //如果状态是断的，并且缓存为空，说明是第一次采集
            if(value==2&&flag==null){
               return false;
            }
        }
        //业务连接状态。
        if(StatusInfoChangeTypeEnum.event_CTC_link.getCode().equals(info.getEventRedisKey())){
            String value  =changeInfo.getValue().toString();
            if(!value.toLowerCase().equals("up")&&flag==false){
                Object object=  eventInfoChangeManagerService.getStateValue(info.getEventRedisKey(),info.getMapKey());
                //如果没有事件说明不是异常的
                if(Objects.isNull(object)){
                    return false;
                }else{
                    //本身就存在异常
                    return true;
                }
            }
            //如果状态是断的，并且缓存为空，说明是第一次采集
            if(!value.toLowerCase().equals("up")&&flag==null){
                return false;
            }
        }

        //业务主备机状态
        if(StatusInfoChangeTypeEnum.status_softMasterState.getCode() .equals(info.getEventRedisKey())){
            if(flag==false){
                Object object=  eventInfoChangeManagerService.getStateValue(info.getEventRedisKey(),info.getMapKey());
                //如果没有事件说明不是异常的
                if(Objects.isNull(object)){
                    return false;
                }else{
                    //本身就存在异常
                    return true;
                }
            }
            //如果状态是断的，并且缓存为空，说明是第一次采集
            if(flag==null){
                return true;
            }

        }

        //进程状态
        if(StatusInfoChangeTypeEnum.event_process_once.getCode().equals(info.getEventRedisKey())){

        }





        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
