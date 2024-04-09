package com.jcca.web.construction.controller.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 分页查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConstructionRecordPageReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * 组织ID syt
     */
    private String orgId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM", timezone = "GMT+8")
    private Date startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM", timezone = "GMT+8")
    private Date endTime;

}
