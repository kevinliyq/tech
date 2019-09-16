package com.study.liyq.techredis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.ShardedJedisPool;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechRedisConfigurationTests {

    @Autowired
    private ShardedJedisPool shardedJedisPool;

    @Test
    public void testSet(){
        String key = "user:id:1";
        String value = "kevin";
        shardedJedisPool.getResource().set(key, value);

        assertEquals(value, shardedJedisPool.getResource().get(key));

        shardedJedisPool.getResource().del(key);
    }


}
