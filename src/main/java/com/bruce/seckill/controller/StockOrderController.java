package com.bruce.seckill.controller;


import com.alibaba.fastjson.JSONObject;
import com.bruce.seckill.rabbit.RabbitOrderDTO;
import com.bruce.seckill.service.StockOrderService;
import com.bruce.seckill.service.StockService;
import com.bruce.seckill.service.UserService;
import com.bruce.seckill.thread.DelCacheThread;
import com.bruce.seckill.thread.ThreadPoolUtil;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    @Autowired
    private RabbitTemplate rabbitTemplate;

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
        // log.info("等待时间" + rateLimiter.acquire());
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

    /**
     * 下单接口：异步处理订单
     */
    @GetMapping(value = "/createUserOrderWithMq")
    public String createUserOrderWithMq(@RequestParam(value = "sid") Integer sid,
                                        @RequestParam(value = "userId") Integer userId) {
        try {
            // 检查缓存中该用户是否已经下单过
            // Boolean hasOrder = stockOrderService.checkUserOrderInfoInCache(sid, userId);
            // if (hasOrder != null && hasOrder) {
            //     log.info("该用户已经抢购过");
            //     return "你已经抢购过了，不要太贪心.....";
            // }
            // 没有下单过，检查缓存中商品是否还有库存
            log.info("没有抢购过，检查缓存中商品是否还有库存");
            Integer count = stockService.getStockCount(sid);
            if (count == 0) {
                return "秒杀请求失败，库存不足.....";
            }

            // 有库存，则将用户id和商品id封装为消息体传给消息队列处理
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            log.info("有库存：[{}]", count);
            RabbitOrderDTO dto = new RabbitOrderDTO();
            dto.setSid(sid);
            dto.setUserId(userId);
            sendToOrderQueue(JSONObject.toJSONString(dto));
            return "秒杀请求提交成功";
        } catch (Exception e) {
            log.error("下单接口：异步处理订单异常：", e);
            return "秒杀请求失败，服务器正忙.....";
        }
    }

    /**
     * 检查缓存中用户是否已经生成订单
     */
    @GetMapping(value = "/checkOrderByUserIdInCache")
    @ResponseBody
    public String checkOrderByUserIdInCache(@RequestParam(value = "sid") Integer sid,
                                            @RequestParam(value = "userId") Integer userId) {
        // 检查缓存中该用户是否已经下单过
        try {
            Boolean hasOrder = stockOrderService.checkUserOrderInfoInCache(sid, userId);
            if (hasOrder != null && hasOrder) {
                return "恭喜您，已经抢购成功！";
            }
        } catch (Exception e) {
            log.error("检查订单异常：", e);
        }
        return "很抱歉，你的订单尚未生成，继续排队。";
    }

    /**
     * 向消息队列orderQueue发送消息
     *
     * @param message
     */
    private void sendToOrderQueue(String message) {
        log.info("这就去通知消息队列开始下单：[{}]", message);
        this.rabbitTemplate.convertAndSend("", "orderQueue", message);
    }

}
