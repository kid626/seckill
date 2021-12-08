package com.bruce.seckill.controller;


import com.bruce.seckill.service.impl.StockOrderServiceImpl;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc Controller 接口
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
@RestController
@RequestMapping
@Slf4j
public class StockOrderController {

    @Autowired
    private StockOrderServiceImpl stockOrderService;

    RateLimiter rateLimiter = RateLimiter.create(10);

    @GetMapping("/createWrongOrder/{sid}")
    @ResponseBody
    public String createWrongOrder(@PathVariable int sid) {
        log.info("购买物品编号sid=[{}]", sid);
        int id = 0;
        try {
            id = stockOrderService.createWrongOrder(sid);
            log.info("创建订单id: [{}]", id);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return String.valueOf(id);
    }

    @GetMapping("/createOptimisticOrder/{sid}")
    @ResponseBody
    public String createOptimisticOrder(@PathVariable int sid) {
        // 1. 阻塞式获取令牌
        log.info("等待时间" + rateLimiter.acquire());
        // 2. 非阻塞式获取令牌
        // if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
        //     log.warn("你被限流了，真不幸，直接返回失败");
        //     return "你被限流了，真不幸，直接返回失败";
        // }
        int id;
        try {
            id = stockOrderService.createOptimisticOrder(sid);
            log.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }

}
