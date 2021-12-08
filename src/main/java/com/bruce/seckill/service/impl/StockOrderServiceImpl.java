package com.bruce.seckill.service.impl;

import com.bruce.seckill.mapper.StockOrderMapper;
import com.bruce.seckill.model.po.Stock;
import com.bruce.seckill.model.po.StockOrder;
import com.bruce.seckill.service.StockOrderService;
import com.bruce.seckill.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        saleStockOptimistic(stock);
        //创建订单
        int id = createOrder(stock);
        return stock.getCount() - stock.getSale();
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

    private void saleStockOptimistic(Stock stock) {
        log.info("查询数据库，尝试更新库存");
        int count = stockService.updateStockByOptimistic(stock);
        if (count == 0) {
            throw new RuntimeException("并发更新库存失败，version不匹配");
        }
    }

}
