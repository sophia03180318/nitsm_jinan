package com.jcca.common.exception.adapter;

import org.springframework.beans.BeansException;

/**
 * @description: 适配器初始化异常
 * @author: Lvyp
 * @create: 2023/11/02 10:09
 */
public class AdapterInitException extends BeansException {


    public AdapterInitException(String message) {
        super(message);
    }
}
