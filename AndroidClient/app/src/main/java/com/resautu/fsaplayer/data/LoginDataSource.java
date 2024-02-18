package com.resautu.fsaplayer.data;

import com.resautu.fsaplayer.FSAPlayerApplication;
import com.resautu.fsaplayer.data.model.LoggedInUser;
import com.resautu.fsaplayer.utils.SPManager;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            SPManager.writeSP("serverAddress", username);
            SPManager.writeSP("key", password);
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}