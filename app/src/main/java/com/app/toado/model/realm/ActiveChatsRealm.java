package com.app.toado.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ghanendra on 28/07/2017.
 */

public class ActiveChatsRealm extends RealmObject {
    private String profpic, name, lastmsgbody, lastmsgtype, lastmsgtime,  otherkey, msgid;
    private Boolean pinned, archived;

    @PrimaryKey
    String chatref;

    public ActiveChatsRealm(String profpic, String msgid, String name, String lastmsgbody, String lastmsgtype, String lastmsgtime,  Boolean archived, String otherkey, String chatref,Boolean pinned) {
        this.profpic = profpic;
        this.name = name;
        this.lastmsgbody = lastmsgbody;
        this.lastmsgtype = lastmsgtype;
        this.lastmsgtime = lastmsgtime;
        this.archived = archived;
        this.otherkey = otherkey;
        this.chatref = chatref;
        this.msgid = msgid;
        this.pinned=pinned;
    }

    public ActiveChatsRealm() {
    }

    public String getChatref() {
        return chatref;
    }

    public void setChatref(String chatref) {
        this.chatref = chatref;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getOtherkey() {
        return otherkey;
    }

    public void setOtherkey(String otherkey) {
        this.otherkey = otherkey;
    }

    public String getProfpic() {
        return profpic;
    }

    public void setProfpic(String profpic) {
        this.profpic = profpic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastmsgbody() {
        return lastmsgbody;
    }

    public void setLastmsgbody(String lastmsgbody) {
        this.lastmsgbody = lastmsgbody;
    }

    public String getLastmsgtype() {
        return lastmsgtype;
    }

    public void setLastmsgtype(String lastmsgtype) {
        this.lastmsgtype = lastmsgtype;
    }

    public String getLastmsgtime() {
        return lastmsgtime;
    }

    public void setLastmsgtime(String lastmsgtime) {
        this.lastmsgtime = lastmsgtime;
    }
}
