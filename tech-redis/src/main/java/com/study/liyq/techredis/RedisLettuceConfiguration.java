package com.study.liyq.techredis;

import com.study.liyq.techredis.lettuce.LettuceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * @author: yoli
 * @since: 2019/09/29
 */
@Configuration
@Profile("lettuce")
public class RedisLettuceConfiguration
{
    @Value("${redis.server1.host}")
    private String hostname1;

    @Value("${redis.server1.port}")
    private int port1;

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public LettuceClient lettuceClient()
    {
        return new LettuceClient(hostname1, port1);
    }
}
