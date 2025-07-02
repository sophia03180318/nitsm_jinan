package com.jcca.common.config.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jcca.admin.system.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * @ClassName CommonDataHandler
 * @Description 自动加入审计字段 create_time,creator,modify_time,modifier
 * @Date 2020/4/9 16:08
 * @Author hanwone
 */
@Slf4j
@Component
public class CommonDataHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        boolean creator = metaObject.hasSetter("creator");
        boolean modifier = metaObject.hasSetter("modifier");
        if (creator && modifier) {
            Subject subject = ThreadContext.getSubject();
            String username = "系统";
            if (Objects.nonNull(subject) && Objects.nonNull(subject.getPrincipal())) {
                SysUser user = (SysUser) subject.getPrincipal();
                username = user.getUsername();
            }
            this.setInsertFieldValByName("creator", username, metaObject);
            this.setInsertFieldValByName("modifier", username, metaObject);
        }

        boolean createTime = metaObject.hasSetter("createTime");
        if (createTime) {
            this.setInsertFieldValByName("createTime", new Date(), metaObject);
        }

        boolean modifyTime = metaObject.hasSetter("modifyTime");
        if (modifyTime) {
            this.setInsertFieldValByName("modifyTime", new Date(), metaObject);
        }

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            boolean modifier = metaObject.hasSetter("modifier");
            if (modifier) {
                Subject subject = ThreadContext.getSubject();
                String username = "系统";
                if (Objects.nonNull(subject) && Objects.nonNull(subject.getPrincipal())) {
                    SysUser user = (SysUser) subject.getPrincipal();
                    username = user.getUsername();
                }
                this.setUpdateFieldValByName("modifier", username, metaObject);
            }

            boolean modifyTime = metaObject.hasSetter("modifyTime");
            if (modifyTime) {
                this.setUpdateFieldValByName("modifyTime", new Date(), metaObject);
            }
        }catch (Exception e){
            log.error("自动更新用户名报错，多线程导致触发此类报警："+e.getMessage());
        }
    }
}
