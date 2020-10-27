package com.distribute.lock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class RedisController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/redisLock")
    public String redisLock(){
        log.info("已进入redisLock（）方法");
        String key="redisKey";
        String value= UUID.randomUUID().toString();

        //获取分布式锁
        Boolean lock = (Boolean) redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                //NX
                RedisStringCommands.SetOption setOption = RedisStringCommands.SetOption.ifAbsent();
                //过期时间
                Expiration seconds = Expiration.seconds(30);

                byte[] redisKey = redisTemplate.getKeySerializer().serialize(key);
                byte[] redisValue = redisTemplate.getValueSerializer().serialize(value);

                Boolean result = redisConnection.set(redisKey, redisValue, seconds, setOption);

                return result;
            }
        });

        if(lock){
            log.info("我进入了锁");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                //释放锁
                String script="if redis.call(\"get\",KEYS[1])==ARGV[1] then \n"
                        +" return redis.call(\"del\",KEYS[1])\n"
                        +" else \n return 0 \n end";
                RedisScript<Boolean> redisScript=RedisScript.of(script,Boolean.class);
                List<String> list= Arrays.asList(key);


                Boolean result = (Boolean) redisTemplate.execute(redisScript, list, value);
                log.info("释放锁的结果："+result);
            }
        }

        log.info("方法执行完成");
        return "redis";
    }

}
