package com.jcca.common.exception.adapter;


/**
 * 未找到合适的适配器异常
 *
 * @description: 未找到合适的适配器异常
 * @author: Lvyp
 * @create: 2023/11/02 10:12
 */
public class AdapterMissingException extends Exception {


    public AdapterMissingException(String message) {
        super(message);
    }

    public AdapterMissingException(String message, Throwable cause) {
        super(message, cause);
    }

}
