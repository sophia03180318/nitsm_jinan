package com.jcca.common.redis.config;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * redis配置
 *
 * @author hanwone
 * @date 2020年06月08日
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Resource
    private RedisProperties redisProperties;


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    public RedisQueueTemplate redisQueueTemplate() {
        return new RedisQueueTemplate(stringRedisTemplate);
    }

    @Bean("redisTemplate")
    public RedisTemplate<String, Object> isTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redis1ConnectionFactory());

        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean("redisTransactionTemplate")
    public RedisTemplate<String, Object> redisTransactionTemplate() {
        RedisTemplate<String, Object> redisTransactionTemplate = new RedisTemplate<>();
        redisTransactionTemplate.setConnectionFactory(redis2ConnectionFactory());

        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        redisTransactionTemplate.setKeySerializer(new StringRedisSerializer());
        redisTransactionTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTransactionTemplate.setHashKeySerializer(genericJackson2JsonRedisSerializer);
        redisTransactionTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);
        redisTransactionTemplate.afterPropertiesSet();

        //开启事务
        redisTransactionTemplate.setEnableTransactionSupport(true);

        return redisTransactionTemplate;
    }


    @Bean
    public JedisPool redisPoolFactory() {
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            //最大空闲连接数
            jedisPoolConfig.setMaxIdle(redisProperties.getMaxIdle());
            //最小空闲连接数
            jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
            //最大连接数
            jedisPoolConfig.setMaxTotal(redisProperties.getMaxTotal());
            jedisPoolConfig.setMaxTotal(redisProperties.getMaxActive());
            jedisPoolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
            jedisPoolConfig.setEvictorShutdownTimeoutMillis(redisProperties.getTimeout());
            //  borrowObject 和 returnObject 时，进行有效性检查
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPoolConfig.setTestWhileIdle(true);
            // 连接空闲多久后可以被驱逐，单位毫秒
            jedisPoolConfig.setMinEvictableIdleTimeMillis(redisProperties.getMinEvictableIdleTimeMillis());
            // 空闲连接驱逐前的检测时间
            jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(redisProperties.getSoftMinEvictableIdleTimeMillis());
            // 每次驱逐检查的连接数量
            jedisPoolConfig.setNumTestsPerEvictionRun(redisProperties.getNumTestsPerEvictionRun());
            // 连接驱逐线程的运行间隔
            jedisPoolConfig.setTimeBetweenEvictionRunsMillis(redisProperties.getTimeBetweenEvictionRunsMillis());
            return new JedisPool(jedisPoolConfig, redisProperties.getHost(), redisProperties.getPort(),
                    redisProperties.getTimeout(), redisProperties.getPassword(), redisProperties.getDatabase());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 不带redis事务的缓存池 poll1
     *
     * @return
     */
    @Bean
    @Primary
    public RedisConnectionFactory redis1ConnectionFactory() {
        RedisStandaloneConfiguration localhost = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        if (StrUtil.isNotEmpty(redisProperties.getPassword())) {
            localhost.setPassword(redisProperties.getPassword());
        }
        if (Objects.nonNull(redisProperties.getDatabase())) {
            localhost.setDatabase(redisProperties.getDatabase());
        }

        return new JedisConnectionFactory(localhost);
    }

    /**
     * 带事务的redis缓存池  poll2
     *
     * @return
     */
    @Bean
    public RedisConnectionFactory redis2ConnectionFactory() {
        RedisStandaloneConfiguration localhost = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        if (StrUtil.isNotEmpty(redisProperties.getPassword())) {
            localhost.setPassword(redisProperties.getPassword());
        }
        if (Objects.nonNull(redisProperties.getDatabase())) {
            localhost.setDatabase(redisProperties.getDatabase());
        }


        return new JedisConnectionFactory(localhost);
    }

}
