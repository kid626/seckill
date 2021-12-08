package com.bruce.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bruce.seckill.mapper.StockMapper;
import com.bruce.seckill.model.po.Stock;
import com.bruce.seckill.service.StockService;
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
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper mapper;

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

}
