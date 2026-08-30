package com.dufeng.module.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer status;
    private List<String> roles;
}
