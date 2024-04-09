package com.jcca.common.bean;

/**
 * @author Manager
 */
public class RestBean {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String NOLOG = "nolog";
    public static final String NOAUTH = "noauth";

    private String code;
    private String msg;
    private Object body;

    public RestBean() {
    }

    private RestBean(String code, String msg, Object body) {
        this.code = code;
        this.msg = msg;
        this.body = body;
    }

    public static RestBean ofSuccess(Object body) {
        return new RestBean(RestBean.SUCCESS, null, body);
    }

    public static RestBean ofSuccess(Object body, String msg) {
        return new RestBean(RestBean.SUCCESS, msg, body);
    }

    public static RestBean ofError(Object body, String msg) {
        return new RestBean(RestBean.ERROR, msg, body);
    }

    public static RestBean ofError(String msg) {
        return new RestBean(RestBean.ERROR, msg, null);
    }

    public static RestBean ofNolog(String msg) {
        return new RestBean(RestBean.NOLOG, msg, null);
    }

    public static RestBean ofNoauth(String msg) {
        return new RestBean(RestBean.NOAUTH, msg, null);
    }

    public static RestBean ofNoauth(String msg, String body) {
        return new RestBean(RestBean.NOAUTH, msg, body);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

}
