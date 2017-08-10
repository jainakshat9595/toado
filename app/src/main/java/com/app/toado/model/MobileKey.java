package com.app.toado.model;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by RajK on 13-06-2017.
 */

public class MobileKey {
    private String userkey,usermob,usermobcc;

    public MobileKey(String userkey, String usermob, String usermobcc) {
        this.userkey = userkey;
        this.usermob = usermob;
        this.usermobcc = usermobcc;
    }

    public String getUsermobcc() {
        return usermobcc;
    }

    public void setUsermobcc(String usermobcc) {
        this.usermobcc = usermobcc;
    }

    public String getUserkey() {
        return userkey;
    }

    public void setUserkey(String userkey) {
        this.userkey = userkey;
    }

    public String getUsermob() {
        return usermob;
    }

    public void setUsermob(String usermob) {
        this.usermob = usermob;
    }

    public MobileKey() {
    }

    public static MobileKey parse(DataSnapshot dataSnapshot) throws NullPointerException {
        MobileKey usr = new MobileKey();
        System.out.println(" datsnapshot mobilekeyparse "+dataSnapshot);
        usr.setUserkey(dataSnapshot.child("userkey").getValue().toString());
        usr.setUsermob(dataSnapshot.child("usermob").getValue().toString());
        usr.setUsermob(dataSnapshot.child("usermobcc").getValue().toString());

        return usr;
    }
}
