package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.dto.CaptchaResponse;
import com.zlyd.customercollection.dto.EmailCodeRequest;
import com.zlyd.customercollection.dto.EmailCodeResponse;
import com.zlyd.customercollection.dto.LoginRequest;
import com.zlyd.customercollection.dto.LoginResponse;
import com.zlyd.customercollection.dto.RegisterRequest;
import com.zlyd.customercollection.service.AuthService;
import com.zlyd.customercollection.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.ok(captchaService.create());
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }

    @PostMapping("/email-code")
    public Result<EmailCodeResponse> emailCode(@Valid @RequestBody EmailCodeRequest request,
                                               HttpServletRequest servletRequest) {
        return Result.ok(authService.sendEmailCode(request.getEmail(), request.getScene(),
                servletRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }
}
