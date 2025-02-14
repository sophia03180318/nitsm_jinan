package com.jcca.common.log.annotation;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.service.SysActionLogService;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.service.DevLogService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author HanHW
 * @description 运维日志记录
 * @className DevLogAop
 * @date 2024/4/8 14:09
 * @since 2.1.0.0
 */
@Aspect
@Component
public class DevLogAop {

    @Pointcut("@annotation(com.jcca.common.log.annotation.DevLog)")
    public void devLog() {
    }

    @Before("devLog()")
    public void recordLogBefore(JoinPoint point) {
        try {
            this.beforeDevLog(point);
        } catch (Exception e) {

        }
    }

    private String dev = "";
    private SysActionLog actionLog;

    @AfterReturning(value = "devLog()", returning = "resultVo")
    public void recordLogAfter(JoinPoint point, Object resultVo) {
        try {
            this.afterDevLog(point, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afterDevLog(JoinPoint point, Object resultVo) {
        if (actionLog == null) {
            return;
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method targetMethod = signature.getMethod();
        DevLog anno = targetMethod.getAnnotation(DevLog.class);
        String devafter = anno.dev();
        if (!dev.equals(devafter)) {
            return;
        }

        if (Objects.nonNull(resultVo)) {
            JSONObject jsonObject = JSONUtil.parseObj(resultVo);
            Object data = jsonObject.get("data");
            Object code = jsonObject.get("code");
            if (data != null) {
                JSONObject jsonData = JSONUtil.parseObj(data);
                code = jsonData.get("code");
            }

            if (ResultEnum.SUCCESS.getCode() != Integer.parseInt(code.toString())
                    && ResultEnum.OUT_SUCCESS.getCode() != Integer.parseInt(code.toString())) {
                actionLog.setLogMsg("失败");
            }
        } else {
            actionLog.setLogMsg("失败");
        }

        SysActionLogService actionLogService = SpringContextUtil.getBean(SysActionLogService.class);
        SysActionLog lastOne = actionLogService.getLatestOne();
        if (Objects.nonNull(lastOne)) {
            lastOne.setModifyTime(actionLog.getCreateTime());
            actionLogService.updateById(lastOne);
        }
        actionLogService.save(actionLog);
    }

    private void beforeDevLog(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method targetMethod = signature.getMethod();
        DevLog anno = targetMethod.getAnnotation(DevLog.class);
        String name = anno.name();
        String title = anno.title();
        String key = anno.key();

        // 封装日志实例对象
        actionLog = new SysActionLog();
        actionLog.setId(MyIdUtil.getId());
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

        StringBuilder sb = new StringBuilder("[");
        Object[] args = point.getArgs();
        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if (i >= args.length) break;
            Object arg = args[i];
            String parameterName = parameterNames[i];
            if (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)) {
                if (i < parameterNames.length - 1) {
                    sb.append(parameterName).append(":").append(JSONUtil.toJsonStr(arg)).append(",");
                } else {
                    sb.append(parameterName).append(":").append(JSONUtil.toJsonStr(arg));
                }
            }
        }
        sb.append("]");
        int length = sb.toString().length();
        if (length < 1000) {
            actionLog.setRecordId(sb.toString());
        }

        dev = anno.dev();
        this.setLog(dev, actionLog, args);
    }


    @Resource
    private List<DevLogService> devLogServiceList;

    private Map<String, DevLogService> devLogServiceMap;

    private void setLog(String devType, SysActionLog actionLog, Object[] args) {
        if (Objects.isNull(devLogServiceMap)) {
            devLogServiceMap = new HashMap<>();
            for (DevLogService devLogService : devLogServiceList) {
                String code = devLogService.getDevType();
                devLogServiceMap.put(code, devLogService);
            }
        }
        DevLogService logService = devLogServiceMap.get(devType);
        if (Objects.nonNull(logService)) {
            logService.setDevLog(actionLog, args);
        }
    }
}
