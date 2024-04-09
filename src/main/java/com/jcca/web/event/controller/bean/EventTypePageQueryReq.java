package com.jcca.web.event.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventTypePageQueryReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String name;

    private String uniqueCode;

}
