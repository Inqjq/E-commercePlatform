package com.dufeng.module.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dufeng.module.goods.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    @Update("UPDATE t_goods SET sales = COALESCE(sales, 0) + #{quantity} WHERE id = #{goodsId}")
    int increaseSales(@Param("goodsId") Long goodsId, @Param("quantity") int quantity);
}
