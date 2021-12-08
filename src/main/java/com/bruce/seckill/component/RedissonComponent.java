package com.bruce.seckill.component;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Copyright Copyright © 2021 fanzh . All rights reserved.
 * @Desc
 * @ProjectName redis
 * @Date 2021/3/21 14:33
 * @Author Bruce
 */
@Component
public class RedissonComponent {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 操作字符串对象
     *
     * @param key
     * @return
     */
    public RBucket<String> getRBucket(String key) {
        return redissonClient.getBucket(key);
    }

    /**
     * 操作 hash 对象
     *
     * @param key
     * @return
     */
    public RMap<String, String> getRMap(String key) {
        return redissonClient.getMap(key);
    }

    /**
     * 获取集合
     * 操作 set 对象，
     *
     * @param key
     * @return
     */
    public RSet<String> getRSet(String key) {
        return redissonClient.getSet(key);
    }

    /**
     * 获取有序集合
     * 操作 list 对象,有序，不重复
     *
     * @param key
     * @return
     */
    public RSortedSet<String> getRSortedSet(String key) {
        return redissonClient.getSortedSet(key);
    }

    /**
     * 获取列表
     * 操作 list 对象
     *
     * @param key
     * @return
     */
    public RList<String> getRList(String key) {
        return redissonClient.getList(key);
    }

    /**
     * 获取队列
     * 操作 list 对象
     *
     * @param key
     * @return
     */
    public RQueue<String> getRQueue(String key) {
        return redissonClient.getQueue(key);
    }

    /**
     * 获取双端队列
     * 操作 list 对象
     *
     * @param key
     * @return
     */
    public RDeque<String> getRDeque(String key) {
        return redissonClient.getDeque(key);
    }

    /**
     * 获取锁
     *
     * @param key
     * @return
     */
    public RLock getRLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * 获取原子数
     *
     * @param key
     * @return
     */
    public RAtomicLong getRAtomicLong(String key) {
        return redissonClient.getAtomicLong(key);
    }

    /**
     * 获取记数锁
     *
     * @param key
     * @return
     */
    public RCountDownLatch getRCountDownLatch(String key) {
        return redissonClient.getCountDownLatch(key);
    }

    /**
     * 获取消息的Topic
     *
     * @param key
     * @return
     */
    public RTopic getRTopic(String key) {
        return redissonClient.getTopic(key);
    }

    /**
     * 操作布隆过滤器
     *
     * @param name
     * @return
     */
    public RBloomFilter<String> getBloomFilter(String name) {
        return redissonClient.getBloomFilter(name);
    }

}
