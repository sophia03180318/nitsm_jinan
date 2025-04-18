package com.jcca.common.redis.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.vo.RedisManagerVo;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis缓存操作类
 */
@Service
public class RedisService {

    private final static String HASH_SEQENCE = "seqence";

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    private String pingStr = "pingAssetState";
    private String pingStr2 = "pingAssetRecord";

    /**
     * 获取设备进程TOP5
     * @param ip
     * @param id
     */
    public List<CollectProcessBean> getProcessTop5V2(String ip, String id,String collectTime, StatusInfoChangeTypeEnum topEnum) {
        String key = ip + ":" + id + ":" + topEnum.getCode();
        Map<String, Object> hashMap = getHashMap(key);

        List<CollectProcessBean> respList = new ArrayList<>();
        if(Objects.isNull(hashMap)){
            return respList;
        }
        Set<String> keySet = hashMap.keySet();
        for (String s : keySet) {
            CollectProcessBean bean = new CollectProcessBean();
            Object value = hashMap.get(s);
            if(topEnum == StatusInfoChangeTypeEnum.status_CPUTop5){
                bean.setCpuRate(value.toString());
            }else if(topEnum == StatusInfoChangeTypeEnum.status_MEMTop5){
                bean.setMemoryRate(value.toString());
            }
            bean.setName(s);
            bean.setCollectTime(collectTime);
            respList.add(bean);
        }

        return respList;
    }

    /**
     * 缓存类型
     */
    public enum CacheTypeEnum {
        PING,
        PROCESS;

    }


