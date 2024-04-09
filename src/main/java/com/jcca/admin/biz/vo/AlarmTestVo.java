package com.jcca.admin.biz.vo;

import lombok.Data;

/**
 * @ClassName AlarmTestVo
 * @Description 告警测试
 * @Date 2020/6/15 10:52
 * @Author hanwone
 */
@Data
public class AlarmTestVo {

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 总数
     */
    private Long total;
    /**
     * 已用
     */
    private Long used;
    /**
     * 可用
     */
    private Long available;
    /**
     * 0：无/断，1：有/通
     */
    private int status;
    /**
     * 类别  ReceiveThresholdConst
     */
    private int category;

}
