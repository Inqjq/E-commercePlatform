package com.dufeng.module.payment.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.config.AlipayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝开放平台服务封装（官方 SDK）。
 * <p>仅做协议交互：下单拿收银台跳转地址、查单、异步通知验签；
 * 入账与订单状态流转统一由 PaymentService 完成。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayProperties properties;

    private volatile AlipayClient client;

    /** 电脑网站支付：返回支付宝收银台跳转地址（GET 形式，前端直接跳转）。 */
    public String createPagePayUrl(String payNo, BigDecimal totalAmount, String subject) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (StringUtils.hasText(properties.getNotifyUrl())) {
            request.setNotifyUrl(properties.getNotifyUrl());
        }
        if (StringUtils.hasText(properties.getReturnUrl())) {
            request.setReturnUrl(properties.getReturnUrl());
        }
        JSONObject biz = new JSONObject();
        // 用支付流水号作为商户订单号，回调/查单可直接定位流水
        biz.put("out_trade_no", payNo);
        biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
        biz.put("total_amount", totalAmount.toPlainString());
        biz.put("subject", subject);
        request.setBizContent(biz.toJSONString());
        try {
            AlipayTradePagePayResponse response = client().pageExecute(request, "GET");
            if (!response.isSuccess() || !StringUtils.hasText(response.getBody())) {
                log.error("[支付宝] 预下单失败：{}", response.getSubMsg());
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "支付宝下单失败：" + response.getSubMsg());
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("[支付宝] 预下单异常：{}", e.getMessage());
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "支付宝下单异常");
        }
    }

    /**
     * 主动查单。
     *
     * @return 交易状态；交易不存在（未扫码支付）返回 null
     */
    public AlipayTradeStatus queryTrade(String payNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject biz = new JSONObject();
        biz.put("out_trade_no", payNo);
        request.setBizContent(biz.toJSONString());
        try {
            AlipayTradeQueryResponse response = client().execute(request);
            if (!response.isSuccess()) {
                // 交易不存在 = 用户还没支付，属正常态
                return null;
            }
            return new AlipayTradeStatus(response.getTradeStatus(),
                    response.getTotalAmount() == null ? null : new BigDecimal(response.getTotalAmount()));
        } catch (AlipayApiException e) {
            log.warn("[支付宝] 查单异常：{}", e.getMessage());
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "支付宝查单异常");
        }
    }

    /** 校验异步通知签名（使用支付宝公钥，防伪造通知）。 */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params, properties.getAlipayPublicKey(),
                    properties.getCharset(), properties.getSignType());
        } catch (AlipayApiException e) {
            log.warn("[支付宝] 通知验签异常：{}", e.getMessage());
            return false;
        }
    }

    private AlipayClient client() {
        if (!properties.isEnabled()) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "支付宝支付未启用");
        }
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new DefaultAlipayClient(properties.getGateway(), properties.getAppId(),
                            properties.getPrivateKey(), properties.getFormat(), properties.getCharset(),
                            properties.getAlipayPublicKey(), properties.getSignType());
                }
            }
        }
        return client;
    }

    /**
     * 支付宝侧交易状态。
     */
    public record AlipayTradeStatus(String tradeStatus, BigDecimal totalAmount) {

        public boolean isPaid() {
            return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        }
    }
}
