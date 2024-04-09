package com.jcca.admin.biz.entity;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author syt
 * @date 2021/08/18  10:32
 * @classname nitsmcom.jcca.admin.biz.entityRoomReq
 */
@Data
public class RoomReq {

    /*
     * 机房ID
     * */
    private String id;
    /* 组织ID
     * */
    @NotEmpty(message = "请选择组织!")
    private String orgId;

    /**
     * 机房名称","号分隔多个名称
     */
    @NotEmpty(message = "请填写名称!")
    private String name;
    /*
     * 描述
     * */
    private String remark;
}
