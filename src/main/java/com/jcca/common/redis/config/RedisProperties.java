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

    @Value("${jcca.redis.host}")
    private String host;

    @Value("${jcca.redis.port}")
    private Integer port;

    @Value("${jcca.redis.password:''}")
    private String password;

    @Value("${jcca.redis.database}")
    private Integer database;

    @Value("${jcca.redis.timeout}")
    private Integer timeout;

    @Value("${jcca.redis.lettuce.pool.max-idle}")
    public Integer maxIdle;

    @Value("${jcca.redis.lettuce.pool.min-idle}")
    public Integer minIdle;

    @Value("${jcca.redis.lettuce.pool.max-active}")
    public Integer maxActive;

    @Value("${jcca.redis.lettuce.pool.max-wait}")
    public Integer maxWait;



}
