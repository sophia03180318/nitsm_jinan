package com.jcca.component.other;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.component.dto.ReceiveAlarmDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName AlarmHandler
 * @Description 处理采集器推送过来的告警
 * @Date 2020/6/8 12:39
 * @Author hanwone
 */
@Slf4j
@Component
public class AlarmHandler implements ApplicationContextAware {

    Map<String, AlarmAdapter> alarmAdapterMap;

    public void alarmHandle(String code, ReceiveAlarmDto alarmDto) {
        log.info("ITSM收到原始告警消息{}", JSONUtil.toJsonStr(alarmDto));
        AlarmAdapter alarmAdapter = alarmAdapterMap.get(code);
        Assert.notNull(alarmAdapter, "未找到code为[" + code + "]的适配器");

        alarmAdapter.handle(alarmDto);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (alarmAdapterMap == null) {
            Map<String, AlarmAdapter> beanMap = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, AlarmAdapter.class, true, false);
            alarmAdapterMap = new HashMap<>(beanMap.size());
            for (Map.Entry<String, AlarmAdapter> entry : beanMap.entrySet()) {
                if (alarmAdapterMap.containsKey(entry.getValue().getCode())) {
                    throw new RuntimeException(
                            "初始化告警处理适配器失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
                } else {
                    alarmAdapterMap.put(entry.getValue().getCode(), entry.getValue());
                }
            }

            if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
                LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "初始化告警处理适配器完成:" + alarmAdapterMap.size());
            }
        }
    }
}
