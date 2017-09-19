package com.app.toado.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ghanendra on 28/07/2017.
 */

public class ActiveChatsWallpaperRealm extends RealmObject {

    private String wallpaperURL;

    @PrimaryKey
    String chatref;

    public ActiveChatsWallpaperRealm(String wallpaperURL, String chatref) {
        this.chatref = chatref;
        this.wallpaperURL = wallpaperURL;
    }

    public ActiveChatsWallpaperRealm() {
    }

    public String getWallpaperURL() {
        return wallpaperURL;
    }

    public void setWallpaperURL(String wallpaperURL) {
        this.wallpaperURL = wallpaperURL;
    }

    public String getChatref() {
        return chatref;
    }

    public void setChatref(String chatref) {
        this.chatref = chatref;
    }

}
