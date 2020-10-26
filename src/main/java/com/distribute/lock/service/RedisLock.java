package com.distribute.lock.service;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Redis实现分布式锁
 */
public class RedisLock implements AutoCloseable{
    private RedisTemplate redisTemplate;
    private String key;
    private String value;
    //单位：秒
    private int expireTime;

    public RedisLock(RedisTemplate redisTemplate, String key, int expireTime) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.expireTime = expireTime;
        this.value = UUID.randomUUID().toString();
    }

    /**
     * 获取分布式锁
     */
    public Boolean getLock() {
        RedisCallback<Boolean> callback = redisConnection -> {
            //NX
            RedisStringCommands.SetOption setOption = RedisStringCommands.SetOption.ifAbsent();
            //过期时间
            Expiration seconds = Expiration.seconds(expireTime);

            byte[] redisKey = redisTemplate.getKeySerializer().serialize(key);
            byte[] redisValue = redisTemplate.getValueSerializer().serialize(value);

            Boolean result = redisConnection.set(redisKey, redisValue, seconds, setOption);

            return result;
        };
        //获取分布式锁
        Boolean lock = (Boolean) redisTemplate.execute(callback);
        return lock;
    }

    /**
     * 释放锁
     */
    public Boolean unLock() {
        //释放锁
        String script = "if redis.call(\"get\"),KEYS[1]==ARGV[1] then \n"
                + " return redis.call(\"del\",KEYS[1])\n"
                + " else \n return 0 \n end";
        RedisScript<Boolean> redisScript = RedisScript.of(script, Boolean.class);
        List<String> list = Arrays.asList(key);

        Boolean result = (Boolean) redisTemplate.execute(redisScript, list, value);
        return result;
    }

    /**
     * 自动关闭功能(IO流等)
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        unLock();
    }
}
