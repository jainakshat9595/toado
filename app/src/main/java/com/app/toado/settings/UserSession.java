package com.app.toado.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.toado.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserSession {
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    Context _context;
    int mode = 0;
    String prefname = "SESSION";
    private String is_loggedin = "is_loggedin";
    private DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Userprofiles").getRef();


    public UserSession(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(prefname, mode);
        editor = pref.edit();
    }

    public void create_oldusersession(final String userkey) {
        DatabaseReference dbRef = dbUser.child(userkey).getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = User.parse(dataSnapshot);
                    editor.putString("name", user.getName());
                    editor.putString("phone", user.getPhone());
                    editor.putString("gender", user.getGender());
                    editor.putString("dob", user.getDob());
                    editor.putString("userkey", dataSnapshot.getKey());
                    editor.putString("profilepic", user.getProfpicurl());
//                editor.putString(is_loggedin,"true");
                    editor.putBoolean(is_loggedin, true);
                    editor.commit();
                    System.out.println(getUserKey() + " datasnapshot usersession java " + dataSnapshot.getValue());
                } else {
                    System.out.println("no datasnapshot usersession java");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isolduser() {
        return pref.getBoolean(is_loggedin, false);
    }

    public String getUserKey() {
        return pref.getString("userkey", "nil");
    }

    public String getUserPic() {
        return pref.getString("profilepic", "");
    }

    public String getUsername() {
        return pref.getString("name", "");
    }

    public boolean getAppFirstRun() {
        return pref.getBoolean("firstrun", true);
    }

    public void setAppFirstRun(Boolean b) {
        editor.putBoolean("firstrun", b);
        editor.commit();
    }

    public void deleteSession() {
        editor.clear();
        editor.commit();
    }
}
