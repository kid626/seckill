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

    /**
     * 根据库存 ID 查询数据库库存信息
     */
    Stock getStockById(Integer sid);

    /**
     * 更新数据库库存信息
     */
    int updateStockById(Stock stock);

    /**
     * 更新数据库库存信息（乐观锁）
     */
    int updateStockByOptimistic(Stock stock);

    /**
     * 查询库存：通过缓存查询库存
     * 缓存命中：返回库存
     * 缓存未命中：查询数据库写入缓存并返回
     */
    Integer getStockCount(int sid);

    /**
     * 获取剩余库存：查数据库
     */
    int getStockCountByDB(int sid);

    /**
     * 获取剩余库存: 查缓存
     */
    Integer getStockCountByCache(int sid);

    /**
     * 删除库存缓存
     */
    void delStockCountCache(int sid);

}
