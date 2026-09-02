package com.dufeng.module.payment.service;

import java.math.BigDecimal;
import java.util.Map;

public interface AlipayService {

    String createPagePayUrl(String payNo, BigDecimal totalAmount, String subject);

    /**
     * 主动查单。
     *
     * @return 交易状态；交易不存在（未支付）返回 null
     */
    AlipayTradeStatus queryTrade(String payNo);

    boolean verifyNotify(Map<String, String> params);

    /**
     * 支付宝侧交易状态。
     */
    record AlipayTradeStatus(String tradeStatus, BigDecimal totalAmount) {

        public boolean isPaid() {
            return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        }
    }
}
