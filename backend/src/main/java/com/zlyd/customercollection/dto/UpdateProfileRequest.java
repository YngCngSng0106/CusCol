package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "请输入用户名")
    @Size(min = 3, max = 20, message = "用户名长度需在3-20位之间")
    private String username;

    private String city;
}
