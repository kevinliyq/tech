package com.study.liyq.techmemcached;

import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
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

	@Test
	public void testCas() throws Exception{
		String key = "user.add";
		String value = "yoli";

		memcachedClient.set(key,0,value).get(10, TimeUnit.SECONDS);

		Assert.assertFalse(memcachedClient.add(key,0, "hello").get());

		CASValue casValue = memcachedClient.gets(key);

		Assert.assertEquals(value, String.valueOf(casValue.getValue()));
		//only binary mode support delete by cas
		Assert.assertTrue(memcachedClient.delete(key).get());
	}

	@Test
	public void testIncrDecr() throws Exception{
		String key = "user.product.1";
		int value = 1;
		//when we try to store integer, you should use 'string' instead of integer
		Assert.assertTrue(memcachedClient.set(key,0,""+value).get());

		Assert.assertEquals(4, memcachedClient.incr(key, 3));

		Assert.assertEquals(3, memcachedClient.decr(key, 1));

		Assert.assertTrue(memcachedClient.delete(key).get());
	}


}
