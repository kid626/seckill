package com.bruce.seckill.service.impl;

import com.bruce.seckill.component.RedissonComponent;
import com.bruce.seckill.mapper.StockOrderMapper;
import com.bruce.seckill.model.constant.RedisConstant;
import com.bruce.seckill.model.po.Stock;
import com.bruce.seckill.model.po.StockOrder;
import com.bruce.seckill.model.po.User;
import com.bruce.seckill.service.StockOrderService;
import com.bruce.seckill.service.StockService;
import com.bruce.seckill.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Iterator;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 实现类
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
@Service
@Slf4j
public class StockOrderServiceImpl implements StockOrderService {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockOrderMapper orderMapper;

    @Autowired
    private RedissonComponent component;

    @Autowired
    private UserService userService;

    @Override
    public int createWrongOrder(int sid) throws Exception {
        //校验库存
        Stock stock = checkStock(sid);
        //扣库存
        saleStock(stock);
        //创建订单
        int id = createOrder(stock);
        return id;
    }

    @Override
    public int createOptimisticOrder(int sid) {
        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存
        boolean success = saleStockOptimistic(stock);
        if (!success) {
            throw new RuntimeException("过期库存值，更新失败");
        }
        //创建订单
        int id = createOrder(stock);
        return stock.getCount() - stock.getSale();
    }

    @Override
    public int createVerifiedOrder(Integer sid, Integer userId, String verifyHash) {
        // 验证是否在抢购时间内
        log.info("请自行验证是否在抢购时间内,假设此处验证成功");
        // 验证hash值合法性
        String hashKey = MessageFormat.format(RedisConstant.HASH_KEY, String.valueOf(sid), String.valueOf(sid));
        String verifyHashInRedis = component.getRBucket(hashKey).get();
        if (!verifyHash.equals(verifyHashInRedis)) {
            throw new RuntimeException("hash值与Redis中不符合");
        }
        log.info("验证hash值合法性成功");

        // 检查用户合法性
        User user = userService.getById(userId.longValue());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        log.info("用户信息验证成功：[{}]", user.toString());

        // 检查商品合法性
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new RuntimeException("商品不存在");
        }
        log.info("商品信息验证成功：[{}]", stock.toString());

        //乐观锁更新库存
        boolean success = saleStockOptimistic(stock);
        if (!success) {
            throw new RuntimeException("过期库存值，更新失败");
        }
        log.info("乐观锁更新库存成功");

        //创建订单
        createOrderWithUserInfoInDB(stock, userId);
        log.info("创建订单成功");

        return stock.getCount() - (stock.getSale());
    }

    @Override
    public void createOrderByMq(Integer sid, Integer userId) throws Exception {

        // // 模拟多个用户同时抢购，导致消息队列排队等候10秒
        // Thread.sleep(10000);

        Stock stock;
        //校验库存（不要学我在trycatch中做逻辑处理，这样是不优雅的。这里这样处理是为了兼容之前的秒杀系统文章）
        try {
            stock = checkStock(sid);
        } catch (Exception e) {
            log.info("库存不足！");
            return;
        }
        //乐观锁更新库存
        boolean updateStock = saleStockOptimistic(stock);
        if (!updateStock) {
            log.warn("扣减库存失败，库存已经为0");
            return;
        }

        log.info("扣减库存成功，剩余库存：[{}]", stock.getCount() - stock.getSale());
        stockService.delStockCountCache(sid);
        log.info("删除库存缓存");

        //创建订单
        log.info("写入订单至数据库");
        createOrderWithUserInfoInDB(stock, userId);
        log.info("写入订单至缓存供查询");
        createOrderWithUserInfoInCache(stock, userId);
        log.info("下单完成");
    }

    @Override
    public Boolean checkUserOrderInfoInCache(Integer sid, Integer userId) {
        String key = MessageFormat.format(RedisConstant.USER_ORDER, String.valueOf(sid));
        log.info("检查用户Id：[{}] 是否抢购过商品Id：[{}] 检查Key：[{}]", userId, sid, key);
        Iterator<String> iterator = component.getRSet(key).iterator(String.valueOf(userId));
        return iterator.hasNext();
    }

    private Stock checkStock(int sid) {
        Stock stock = stockService.getStockById(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    private int saleStock(Stock stock) {
        stock.setSale(stock.getSale() + 1);
        return stockService.updateStockById(stock);
    }

    private int createOrder(Stock stock) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        orderMapper.insert(order);
        return order.getId();
    }

    private boolean saleStockOptimistic(Stock stock) {
        log.info("查询数据库，尝试更新库存");
        int count = stockService.updateStockByOptimistic(stock);
        return count != 0;
    }

    /**
     * 创建订单：保存用户订单信息到数据库
     */
    private int createOrderWithUserInfoInDB(Stock stock, Integer userId) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setUserId(userId);
        orderMapper.insert(order);
        return order.getId();
    }

    /**
     * 创建订单：保存用户订单信息到缓存
     */
    private void createOrderWithUserInfoInCache(Stock stock, Integer userId) {
        String key = MessageFormat.format(RedisConstant.USER_ORDER, String.valueOf(stock.getId()));
        log.info("写入用户订单数据Set：[{}] [{}]", key, userId.toString());
        component.getRSet(key).add(String.valueOf(userId));
    }

}
