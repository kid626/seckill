package com.bruce.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bruce.seckill.component.RedissonComponent;
import com.bruce.seckill.mapper.StockMapper;
import com.bruce.seckill.model.constant.RedisConstant;
import com.bruce.seckill.model.po.Stock;
import com.bruce.seckill.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 实现类
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
@Service
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper mapper;

    @Autowired
    private RedissonComponent component;

    @Override
    public Stock getStockById(Integer sid) {
        return mapper.selectById(sid);
    }

    @Override
    public int updateStockById(Stock stock) {
        return mapper.updateById(stock);
    }

    @Override
    public int updateStockByOptimistic(Stock stock) {
        UpdateWrapper<Stock> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(Stock::getId, stock.getId()).eq(Stock::getVersion, stock.getVersion());
        stock.setSale(stock.getSale() + 1);
        stock.setVersion(stock.getVersion() + 1);
        return mapper.update(stock, wrapper);
    }

    @Override
    public Integer getStockCount(int sid) {
        Integer stockLeft;
        stockLeft = getStockCountByCache(sid);
        log.info("缓存中取得库存数：[{}]", stockLeft);
        if (stockLeft == null) {
            stockLeft = getStockCountByDB(sid);
            log.info("缓存未命中，查询数据库，并写入缓存");
            setStockCountCache(sid, stockLeft);
        }
        return stockLeft;
    }

    @Override
    public int getStockCountByDB(int sid) {
        Stock stock = mapper.selectById(sid);
        return stock.getCount() - stock.getSale();
    }

    @Override
    public Integer getStockCountByCache(int sid) {
        String hashKey = MessageFormat.format(RedisConstant.STOCK_COUNT, String.valueOf(sid));
        String countStr = component.getRBucket(hashKey).get();
        if (countStr != null) {
            return Integer.parseInt(countStr);
        } else {
            return null;
        }
    }

    @Override
    public void delStockCountCache(int sid) {
        log.info("删除商品id：[{}] 缓存", sid);
        String hashKey = MessageFormat.format(RedisConstant.STOCK_COUNT, String.valueOf(sid));
        component.getRBucket(hashKey).delete();
    }

    private void setStockCountCache(int sid, int count) {
        String hashKey = MessageFormat.format(RedisConstant.STOCK_COUNT, String.valueOf(sid));
        String countStr = String.valueOf(count);
        log.info("写入商品库存缓存: [{}] [{}]", hashKey, countStr);
        component.getRBucket(hashKey).set(countStr, 3600, TimeUnit.SECONDS);
    }

}
