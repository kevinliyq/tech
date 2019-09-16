package com.study.liyq.techredis;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

@Configuration
@PropertySource("classpath:redis.properties")
public class RedisConfiguration extends CachingConfigurerSupport {

    @Value("${jedis.max.total}")
    private int maxTotal;

    @Value("${jedis.max.idle}")
    private int maxIdle;

    @Value("${jedis.max.active}")
    private int activeTime;

    @Value("${jedis.max.waitmillis}")
    private long maxWaitMillis;

    @Value("${jedis.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${jedis.server1.host}")
    private String hostname1;

    @Value("${jedis.server1.port}")
    private int port1;

    @Value("${jedis.server2.host}")
    private String hostname2;

    @Value("${jedis.server2.port}")
    private int port2;


    @Bean
    public ShardedJedisPool shardedJedisPool()
    {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);

        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);

        List<JedisShardInfo> jedisShardInfos = new ArrayList<>();
        jedisShardInfos.add(new JedisShardInfo(hostname1, port1));
        //jedisShardInfos.add(new JedisShardInfo(hostname2, port2));

        return new ShardedJedisPool(jedisPoolConfig, jedisShardInfos);
    }
}