package com.zlyd.customercollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailCodeResponse {

    private String message;
    private String devCode;
}
