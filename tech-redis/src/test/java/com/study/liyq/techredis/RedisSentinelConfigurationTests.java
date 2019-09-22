package com.study.liyq.techredis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisSentinelConfigurationTests {

    @Autowired
    private JedisSentinelPool jedisJedisPool;

    @Autowired
    private JedisPool jedisPool;

    @Test
    public void testSentinel(){
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

    /**
     * Jedis does not support shardJedisSentinelPool
     */
    @Test
    public void testRedisPool(){
        String key = "pool:id:1";
        String value = "kevin";
        Jedis jedis = jedisPool.getResource();

        try {
            jedis.set(key,value,"NX", "EX", 15);

            System.out.println(jedisPool.getNumActive());

            assertEquals(value, jedis.get(key));

            jedis.del(key);
        } finally {
            jedis.close();
        }
    }
}
