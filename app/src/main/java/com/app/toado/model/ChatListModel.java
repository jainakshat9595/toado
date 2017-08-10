package com.app.toado.model;

/**
 * Created by RajK on 20-06-2017.
 */

public class ChatListModel {
    private String name,profpic,userkey,dbTableKey;

    public ChatListModel() {
    }

    public String getDbTableKey() {
        return dbTableKey;
    }

    public void setDbTableKey(String dbTableKey) {
        this.dbTableKey = dbTableKey;
    }

    public ChatListModel(String name, String profpic, String userkey, String dbTableKey) {
        this.name = name;
        this.profpic = profpic;
        this.userkey = userkey;
        this.dbTableKey = dbTableKey;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfpic() {
        return profpic;
    }

    public void setProfpic(String profpic) {
        this.profpic = profpic;
    }

    public String getUserkey() {
        return userkey;
    }

    public void setUserkey(String userkey) {
        this.userkey = userkey;
    }

}
