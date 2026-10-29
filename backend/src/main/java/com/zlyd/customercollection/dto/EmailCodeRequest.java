package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailCodeRequest {

    @NotBlank(message = "请输入邮箱")
    private String email;

    private Integer scene = 1;
}
