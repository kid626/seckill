package com.bruce.seckill.component;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
