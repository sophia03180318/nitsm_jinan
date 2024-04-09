package com.jcca.web2.vo;

import com.jcca.web2.constant.Web2Const;

/**
 * @description: 操作消息推送
 * @author: Lvyp
 * @create: 2023/11/16 09:45
 */
public class OptionSocketVo {

    private String code = Web2Const.STATISTICS_TOP_MSG_HANDLE;

    private String message;

    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = Web2Const.STATISTICS_TOP_MSG_HANDLE;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
