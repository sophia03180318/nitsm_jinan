package com.jcca.web.auth.vo;

import lombok.Data;

/**
 * @ClassName UserJoson
 * @Description TODO
 * @Date 2020/4/15 14:14
 * @Author hanwone
 */
@Data
public class SysUserVo {

    private String username;
    private String nickname;
    private Byte gender;
    private String picture;
    private String phone;

}
