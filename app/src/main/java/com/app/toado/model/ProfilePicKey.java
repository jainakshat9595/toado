package com.app.toado.model;

import com.google.firebase.database.DataSnapshot;


public class ProfilePicKey {
    private String userkey,userprofilepic ;

    public ProfilePicKey(String userkey, String userprofilepic ) {
        this.userkey = userkey;
        this.userprofilepic = userprofilepic;
    }



    public String getUserkey() {
        return userkey;
    }

    public void setUserkey(String userkey) {
        this.userkey = userkey;
    }


    public ProfilePicKey() {
    }


    public String getUserprofilepic() {
        return userprofilepic;
    }

    public void setUserprofilepic(String userprofilepic) {
        this.userprofilepic = userprofilepic;
    }

    public static ProfilePicKey parse(DataSnapshot dataSnapshot) throws NullPointerException {
        ProfilePicKey usr = new ProfilePicKey();
        System.out.println(" datsnapshot userprofilepic "+dataSnapshot);
        usr.setUserkey(dataSnapshot.child("userkey").getValue().toString());
        usr.setUserprofilepic(dataSnapshot.child("userprofilepic").getValue().toString());
        return usr;
    }
}
