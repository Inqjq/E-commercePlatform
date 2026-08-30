package com.dufeng.module.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dufeng.module.goods.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    @Update("UPDATE t_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity} AND status = 1")
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Update("UPDATE t_sku SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Update("UPDATE t_sku SET stock = stock + #{delta} WHERE id = #{skuId}")
    int adjustStock(@Param("skuId") Long skuId, @Param("delta") int delta);
}
