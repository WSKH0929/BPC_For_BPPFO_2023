package com.wskh.utils;

import com.wskh.classes.Parameter;

public class TimeUtil {
    public static long startTime;

    public static long getCurTime() {
        return System.currentTimeMillis() - startTime;
    }

    public static boolean isTimeLimit() {
        return System.currentTimeMillis() - startTime >= Parameter.TimeLimit;
    }

    public static long getRemainingTime() {
        return Math.max(0, Parameter.TimeLimit - getCurTime());
    }
}
