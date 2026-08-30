package com.dufeng.module.payment.service;

import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.config.AlipayProperties;
import com.dufeng.config.PaymentProperties;
import com.dufeng.module.order.constant.OrderStatus;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.service.OrderService;
import com.dufeng.module.payment.dto.PayCallbackRequest;
import com.dufeng.module.payment.entity.Payment;
import com.dufeng.module.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付回调安全约束单测：验签、金额与归属校验、status 缺省按失败、幂等；
 * 支付宝异步通知的验签、app_id 校验与入账。
 */
class PaymentServiceTest {

    private static final String SECRET = "unit-test-pay-secret";
    private static final String APP_ID = "9021000123456789";

    private PaymentMapper paymentMapper;
    private OrderService orderService;
    private AlipayService alipayService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentMapper = mock(PaymentMapper.class);
        orderService = mock(OrderService.class);
        alipayService = mock(AlipayService.class);
        PaymentProperties paymentProperties = new PaymentProperties();
        paymentProperties.setSecret(SECRET);
        AlipayProperties alipayProperties = new AlipayProperties();
        alipayProperties.setEnabled(false);
        alipayProperties.setAppId(APP_ID);
        paymentService = new PaymentService(paymentMapper, orderService, paymentProperties,
                alipayProperties, alipayService);
    }

    private static String sign(String orderNo, String amount, String channel, String payNo) {
        try {
            String raw = String.join("|", orderNo, amount, channel, payNo, SECRET);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Orders order(Long id, String orderNo, int status) {
        Orders order = new Orders();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setUserId(10L);
        order.setStatus(status);
        order.setPayAmount(new BigDecimal("89.00"));
        return order;
    }

    private static Payment payment(Long id, Long orderId, String payNo, BigDecimal amount, int status) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setOrderId(orderId);
        payment.setOrderNo("ORDER1");
        payment.setPayNo(payNo);
        payment.setUserId(10L);
        payment.setChannel("alipay");
        payment.setAmount(amount);
        payment.setStatus(status);
        return payment;
    }

    private static PayCallbackRequest callback(String orderNo, String payNo,
                                               String amount, Integer status, String sign) {
        PayCallbackRequest request = new PayCallbackRequest();
        request.setOrderNo(orderNo);
        request.setPayNo(payNo);
        request.setChannel("alipay");
        request.setAmount(new BigDecimal(amount));
        request.setStatus(status);
        request.setSign(sign);
        return request;
    }

    @Test
    @DisplayName("合法签名且金额一致时，流水置为成功并推进订单状态")
    void validCallbackMarksOrderPaid() {
        when(orderService.getByNo("ORDER1")).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_PAY));
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 0));
        when(orderService.tryMarkPaid(1L)).thenReturn(true);

        paymentService.handleCallback(callback("ORDER1", "PAY1", "89.00", 1,
                sign("ORDER1", "89.00", "alipay", "PAY1")));

        verify(paymentMapper).updateById(argThat((Payment p) -> Integer.valueOf(1).equals(p.getStatus())));
        verify(orderService).tryMarkPaid(1L);
    }

    @Test
    @DisplayName("签名不匹配时拒绝回调")
    void invalidSignRejected() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.handleCallback(callback("ORDER1", "PAY1", "89.00", 1, "deadbeef")));

        assertEquals(ResultCode.PAY_SIGN_ERROR.getCode(), exception.getCode());
        verify(paymentMapper, never()).updateById(Mockito.<Payment>any());
    }

    @Test
    @DisplayName("回调金额与应付金额不一致时拒绝")
    void amountMismatchRejected() {
        when(orderService.getByNo("ORDER1")).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_PAY));
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 0));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.handleCallback(callback("ORDER1", "PAY1", "0.01", 1,
                        sign("ORDER1", "0.01", "alipay", "PAY1"))));

        assertEquals(ResultCode.PAY_AMOUNT_MISMATCH.getCode(), exception.getCode());
        verify(paymentMapper, never()).updateById(Mockito.<Payment>any());
    }

    @Test
    @DisplayName("支付流水与订单不匹配时拒绝（防交叉伪造）")
    void orderPaymentMismatchRejected() {
        when(orderService.getByNo("ORDER1")).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_PAY));
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 999L, "PAY1", new BigDecimal("89.00"), 0));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.handleCallback(callback("ORDER1", "PAY1", "89.00", 1,
                        sign("ORDER1", "89.00", "alipay", "PAY1"))));

        assertEquals(ResultCode.PAY_ORDER_MISMATCH.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("status 缺省按失败处理，不推进订单状态")
    void nullStatusTreatedAsFailure() {
        when(orderService.getByNo("ORDER1")).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_PAY));
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 0));

        paymentService.handleCallback(callback("ORDER1", "PAY1", "89.00", null,
                sign("ORDER1", "89.00", "alipay", "PAY1")));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentMapper).updateById(captor.capture());
        assertEquals(2, captor.getValue().getStatus());
        verify(orderService, never()).tryMarkPaid(any());
    }

    @Test
    @DisplayName("已成功的流水重复回调时直接幂等返回")
    void duplicateCallbackIsIdempotent() {
        when(orderService.getByNo("ORDER1")).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_SHIP));
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 1));

        paymentService.handleCallback(callback("ORDER1", "PAY1", "89.00", 1,
                sign("ORDER1", "89.00", "alipay", "PAY1")));

        verify(paymentMapper, never()).updateById(Mockito.<Payment>any());
        verify(orderService, never()).tryMarkPaid(any());
    }

    @Test
    @DisplayName("订单已取消时回调不推进订单，但流水按已支付落库（留给对账处理）")
    void callbackOnCancelledOrderDoesNotTouchOrder() {
        when(orderService.getByNo("ORDER1")).thenReturn(order(1L, "ORDER1", OrderStatus.CANCELLED));
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 0));
        when(orderService.tryMarkPaid(1L)).thenReturn(false);

        paymentService.handleCallback(callback("ORDER1", "PAY1", "89.00", 1,
                sign("ORDER1", "89.00", "alipay", "PAY1")));

        verify(orderService).tryMarkPaid(1L);
        verify(paymentMapper).updateById(argThat((Payment p) -> Integer.valueOf(1).equals(p.getStatus())));
    }

    // ===== 支付宝异步通知 =====

    private Map<String, String> alipayNotify(String tradeStatus, String payNo, String amount) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", APP_ID);
        params.put("trade_status", tradeStatus);
        params.put("out_trade_no", payNo);
        params.put("total_amount", amount);
        return params;
    }

    @Test
    @DisplayName("支付宝通知验签失败返回 failure，不入账")
    void alipayNotifyBadSignRejected() {
        when(alipayService.verifyNotify(any())).thenReturn(false);

        assertEquals("failure", paymentService.handleAlipayNotify(
                alipayNotify("TRADE_SUCCESS", "PAY1", "89.00")));
        verify(paymentMapper, never()).updateById(Mockito.<Payment>any());
    }

    @Test
    @DisplayName("支付宝通知 app_id 不匹配返回 failure")
    void alipayNotifyWrongAppIdRejected() {
        when(alipayService.verifyNotify(any())).thenReturn(true);
        Map<String, String> params = alipayNotify("TRADE_SUCCESS", "PAY1", "89.00");
        params.put("app_id", "other-app-id");

        assertEquals("failure", paymentService.handleAlipayNotify(params));
        verify(paymentMapper, never()).updateById(Mockito.<Payment>any());
    }

    @Test
    @DisplayName("支付宝成功通知：金额一致则入账并返回 success")
    void alipayNotifySuccessMarksPaid() {
        when(alipayService.verifyNotify(any())).thenReturn(true);
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 0));
        when(orderService.getById(1L)).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_PAY));
        when(orderService.tryMarkPaid(1L)).thenReturn(true);

        assertEquals("success", paymentService.handleAlipayNotify(
                alipayNotify("TRADE_SUCCESS", "PAY1", "89.00")));
        verify(orderService).tryMarkPaid(1L);
        verify(paymentMapper).updateById(argThat((Payment p) -> Integer.valueOf(1).equals(p.getStatus())));
    }

    @Test
    @DisplayName("支付宝通知金额与流水不一致拒绝入账")
    void alipayNotifyAmountMismatchRejected() {
        when(alipayService.verifyNotify(any())).thenReturn(true);
        when(paymentMapper.selectOne(any())).thenReturn(
                payment(5L, 1L, "PAY1", new BigDecimal("89.00"), 0));
        when(orderService.getById(1L)).thenReturn(order(1L, "ORDER1", OrderStatus.WAIT_PAY));

        assertEquals("failure", paymentService.handleAlipayNotify(
                alipayNotify("TRADE_SUCCESS", "PAY1", "1.00")));
        verify(orderService, never()).tryMarkPaid(any());
    }

    @Test
    @DisplayName("支付宝非成功态通知确认收到但不改状态")
    void alipayNotifyNonSuccessAckOnly() {
        when(alipayService.verifyNotify(any())).thenReturn(true);

        assertEquals("success", paymentService.handleAlipayNotify(
                alipayNotify("TRADE_CLOSED", "PAY1", "89.00")));
        verify(paymentMapper, never()).updateById(Mockito.<Payment>any());
        verify(orderService, never()).tryMarkPaid(any());
    }
}
