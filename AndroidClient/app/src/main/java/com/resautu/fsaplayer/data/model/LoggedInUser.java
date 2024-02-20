package com.resautu.fsaplayer.data.model;

import com.resautu.fsaplayer.data.ServerData;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private ServerData serverData;

    public LoggedInUser(String userId, String displayName, ServerData serverData) {
        this.userId = userId;
        this.displayName = displayName;
        this.serverData = serverData;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ServerData getServerData() {
        return serverData;
    }
}