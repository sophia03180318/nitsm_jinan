package com.jcca.component.casco;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.component.dto.ItsmQueueReq;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 第三方业务告警处理器
 * 包括：阈值告警，连接告警，主备切换
 *
 * @author hanwone
 * @date 2021-01-07
 */
@Slf4j
//@Component
public class BrokerAlarmHandler implements ApplicationContextAware {

    @Resource
    private AssetService assetService;

    Map<String, BrokerAlarmAdapter> alarmAdapterMap;

    public void alarmHandle(String code, ItsmQueueReq alarmDto) {
        log.info("ITSM收到第三方业务原始告警消息{}", JSONUtil.toJsonStr(alarmDto));
        if (Objects.isNull(alarmDto.getAssetId())) {
            log.error("第三方业务告警消息缺少资产ID{}", JSONUtil.toJsonStr(alarmDto));
            return;
        }

        BrokerAlarmAdapter alarmAdapter = alarmAdapterMap.get(code);
        Assert.notNull(alarmAdapter, "未找到code为[" + code + "]的第三方业务适配器");

        Asset asset = assetService.getById(alarmDto.getAssetId());
        Assert.notNull(asset, "ID为[" + alarmDto.getAssetId() + "]的资产不存在");

        alarmAdapter.handle(alarmDto);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (alarmAdapterMap == null) {
            Map<String, BrokerAlarmAdapter> beanMap = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, BrokerAlarmAdapter.class, true, false);
            alarmAdapterMap = new HashMap<>(beanMap.size());
            for (Map.Entry<String, BrokerAlarmAdapter> entry : beanMap.entrySet()) {
                if (alarmAdapterMap.containsKey(entry.getValue().getCode())) {
                    throw new RuntimeException(
                            "初始化第三方业务告警处理适配器失败,code[" + entry.getValue().getCode() + "] 发现多个适配器");
                } else {
                    alarmAdapterMap.put(entry.getValue().getCode(), entry.getValue());
                }
            }

            if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
                log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "初始化第三方业务告警处理适配器完成:" + alarmAdapterMap.size()));
            }
        }
    }
}
