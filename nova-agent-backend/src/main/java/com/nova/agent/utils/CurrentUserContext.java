package com.nova.agent.utils;

import com.nova.agent.model.dto.CurrentUser;

public class CurrentUserContext {
    private static final ThreadLocal<CurrentUser> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(CurrentUser user) {
        USER_THREAD_LOCAL.set(user);
    }

    public static CurrentUser getUser() {
        return USER_THREAD_LOCAL.get();
    }

    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}
