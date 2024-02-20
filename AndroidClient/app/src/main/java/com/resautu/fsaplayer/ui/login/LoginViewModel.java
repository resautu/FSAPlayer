package com.resautu.fsaplayer.ui.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.resautu.fsaplayer.FSAPlayerApplication;
import com.resautu.fsaplayer.data.LoginRepository;
import com.resautu.fsaplayer.data.Result;
import com.resautu.fsaplayer.data.model.LoggedInUser;
import com.resautu.fsaplayer.R;
import com.resautu.fsaplayer.utils.SPManager;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.Response;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private FSAPlayerApplication app;
    private Result<LoggedInUser> result;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName(), data.getServerData())));
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        }
    };
    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        this.app = FSAPlayerApplication.getInstance();
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Runnable r = new Runnable() {
            @Override
            public void run() {
                result = loginRepository.login(username, password);
                Message message = new Message();
                handler.sendMessage(message);
            }
        };
        new Thread(r).start();
        return;
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_address, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
            return !username.trim().isEmpty();
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");

        return password != null && pattern.matcher(password).matches();
    }
}