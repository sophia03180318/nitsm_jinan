package com.jcca.admin.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ Author：sophia
 * @ Date：Created in 15:58 2021/8/24
 * @ Description:
 */
@Data
public class MinuteVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String prefix;
    private String minute;
    private String suffix;
    private String ip;
    private String community;
    private String type;


}
