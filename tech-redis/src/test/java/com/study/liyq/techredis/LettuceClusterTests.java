package com.study.liyq.techredis;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lambdaworks.redis.SetArgs;
import com.study.liyq.techredis.lettuce.LettuceClient;
import com.study.liyq.techredis.lettuce.LettuceClusterClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ActiveProfiles("cluster")
@SpringBootTest
public class LettuceClusterTests
{

    @Autowired
    private LettuceClusterClient lettuceClient;

    @Test
    public void testNonExistKey()
    {
        String key1 = "noexist:user:id:1";

        assertNull(lettuceClient.getConnection().sync().get(key1));

        System.out.println(lettuceClient.getConnection().sync().del(key1));
    }


    @Test
    public void testMset()
    {
        String key = "string:user:id:";
        String value = "kevin";

        Map<String, String> keys = new HashMap<>();
        int length = 20;
        for (int i = 0 ; i < length; i ++)
        {
            keys.put(key + i, value + i);
        }
        lettuceClient.getConnection().sync().mset(keys);

        List<String> values = lettuceClient.getConnection().sync().mget(keys.keySet().toArray(new String[0]));

        assertTrue(values.size() == length);

        List<String> expectedValues = Lists.newArrayList(keys.values());
        assertEquals(expectedValues,values);
        lettuceClient.getConnection().sync().del(keys.keySet().toArray(new String[0]));

    }

}
