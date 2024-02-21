package com.resautu.fsaplayer.data;

import android.util.Log;

import com.resautu.fsaplayer.FSAPlayerApplication;
import com.resautu.fsaplayer.data.model.LoggedInUser;
import com.resautu.fsaplayer.utils.FileUtil;
import com.resautu.fsaplayer.utils.SPManager;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.Charset;

import okhttp3.Response;
import okio.BufferedSource;

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
            app.httpClient.setServerInformation(username);
            Map<String, String> form = new HashMap<>();
            form.put("key", password);

            Response response = app.httpClient.sendSyncPostRequest(form, "/login");
            String ret = "";
            if(response == null || response.code() != 200){
                ret = "登录失败，请检查输入是否正确以及网络连接是否正常";
                return new Result.Error(new IOException("Error logging in"));
            }

            Response inform_reponse = app.httpClient.sendSyncGetRequest("/inform");
            if(inform_reponse != null && inform_reponse.code() == 200){
                ret = "登录成功";
                String jsonString = inform_reponse.body().string();
                Gson gson = new Gson();
                ServerData serverData = gson.fromJson(jsonString, ServerData.class);

                String configPath = app.getFilesDir().getPath() + File.separator + app.httpClient.getServerID() + ".json";
                FileUtil.fileWriter(configPath, jsonString);

                LoggedInUser fakeUser =
                        new LoggedInUser(
                                password,
                                "Welcome!",
                                serverData);
                return new Result.Success<>(fakeUser);
            } else{
                ret = "登录失败，请检查输入是否正确以及网络连接是否正常";
                return new Result.Error(new IOException("Error logging in"));
            }
        } catch (Exception e) {
            Log.e("LoginDataSource", "login: ", e);
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}