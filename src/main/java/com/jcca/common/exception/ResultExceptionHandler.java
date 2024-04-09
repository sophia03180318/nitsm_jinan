package com.jcca.common.exception;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.UnknownSessionException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 全局统一异常处理
 *
 * @author hanwone
 * @date 2018/8/14
 */
@ControllerAdvice
@Slf4j
public class ResultExceptionHandler {

    /**
     * 拦截自定义异常
     */
    @ExceptionHandler(ResultException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResultVo<Object> resultException(ResultException e) {
        return ResultVoUtil.error(e.getCode(), e.getMessage());
    }

    /**
     * 账户已锁定
     **/
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> authenticationException(AuthenticationException e) {
        AppLogUtils.buildLogError(LogFunctionEnum.LOGIN_WEB, "账户已锁定或不存在", null);
        return ResultVoUtil.error(ResultEnum.ACCOUNT_FREEZED.getCode(), ResultEnum.ACCOUNT_FREEZED.getMessage());
    }

    /**
     * 拦截表单验证异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResultVo<Object> bindException(BindException e) {
        AppLogUtils.buildLogError(LogFunctionEnum.SELF_EXCEPTION_BIND, "表单参数异常", null);
        List<FieldError> fieldErrors = e.getFieldErrors();
        for (FieldError error : fieldErrors) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), error.getDefaultMessage());
        }
        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), ResultEnum.PARAM_ERROR.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> runtimeException(RuntimeException e) {
        AppLogUtils.buildLogError(LogFunctionEnum.SELF_EXCEPTION_RUNTIME, "运行时未知系统异常", e);
        return ResultVoUtil.error(ResultEnum.ERROR.getCode(), ResultEnum.ERROR.getMessage());
    }

    /**
     * 没有权限 异常
     */
    @ExceptionHandler({UnauthenticatedException.class, AuthorizationException.class, UnauthorizedException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> processUnauthenticatedException(HttpServletRequest request) {
        AppLogUtils.buildLogError(LogFunctionEnum.SELF_EXCEPTION_UNAUTH, "接口无权限：" + request.getRequestURI(), null);
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.isAuthenticated()) {
//            subject.logout();
//        }
        return ResultVoUtil.error(ResultEnum.NO_PERMISSIONS.getCode(), ResultEnum.NO_PERMISSIONS.getMessage(), request.getRequestURI());
    }

    /**
     * 用户名或密码错误
     *
     * @return
     */
    @ExceptionHandler({IncorrectCredentialsException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> tokenExpiredException() {
        return ResultVoUtil.error(ResultEnum.USERNAME_PWD_ERROR.getCode(), ResultEnum.USERNAME_PWD_ERROR.getMessage());
    }

    /**
     * 账号已在其他地方登录
     *
     * @return
     */
    @ExceptionHandler({UnknownSessionException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> unknownSessionException() {

        return ResultVoUtil.error(ResultEnum.MANY_LOGIN.getCode(), ResultEnum.MANY_LOGIN.getMessage());
    }

    /**
     * session过期异常
     *
     * @return
     */
    @ExceptionHandler({ExpiredSessionException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> expiredSessionException() {

        return ResultVoUtil.error(ResultEnum.TOKEN_EXPIRY.getCode(), ResultEnum.TOKEN_EXPIRY.getMessage());
    }

    /**
     * 拦截其他异常
     *
     * @return
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> otherException(Exception e) {
        AppLogUtils.buildLogError(LogFunctionEnum.SELF_EXCEPTION, "未知系统异常", e);
        return ResultVoUtil.error(ResultEnum.ERROR.getCode(), ResultEnum.ERROR.getMessage());
    }

    /**
     * 连接超时
     *
     * @return
     */
    @ExceptionHandler({SocketTimeoutException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResultVo<Object> timeOutException(HttpServletRequest request) {
        AppLogUtils.buildLogError(LogFunctionEnum.SELF_EXCEPTION_TIMEOUT, "连接超时异常：" + request.getRequestURL(), null);
        return ResultVoUtil.error(ResultEnum.TIME_OUT_ERROR.getCode(), ResultEnum.TIME_OUT_ERROR.getMessage(), request.getRequestURI());
    }

    /**
     * 参数校验异常
     *
     * @param e
     * @return
     */
    @SuppressWarnings("rawtypes")
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResultVo handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        AppLogUtils.buildLogError(LogFunctionEnum.SELF_EXCEPTION_PARAM, "参数校验异常", null);
        BindingResult bindingResult = e.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), fieldError.getDefaultMessage());
        }
        return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), ResultEnum.PARAM_ERROR.getMessage());
    }

}
