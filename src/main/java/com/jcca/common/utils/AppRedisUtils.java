package com.jcca.common.utils;

import org.springframework.data.redis.connection.RedisConnection;

public class AppRedisUtils {

    /**
     * 检测 redis 的有效性
     * 如果redis 已经失效自动关闭
     * @return
     */
    public static boolean verifyRedisConn(RedisConnection connection){
        try {
            if (connection.isClosed()) {
                return false;
            }
            String result = connection.ping();
            boolean equals = "PONG".equals(result);
            if(!equals){
                connection.close();
            }
            return equals;
        } catch (Exception e) {
            return false;
        }
    }

}
