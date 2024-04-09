package com.jcca.common.redis.config;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * redis配置
 *
 * @author hanwone
 * @date 2020年06月08日
 */
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
    public RedisTemplate<String, Object> redisTemplate() {
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
    @Primary
    public RedisConnectionFactory redis1ConnectionFactory() {
        RedisStandaloneConfiguration localhost = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        if(StrUtil.isNotEmpty(redisProperties.getPassword())){
            localhost.setPassword(redisProperties.getPassword());
        }
        if(Objects.nonNull(redisProperties.getDatabase())){
            localhost.setDatabase(redisProperties.getDatabase());
        }

        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpb =
                (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisProperties.getMaxIdle());
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
        jedisPoolConfig.setMaxTotal(redisProperties.getMaxActive());
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
        jedisPoolConfig.setEvictorShutdownTimeoutMillis(redisProperties.getTimeout());
        jpb.poolConfig(jedisPoolConfig);

        return new JedisConnectionFactory(localhost,jpb.build());
    }

    @Bean
    public RedisConnectionFactory redis2ConnectionFactory() {
        RedisStandaloneConfiguration localhost = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        if(StrUtil.isNotEmpty(redisProperties.getPassword())){
            localhost.setPassword(redisProperties.getPassword());
        }
        if(Objects.nonNull(redisProperties.getDatabase())){
            localhost.setDatabase(redisProperties.getDatabase());
        }

        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpb =
                (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisProperties.getMaxIdle());
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
        jedisPoolConfig.setMaxTotal(redisProperties.getMaxActive());
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
        jedisPoolConfig.setEvictorShutdownTimeoutMillis(redisProperties.getTimeout());
        jpb.poolConfig(jedisPoolConfig);

        return new JedisConnectionFactory(localhost,jpb.build());
    }

}
