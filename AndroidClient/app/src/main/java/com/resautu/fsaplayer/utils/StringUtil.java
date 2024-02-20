package com.resautu.fsaplayer.utils;

public class StringUtil {

    public static String converTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minute = seconds / 60;
        int second = seconds % 60;
        return String.format("%02d:%02d", minute, second);
    }
}
