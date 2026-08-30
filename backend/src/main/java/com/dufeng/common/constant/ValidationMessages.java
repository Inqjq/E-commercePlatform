package com.dufeng.common.constant;

/**
 * Bean Validation 校验提示统一管理。
 */
public final class ValidationMessages {

    public static final String USERNAME_NOT_BLANK = "用户名不能为空";
    public static final String USERNAME_SIZE = "用户名长度需在 4-32 位之间";
    public static final String PASSWORD_NOT_BLANK = "密码不能为空";
    public static final String PASSWORD_SIZE = "密码长度需在 6-32 位之间";
    public static final String NEW_PASSWORD_NOT_BLANK = "新密码不能为空";
    public static final String NEW_PASSWORD_SIZE = "新密码长度需在 6-32 位之间";
    public static final String OLD_PASSWORD_NOT_BLANK = "原密码不能为空";
    public static final String ACCOUNT_NOT_BLANK = "账号不能为空";
    public static final String PHONE_PATTERN = "手机号格式不正确";
    public static final String PHONE_NOT_BLANK = "手机号不能为空";
    public static final String EMAIL_PATTERN = "邮箱格式不正确";
    public static final String VERIFY_CODE_NOT_BLANK = "验证码不能为空";
    public static final String NICKNAME_SIZE = "昵称最长 64 字符";
    public static final String INTRO_SIZE = "简介最长 200 字符";

    public static final String RECEIVER_NOT_BLANK = "收货人不能为空";
    public static final String CONTACT_PHONE_NOT_BLANK = "联系电话不能为空";
    public static final String PROVINCE_NOT_BLANK = "省份不能为空";
    public static final String CITY_NOT_BLANK = "城市不能为空";
    public static final String DISTRICT_NOT_BLANK = "区县不能为空";
    public static final String DETAIL_NOT_BLANK = "详细地址不能为空";

    public static final String SKU_NOT_NULL = "SKU 不能为空";
    public static final String QUANTITY_NOT_NULL = "数量不能为空";
    public static final String QUANTITY_MIN = "数量至少为 1";
    public static final String CHECKED_NOT_NULL = "勾选状态不能为空";

    public static final String CATEGORY_NOT_NULL = "类目不能为空";
    public static final String GOODS_TITLE_NOT_BLANK = "商品标题不能为空";
    public static final String SKU_PRICE_NOT_NULL = "规格价格不能为空";
    public static final String SKU_STOCK_NOT_NULL = "规格库存不能为空";
    public static final String STOCK_NON_NEGATIVE = "库存不能为负数";

    public static final String MERCHANT_NAME_NOT_BLANK = "商家名称不能为空";
    public static final String LICENSE_NO_NOT_BLANK = "营业执照号不能为空";
    public static final String LEGAL_PERSON_NOT_BLANK = "法人不能为空";
    public static final String MERCHANT_PHONE_PATTERN = "联系电话格式不正确";
    public static final String SHOP_NAME_NOT_BLANK = "店铺名称不能为空";

    public static final String ADDRESS_ID_NOT_NULL = "收货地址不能为空";
    public static final String LOGISTICS_COMPANY_NOT_BLANK = "物流公司不能为空";
    public static final String LOGISTICS_NO_NOT_BLANK = "物流单号不能为空";
    public static final String ORDER_NO_NOT_BLANK = "订单号不能为空";
    public static final String PAY_NO_NOT_BLANK = "支付流水号不能为空";
    public static final String PAY_CHANNEL_NOT_BLANK = "支付渠道不能为空";
    public static final String PAY_AMOUNT_NOT_NULL = "支付金额不能为空";

    public static final String ORDER_ITEM_ID_NOT_NULL = "订单明细不能为空";
    public static final String SCORE_NOT_NULL = "评分不能为空";
    public static final String SCORE_MIN = "评分最低 1 分";
    public static final String SCORE_MAX = "评分最高 5 分";
    public static final String REVIEW_CONTENT_SIZE = "评价最长 1024 字符";

    public static final String AUDIT_RESULT_NOT_NULL = "审核结果不能为空";
    public static final String ROLE_NAME_NOT_BLANK = "角色名称不能为空";
    public static final String ROLE_CODE_NOT_BLANK = "角色编码不能为空";

    public static final String FROM_CART_NOT_NULL = "下单方式不能为空";
    public static final String ORDER_ITEMS_SIZE = "单次下单商品种类不能超过 50 种";
    public static final String REMARK_SIZE = "订单备注最长 200 字符";

    private ValidationMessages() {
    }
}
