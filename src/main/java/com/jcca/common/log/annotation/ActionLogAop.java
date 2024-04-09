package com.jcca.common.log.annotation;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Objects;

/**
 * 行为日志注解AOP
 *
 * @date 2018/11/12
 */
@Aspect
@Component
public class ActionLogAop {

    private Gson gson = new Gson();

    @Pointcut("@annotation(com.jcca.common.log.annotation.ActionLog)")
    public void actionLog() {
    }

    @AfterReturning(pointcut = "actionLog()", returning = "resultVo")
    public void recordLog(JoinPoint point, Object resultVo) {
        try {
            this.handActionLog(point, resultVo);
        } catch (Exception e) {

        }
    }

    private void handActionLog(JoinPoint point, Object resultVo) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method targetMethod = signature.getMethod();
        ActionLog anno = targetMethod.getAnnotation(ActionLog.class);
        String name = anno.name();
        String title = anno.title();
        String key = anno.key();

        // 封装日志实例对象
        SysActionLog actionLog = new SysActionLog();
        actionLog.setCreateTime(new Date());
        actionLog.setCreator("system");
        if (ShiroUtil.getSubject() != null) {
            actionLog.setCreator(ShiroUtil.getSubject().getUsername());
        }
        actionLog.setLogModel(title);
        actionLog.setLogName(name);
        actionLog.setLogType(Byte.parseByte(key));
        actionLog.setLogClass(point.getTarget().getClass().getName());
        actionLog.setLogMethod(targetMethod.getName());
        actionLog.setOperUserIp(ShiroUtil.getIp());
        actionLog.setRecordId("");
        actionLog.setLogMsg("成功");
        if (Objects.nonNull(resultVo)) {
            try {
                JSONObject jsonObject = JSONUtil.parseObj(resultVo);
                Object code = jsonObject.get("code");
                if (ResultEnum.SUCCESS.getCode() != Integer.parseInt(code.toString())) {
                    actionLog.setLogMsg("失败");
                }
            } catch (Exception e) {

            }
        }

        StringBuilder sb = new StringBuilder("[");
        Object[] args = point.getArgs();
        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if (i >= args.length) break;
            Object arg = args[i];
            String parameterName = parameterNames[i];
            if (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)) {
                if (i < parameterNames.length - 1) {
                    sb.append(parameterName).append(":").append(gson.toJson(arg)).append(",");
                } else {
                    sb.append(parameterName).append(":").append(gson.toJson(arg));
                }
            }
        }
        sb.append("]");
        int length = sb.toString().length();
        if (length < 1000) {
            actionLog.setRecordId(sb.toString());
        }

        SysActionLogService actionLogService = SpringContextUtil.getBean(SysActionLogService.class);
        // 本次动作的开始时间作为上次动作的结束时间.更新上次日志记录
        SysActionLog lastOne = actionLogService.getLatestOne();
        if (Objects.nonNull(lastOne)) {
            lastOne.setModifyTime(actionLog.getCreateTime());
            actionLogService.updateById(lastOne);
        }

        // 保存本次日志记录
        actionLog.setId(MyIdUtil.getId());
        actionLogService.save(actionLog);
    }
}
