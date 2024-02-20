package com.resautu.fsaplayer;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.resautu.fsaplayer.data.ItemData;
import com.resautu.fsaplayer.data.ServerData;
import com.resautu.fsaplayer.ui.login.LoginViewModel;
import com.resautu.fsaplayer.ui.login.LoginViewModelFactory;
import com.resautu.fsaplayer.utils.HTTPClient;
import com.resautu.fsaplayer.utils.SPManager;

import java.util.List;


public class FSAPlayerApplication extends Application {
    private volatile static FSAPlayerApplication instance;
    private ServerData serverData;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public HTTPClient httpClient;
    private MutableLiveData<String> displayName = new MutableLiveData<>();
    private List<ItemData> audioItemList;

    public void setServerData(ServerData serverData) {
        this.serverData = serverData;
    }
    public ServerData getServerData() {
        return serverData;
    }

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

    public MutableLiveData<String> getDisplayName() {
        return displayName;
    }

    public List<ItemData> getAudioItemList() {
        return audioItemList;
    }

    public void setAudioItemList(List<ItemData> audioItemList) {
        this.audioItemList = audioItemList;
    }
}
