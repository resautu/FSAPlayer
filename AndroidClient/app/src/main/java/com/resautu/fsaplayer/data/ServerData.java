package com.resautu.fsaplayer.data;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ServerData {
    @SerializedName("CalculateTime")
    private String hashCalculateTime;
    @SerializedName("ServerIp")
    private String serverIP;
    @SerializedName("Port")
    private String port;
    @SerializedName("ServerDesc")
    private String serverDesc;
    @SerializedName("Hashes")
    private HashMap<String, ItemData> items;

    public ServerData(String hashCalculateTime, String serverIP, String port, String serverDesc, HashMap<String, ItemData> items){
        this.hashCalculateTime = hashCalculateTime;
        this.serverIP = serverIP;
        this.port = port;
        this.serverDesc = serverDesc;
        this.items = items;
    }

    public String getHashCalculateTime() {
        return hashCalculateTime;
    }

    public String getServerIP() {
        return serverIP;
    }

    public String getPort() {
        return port;
    }

    public String getServerDesc() {
        return serverDesc;
    }

    public HashMap<String, ItemData> getItems() {
        return items;
    }

    public List<ItemData> getAudioList() {
        return new ArrayList<ItemData>(items.values());
    }
}
