package com.study.liyq.techredis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisSentinelConfigurationTests {

    @Autowired
    private JedisSentinelPool jedisJedisPool;

    @Test
    public void testSet(){
        String key = "sentinel:id:1";
        String value = "kevin";
        Jedis jedis = jedisJedisPool.getResource();

        try {
            jedis.set(key, value);

            assertEquals(value, jedis.get(key));

            jedis.del(key);
        } finally {
            jedis.close();
        }
    }
}
