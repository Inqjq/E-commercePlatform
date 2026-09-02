package com.dufeng.module.user.service;

import com.dufeng.module.admin.entity.Role;
import com.dufeng.module.user.dto.ChangePasswordRequest;
import com.dufeng.module.user.dto.LoginResponse;
import com.dufeng.module.user.dto.RegisterRequest;
import com.dufeng.module.user.dto.ResetPasswordRequest;
import com.dufeng.module.user.dto.UpdateProfileRequest;
import com.dufeng.module.user.dto.UserVO;
import com.dufeng.module.user.entity.User;
import java.util.List;

public interface UserService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(String account, String password);

    void logout(String token, Long userId);

    UserVO getCurrent(Long userId);

    UserVO updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    User getById(Long id);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    User findByUsername(String username);

    User findByPhone(String phone);

    User findByAccount(String account);

    List<String> getRoles(Long userId);

    void assignRole(Long userId, String roleCode);

}
