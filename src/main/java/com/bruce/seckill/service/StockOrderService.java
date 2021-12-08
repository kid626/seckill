package com.bruce.seckill.service;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 层
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
public interface StockOrderService {

    /**
     * 普通下单
     */
    int createWrongOrder(int sid) throws Exception;

    /**
     * 创建正确订单：下单乐观锁
     */
    int createOptimisticOrder(int sid);

    /**
     * 创建正确订单：验证库存 + 用户 + 时间 合法性 + 下单乐观锁
     */
    int createVerifiedOrder(Integer sid, Integer userId, String verifyHash);
}
