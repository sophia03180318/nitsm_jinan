package com.jcca.component.event;

/**
 * 用于并发环境生成唯一ID
 *
 * @author lyp
 */
public interface CodeService {

    String getCode(String code, Long num);

}
