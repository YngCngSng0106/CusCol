package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;

    @NotBlank(message = "请输入验证码")
    private String captchaCode;

    @NotBlank(message = "验证码已失效，请刷新")
    private String captchaKey;
}
