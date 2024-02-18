package com.resautu.fsaplayer.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class SPManager {
    private static java.util.concurrent.Executors Executors;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void init(Context context){
        sharedPreferences = context.getSharedPreferences("FSAPlayer", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public static void writeSP(Map<String, String> mp){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, String> entry : mp.entrySet()) {
                    editor.putString(entry.getKey(), entry.getValue());
                }
                editor.apply();
            }
        });
    }
    public static void writeSP(String key, String value){

        executor.execute(new Runnable() {
            @Override
            public void run() {
                editor.putString(key, value);
                editor.apply();
            }
        });
    }
    public static void writeSP(String key, Set<String> st){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                editor.putStringSet(key, st);
                editor.apply();
            }
        });
    }
    public static String readSP( String key, String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }

}
