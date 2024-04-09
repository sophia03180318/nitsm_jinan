package com.jcca.web.handbook.controller.bean;

import lombok.Data;

/**
 * @ClassName CabinetTreeVo
 * @Author syt
 * @Date 2021/11/5  11:52
 */
@Data
public class CabinetTreeVo {
    private String id;
    private String title;
    private String pTitle;
    private String pid;
    private String orgId;
    private byte type;
}
