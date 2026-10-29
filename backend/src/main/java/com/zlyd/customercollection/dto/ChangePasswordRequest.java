package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "请输入旧密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "新密码长度需在6-32位之间")
    private String newPassword;

    @NotBlank(message = "请确认新密码")
    private String confirmPassword;
}
