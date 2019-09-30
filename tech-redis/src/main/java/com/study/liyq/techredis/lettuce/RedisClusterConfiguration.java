package com.study.liyq.techredis.lettuce;

import com.google.common.collect.Lists;
import com.lambdaworks.redis.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * @author: yoli
 * @since: 2019/09/29
 */
@Configuration
@Profile("cluster")
public class RedisClusterConfiguration
{
    @Value("${redis.server1.host}")
    private String hostname1;

    @Value("${redis.server1.port}")
    private int port1;

    @Value("${redis.server2.host}")
    private String hostname2;

    @Value("${redis.server2.port}")
    private int port2;

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public LettuceClusterClient lettuceClient()
    {
        List<RedisURI> redisNodes = Lists.newArrayList(RedisURI.Builder.redis(hostname1, port1).build(),
                                                       RedisURI.Builder.redis(hostname2, port2).build());
        return new LettuceClusterClient(redisNodes);
    }
}
