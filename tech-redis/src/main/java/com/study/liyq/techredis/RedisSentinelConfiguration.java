package com.study.liyq.techredis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@PropertySource("classpath:redis-sentinel.properties")
public class RedisSentinelConfiguration extends CachingConfigurerSupport {

    @Value("${jedis.max.total}")
    private int maxTotal;

    @Value("${jedis.max.idle}")
    private int maxIdle;

    @Value("${jedis.min.idle}")
    private int minIdle;

    @Value("${jedis.max.active}")
    private int activeTime;

    @Value("${jedis.max.waitmillis}")
    private long maxWaitMillis;

    @Value("${jedis.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${jedis.max.timeout}")
    private int timeout;

    @Value("${jedis.master.name}")
    private String master;

    @Value("${jedis.sentinels}")
    private Set sentinels;


    @Bean("jedisPoolConfig")
    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,true阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(true);
        //在空闲时检查有效性, 默认false
        jedisPoolConfig.setTestWhileIdle(true);
        //获取连接是检查有效性
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        //最大连接数
        jedisPoolConfig.setMaxTotal(maxTotal);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(1800000);
        //最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setBlockWhenExhausted(true);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(minIdle);
        //逐出连接的最小空闲时间 默认1800000毫秒(15分钟)
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(900000);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        return jedisPoolConfig;
    }

    @Bean("jedisPool")
    public JedisPool jedisPool(@Autowired JedisSentinelPool jedisSentinelPool, @Autowired JedisPoolConfig jedisPoolConfig) {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, jedisSentinelPool.getCurrentHostMaster().getHost(), jedisSentinelPool.getCurrentHostMaster().getPort(), timeout, "");
        return jedisPool;
    }

    @Bean
    public JedisSentinelPool jedisSentinelPool(@Autowired JedisPoolConfig jedisPoolConfig)
    {
        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(master,sentinels,jedisPoolConfig);
        return jedisSentinelPool;
    }
}