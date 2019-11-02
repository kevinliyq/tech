package com.study.liyq.techredis;

import com.google.common.collect.Sets;
import com.lambdaworks.redis.SetArgs;
import com.study.liyq.techredis.lettuce.LettuceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ActiveProfiles("lettuce")
@SpringBootTest
public class LettuceTests
{

    @Autowired
    private LettuceClient lettuceClient;

    @Test
    public void testString()
    {
        String key = "string:user:id:1";
        String value = "kevin";

        lettuceClient.getConnection().sync().set(key, value, SetArgs.Builder.ex(60));

        assertEquals(value, lettuceClient.getConnection().sync().get(key));

        lettuceClient.getConnection().sync().del(key);
    }

    @Test
    public void testList()
    {
        String key = "list:user:id:1";
        String value1 = "kevin";
        String value2 = "kevin2";

        lettuceClient.getConnection().sync().lpush(key, value1, value2);

        assertEquals(value2, lettuceClient.getConnection().sync().lpop(key));

        assertTrue(1L == lettuceClient.getConnection().sync().llen(key));

        lettuceClient.getConnection().sync().del(key);
    }

    @Test
    public void testSet()
    {
        String key1 = "set:user:id:1";
        String value11 = "kevin";
        String value12 = "kevin2";
        String value13 = "lili";

        String key2 = "set:user:id:2";
        String value21 = "yoli";
        String value22 = "kevin2";
        String value23 = "lili";

        lettuceClient.getConnection().sync().sadd(key1, value11, value12, value13);

        lettuceClient.getConnection().sync().sadd(key2, value21, value22, value23);

        Set<String> diffSet = Sets.newHashSet(value11);

        assertEquals(diffSet,  lettuceClient.getConnection().sync().sdiff(key1, key2));

        Set<String> interSet = Sets.newHashSet(value12, value13);
        assertEquals(interSet,  lettuceClient.getConnection().sync().sinter(key1, key2));

        Set<String> unionSet = Sets.newHashSet(value11, value12, value13, value21);
        assertEquals(unionSet, lettuceClient.getConnection().sync().sunion(key1, key2));

        lettuceClient.getConnection().sync().del(key1);
        lettuceClient.getConnection().sync().del(key2);
    }

    @Test
    public void testBitmap()
    {
        String key1 = "bitmap:ap:product";
        int attractionId = 1000000;

        lettuceClient.getConnection().sync().setbit(key1, attractionId, 1);

        assertTrue(1 == lettuceClient.getConnection().sync().getbit(key1, attractionId));

        assertTrue(0 == lettuceClient.getConnection().sync().getbit(key1, 3));

        lettuceClient.getConnection().sync().del(key1);
    }

    @Test
    public void testExclusiveLock()
    {
        String key1 = "lock:user:id:1";
        String value1 = "yoli";
        String value2 = "mayi";
        lettuceClient.getConnection().sync().set(key1, value1, SetArgs.Builder.nx().ex(60));

        assertEquals(value1, lettuceClient.getConnection().sync().get(key1));

        lettuceClient.getConnection().sync().set(key1, value2, SetArgs.Builder.nx());

        assertEquals(value1, lettuceClient.getConnection().sync().get(key1));

        lettuceClient.getConnection().sync().del(key1);
    }

}
