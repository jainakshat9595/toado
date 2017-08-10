package com.app.toado.model;

/**
 * Created by RajK on 13-06-2017.
 */

public class Usersession {
    private String lastseen;
    private Boolean online;

    public Usersession() {
    }

    public Usersession(String lastseen, Boolean online) {
        this.lastseen = lastseen;
        this.online = online;
    }

    public String getLastseen() {
        return lastseen;
    }

    public void setLastseen(String lastseen) {
        this.lastseen = lastseen;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
