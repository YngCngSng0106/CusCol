package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "请输入用户名")
    @Size(min = 3, max = 20, message = "用户名长度需在3-20位之间")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码长度需在6-32位之间")
    private String password;

    @NotBlank(message = "请输入手机号")
    private String phone;

    @NotBlank(message = "请输入邮箱")
    private String email;

    @NotBlank(message = "请输入邮箱验证码")
    private String code;
}
