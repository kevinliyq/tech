package com.study.liyq.techmemcached;

import com.google.common.base.Splitter;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: yoli
 * @since: 2019/10/01
 */
@Configuration
public class MemcachedConfiguration
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${memcached.host.servers}")
    private List<String> servers;


    private MemcachedClient memcachedClient;

    @Bean(destroyMethod = "shutdown")
    public MemcachedClient memcachedClient(){

        ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
        cfb.setHashAlg(DefaultHashAlgorithm.KETAMA_HASH)
                .setDaemon(true)
                .setInitialObservers(Collections.singleton(new ConnectionObserver(){
                    @Override public void connectionEstablished(SocketAddress sa, int reconnectCount)
                    {
                        logger.info("connectionEstablished {}",sa);
                    }

                    @Override public void connectionLost(SocketAddress sa)
                    {
                        logger.info("connectionLost {}",sa);
                    }
                }))
                .setMaxReconnectDelay(60)
                .setTimeoutExceptionThreshold(100)
                .setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT)
                .setFailureMode(FailureMode.Redistribute);

        List<InetSocketAddress> inetSocketAddresses = getInetSocketAddress();
        try
        {
            memcachedClient = new MemcachedClient(inetSocketAddresses);
        }
        catch (IOException e)
        {
            throw new RuntimeException("unable to init memcached client", e);
        }
        return memcachedClient;
    }

    private List<InetSocketAddress> getInetSocketAddress()
    {
        return servers.stream().map(s -> {
            String[] hostAndPort = s.split(":");
            return new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
        }).collect(Collectors.toList());
    }
}
