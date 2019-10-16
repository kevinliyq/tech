package com.study.liyq.techzookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechZookeeperApplicationTests {

	@Autowired
	private CuratorFramework zooKeeperClient;

	private CuratorListener listener;

	@Before
	public void setUp(){
		listener = (CuratorFramework client, CuratorEvent event) ->    {
			System.out.println("Event:" + event.getWatchedEvent().getType());
			System.out.println(event.getData());
		};
		zooKeeperClient.getCuratorListenable().addListener(listener);
	}

	@After
	public void tearDown(){
		if (listener != null)
		{
			zooKeeperClient.getCuratorListenable().removeListener(listener);
		}
	}

	@Test
	public void testZookeeper() throws Exception {
		//it will create path namespace + path
		String path = "/mytest";
		byte[] data = "Hello Test".getBytes();
		if(zooKeeperClient.checkExists().forPath(path) == null)
		{
			zooKeeperClient.create()
					.withMode(CreateMode.EPHEMERAL)
					.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
					.forPath(path, data);
		}

		Assert.assertEquals(new String(data), new String(zooKeeperClient.getData().forPath(path)));

		byte[] newData = "Hello Beijing".getBytes();
		zooKeeperClient.setData().forPath(path, newData);

		Assert.assertEquals(new String(newData), new String(zooKeeperClient.getData().forPath(path)));

		byte[] payload = "Hello Shanghai".getBytes();
		CountDownLatch countDownLatch = new CountDownLatch(1);
		AtomicReference<byte[]> result = new AtomicReference<>();

		zooKeeperClient.setData().inBackground((client, event) -> {
			System.out.println("inBackground:"+event.getType());
			System.out.println(event.getData());
			countDownLatch.countDown();
		}).forPath(path, payload);

		countDownLatch.await();
		//Assert.assertEquals(new String(payload), new String(result.get()));

		zooKeeperClient.delete().forPath(path);

		Assert.assertNull(zooKeeperClient.checkExists().forPath(path));

	}

	@Test
	public void testWatch() throws Exception{

		String parentPath = "/test";
		String path1 = parentPath + "/watchtest1";
		String path2 = parentPath + "/watchtest2";
		Watcher watcher = (WatchedEvent watchedEvent) ->
			{
				System.out.println(String.format("event=%s,path=%s,state=%s",watchedEvent.getType(),watchedEvent.getPath(),watchedEvent.getState()));
			}
		;
		//if parent is EPHEMERAL node, then can't create children
		createPath(parentPath, CreateMode.PERSISTENT);
		zooKeeperClient.getChildren().usingWatcher(watcher).forPath(parentPath);
		Assert.assertNotNull(zooKeeperClient.checkExists().forPath(parentPath));

		createPath(path1, CreateMode.EPHEMERAL);

		Assert.assertNotNull(zooKeeperClient.checkExists().forPath(path1));
		zooKeeperClient.getData().usingWatcher(watcher).forPath(path1);

		byte[] payload = "Hello Shanghai".getBytes();
		zooKeeperClient.setData().forPath(path1, payload);

		createPath(path2, CreateMode.EPHEMERAL);
		zooKeeperClient.getData().usingWatcher(watcher).forPath(path2);
		Assert.assertNotNull(zooKeeperClient.checkExists().forPath(path2));

		deletePath(path2);
		Assert.assertNull(zooKeeperClient.checkExists().forPath(path2));

		deletePath(parentPath);
		Assert.assertNull(zooKeeperClient.checkExists().forPath(parentPath));
	}

	private void createPath(String path, CreateMode mode) throws Exception{
		zooKeeperClient.create()
				.withMode(mode)
				.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
				.forPath(path);
	}

	private void deletePath(String path) throws Exception{
		zooKeeperClient.delete().deletingChildrenIfNeeded().forPath(path);
	}



}
