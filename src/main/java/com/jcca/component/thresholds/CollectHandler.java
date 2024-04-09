package com.jcca.component.thresholds;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONArray;
import com.google.gson.Gson;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 处理适配
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class CollectHandler implements ApplicationContextAware {

    /**
     * 采集的处理集合
     */
    public Map<String, CollectAdapter> collectDisposeMap;

    private String data;

    private Gson gson = new Gson();

    public void disposeData(String code, JSONArray data) {
        CollectAdapter collectAdapter = collectDisposeMap.get(code);
        Assert.notNull(collectAdapter, "未找到code为[" + code + "]的采集适配器");

        collectAdapter.dispose(data);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (Objects.nonNull(collectDisposeMap)) {
            return;
        }
        Map<String, CollectAdapter> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                CollectAdapter.class);
        collectDisposeMap = new HashMap<>(beanMap.size());
        for (Map.Entry<String, CollectAdapter> entry : beanMap.entrySet()) {
            if (collectDisposeMap.containsKey(entry.getValue().getCode())) {
                throw new RuntimeException("初始化告警处理适配器失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
            } else {
                collectDisposeMap.put(entry.getValue().getCode(), entry.getValue());
            }
        }

        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "初始化采集处理适配器完成:" + collectDisposeMap.size());
        }
    }

}
