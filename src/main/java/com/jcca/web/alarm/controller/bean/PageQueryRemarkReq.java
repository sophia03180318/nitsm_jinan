package com.jcca.web.alarm.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class PageQueryRemarkReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String alarmCode;

    private String alarmId;

}
