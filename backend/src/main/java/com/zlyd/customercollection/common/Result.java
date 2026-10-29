package com.zlyd.customercollection.common;

import lombok.Getter;

@Getter
public class Result<T> {

    public static final int SUCCESS = 0;
    public static final int ERROR = 500;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;

    private final int code;
    private final String message;
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, "成功", data);
    }

    public static Result<Void> ok() {
        return new Result<>(SUCCESS, "成功", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ERROR, message, null);
    }
}
