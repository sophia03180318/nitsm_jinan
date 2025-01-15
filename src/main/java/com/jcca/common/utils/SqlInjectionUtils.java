package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * @description: sql注入处理
 * @author: Lvyp
 * @create: 2025/01/09 18:36
 */
public class SqlInjectionUtils {

    /**
     * Like字段用此方法转义   如果自定义sql sql后写上 ESCAPE '\'
     *
     * @param parameter
     * @return
     */
    public static String formattingParameter(String parameter) {
        if (StrUtil.isEmpty(parameter)) {
            return parameter;
        }
        if (parameter.contains("%") || parameter.contains("_")) {
            return parameter.replace("%", "\\%").replace("_", "\\_");
        }
        return parameter;
    }

    /**
     * QueryWrapper 转义
     *
     * @param parameter
     * @return
     */
    public static String formattingQueryWrapper(QueryWrapper queryWrapper, String field, String parameter) {
        if (StrUtil.isEmpty(parameter)) {
            return parameter;
        }
        if (parameter.contains("%") || parameter.contains("_")) {
            queryWrapper.apply(field + " LIKE '%" + parameter.replace("%", "\\%").replace("_", "\\_") + "%' ESCAPE '\\'");
        } else {
            queryWrapper.like(field, parameter);
        }
        return parameter;
    }
}
