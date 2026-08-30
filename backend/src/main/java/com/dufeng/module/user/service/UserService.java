package com.dufeng.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dufeng.common.constant.SecurityConstants;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.admin.entity.Role;
import com.dufeng.module.admin.mapper.RoleMapper;
import com.dufeng.module.user.dto.ChangePasswordRequest;
import com.dufeng.module.user.dto.LoginResponse;
import com.dufeng.module.user.dto.RegisterRequest;
import com.dufeng.module.user.dto.ResetPasswordRequest;
import com.dufeng.module.user.dto.UpdateProfileRequest;
import com.dufeng.module.user.dto.UserVO;
import com.dufeng.module.user.entity.User;
import com.dufeng.module.user.entity.UserRole;
import com.dufeng.module.user.mapper.UserMapper;
import com.dufeng.module.user.mapper.UserRoleMapper;
import com.dufeng.security.AuthSessionService;
import com.dufeng.security.JwtUtil;
import com.dufeng.security.LoginGuard;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户注册、登录、资料与密码管理。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthSessionService authSessionService;
    private final SmsCodeService smsCodeService;
    private final LoginGuard loginGuard;

    /**
     * 注册并自动登录，默认绑定 USER 角色。
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        if (StringUtils.hasText(request.getPhone()) && existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.USER_EXISTS, BusinessMessages.PHONE_ALREADY_REGISTERED);
        }
        // 绑定手机号必须校验短信验证码，防止绕过手机归属校验
        if (StringUtils.hasText(request.getPhone())) {
            if (!StringUtils.hasText(request.getVerifyCode())) {
                throw new BusinessException(ResultCode.SMS_VERIFY_CODE_REQUIRED);
            }
            smsCodeService.verifyCode(request.getPhone(), request.getVerifyCode());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(StringUtils.hasText(request.getNickname())
                ? request.getNickname() : request.getUsername());
        user.setGender(0);
        user.setStatus(1);
        userMapper.insert(user);

        assignRole(user.getId(), SecurityConstants.ROLE_USER);

        List<String> roles = getRoles(user.getId());
        return buildLoginResponse(user, roles);
    }

    public LoginResponse login(String account, String password) {
        loginGuard.checkLocked(account);
        User user = findByAccount(account);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            loginGuard.recordFailure(account);
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        loginGuard.reset(account);
        List<String> roles = getRoles(user.getId());
        return buildLoginResponse(user, roles);
    }

    public void logout(String token, Long userId) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            authSessionService.logout(userId, claims.getId(), jwtUtil.getExpireSeconds());
        } catch (Exception e) {
            // 令牌过期或无效时静默退出
        }
    }

    public UserVO getCurrent(Long userId) {
        User user = getById(userId);
        return toVO(user, getRoles(userId));
    }

    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        userMapper.updateById(user);
        return getCurrent(userId);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        // 密码已变更，失效当前会话，强制重新登录
        authSessionService.invalidateUser(userId);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = findByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        smsCodeService.verifyCode(request.getPhone(), request.getVerifyCode());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        authSessionService.invalidateUser(user.getId());
    }

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    public boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    public boolean existsByPhone(String phone) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)) > 0;
    }

    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    public User findByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    public User findByAccount(String account) {
        User user = findByUsername(account);
        if (user != null) {
            return user;
        }
        user = findByPhone(account);
        if (user != null) {
            return user;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, account));
    }

    public List<String> getRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                .stream()
                .map(UserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of(SecurityConstants.ROLE_USER);
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet())
                .stream()
                .toList();
    }

    public void assignRole(Long userId, String roleCode) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
        if (role == null) {
            return;
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, role.getId()));
        if (count > 0) {
            return;
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);
        // 角色写入 JWT，需失效存量会话让用户重新登录获取新角色
        authSessionService.invalidateUser(userId);
    }

    private LoginResponse buildLoginResponse(User user, List<String> roles) {
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), roles);
        authSessionService.saveSession(user.getId(), token);
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .build();
    }

    private UserVO toVO(User user, List<String> roles) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setRoles(roles);
        return vo;
    }
}
