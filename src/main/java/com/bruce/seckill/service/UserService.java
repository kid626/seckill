package com.bruce.seckill.service;

import com.bruce.seckill.model.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 层
 * @ProjectName seckill
 * @Date 2021-12-8 10:04:25
 * @Author Bruce
 */
public interface UserService extends IService<User> {

    /**
     * 获取 hash 值
     */
    String getVerifyHash(Integer sid, Integer userId);

    /**
     * 添加用户访问次数
     */
    int addUserCount(Integer userId);

    /**
     * 检查用户是否被禁
     */
    boolean getUserIsBanned(Integer userId);

}
