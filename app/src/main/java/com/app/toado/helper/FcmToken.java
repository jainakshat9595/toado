package com.app.toado.helper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.app.toado.helper.ToadoConfig.DBREF_FCM;

/**
 * Created by ghanendra on 05/07/2017.
 */

public class FcmToken {
    private static String receiverToken;

    public static String getRecivertoken(String otheruserkey) {
        DBREF_FCM.child(otheruserkey).child("token").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    receiverToken = dataSnapshot.getValue().toString();
                    System.out.println(dataSnapshot.getValue() + "recd token in chat act " + receiverToken);
                } else {
                    receiverToken = "nil";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return receiverToken;
    }
}
