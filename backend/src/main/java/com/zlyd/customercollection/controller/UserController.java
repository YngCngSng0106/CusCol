package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.common.UserContext;
import com.zlyd.customercollection.dto.ChangePasswordRequest;
import com.zlyd.customercollection.dto.UpdateProfileRequest;
import com.zlyd.customercollection.dto.UserProfileVO;
import com.zlyd.customercollection.entity.Attachment;
import com.zlyd.customercollection.service.AttachmentService;
import com.zlyd.customercollection.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AttachmentService attachmentService;

    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.ok(userService.getProfile(UserContext.userId()));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(UserContext.userId(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(UserContext.userId(), request);
        return Result.ok();
    }

    @PostMapping("/avatar")
    public Result<Attachment> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.ok(attachmentService.uploadAvatar(file, UserContext.userId()));
    }
}
