package com.jcca.common.redis.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis队列 实现类
 *
 * @author hanwone
 * @date 2018年1月11日
 */
@Component
@Slf4j
public class RedisQueueTemplate {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisQueueTemplate(StringRedisTemplate redisTemplate) {
        if (redisTemplate == null) {
            throw new RuntimeException("redisTemplate can not be null ");
        }
        this.redisTemplate = redisTemplate;
    }

    public long rPush(final String queueName, final String value) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.rPush(redisTemplate.getStringSerializer().serialize((queueName)),
                        redisTemplate.getStringSerializer().serialize(value));
            }
        });
    }

    public String lPop(final String queueName) {
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] value = connection.lPop(redisTemplate.getStringSerializer().serialize((queueName)));
                return redisTemplate.getStringSerializer().deserialize(value);
            }
        });
    }



    public long rPush(final String queueName, final List<String> values) {
        if (values != null && values.size() > 0) {
            return redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    List<byte[]> valuesByte = new ArrayList<>();
                    for (String value : values) {
                        if (value != null) {
                            valuesByte.add(redisTemplate.getStringSerializer().serialize(value));
                        }
                    }
                    return connection.rPush(redisTemplate.getStringSerializer().serialize((queueName)),
                            valuesByte.toArray(new byte[valuesByte.size()][]));
                }
            });
        }
        return 0L;
    }

    public List<String> lPop(final String queueName, final int length) {
        return redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
                List<String> values = new ArrayList<>();
                byte[] key = redisTemplate.getStringSerializer().serialize((queueName));
                List<byte[]> valuesByte = connection.lRange(key, 0, length - 1);
                connection.lTrim(key, valuesByte.size(), -1);
                for (byte[] valueByte : valuesByte) {
                    values.add(redisTemplate.getStringSerializer().deserialize(valueByte));
                }

                return values;
            }
        });
    }

    public List<String> lRange(final String queueName, final int length) {
        return redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
                List<String> values = new ArrayList<>();
                byte[] key = redisTemplate.getStringSerializer().serialize((queueName));
                List<byte[]> valuesByte = connection.lRange(key, 0, length - 1);
                for (byte[] valueByte : valuesByte) {
                    values.add(redisTemplate.getStringSerializer().deserialize(valueByte));
                }
                return values;
            }
        });
    }

    public void lRem(final String queueName, final int length) {
        redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize((queueName));
                connection.lTrim(key, length, -1);
                return 1L;
            }
        });
    }

    public long lLen(final String queueName) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize((queueName));
                return connection.lLen(key);
            }
        });
    }

}
