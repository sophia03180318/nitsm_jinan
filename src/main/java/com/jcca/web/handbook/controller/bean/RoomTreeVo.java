package com.jcca.web.handbook.controller.bean;

import lombok.Data;

/**
 * @ClassName RoomVo
 * @Author syt
 * @Date 2021/11/5  11:52
 */
@Data
public class RoomTreeVo {
    private String id;
    private String pid;
    private String title;
    private String pTitle;
    private byte type;
}
