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

    @Value("${jcca.redis.jedis.pool1.max-idle}")
    public Integer maxIdle;

    @Value("${jcca.redis.jedis.pool1.min-idle}")
    public Integer minIdle;

    @Value("${jcca.redis.jedis.pool1.max-total}")
    public Integer maxTotal;

    @Value("${jcca.redis.jedis.pool1.max-active}")
    public Integer maxActive;

    @Value("${jcca.redis.jedis.pool1.max-wait}")
    public Integer maxWait;
    /**
     * 连接空闲多久后可以被驱逐，单位毫秒 60s
     */
    @Value("${jcca.redis.jedis.pool1.minEvictableIdleTimeMillis:60000}")
    private Integer minEvictableIdleTimeMillis;
    /**
     * 空闲连接驱逐前的检测时间
     */
    @Value("${jcca.redis.jedis.pool1.softMinEvictableIdleTimeMillis:10000}")
    private Integer softMinEvictableIdleTimeMillis;
    /**
     * 每次驱逐检查的连接数量
     */
    @Value("${jcca.redis.jedis.pool1.numTestsPerEvictionRun:300}")
    private Integer numTestsPerEvictionRun;

    /**
     * 连接驱逐线程的运行间隔 毫秒
     */
    @Value("${jcca.redis.jedis.pool1.timeBetweenEvictionRunsMillis:20000}")
    private Integer timeBetweenEvictionRunsMillis;


    @Value("${jcca.redis.jedis.pool2.max-idle}")
    public Integer maxIdle2;

    @Value("${jcca.redis.jedis.pool2.min-idle}")
    public Integer minIdle2;

    @Value("${jcca.redis.jedis.pool2.max-total}")
    public Integer maxTotal2;

    @Value("${jcca.redis.jedis.pool2.max-active}")
    public Integer maxActive2;

    @Value("${jcca.redis.jedis.pool2.max-wait}")
    public Integer maxWait2;

    /**
     * 连接空闲多久后可以被驱逐，单位毫秒 60s
     */
    @Value("${jcca.redis.jedis.pool2.minEvictableIdleTimeMillis:60000}")
    private Integer minEvictableIdleTimeMillis2;
    /**
     * 空闲连接驱逐前的检测时间
     */
    @Value("${jcca.redis.jedis.pool2.softMinEvictableIdleTimeMillis:10000}")
    private Integer softMinEvictableIdleTimeMillis2;
    /**
     * 每次驱逐检查的连接数量
     */
    @Value("${jcca.redis.jedis.pool2.numTestsPerEvictionRun:300}")
    private Integer numTestsPerEvictionRun2;

    /**
     * 连接驱逐线程的运行间隔 毫秒
     */
    @Value("${jcca.redis.jedis.pool2.timeBetweenEvictionRunsMillis:20000}")
    private Integer timeBetweenEvictionRunsMillis2;


}
