package com.study.liyq.techredis.lettuce;

import com.google.common.collect.Lists;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulConnection;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.cluster.ClusterClientOptions;
import com.lambdaworks.redis.cluster.ClusterTopologyRefreshOptions;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import com.study.liyq.techredis.RedisLettuceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author: yoli
 * @since: 2019/09/29
 */
public class LettuceClient
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String hostname;
    private int port;

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection = null;

    public RedisClient getClient()
    {
        return client;
    }

    public StatefulRedisConnection<String, String> getConnection()
    {
        return connection;
    }

    public LettuceClient(String hostname, int port)
    {
        this.hostname = hostname;
        this.port = port;
    }

    private void init()
    {
        try
        {
            client = RedisClient.create(RedisURI.Builder.redis(hostname, port).build());
            client.setOptions(ClientOptions.builder()
                                      // don't want unlimited
                                      .requestQueueSize(10000)
                                      .autoReconnect(true)
                                      // err on side of not keeping things in queue when connections are not working
                                      .cancelCommandsOnReconnectFailure(true)
                                      .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                                      // needed to keep cluster topology current as pods come and go
                                      //                                      .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder().enablePeriodicRefresh().build())
                                      .build());

            connection = client.connect();

        }
        catch (Exception e)
        {
            logger.error("fail to build lettuce connection", e);
            if (client != null)
                client.shutdown();
        }
    }

    public void shutdown()
    {
        logger.info("shutdown now");
        if (connection != null)
        {
            connection.close();
        }
        client.shutdown();
    }
}
