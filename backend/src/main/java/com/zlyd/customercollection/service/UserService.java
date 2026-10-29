package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.dto.ChangePasswordRequest;
import com.zlyd.customercollection.dto.UpdateProfileRequest;
import com.zlyd.customercollection.dto.UserProfileVO;
import com.zlyd.customercollection.entity.User;
import com.zlyd.customercollection.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AttachmentService attachmentService;

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public UserProfileVO getProfile(Long userId) {
        return buildProfile(getById(userId));
    }

    public UserProfileVO buildProfile(User user) {
        UserProfileVO profile = new UserProfileVO();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setPhone(user.getPhone());
        profile.setEmail(user.getEmail());
        profile.setCity(user.getCity());
        profile.setStatus(user.getStatus());
        profile.setLastLoginTime(user.getLastLoginTime());
        profile.setCreateTime(user.getCreateTime());
        profile.setAvatarUrl(attachmentService.firstAvatarUrl(user.getId()));
        profile.setAvatarThumbUrl(attachmentService.firstAvatarThumbUrl(user.getId()));
        return profile;
    }

    @Transactional
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest request) {
        String username = request.getUsername().trim();
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)
                .ne(User::getId, userId));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = getById(userId);
        user.setUsername(username);
        user.setCity(request.getCity());
        user.setUpdateBy(userId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return buildProfile(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        User user = getById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateBy(userId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
