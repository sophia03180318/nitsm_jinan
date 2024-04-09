package com.jcca.web.xunjian.adapter.v2;

import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
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
 * 巡检
 */
@Slf4j
@Component
public class XunjianV2Handler implements ApplicationContextAware {

    /**
     * 巡检命令执行器
     */
    public Map<String, XunjianV2Adapter> xunjianDisposeMap;


    public XunjianDetailV2 xunjian(String code, Asset asset, String xunjianRecordId,String orgMsg) {
        XunjianV2Adapter xunjianV2Adapter = xunjianDisposeMap.get(code);
        if(Objects.isNull(xunjianV2Adapter)){
            return null;
        }
        String[] split = code.split(":");
        XunjianDetailV2 xunjianDetailV2 = xunjianV2Adapter.xunJian(asset, xunjianRecordId, orgMsg);
        if(Objects.isNull(xunjianDetailV2)){
            return null;
        }
        xunjianDetailV2.setXunjianAdapter(split[0]);
        return xunjianDetailV2;
    }

    /**
     * 获取指标名称
     * @param code
     * @return
     */
    public String getTargetName(String code,Asset asset) {
        XunjianV2Adapter xunjianV2Adapter = xunjianDisposeMap.get(code);
        if(Objects.isNull(xunjianV2Adapter)){
            return "";
        }
        return xunjianV2Adapter.getName(asset);
    }

    /**
     * 获取指标命令
     * @param code
     * @return
     */
    public String getCommand(String code) {
        XunjianV2Adapter xunjianV2Adapter = xunjianDisposeMap.get(code);
        if(Objects.isNull(xunjianV2Adapter)){
            return "";
        }
        return xunjianV2Adapter.getCommand();
    }

    /**
     * 获取边界值
     * @param code
     * @return
     */
    public String getMaxValue(String code) {
        XunjianV2Adapter xunjianV2Adapter = xunjianDisposeMap.get(code);
        if (Objects.isNull(xunjianV2Adapter) || Objects.isNull(xunjianV2Adapter.getMaxValue())) {
            return "";
        }
        return xunjianV2Adapter.getMaxValue().toString();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (Objects.nonNull(xunjianDisposeMap)) {
            return;
        }
        Map<String, XunjianV2Adapter> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                XunjianV2Adapter.class);
        xunjianDisposeMap = new HashMap<>(beanMap.size());
        for (Map.Entry<String, XunjianV2Adapter> entry : beanMap.entrySet()) {
            if (xunjianDisposeMap.containsKey(entry.getValue().getCode())) {
                throw new RuntimeException("初始化巡检适配器V2失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
            } else {
                xunjianDisposeMap.put(entry.getValue().getCode(), entry.getValue());
            }
        }
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "初始化巡检处理适配器V2完成:" + xunjianDisposeMap.size()));
        }
    }
}
