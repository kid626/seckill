package com.bruce.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bruce.seckill.component.RedissonComponent;
import com.bruce.seckill.mapper.UserMapper;
import com.bruce.seckill.model.constant.RedisConstant;
import com.bruce.seckill.model.po.Stock;
import com.bruce.seckill.model.po.User;
import com.bruce.seckill.service.StockService;
import com.bruce.seckill.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 实现类
 * @ProjectName seckill
 * @Date 2021-12-8 10:04:25
 * @Author Bruce
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private RedissonComponent component;

    private static final String SALT = "randomString";
    private static final long ALLOW_COUNT = 5;

    @Override
    public String getVerifyHash(Integer sid, Integer userId) {
        // 验证是否在抢购时间内
        log.info("请自行验证是否在抢购时间内");

        // 检查用户合法性
        User user = userMapper.selectById(userId.longValue());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        log.info("用户信息：[{}]", user.toString());

        // 检查商品合法性
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new RuntimeException("商品不存在");
        }
        log.info("商品信息：[{}]", stock.toString());

        // 生成hash
        String verify = SALT + sid + userId;
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());

        // 将hash和用户商品信息存入redis
        String hashKey = MessageFormat.format(RedisConstant.HASH_KEY, String.valueOf(sid), String.valueOf(sid));
        component.getRBucket(hashKey).set(verifyHash, 3600, TimeUnit.SECONDS);
        log.info("Redis写入：[{}] [{}]", hashKey, verifyHash);
        return verifyHash;

    }

    @Override
    public int addUserCount(Integer userId) {
        String limitKey = MessageFormat.format(RedisConstant.LIMIT_KEY, String.valueOf(userId));
        long limit = component.getRAtomicLong(limitKey).getAndIncrement();
        component.getRBucket(limitKey).expire(3600, TimeUnit.SECONDS);
        return Integer.parseInt(String.valueOf(limit));
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = MessageFormat.format(RedisConstant.LIMIT_KEY, String.valueOf(userId));
        long limitNum = component.getRAtomicLong(limitKey).get();
        return limitNum > ALLOW_COUNT;
    }
}
