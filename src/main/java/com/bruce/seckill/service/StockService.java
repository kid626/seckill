package com.bruce.seckill.service;

import com.bruce.seckill.model.po.Stock;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 层
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
public interface StockService {

    Stock getStockById(Integer sid);

    int updateStockById(Stock stock);

    int updateStockByOptimistic(Stock stock);
}
