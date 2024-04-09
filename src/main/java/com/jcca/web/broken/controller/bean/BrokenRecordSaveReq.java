package com.jcca.web.broken.controller.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 保存故障记录请求参数
 *
 * @author lyp
 */
@Data
public class BrokenRecordSaveReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = false, hidden = true)
    private String id;
    /**
     * 资产Id
     */
    @NotEmpty(message = "请选择资产")
    private String assetId;
    /**
     * 故障现象描述
     */
    @NotEmpty(message = "请输入故障现象描述")
    private String description;
    /**
     * 故障原因
     */
    private String reason;
    /**
     * 故障发生时间
     */
    @NotNull(message = "请选择故障发生时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
}
