package com.app.toado.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ghanendra on 29/08/2017.
 */

public class UploadTable extends RealmObject {

    @PrimaryKey
    String msgid;
    String filetype;
    String filepath;
    String mykey;
    String otheruserkey;
    String otherusername;
    String uploadstatus;
    String msg;

    public UploadTable(String msg,String msgid, String filetype, String filepath, String mykey, String otheruserkey, String otherusername, String uploadstatus) {
        this.msgid = msgid;
        this.msg=msg;
        this.filetype = filetype;
        this.filepath = filepath;
        this.mykey = mykey;
        this.otheruserkey = otheruserkey;
        this.otherusername = otherusername;
        this.uploadstatus = uploadstatus;
    }

    public UploadTable() {
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getMykey() {
        return mykey;
    }

    public void setMykey(String mykey) {
        this.mykey = mykey;
    }

    public String getOtheruserkey() {
        return otheruserkey;
    }

    public void setOtheruserkey(String otheruserkey) {
        this.otheruserkey = otheruserkey;
    }

    public String getOtherusername() {
        return otherusername;
    }

    public void setOtherusername(String otherusername) {
        this.otherusername = otherusername;
    }


    public String getUploadstatus() {
        return uploadstatus;
    }

    public void setUploadstatus(String uploadstatus) {
        this.uploadstatus = uploadstatus;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
