package com.bruce.seckill.service.impl;

import com.bruce.seckill.model.po.User;
import com.bruce.seckill.mapper.UserMapper;
import com.bruce.seckill.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc service 实现类
 * @ProjectName seckill
 * @Date 2021-12-8 10:04:25
 * @Author Bruce
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
