package com.zlyd.customercollection.common;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(Result.UNAUTHORIZED, "请先登录");
        }
        return user.id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
