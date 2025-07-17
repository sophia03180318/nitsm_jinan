package com.jcca.web.cycles.service.bean;

import lombok.Data;

/**
 * @description: 周期处理异常
 * @author: Lvyp
 * @create: 2024/11/20 10:30
 */
@Data
public class CyclesException extends Exception {

    private String cyclesErrorCode;

    private String cyclesErrorMsg;

    public CyclesException(String message) {
        super(message);
        this.cyclesErrorMsg = message;
    }

    public CyclesException(String cyclesErrorCode, String cyclesErrorMsg) {
        super(cyclesErrorMsg);
        this.cyclesErrorMsg = cyclesErrorMsg;
        this.cyclesErrorCode = cyclesErrorCode;

    }
}
