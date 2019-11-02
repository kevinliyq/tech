package com.study.liyq.techzookeeper;

import com.google.common.base.Joiner;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: yoli
 * @since: 2019/10/07
 */
@Configuration
public class ZookeeperConfiguration implements Watcher
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zookeeper.servers}")
    private String zkClientStr;

    @Value("${spring.application.name}")
    private String applicationName;

    private String rootPath = "/admin";

    @Bean(destroyMethod = "close")
    public CuratorFramework zooKeeperClient() throws Exception
    {
        RetryPolicy retryPolicy = new RetryNTimes(10, 5000);

        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkClientStr)
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                //.namespace("admin")
                .connectionTimeoutMs(30 * 1000)
                .sessionTimeoutMs(90 * 1000)
                .build();

        client.start();


        CuratorWatcher watcher = (WatchedEvent watchedEvent) -> {
            logger.info(String.format("monitored: event=%s,path=%s,state=%s",watchedEvent.getType(),watchedEvent.getPath(),watchedEvent.getState()));
        };

        if (client.checkExists().forPath(rootPath) == null)
        {
            createPath(client, rootPath);
        }

        logger.info("watcher path:{}",rootPath);
        client.getData().usingWatcher(watcher).forPath(rootPath);

        //register data change for itself, it is can't be notified when children is changed.
        //byte[] parentConfig = client.getData().usingWatcher(watcher).forPath("/config");
        String currentPath = "/config";
        registerNodeListener(client, currentPath);
        //register watcher to monitor children change
        //client.getChildren().usingWatcher(this).forPath("/config");

        //logger.info("config data {}",new String(parentConfig));

        createPath(client, currentPath);

        String path = Joiner.on("/").join(rootPath, applicationName);
        createPath(client, path);

        return client;
    }

    private void registerNodeListener(CuratorFramework client, String path)
    {
        final NodeCache nodeCache = new NodeCache(client, path, false);
        nodeCache.getListenable().addListener(() -> logger.info("Current node data: {}", nodeCache.getCurrentData()));
    }

    @Override public void process(WatchedEvent watchedEvent)
    {
        logger.info(String.format("monitored: event=%s,path=%s,state=%s",watchedEvent.getType(),watchedEvent.getPath(),watchedEvent.getState()));
    }

    private void createPath(CuratorFramework client, String path) throws Exception
    {
        if (client.checkExists().forPath(path) == null)
        {
            client.create()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(path);
        }
    }
}
