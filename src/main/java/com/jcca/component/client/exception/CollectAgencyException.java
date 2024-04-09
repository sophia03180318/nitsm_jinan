package com.jcca.component.client.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 采集异常
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CollectAgencyException extends Throwable {

    private static final long serialVersionUID = 1L;

    /**
     * 异常枚举
     *
     * @author Lvyp
     */
    @Getter
    public enum CollectAgencyEnum {
        GET_MASTER_IP_ERRO("获取masterIP失败"),
        GET_MASTER_NET_ERRO("请求中心采集器可能发生了网络问题，无法获取到任何结果"),
        RESPONSE_FORMAT_ERRO("采集器响应信息格式错误"),
        REQ_PARAM_ERRO("请求参数校验错误"),
        REQ_COLLECT_HTTP("请求采集器异常");

        private String msg;

        private CollectAgencyEnum(String msg) {
            this.msg = msg;
        }

    }

    /**
     * 异常编号
     */
    private String code;
    /**
     * 异常信息
     */
    private String msg;

    public CollectAgencyException(CollectAgencyEnum agencyEnum) {
        super();
        this.code = agencyEnum.name();
        this.msg = agencyEnum.getMsg();
    }

    public CollectAgencyException(String code, String msg) {
        super();
        this.code = code;
        this.msg = String.format("错误信息：%s,错误码：%s", StrUtil.isEmpty(msg) ? "--" : msg, code);
    }

    @Override
    public String toString() {
        return "CollectAgencyException [code=" + code + ", msg=" + msg + "]";
    }

}
