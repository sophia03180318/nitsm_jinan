package com.jcca.web2.adapter.cabinet;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 机柜内设备详情的处理器
 * @author: Lvyp
 * @create: 2023/10/26 10:58
 */
@Component
public class CabinetAssetInfoHeader implements ApplicationContextAware {

    /**
     * 所有支持型号补充的实现
     */
    private Map<Integer, CabinetAssetInfoAdapter> cabinetAssetInfoAdapterMap = null;

    /**
     * 填充vo信息
     *
     * @param vo
     * @return
     */
    public CabinetAssetInfoVo fattenCabinetInfo(CabinetAssetInfoVo vo) {
        CabinetAssetInfoAdapter cabinetAssetInfoAdapter = cabinetAssetInfoAdapterMap.get(vo.getAssetMode());
        if (Objects.isNull(cabinetAssetInfoAdapter)) {
            return vo;
        }
        return cabinetAssetInfoAdapter.fattenInfo(vo);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (Objects.isNull(cabinetAssetInfoAdapterMap)) {
            Map<String, CabinetAssetInfoAdapter> beanMap = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, CabinetAssetInfoAdapter.class, true, false);
            cabinetAssetInfoAdapterMap = new HashMap<>(beanMap.size());
            for (Map.Entry<String, CabinetAssetInfoAdapter> entry : beanMap.entrySet()) {
                List<Integer> modelArray = entry.getValue().getModel();
                for (Integer model : modelArray) {
                    if (cabinetAssetInfoAdapterMap.containsKey(model)) {
                        throw new RuntimeException(
                                "机柜详情处理适配器失败,code[" + model + "] 发现多个适配器");
                    }
                    cabinetAssetInfoAdapterMap.put(model, entry.getValue());
                }
            }

            AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, null, "机柜详情处理适配器初始化成功");

        }
    }
}
