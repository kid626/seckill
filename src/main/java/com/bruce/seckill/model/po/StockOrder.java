package com.bruce.seckill.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

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
@TableName("stock_order")
@ApiModel(value = "StockOrder对象", description = "")
public class StockOrder implements Serializable {

    private static final long serialVersionUID = -1L;

    @ApiModelProperty(value = "")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "库存ID")
    private Integer sid;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "用户主键")
    private Integer userId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}