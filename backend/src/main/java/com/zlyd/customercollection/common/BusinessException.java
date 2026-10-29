package com.zlyd.customercollection.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(Result.ERROR, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
