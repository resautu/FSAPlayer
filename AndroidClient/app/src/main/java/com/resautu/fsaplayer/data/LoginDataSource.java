package com.resautu.fsaplayer.data;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.resautu.fsaplayer.FSAPlayerApplication;
import com.resautu.fsaplayer.data.model.LoggedInUser;
import com.resautu.fsaplayer.utils.SPManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private FSAPlayerApplication app;

    public LoginDataSource() {
        this.app = FSAPlayerApplication.getInstance();
    }

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            SPManager.writeSP("serverAddress", username);
            SPManager.writeSP("key", password);
            app.httpClient.init(username);
            Map<String, String> form = new HashMap<>();
            form.put("key", password);

            Response response = app.httpClient.sendSyncPostRequest(form, "/login");
            String ret = "";
            if(response != null && response.code() == 200){
                ret = "登录成功";
            } else{
                ret = "登录失败，请检查输入是否正确以及网络连接是否正常";
            }
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            password,
                            "Welcome!");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}