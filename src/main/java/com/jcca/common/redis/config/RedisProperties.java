package com.jcca.common.redis.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @description: redis 配置信息
 * @author: Lvyp
 * @create: 2024/02/04 10:23
 */
@Order(value = 1)
@Data
@Component
public class RedisProperties {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database}")
    private Integer database;

    @Value("${spring.redis.timeout}")
    private Integer timeout;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    public Integer maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    public Integer minIdle;

    @Value("${spring.redis.lettuce.pool.max-active}")
    public Integer maxActive;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    public Integer maxWait;



}
