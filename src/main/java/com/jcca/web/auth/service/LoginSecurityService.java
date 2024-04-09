package com.jcca.web.auth.service;

import com.jcca.common.exception.ResultException;

/**
 * 登录安全验证
 *
 * @author lyp
 */
public interface LoginSecurityService {

    /**
     * 账号安全策略验证
     *
     * @param userId
     * @param remoteAddr
     * @throws ResultException
     */
    void securityVerify(String userId, String remoteAddr) throws ResultException;

}
