package com.study.liyq.techmemcached;

import net.spy.memcached.MemcachedClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechMemcachedApplicationTests {

	@Autowired
	private MemcachedClient memcachedClient;
	@Test
	public void testMemcached() throws Exception{
		String key = "user.1";
		String value = "yoli";

		memcachedClient.set(key,0,value).get(10, TimeUnit.SECONDS);

		Assert.assertEquals(value, memcachedClient.get(key));

		Assert.assertTrue(memcachedClient.delete(key).get(10, TimeUnit.SECONDS));
	}

}
