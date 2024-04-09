package com.jcca.common.config.thymeleaf.utility;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.SpringContextUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @date 2018/10/16
 */
public class LogUtil {

    /**
     * 获取实体对象的日志
     *
     * @param entity 实体对象
     */
    public List<SysActionLog> entityList(Object entity) {
        SysActionLogService actionLogService = SpringContextUtil.getBean(SysActionLogService.class);
        TableName table = entity.getClass().getAnnotation(TableName.class);
        String tableName = table.value();
        try {
            Object object = EntityBeanUtil.getField(entity, "id");
            String entityId = String.valueOf(object);
            QueryWrapper<SysActionLog> wrapper = new QueryWrapper<>();

            SysActionLog log = new SysActionLog();
            log.setLogModel(tableName);
            log.setRecordId(entityId);
            wrapper.setEntity(log);
            return actionLogService.list(wrapper);
        } catch (InvocationTargetException | IllegalAccessException e) {

        }
        return null;
    }
}
