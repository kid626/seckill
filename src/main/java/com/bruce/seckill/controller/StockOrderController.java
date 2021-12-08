package com.bruce.seckill.controller;


import com.bruce.seckill.service.StockOrderService;
import com.bruce.seckill.service.StockService;
import com.bruce.seckill.service.UserService;
import com.bruce.seckill.thread.DelCacheThread;
import com.bruce.seckill.thread.ThreadPoolUtil;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
    private StockOrderService stockOrderService;
    @Autowired
    private StockService stockService;

    @Autowired
    private UserService userService;

    private RateLimiter rateLimiter = RateLimiter.create(10);

    private final ExecutorService executor =
            new ThreadPoolUtil.Builder()
                    .setPrefix("del-cache-task")
                    .setKeepAliveTime(60)
                    .setTimeUnit(TimeUnit.SECONDS)
                    .setScheduled(false)
                    .setUncaughtExceptionHandler(ThreadPoolUtil.DEFAULT_UNCAUGHT_EXCEPTION_HANDLER)
                    .build();

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

    @GetMapping(value = "/getVerifyHash")
    public String getVerifyHash(@RequestParam(value = "sid") Integer sid,
                                @RequestParam(value = "userId") Integer userId) {
        String hash;
        try {
            hash = userService.getVerifyHash(sid, userId);
        } catch (Exception e) {
            log.error("获取验证hash失败，原因：[{}]", e.getMessage());
            return "获取验证hash失败";
        }
        return String.format("请求抢购验证hash值为：%s", hash);
    }

    @GetMapping(value = "/createOrderWithVerifiedUrl")
    public String createOrderWithVerifiedUrl(@RequestParam(value = "sid") Integer sid,
                                             @RequestParam(value = "userId") Integer userId,
                                             @RequestParam(value = "verifyHash") String verifyHash) {
        int stockLeft;
        try {
            int count = userService.addUserCount(userId);
            log.info("用户截至该次的访问次数为: [{}]", count);
            boolean isBanned = userService.getUserIsBanned(userId);
            if (isBanned) {
                return "购买失败，超过频率限制";
            }
            stockLeft = stockOrderService.createVerifiedOrder(sid, userId, verifyHash);
            log.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为：%d", stockLeft);
    }


    /**
     * 下单接口：先删除缓存，再更新数据库，缓存延时双删
     */
    @GetMapping("/createOrderWithCache/{sid}")
    public String createOrderWithCache(@PathVariable int sid) {
        int count;
        try {
            // 删除库存缓存
            stockService.delStockCountCache(sid);
            // 完成扣库存下单事务
            count = stockOrderService.createOptimisticOrder(sid);
            log.info("完成下单事务");
            // 延时指定时间后再次删除缓存
            executor.execute(new DelCacheThread(sid, stockService));
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        log.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

}
