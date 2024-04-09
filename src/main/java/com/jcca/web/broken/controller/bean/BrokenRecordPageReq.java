package com.jcca.web.broken.controller.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrokenRecordPageReq extends PageQuery {

    private static final long serialVersionUID = 1L;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 告警标题
     */
    private String alarmTitle;
    /**
     * 资产ip
     */
    private String assetIp;
    /**
     * 故障现象
     */
    private String description;
    /**
     * 故障开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurStartTime;
    /**
     * 故障结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurEndTime;
    /**
     * 创建开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createStartTime;
    /**
     * 创建结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createEndTime;
    /**
     * 状态
     * BrokenRecordConst
     */
    private Byte status;

}
