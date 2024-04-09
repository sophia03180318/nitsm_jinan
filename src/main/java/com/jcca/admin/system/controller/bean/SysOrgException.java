package com.jcca.admin.system.controller.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author syt
 * @date 2021/08/19  10:43
 * @classname nitsmcom.jcca.admin.system.controller.beanSysOrgException
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysOrgException extends Exception {
    private Integer code;
    private String message;

}
