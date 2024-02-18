package com.resautu.fsaplayer;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.resautu.fsaplayer.ui.login.LoginViewModel;
import com.resautu.fsaplayer.ui.login.LoginViewModelFactory;
import com.resautu.fsaplayer.utils.HTTPClient;
import com.resautu.fsaplayer.utils.SPManager;


public class FSAPlayerApplication extends Application {
    private volatile static FSAPlayerApplication instance;
    private static String serverAddress;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public LoginViewModel loginViewModel;
    public HTTPClient httpClient;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        sharedPreferences = getSharedPreferences("FSAPlayer", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        SPManager.init(this);
        httpClient = HTTPClient.getInstance();
    }
    public synchronized static FSAPlayerApplication getInstance() {
        return instance;
    }
}
