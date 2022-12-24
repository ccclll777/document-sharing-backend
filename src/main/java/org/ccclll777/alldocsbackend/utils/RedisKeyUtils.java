package org.ccclll777.alldocsbackend.utils;

public class RedisKeyUtils {
    private RedisKeyUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getSearchHistoryKey(String userid) {
        return userid;
    }

}
