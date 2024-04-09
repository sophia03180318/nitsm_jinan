package com.jcca.web.auth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName SysOrgVo
 * @Description TODO
 * @Date 2020/4/22 15:39
 * @Author hanwone
 */
@Data
public class SysOrgVo implements Serializable {

    private String id;
    private String pid;
    private String pids;
    private String title;
    private Byte sort;
    private Byte status;
}
