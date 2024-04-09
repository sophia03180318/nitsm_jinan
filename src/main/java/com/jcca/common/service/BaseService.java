package com.jcca.common.service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

/**
 * @author HanHW
 * @description 单表统一查询
 * @className BaseService
 * @date 2023/9/15 17:58
 * @since 2.0.6.0
 */
@Service
public class BaseService {

    /**
     * @description: 带条件的查询包装，若条件为空则返回相等查询包装
     * @author: HanHW
     * @date: 2023/9/16 15:36
     * @param: [queryReq, entity, conditionList]
     * queryReq: 查询条件，参数名需要与表实体类中属性一致
     * entity：表对应实体类
     * conditionList：非相等的查询条件，没有则传入null
     * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>
     **/
    public <T> QueryWrapper<T> getBaseQueryWrapper(Object queryReq, T entity, List<BaseQueryCondition> conditionList) {
        QueryWrapper<T> queryWrapper = this.getEqQueryWrapper(queryReq, entity);
        if (Objects.nonNull(conditionList)) {
            this.getConditionWrapper(queryWrapper, conditionList);
        }
        return queryWrapper;
    }

    /**
     * @description: 实体类做为查询参数
     * @author: HanHW
     * @date: 2023/9/19 10:32
     * @param: [entity]
     * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>
     **/
    public <T> QueryWrapper<T> getEqQueryWrapper(T entity) {
        QueryWrapper<T> query = Wrappers.query();
        Class<?> claz = entity.getClass();
        boolean annotationPresent = claz.isAnnotationPresent(TableName.class);
        if (!annotationPresent) return query;

        Field[] fields = claz.getDeclaredFields();
        for (int i = 0; i < fields.length - 1; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (Objects.isNull(value) || "".equals(value)) continue;

                TableField tableField = field.getAnnotation(TableField.class);
                if (Objects.nonNull(tableField) && tableField.exist()) {
                    query.eq(tableField.value(), value);
                    continue;
                }
                TableId tableId = field.getAnnotation(TableId.class);
                if (Objects.nonNull(tableId)) {
                    query.eq(tableId.value(), value);
                }
            } catch (Exception e) {
                throw new RuntimeException("基础服务实体类做查询参数异常-BaseService.getEqQueryWrapper");
            }
        }
        return query;
    }

    /**
     * @description: 返回相等查询包装
     * @author: HanHW
     * @date: 2023/9/16 15:35
     * @param: [queryReq, entity] 查询参数名需要与表实体类中属性一致
     * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>
     **/
    public <T> QueryWrapper<T> getEqQueryWrapper(Object queryReq, T entity) {
        try {
            BeanUtils.copyProperties(entity, queryReq);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("基础服务实体类等值查询异常-BaseService.getEqQueryWrapper");
        }
        return this.getEqQueryWrapper(entity);
    }

    /**
     * @description: 组装查询条件
     * @author: HanHW
     * @date: 2023/9/16 15:46
     * @param: [queryWrapper, conditionList]
     * @return: void
     **/
    private <T> void getConditionWrapper(QueryWrapper<T> queryWrapper, List<BaseQueryCondition> conditionList) {
        for (BaseQueryCondition condition : conditionList) {
            BaseQueryEnum queryEnum = condition.getCondition();
            switch (queryEnum) {
                case LE:
                    queryWrapper.le(condition.getColum(), condition.getValue());
                    break;
                case LT:
                    queryWrapper.lt(condition.getColum(), condition.getValue());
                    break;
                case GE:
                    queryWrapper.ge(condition.getColum(), condition.getValue());
                    break;
                case GT:
                    queryWrapper.gt(condition.getColum(), condition.getValue());
                    break;
                case NE:
                    queryWrapper.ne(condition.getColum(), condition.getValue());
                    break;
                case LIKE:
                    queryWrapper.like(condition.getColum(), condition.getValue());
                    break;
                case IN:
                    queryWrapper.in(condition.getColum(), condition.getValueList());
                    break;
                case ORDER_ASC:
                    List<Object> valueList1 = condition.getValueList();
                    for (Object v : valueList1) {
                        queryWrapper.orderByAsc(v.toString());
                    }
                    break;
                case ORDER_DESC:
                    List<Object> valueList2 = condition.getValueList();
                    for (Object v : valueList2) {
                        queryWrapper.orderByDesc(v.toString());
                    }
                    break;
            }
        }
    }

}
