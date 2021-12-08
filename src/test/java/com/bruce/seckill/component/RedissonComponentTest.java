package com.bruce.seckill.component;

import com.alibaba.fastjson.JSONObject;
import com.bruce.seckill.model.po.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright Copyright © 2021 fanzh . All rights reserved.
 * @Desc
 * @ProjectName seckill
 * @Date 2021/12/8 10:31
 * @Author fzh
 */
@SpringBootTest
public class RedissonComponentTest {

    @Autowired
    private RedissonComponent component;

    @Test
    public void get() {
        component.getRBucket("test").set("test", 1, TimeUnit.MINUTES);
    }

    @Test
    public void rset() {
        component.getRSet("set_test").add(JSONObject.toJSONString(new User()));
        Iterator<String> iterator = component.getRSet("set_test").iterator("test1");
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

}
