package com.jcca.web.ip.controller.bean;

import lombok.Data;

import java.io.Serializable;

/*查询网络资源概述
 * */
@Data
public class QuryNetWorkSevenResp implements Serializable {
    private static final long serialVersionUID = 1L;
    // ip总数
    private int countIp;
    // 已经审批
    private int finishAUTH;
    // 未审批
    private int unfinishAUTH;
    // 已占用
    private int used;
    // 未占用
    private int unused;

    /**
     * 已审批百分比
     */
    private double situationAUTH;
    /**
     * 已占用百分比
     */
    private double situationUse;
}
