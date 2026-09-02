package com.dufeng.module.user.service;


public interface SmsCodeService {

    void sendCode(String phone);

    void verifyCode(String phone, String code);

}