    /**
     * 产生序列号
     *
     * @param seqenceName
     * @param maxValue
     * @return
     */
    public String getIdSeqence(String seqenceName, Long maxValue) {
        Long seq = stringRedisTemplate.opsForHash().increment(HASH_SEQENCE, seqenceName, 1);

        if (seq > maxValue) {
            seq = 0L;
            stringRedisTemplate.opsForHash().put(HASH_SEQENCE, seqenceName, "0");
        }

        int expectedLen = String.valueOf(maxValue).length();

        return StrUtil.padPre(String.valueOf(seq), expectedLen, "0");
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量获取某一类的key值
     *
     * @param pattern
     * @return
     */
    public List<Object> getPattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys != null && keys.size() > 0) {
            List<Object> list = new ArrayList<>();
            for (Serializable key : keys) {
                list.add(get(key.toString()));
            }
            return list;
        }
        return null;
    }


    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
            redisTemplate.delete(key);
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 哈希 添加
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void hmSet(String key, Object hashKey, Object value) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key, hashKey, value);
    }

    public void hmDel(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.delete(key, hashKey);
    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Object hmGet(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.get(key, hashKey);
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public void lPush(String k, Object v) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPush(k, v);
    }

    /**
     * 列表获取
     *
     * @param k
     * @param l
     * @param l1
     * @return
     */
    public List<Object> lRange(String k, long l, long l1) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        return list.range(k, l, l1);
    }

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */
    public void add(String key, Object value) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        set.add(key, value);
    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public Set<Object> setMembers(String key) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        return set.members(key);
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */
    public void zAdd(String key, Object value, double scoure) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.add(key, value, scoure);
    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.rangeByScore(key, scoure, scoure1);
    }


    /**
     * 发送队列消息
     *
     * @param channel 队列名称
     * @param message 消息内容
     */
    public void convertAndSend(String channel, String message) {
        stringRedisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.rPush(redisTemplate.getStringSerializer().serialize((channel)),
                        redisTemplate.getStringSerializer().serialize(message));
            }
        });
    }


    /**
     * 获取key为string类型的正则List
     */
    public List<String> getKeyByPattern(final String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (ObjectUtil.isNotNull(keys)) {
            return new ArrayList<>(keys);
        }
        return null;
    }

    /**
     * 获取hashMap
     */
    public Map<String, Object> getHashMap(String hashkey) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> map = hashOperations.entries(hashkey);
        return map;
    }

    /**
     * 格式化字符串
     * @param str
     * @return
     */
    public String formatStr(String str){
        String s = str.replaceAll("\"", "");
        return s;
    }

    /**
     * 删除指定hashMap
     */
    public void deleteHashMap(String hashkey, String key) {
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        Long delete = hashOperations.delete(hashkey, key);
    }

    /**
     * 获取zset格式数据
     */
    public Cursor<String> getSet(String setKey) {
        Long size = stringRedisTemplate.opsForSet().getOperations().boundSetOps(setKey).size();
        Cursor<String> scan = stringRedisTemplate.opsForSet().getOperations().boundSetOps(setKey).scan(ScanOptions.NONE);
        return scan;
    }

    /**
     * 获取key的过期时间  还剩多少秒
     */
    public Long getExpireTime(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }

    /**
     * 分布式锁
     *
     * @param key
     * @param timeOut
     * @return
     */
    public Boolean setNx(String key, Integer timeOut) {
        return redisTemplate.opsForValue().setIfAbsent(key, key, timeOut, TimeUnit.MINUTES);
    }


    /**
     * 规则获取redis数据
     * num->取出个数
     * <p>
     * 在 String Hash ZSet 格式之外  又可能会报类型错误
     */
    public ArrayList<RedisManagerVo> getRedisAllData(List<String> keys, int num) {
        ArrayList<RedisManagerVo> redisManagerVos = new ArrayList<>();
        int i = 0;
        for (String key : keys) {
            try {
                String value = get(key).toString();
                RedisManagerVo stringRedis = new RedisManagerVo();
                stringRedis.setValue(value);
                stringRedis.setKey(key);
                Long expireTime = getExpireTime(key);
                if (expireTime != -1L) {
                    stringRedis.setExpireTime(expireTime + "s");
                }
                redisManagerVos.add(stringRedis);
                i++;
                if (i >= num) {
                    return redisManagerVos;
                }
            } catch (Exception e) {
                try {
                    Map<String, Object> hashMap = getHashMap(key);
                    for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                        RedisManagerVo hashRedis = new RedisManagerVo();
                        hashRedis.setKey(key + ":" + entry.getKey());
                        hashRedis.setValue(entry.getValue().toString());
                        redisManagerVos.add(hashRedis);
                        i++;
                        if (i >= num) {
                            return redisManagerVos;
                        }
                    }
                } catch (RedisSystemException e2) {
                    try {
                        Cursor<String> cursor = getSet(key);
                        while (cursor.hasNext()) {
                            RedisManagerVo zsetRedis = new RedisManagerVo();
                            zsetRedis.setKey(key);
                            zsetRedis.setValue(cursor.next());
                            redisManagerVos.add(zsetRedis);
                            i++;
                            if (i >= num) {
                                return redisManagerVos;
                            }
                        }
                    } catch (RedisSystemException e3) {

                    } catch (Exception ee) {

                    }
                } catch (Exception ee) {

                }
            }

        }

        return redisManagerVos;
    }


    /**
     * 清除采集器 ping + 进程 状态信息
     */
    public void deleteStatus() {
        this.removePattern("*SYN_*");
        this.removePattern("*" + pingStr + "*");
        this.removePattern("*" + pingStr2 + "*");

    }

    /**
     * 删除进程先关的缓存KEY
     * @param assetId
     * @param assetCode
     */
    public void delProcess(String assetId,String assetCode){
        this.remove("SYN_GROUP_PROCESS:" + assetCode, "SYN_PROCESS_FRIST:" + assetId, "SYN_PROCESS_FRIST:" + assetId);
    }

    /**
     * 删除PING状态
     *
     * @param assetId
     */
    public void delPingStatus(String assetId) {
        this.remove(pingStr + ":" + assetId, pingStr2 + ":" + assetId);
    }



}
