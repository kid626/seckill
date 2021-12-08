package com.bruce.seckill.model.constant;

/**
 * @Copyright Copyright © 2021 fanzh . All rights reserved.
 * @Desc
 * @ProjectName seckill
 * @Date 2021/12/8 10:16
 * @Author fzh
 */
public class RedisConstant {

    private RedisConstant() {
    }

    public static final String HASH_KEY = "seckill:hash:key:{0}:{1}";

    public static final String LIMIT_KEY = "seckill:limit:key:{0}";

    public static final String STOCK_COUNT = "seckill:stock:count:{0}";

    public static final String USER_ORDER = "seckill:user:order:{0}";

}
