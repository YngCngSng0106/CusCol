package com.zlyd.customercollection.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String city;
    private Integer status;
    private String avatarUrl;
    private String avatarThumbUrl;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
