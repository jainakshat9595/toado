package com.app.toado.model;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ghanendra on 20/08/2017.
 */

public class ChatMessageForward implements Serializable {
     private String msgstring;
    private String msgtype;
    private String msglocalurl;
    private String msgweburl;
    private String mediathumbnail;

    public ChatMessageForward( String msgstring, String msgtype, String msglocalurl, String msgweburl, String mediathumbnail) {
         this.msgstring = msgstring;
        this.msgtype = msgtype;
        this.msglocalurl = msglocalurl;
        this.msgweburl = msgweburl;
        this.mediathumbnail = mediathumbnail;
    }

    public String getMsgstring() {
        return msgstring;
    }

    public void setMsgstring(String msgstring) {
        this.msgstring = msgstring;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getMsglocalurl() {
        return msglocalurl;
    }

    public void setMsglocalurl(String msglocalurl) {
        this.msglocalurl = msglocalurl;
    }

    public String getMsgweburl() {
        return msgweburl;
    }

    public void setMsgweburl(String msgweburl) {
        this.msgweburl = msgweburl;
    }

    public String getMediathumbnail() {
        return mediathumbnail;
    }

    public void setMediathumbnail(String mediathumbnail) {
        this.mediathumbnail = mediathumbnail;
    }
}
