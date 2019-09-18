package com.study.liyq.techredis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TechRedisConfigurationTests {

    @Autowired
    private ShardedJedisPool shardedJedisPool;

    private static int COUNTER = 0;

    private static int TOTAL = 100;

    @Test
    public void testSet(){
        String key = "user:id:1";
        String value = "kevin";
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        try {
            shardedJedis.set(key, value);

            assertEquals(value, shardedJedis.get(key));

            shardedJedis.del(key);
        } finally {
            shardedJedis.close();
        }
    }

    @Test
    public void testDistributedLock() throws Exception
    {
        CompletableFuture<Void> completableFuture1 = CompletableFuture.runAsync(new CounterIncr()).whenComplete((result, e) -> {System.out.println("completableFuture1 is ended"); });

        CompletableFuture<Void> completableFuture2 =  CompletableFuture.runAsync(new CounterIncr()).whenComplete((result, e) -> {System.out.println("completableFuture2 is ended"); });

        CompletableFuture.allOf(completableFuture1, completableFuture2).whenComplete((t, u) -> {System.out.println("All completed");}).get();
    }

    private boolean lock(String key, String value)
    {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        String response;
        try {
            response = shardedJedis.set(key, value, "NX", "EX", 10);

        } finally {
            shardedJedis.close();
        }

        boolean isLock = "OK".equals(response);
        return isLock;
    }

    /**
     * release lock only if the owner to release is the one who locked
     * @param key
     * @param value
     */
    private void unlock(String key, String value)
    {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        try {
            if (value.equals(shardedJedis.get(key))) {
                shardedJedis.del(key);
            }
        }finally {
            shardedJedis.close();
        }
    }

     class CounterIncr implements Runnable{

        @Override
        public void run() {
            String uniqueId = UUID.randomUUID().toString();
            while (COUNTER < TOTAL) {
                while (COUNTER < TOTAL && !lock("user.lock", uniqueId)) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        break;
                    }
                }

                if (COUNTER >= TOTAL) {
                    System.out.println(Thread.currentThread().getName() + " break");
                    return;
                }

                try {
                    System.out.println(Thread.currentThread().getName() + ":" + (++COUNTER));
                } finally {
                    unlock("user.lock", uniqueId);
                }

            }

        }
    }


}
