package com.jcca.web2.adapter.db;

import com.jcca.common.exception.adapter.AdapterInitException;
import com.jcca.common.exception.adapter.AdapterMissingException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web2.dto.ExeSqlQuery;
import com.jcca.web2.vo.AssetDataVo;
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
 * @description: 数据源执行器
 * @author: Lvyp
 * @create: 2023/11/02 09:51
 */
@Component
public class DbSourceHeader implements ApplicationContextAware {

    /**
     * 所有支持型号补充的实现
     */
    private Map<String, DbSourceAdapter> sourceAdapterMap = null;


    /**
     * 执行数据查询，从不同的数据源中
     *
     * @param query
     * @return
     * @throws AdapterMissingException
     */
    public List<AssetDataVo> exeCommand(ExeSqlQuery query) throws AdapterMissingException {
        DbSourceAdapter dbSourceAdapter = sourceAdapterMap.get(query.getDbSource());
        if (Objects.isNull(dbSourceAdapter)) {
            throw new AdapterMissingException("系统没有找到一个code为【" + query.getDbSource() + "】的适配器");
        }
        return dbSourceAdapter.exeCommand(query);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (Objects.isNull(sourceAdapterMap)) {
            Map<String, DbSourceAdapter> beanMap = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, DbSourceAdapter.class, true, false);
            sourceAdapterMap = new HashMap<>(beanMap.size());
            for (Map.Entry<String, DbSourceAdapter> entry : beanMap.entrySet()) {
                if (sourceAdapterMap.containsKey(entry.getValue().getAdapterCode())) {
                    throw new AdapterInitException(
                            "数据源处理适配器失败,code[" + entry.getValue().getAdapterCode() + "] 发现多个适配器");
                }
                sourceAdapterMap.put(entry.getValue().getAdapterCode(), entry.getValue());
            }

            AppLogUtils.buildLogInfo(LogFunctionEnum.DEFAULT_CONFIG, null, "数据源适配器初始化成功");

        }
    }
}
