package com.jcca.admin.biz.vo;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 11:08 2022/9/30
 * @ Description:
 */
@Data
public class MetaDataRep {
    private Integer metaType;
    private String metaMsg;
    private String command;
    private String ttname;
    private String ccname;
    private String entity;
    private Integer index;


}
