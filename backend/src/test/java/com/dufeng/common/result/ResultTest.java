package com.dufeng.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void successShouldCarryZeroCodeAndData() {
        Result<String> result = Result.success("ok");
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("ok", result.getData());
    }

    @Test
    void failureShouldCarryErrorCode() {
        Result<Void> result = Result.failure(ResultCode.USER_NOT_FOUND);
        assertEquals(10001, result.getCode());
        assertEquals("用户不存在或已被禁用", result.getMessage());
        assertNull(result.getData());
    }
}
