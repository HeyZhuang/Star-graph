package cn.itcast.star.graph.core.utils;

import cn.itcast.star.graph.core.pojo.User;

public class UserUtils {

    static ThreadLocal<User> local = new ThreadLocal<>();

    public static void saveUser(User user) {
        local.set(user);
    }

    public static User getUser() {
        return local.get();
    }

    public static void removeUser() {
        local.remove();
    }
}
