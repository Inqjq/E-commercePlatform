package com.dufeng.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dufeng.module.order.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM t_orders WHERE pay_time >= #{start}")
    BigDecimal sumPaidAmountSince(@Param("start") LocalDateTime start);
}
