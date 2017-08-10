package com.app.toado.model.realm;

import com.app.toado.model.ChatMessage;
import com.google.firebase.database.DataSnapshot;

import java.util.Random;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatMessageRealm extends RealmObject{

    @PrimaryKey
    private String msgid;// msg sending time in millis
    private String otherjid;
    private String msgstring;
    private String senderjid;
    private String sendertime;//hh:mm aa   where aa is am or pm
    private String senderdate;//dd-MMM-yyyy
    private String msgtype;
    private String msgstatus;
    private String msglocalurl;
    private String msgweburl;
    private String chatref;
    private String mediathumbnail;

    public ChatMessageRealm() {
    }

    //for text msgs
    public ChatMessageRealm(String chatref,String otherjid,String msgstring, String senderjid, String sendertime,String senderdate, String msgtype, String msgid, String msgstatus) {
        this.chatref=chatref;
        this.otherjid=otherjid;
        this.msgstring = msgstring;
        this.senderjid = senderjid;
        this.sendertime = sendertime;
        this.senderdate = senderdate;
        this.msgtype = msgtype;
        this.msgid = msgid;
        this.msgstatus = msgstatus;
    }

    //for media msgs
    public ChatMessageRealm(String chatref,String otherjid,String msgstring, String senderjid, String sendertime,String senderdate, String msgtype, String msgid, String msgstatus,  String msgweburl,String msglocalurl,String mediathumbnail) {
        this.chatref=chatref;
        this.otherjid=otherjid;
        this.msgstring = msgstring;
        this.senderjid = senderjid;
        this.sendertime = sendertime;
        this.senderdate = senderdate;
        this.msgtype = msgtype;
        this.msgid = msgid;
        this.msgstatus = msgstatus;
        this.msgweburl = msgweburl;
        this.msglocalurl=msglocalurl;
        this.mediathumbnail=mediathumbnail;
    }


    public String getMediathumbnail() {
        return mediathumbnail;
    }

    public void setMediathumbnail(String mediathumbnail) {
        this.mediathumbnail = mediathumbnail;
    }

    public String getChatref() {
        return chatref;
    }

    public void setChatref(String chatref) {
        this.chatref = chatref;
    }

    public String getOtherjid() {
        return otherjid;
    }

    public void setOtherjid(String otherjid) {
        this.otherjid = otherjid;
    }

    public String getMsgstring() {
        return msgstring;
    }

    public void setMsgstring(String msgstring) {
        this.msgstring = msgstring;
    }

    public String getSenderjid() {
        return senderjid;
    }

    public void setSenderjid(String senderjid) {
        this.senderjid = senderjid;
    }

    public String getSendertime() {
        return sendertime;
    }

    public void setSendertime(String sendertime) {
        this.sendertime = sendertime;
    }

    public String getSenderdate() {
        return senderdate;
    }

    public void setSenderdate(String senderdate) {
        this.senderdate = senderdate;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getMsgstatus() {
        return msgstatus;
    }

    public void setMsgstatus(String msgstatus) {
        this.msgstatus = msgstatus;
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

}