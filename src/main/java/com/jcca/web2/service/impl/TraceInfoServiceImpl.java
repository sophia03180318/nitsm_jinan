package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.alarm.dao.AlarmInfoMapper;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web2.dao.TraceInfoMapper;
import com.jcca.web2.entity.TraceInfo;
import com.jcca.web2.service.TraceInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @description:
 * @author: sophia
 * @create: 2024/12/24 15:24
 **/
@Service
public class TraceInfoServiceImpl extends ServiceImpl<TraceInfoMapper, TraceInfo>  implements TraceInfoService {

    @Resource
    private TraceInfoMapper traceInfoMapper;
    @Resource
    private AlarmInfoMapper alarmInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void alarmTransform(TraceInfo info) throws ResultException {
        if(StrUtil.isEmpty(info.getAlarmId())){
            throw new ResultException(ResultEnum.TRACE_ALARM_ID_NULL);
        }
        if(StrUtil.isEmpty(info.getContent())){
            throw new ResultException(ResultEnum.TRACE_CONTENT_NULL);
        }
        AlarmInfo alarmInfo = alarmInfoMapper.selectById(info.getAlarmId());
        if(Objects.isNull(alarmInfo)){
            throw new ResultException(ResultEnum.TRACE_ALARM_STATUS_ERROR);
        }

        info.setCreator(ShiroUtil.getSubject().getUsername());
        info.setId(MyIdUtil.getId());
        traceInfoMapper.insert(info);

        alarmInfo.setTraceStatus(1);
        alarmInfoMapper.updateById(alarmInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void dispose(TraceInfo info) {
        if(StrUtil.isEmpty(info.getAlarmId())){
            throw new ResultException(ResultEnum.TRACE_ALARM_ID_NULL);
        }
        if(StrUtil.isEmpty(info.getContent())){
            throw new ResultException(ResultEnum.TRACE_CONTENT_NULL);
        }
        AlarmInfo alarmInfo = alarmInfoMapper.selectById(info.getAlarmId());
        if(Objects.isNull(alarmInfo)){
            throw new ResultException(ResultEnum.TRACE_ALARM_STATUS_ERROR);
        }
        if(Objects.isNull(alarmInfo.getTraceStatus()) || alarmInfo.getTraceStatus() == 2){
            throw new ResultException(ResultEnum.TRACE_ALARM_PROHIBIT_OPTION);
        }
        if (info.getStatus() == 2) {//触发结束
            alarmInfo.setTraceStatus(2);
            alarmInfoMapper.updateById(alarmInfo);
        }
        info.setCreator(ShiroUtil.getSubject().getUsername());
        info.setId(MyIdUtil.getId());
        traceInfoMapper.insert(info);
    }
}