package com.study.liyq.techredis.lettuce;

import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.ReadFrom;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.cluster.ClusterClientOptions;
import com.lambdaworks.redis.cluster.ClusterTopologyRefreshOptions;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: yoli
 * @since: 2019/09/29
 */
public class LettuceClusterClient
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Iterable<RedisURI> redisURIS;

    private RedisClusterClient client;
    private StatefulRedisClusterConnection<String, String> connection = null;

    public StatefulRedisClusterConnection<String, String> getConnection()
    {
        return connection;
    }

    public LettuceClusterClient(Iterable<RedisURI> redisURIS)
    {
        this.redisURIS = redisURIS;
    }

    private void init()
    {
        try
        {
            client = RedisClusterClient.create(redisURIS);

            client.setOptions(ClusterClientOptions.builder()
                                      // don't want unlimited
                                      .requestQueueSize(10000)
                                      // err on side of not keeping things in queue when connections are not working
                                      .cancelCommandsOnReconnectFailure(true)
                                      .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                                      // needed to keep cluster topology current as pods come and go
                                      .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder().enablePeriodicRefresh().build())
                                      .build());

            connection = client.connect();
            //by default, connection read from master
            connection.setReadFrom(ReadFrom.MASTER);

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
