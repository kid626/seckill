package com.bruce.seckill.thread;

import com.bruce.seckill.service.StockService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Copyright Copyright © 2021 fanzh . All rights reserved.
 * @Desc
 * @ProjectName seckill
 * @Date 2021/12/8 14:48
 * @Author fzh
 */
@Slf4j
public class DelCacheThread implements Runnable {

    private final int sid;

    private final StockService stockService;

    /**
     * 延时时间：预估读数据库数据业务逻辑的耗时，用来做缓存再删除
     */
    private static final int DELAY_MILLISECONDS = 1000;

    public DelCacheThread(int sid, StockService stockService) {
        this.sid = sid;
        this.stockService = stockService;
    }


    @Override
    public void run() {
        try {
            log.info("异步执行缓存再删除，商品id：[{}]， 首先休眠：[{}] 毫秒", sid, DELAY_MILLISECONDS);
            Thread.sleep(DELAY_MILLISECONDS);
            stockService.delStockCountCache(sid);
            log.info("再次删除商品id：[{}] 缓存", sid);
        } catch (Exception e) {
            log.error("delCacheByThread执行出错", e);
        }
    }
}
