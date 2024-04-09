package com.jcca.web.event.controller.bean;

import lombok.Data;

import java.util.List;

/**
 * 删除事件类型
 *
 * @author lyp
 */
@Data
public class EventTypeRemoveReq {


    private List<String> idList;

}
