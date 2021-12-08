package com.bruce.seckill.service;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 层
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
public interface StockOrderService {

    int createWrongOrder(int sid) throws Exception;

    int createOptimisticOrder(int sid);
}
