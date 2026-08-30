package com.dufeng.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dufeng.common.constant.PaymentConstants;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.config.AlipayProperties;
import com.dufeng.config.PaymentProperties;
import com.dufeng.module.order.constant.OrderStatus;
import com.dufeng.module.order.entity.Orders;
import com.dufeng.module.order.service.OrderService;
import com.dufeng.module.payment.dto.PayCallbackRequest;
import com.dufeng.module.payment.dto.PayResponse;
import com.dufeng.module.payment.dto.PayStatusResponse;
import com.dufeng.module.payment.entity.Payment;
import com.dufeng.module.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付服务。真实场景对接微信/支付宝 SDK，这里做可扩展的模拟实现：
 * 模拟渠道回调签名：HmacSHA256(orderNo|amount|channel|payNo|secret)，密钥从配置读取。
 * <p>支付宝真实支付（沙箱/生产）由 {@link AlipayService} 承担协议交互，本服务负责入账与状态流转。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderService orderService;
    private final PaymentProperties paymentProperties;
    private final AlipayProperties alipayProperties;
    private final AlipayService alipayService;

    @Transactional(rollbackFor = Exception.class)
    public PayResponse createPay(Long userId, String orderNo, String channel) {
        Orders order = orderService.getByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (!Integer.valueOf(OrderStatus.WAIT_PAY).equals(order.getStatus())) {
            // 已支付订单幂等返回既有流水，便于收银台重复点击
            if (Integer.valueOf(OrderStatus.WAIT_SHIP).equals(order.getStatus())) {
                Payment paid = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, order.getId())
                        .eq(Payment::getStatus, 1)
                        .last("LIMIT 1"));
                if (paid != null) {
                    return toResponse(paid, order);
                }
            }
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
        // 若存在未支付流水则复用
        Payment existing = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, order.getId())
                .eq(Payment::getStatus, 0)
                .last("LIMIT 1"));
        if (existing != null) {
            return toResponse(existing, order);
        }
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setPayNo(generatePayNo());
        payment.setUserId(order.getUserId());
        payment.setChannel(channel);
        payment.setAmount(order.getPayAmount());
        payment.setStatus(0);
        paymentMapper.insert(payment);
        // 支付宝真实渠道：返回收银台跳转地址，等异步通知/主动查单入账；
        // 其余渠道：模拟网关回调（仅开发/联调开启），走完整的验签与状态流转链路
        if (!isRealAlipay(payment.getChannel())) {
            if (paymentProperties.isMockCallbackEnabled()) {
                simulateGatewayCallback(payment);
            }
        }
        return toResponse(payment, order);
    }

    /** 是否走真实支付宝（渠道为 alipay 且已启用沙箱/生产配置）。 */
    private boolean isRealAlipay(String channel) {
        return PaymentConstants.CHANNEL_ALIPAY.equals(channel) && alipayProperties.isEnabled();
    }

    /**
     * 支付回调：验签 + 金额/归属校验 + 幂等更新订单与支付状态。
     * <p>安全约束：status 缺省按失败处理；金额必须与支付流水一致；
     * 流水与订单必须归属同一笔交易；订单状态流转采用 CAS，冲突即回滚。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(PayCallbackRequest request) {
        if (!verifySign(request)) {
            throw new BusinessException(ResultCode.PAY_SIGN_ERROR);
        }
        Orders order = orderService.getByNo(request.getOrderNo());
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, request.getPayNo()));
        if (payment == null) {
            throw new BusinessException(ResultCode.PAY_NOT_FOUND);
        }
        if (!Objects.equals(payment.getOrderId(), order.getId())) {
            throw new BusinessException(ResultCode.PAY_ORDER_MISMATCH);
        }
        boolean success = Integer.valueOf(1).equals(request.getStatus());
        if (success && payment.getAmount() != null
                && request.getAmount().compareTo(payment.getAmount()) != 0) {
            throw new BusinessException(ResultCode.PAY_AMOUNT_MISMATCH);
        }
        // 幂等：已成功则直接返回
        if (Integer.valueOf(1).equals(payment.getStatus())) {
            return;
        }
        payment.setStatus(success ? 1 : 2);
        payment.setCallbackTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        if (!success) {
            log.info("[支付] 回调失败结果，orderNo={}，payNo={}", request.getOrderNo(), request.getPayNo());
            return;
        }
        // CAS 将订单置为已支付：仅当订单仍为待付款时生效，与取消/关单并发时只有一方能赢
        if (!orderService.tryMarkPaid(order.getId())) {
            if (Integer.valueOf(OrderStatus.WAIT_SHIP).equals(order.getStatus())) {
                // 订单已支付但流水未更新（前次中断），补记流水即可
                log.warn("[支付] 订单已支付但流水未同步，补记流水 orderNo={}，payNo={}", order.getOrderNo(), payment.getPayNo());
                return;
            }
            // 订单已取消/关闭：记录日志并放弃（不再回滚流水，避免网关无限重试），由对账/退款流程处理
            log.error("[支付] 订单非待付款状态，回调被拒绝，需人工核对 orderNo={}，payNo={}", order.getOrderNo(), payment.getPayNo());
        }
    }

    /**
     * 模拟支付网关异步回调：构造带合法签名的成功回调走完整处理链路。
     */
    private void simulateGatewayCallback(Payment payment) {
        PayCallbackRequest callback = new PayCallbackRequest();
        callback.setOrderNo(payment.getOrderNo());
        callback.setPayNo(payment.getPayNo());
        callback.setChannel(payment.getChannel());
        callback.setAmount(payment.getAmount());
        callback.setStatus(1);
        callback.setSign(sign(payment.getOrderNo(), payment.getAmount(),
                payment.getChannel(), payment.getPayNo()));
        handleCallback(callback);
    }

    private boolean verifySign(PayCallbackRequest request) {        if (request.getSign() == null || request.getSign().isBlank()) {
            return false;
        }
        String expected = sign(request.getOrderNo(), request.getAmount(), request.getChannel(), request.getPayNo());
        return expected.equalsIgnoreCase(request.getSign());
    }

    private String sign(String orderNo, BigDecimal amount, String channel, String payNo) {
        String raw = orderNo + PaymentConstants.SIGN_FIELD_SEPARATOR
                + amount.toPlainString() + PaymentConstants.SIGN_FIELD_SEPARATOR
                + channel + PaymentConstants.SIGN_FIELD_SEPARATOR
                + payNo + PaymentConstants.SIGN_FIELD_SEPARATOR
                + paymentProperties.getSecret();
        try {
            Mac mac = Mac.getInstance(PaymentConstants.SIGN_ALGORITHM);
            mac.init(new SecretKeySpec(
                    paymentProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                    PaymentConstants.SIGN_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("签名计算失败", e);
        }
    }

    private PayResponse toResponse(Payment payment, Orders order) {
        PayResponse.PayResponseBuilder builder = PayResponse.builder()
                .orderNo(order.getOrderNo())
                .payNo(payment.getPayNo())
                .amount(order.getPayAmount())
                .channel(payment.getChannel())
                .prepayParams(PaymentConstants.PREPAY_PARAMS_PREFIX + payment.getPayNo() + ":" + payment.getChannel());
        if (isRealAlipay(payment.getChannel())) {
            builder.payUrl(alipayService.createPagePayUrl(payment.getPayNo(), payment.getAmount(),
                    "渡风电商订单 " + order.getOrderNo()));
        }
        return builder.build();
    }

    /**
     * 收银台轮询：订单已支付直接返回；未支付且为支付宝渠道时主动查单并入账。
     * 使本地无公网回调地址时也能完成支付闭环。
     */
    @Transactional(rollbackFor = Exception.class)
    public PayStatusResponse syncPayStatus(Long userId, String orderNo) {
        Orders order = orderService.getByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (Integer.valueOf(OrderStatus.WAIT_PAY).equals(order.getStatus()) && alipayProperties.isEnabled()) {
            Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getOrderId, order.getId())
                    .eq(Payment::getChannel, PaymentConstants.CHANNEL_ALIPAY)
                    .orderByDesc(Payment::getId)
                    .last("LIMIT 1"));
            if (payment != null && !Integer.valueOf(1).equals(payment.getStatus())) {
                AlipayService.AlipayTradeStatus trade = alipayService.queryTrade(payment.getPayNo());
                if (trade != null && trade.isPaid()) {
                    markGatewayPaid(payment.getPayNo(), trade.totalAmount());
                }
            }
        }
        boolean paid = !Integer.valueOf(OrderStatus.WAIT_PAY).equals(
                orderService.getById(order.getId()).getStatus());
        return PayStatusResponse.builder().orderNo(orderNo).paid(paid).build();
    }

    /**
     * 支付宝异步通知处理：验签（支付宝公钥）+ app_id 校验 + 金额校验 + 幂等入账。
     * 返回 "success"/"failure" 纯文本，支付宝以 "success" 判定通知成功。
     */
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        if (!alipayService.verifyNotify(params)) {
            log.warn("[支付宝] 异步通知验签失败，可能为伪造请求");
            return "failure";
        }
        if (StringUtils.hasText(alipayProperties.getAppId())
                && !alipayProperties.getAppId().equals(params.get("app_id"))) {
            log.warn("[支付宝] 异步通知 app_id 不匹配：{}", params.get("app_id"));
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            // 非成功态（交易创建/关闭等）确认收到即可，不改订单状态
            return "success";
        }
        BigDecimal paidAmount;
        try {
            paidAmount = params.get("total_amount") == null ? null : new BigDecimal(params.get("total_amount"));
        } catch (NumberFormatException e) {
            log.warn("[支付宝] 异步通知金额格式非法：{}", params.get("total_amount"));
            return "failure";
        }
        return markGatewayPaid(params.get("out_trade_no"), paidAmount) ? "success" : "failure";
    }

    /**
     * 网关侧支付成功入账：金额比对 + 流水幂等 + 订单 CAS 推进。
     * 供支付宝异步通知与主动查单共用。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean markGatewayPaid(String payNo, BigDecimal paidAmount) {
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo));
        if (payment == null) {
            log.warn("[支付] 入账失败，流水不存在 payNo={}", payNo);
            return false;
        }
        Orders order = orderService.getById(payment.getOrderId());
        if (paidAmount != null && payment.getAmount() != null
                && paidAmount.compareTo(payment.getAmount()) != 0) {
            log.error("[支付] 网关金额与应付不一致 payNo={} 网关={} 应付={}，拒绝入账",
                    payNo, paidAmount, payment.getAmount());
            return false;
        }
        if (Integer.valueOf(1).equals(payment.getStatus())) {
            return true;
        }
        payment.setStatus(1);
        payment.setCallbackTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        if (!orderService.tryMarkPaid(order.getId())) {
            if (Integer.valueOf(OrderStatus.WAIT_SHIP).equals(order.getStatus())) {
                return true;
            }
            log.error("[支付] 订单非待付款状态，入账被拒绝，需人工核对 payNo={} orderNo={}", payNo, order.getOrderNo());
        }
        return true;
    }

    private String generatePayNo() {
        return PaymentConstants.PAY_NO_PREFIX + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
