package com.bruce.seckill.rabbit;

import com.alibaba.fastjson.JSONObject;
import com.bruce.seckill.service.StockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Copyright Copyright © 2021 fanzh . All rights reserved.
 * @Desc
 * @ProjectName seckill
 * @Date 2021/12/8 15:38
 * @Author fzh
 */
@Component
@RabbitListener(queues = "orderQueue")
@Slf4j
public class OrderMqListener {


    @Autowired
    private StockOrderService orderService;

    @RabbitHandler
    public void process(String message) {
        log.info("OrderMqReceiver收到消息开始用户下单流程: " + message);
        try {
            RabbitOrderDTO dto = JSONObject.parseObject(message, RabbitOrderDTO.class);
            orderService.createOrderByMq(dto.getSid(), dto.getUserId());
        } catch (Exception e) {
            log.error("消息处理异常：", e);
        }
    }
}
