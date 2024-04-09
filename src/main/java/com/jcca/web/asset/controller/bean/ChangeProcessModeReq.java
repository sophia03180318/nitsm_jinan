package com.jcca.web.asset.controller.bean;

import lombok.Data;

/**
 * 更改进程模式
 *
 * @author syt
 * @date 2020-04-20 15:37:48
 **/
@Data
public class ChangeProcessModeReq implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /*
     *   进程数据ID
     */
    private String id;

    /*
     * 模式:1双机单活,2双机双活,3普通
     */
    private Integer mode;

    /*
     *   进程名称
     */
    private String processName;

}