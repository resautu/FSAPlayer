package com.resautu.fsaplayer.ui.login;

import com.resautu.fsaplayer.data.ServerData;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String displayName;
    private ServerData serverData;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName, ServerData serverData) {
        this.displayName = displayName;
        this.serverData = serverData;
    }

    String getDisplayName() {
        return displayName;
    }

    public ServerData getServerData() {
        return serverData;
    }
}