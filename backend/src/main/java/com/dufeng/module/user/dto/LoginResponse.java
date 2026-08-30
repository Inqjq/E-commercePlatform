package com.dufeng.module.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
}
