package com.bruce.seckill.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @Copyright Copyright © 2021 Bruce . All rights reserved.
 * @Desc 实体类
 * @ProjectName seckill
 * @Date 2021-12-7 15:27:37
 * @Author Bruce
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stock")
@ApiModel(value = "Stock对象", description = "")
public class Stock implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "库存")
    private Integer count;

    @ApiModelProperty(value = "已售")
    private Integer sale;

    @ApiModelProperty(value = "乐观锁，版本号")
    private Integer version;


}