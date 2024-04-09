package com.jcca.web.asset.detail;

import cn.hutool.core.lang.Assert;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.web.asset.entity.Asset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName DetailHandler
 * @Description 资产详情公共方法
 * @Date 2020/6/22 13:42
 * @Author hanwone
 */
@Slf4j
@Component
public class DetailHandler implements ApplicationContextAware {

    Map<String, DetailAdapter> detailAdapterMap;

    public ResultVo handle(String code, Asset asset) {
        DetailAdapter thresholdAdapter = detailAdapterMap.get(code);
        Assert.notNull(thresholdAdapter, "未找到code为[" + code + "]的适配器");

        return thresholdAdapter.handle(asset);
    }

    public ResultVo getAssetGeneralInfo(String code, Asset asset) {
        DetailAdapter thresholdAdapter = detailAdapterMap.get(code);
        Assert.notNull(thresholdAdapter, "未找到code为[" + code + "]的适配器");
        return thresholdAdapter.getAssetGeneralInfo(asset);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (detailAdapterMap == null) {
            Map<String, DetailAdapter> beanMap = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, DetailAdapter.class, true, false);
            detailAdapterMap = new HashMap<>(beanMap.size());
            for (Map.Entry<String, DetailAdapter> entry : beanMap.entrySet()) {
                if (detailAdapterMap.containsKey(entry.getValue().getCode())) {
                    throw new RuntimeException(
                            "初始化资产详情适配器失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
                } else {
                    detailAdapterMap.put(entry.getValue().getCode(), entry.getValue());
                }
            }
            if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
                log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "初始化资产详情适配器完成:" + detailAdapterMap.size()));
            }
        }
    }
}
