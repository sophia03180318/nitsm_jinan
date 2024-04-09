package com.jcca.web.auth.constant;

/**
 * @author hanwone
 */
public interface TokenConst {

    /**
     * token密钥
     */
    String TOKEN_SECRECT = "thisISAsecrectCOde";

    /**
     * 过期时间,单位：天
     */
    Integer TOKEN_EXPIRY_AMOUNT = 99999;
}
