package com.zlyd.customercollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CaptchaResponse {

    private String key;
    private String image;
}
