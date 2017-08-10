package com.app.toado.model;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by ghanendra on 28/06/2017.
 */

public class CallDetails {
    String duration, calleruid, receiveruid, status, callid, timestamp, profpicurl, otherusrname, calltype;

    public CallDetails() {
    }

    public CallDetails(String duration, String calleruid, String receiveruid, String status, String callid, String timestamp, String profpicurl, String otherusrname, String calltype) {
        this.duration = duration;
        this.calleruid = calleruid;
        this.receiveruid = receiveruid;
        this.status = status;
        this.callid = callid;
        this.timestamp = timestamp;
        this.profpicurl = profpicurl;
        this.otherusrname = otherusrname;
        this.calltype = calltype;
    }

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public String getOtherusrname() {
        return otherusrname;
    }

    public void setOtherusrname(String otherusrname) {
        this.otherusrname = otherusrname;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCallid() {
        return callid;
    }

    public void setCallid(String callid) {
        this.callid = callid;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCalleruid() {
        return calleruid;
    }

    public void setCalleruid(String calleruid) {
        this.calleruid = calleruid;
    }

    public String getReceiveruid() {
        return receiveruid;
    }

    public void setReceiveruid(String receiveruid) {
        this.receiveruid = receiveruid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfpicurl() {
        return profpicurl;
    }

    public void setProfpicurl(String profpicurl) {
        this.profpicurl = profpicurl;
    }

    public static CallDetails parse(DataSnapshot dataSnapshot) throws NullPointerException {
        CallDetails usr = new CallDetails();
        usr.setCalleruid(dataSnapshot.child("calleruid").getValue().toString());
        usr.setReceiveruid(dataSnapshot.child("receiveruid").getValue().toString());
        usr.setDuration(dataSnapshot.child("duration").getValue().toString());
        usr.setStatus(dataSnapshot.child("status").getValue().toString());
        usr.setTimestamp(dataSnapshot.child("timestamp").getValue().toString());
        usr.setOtherusrname(dataSnapshot.child("otherusrname").getValue().toString());
        if (dataSnapshot.child("profpicurl").getValue() != null)
            usr.setProfpicurl(dataSnapshot.child("profpicurl").getValue().toString());
        usr.setCallid(dataSnapshot.child("callid").getValue().toString());
        usr.setCalltype(dataSnapshot.child("calltype").getValue().toString());
//        System.out.println("from user parse function"+usr.getName()+usr.getDob()+usr.getUserLikes());
        return usr;
    }

}
