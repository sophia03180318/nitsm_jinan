package com.jcca.web.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.common.utils.HttpServletUtil;
import com.jcca.common.utils.ToolUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName TokenUtil
 * @Description token工具类
 * @Date 2020/4/16 9:26
 * @Author hanwone
 */
@Slf4j
public class TokenUtil {

    /**
     * 生成JwtToken
     *
     * @param username 用户名
     * @param secret   秘钥
     * @param amount   过期天数
     */
    public static String getToken(String username, String secret, int amount) {
        SysUser user = new SysUser();
        user.setUsername(username);
        return getToken(user, secret, amount);
    }

    /**
     * 生成JwtToken
     *
     * @param user   用户对象
     * @param secret 秘钥
     * @param amount 过期天数
     */
    public static String getToken(SysUser user, String secret, int amount) {
        // 过期时间
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, amount);

        // 随机Claim
        String random = ToolUtil.getRandomString(6);

        // 创建JwtToken对象
        String token = "";
        token = JWT.create()
                // 用户名
                .withSubject(user.getUsername())
                // 发布时间
                .withIssuedAt(new Date())
                // 过期时间
                .withExpiresAt(ca.getTime())
                // 自定义随机Claim
                .withClaim("ran", random)
                .sign(getSecret(secret, random));

        return token;
    }

    /**
     * 获取请求对象中的token数据
     */
    public static String getRequestToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    /**
     * 获取当前token中的用户名
     */
    public static String getSubject() {
        HttpServletRequest request = HttpServletUtil.getRequest();
        String token = getRequestToken(request);
        return JWT.decode(token).getSubject();
    }

    /**
     * 验证JwtToken
     *
     * @param token JwtToken数据
     * @return true 验证通过
     * @throws JWTVerificationException 令牌无效（验证不通过）
     */
    public static boolean verifyToken(String token, String secret) throws JWTVerificationException {
        String ran = JWT.decode(token).getClaim("ran").asString();
        JWTVerifier jwtVerifier = JWT.require(getSecret(secret, ran)).build();
        try {
            jwtVerifier.verify(token);
            return true;
        } catch (Exception e) {
            log.error("用户[{}]token验证失败", JWT.decode(token).getSubject());
        }
        return false;
    }

    /**
     * 生成Secret混淆数据
     */
    private static Algorithm getSecret(String secret, String random) {
        String salt = "生若夏花死若静草";
        return Algorithm.HMAC256(secret + salt + random);
    }
}
