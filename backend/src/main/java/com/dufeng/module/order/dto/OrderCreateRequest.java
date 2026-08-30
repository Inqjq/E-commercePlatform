package com.dufeng.module.order.dto;

import com.dufeng.common.constant.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    @NotNull(message = ValidationMessages.ADDRESS_ID_NOT_NULL)
    private Long addressId;

    /** 下单来源必须由客户端显式声明：true 购物车结算，false 立即购买。 */
    @NotNull(message = ValidationMessages.FROM_CART_NOT_NULL)
    private Boolean fromCart;

    /** 立即购买时传 SKU 明细；fromCart=false 时生效。 */
    @Valid
    @Size(max = 50, message = ValidationMessages.ORDER_ITEMS_SIZE)
    private List<OrderItemRequest> items;

    @Size(max = 200, message = ValidationMessages.REMARK_SIZE)
    private String remark;

    /** 幂等请求标识，防止重复下单。 */
    private String requestId;
}
