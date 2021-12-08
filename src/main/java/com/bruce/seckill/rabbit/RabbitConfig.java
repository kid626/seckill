package com.bruce.seckill.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Copyright Copyright © 2021 fanzh . All rights reserved.
 * @Desc
 * @ProjectName seckill
 * @Date 2021/12/8 15:38
 * @Author fzh
 */
@Configuration
public class RabbitConfig {


    @Bean
    public Queue orderQueue() {
        return new Queue("orderQueue");
    }
}
