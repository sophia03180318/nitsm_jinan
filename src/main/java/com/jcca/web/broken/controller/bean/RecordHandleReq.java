package com.jcca.web.broken.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName RecordHandleReq
 * @Description 故障记录处理
 * @Date 2020/6/28 17:14
 * @Author hanwone
 */
@Data
public class RecordHandleReq {

    /**
     * 故障记录ID
     */
    @NotNull(message = "ID不能为空")
    private String id;
    /**
     * 处理意见
     */
    private String opinion;
    /**
     * 是否完成，0未完成，1完成
     */
    private int flag;
}
