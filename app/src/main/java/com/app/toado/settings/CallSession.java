package com.app.toado.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.toado.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CallSession {
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    Context _context;
    int mode = Context.MODE_PRIVATE;
    String prefname = "CALLSESSION";
    private String is_active = "callactive";

    public CallSession(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(prefname, mode);
        editor = pref.edit();
    }


    public boolean getCallactive() {
        return pref.getBoolean(is_active, false);
    }

    public String getCallId() {
        return pref.getString("callid", "nil");
    }

    public String getCallType() {
        return pref.getString("calltype", "nil");
    }

    public String getotherUserName() {
        return pref.getString("otherusrname", "");
    }

    public String getCallerUid() {
        return pref.getString("calleruid", "");
    }

    public String getTimestamp() {
        return pref.getString("timestamp", "");
    }
    public String getReceiverUid() {
        return pref.getString("receiveruid", "");
    }

    public String getImgurl() {
        return pref.getString("imgurl", "");
    }

    public Long getCallStarttime() {
        return pref.getLong("callstart", 0);
    }

    public void deleteSession() {
        System.out.println("deleting call session");
        editor.clear();
        editor.commit();
    }

    public void setTimestamp(String timestamp) {
         editor.putString("timestamp", timestamp);
        editor.commit();
    }

    public void setCallStartTime(Long time){
        editor.putLong("callstart",time);
        editor.commit();
    }

    public void setCallData(String calltype,String mCallId, String mCaller, String mReceiver, String otherusername, String otherusrimg, String mTimestamp) {
        editor.clear();
        editor.putBoolean(is_active, true);
        editor.putString("callid", mCallId).toString();
        editor.putString("calltype", calltype).toString();
        editor.putString("otherusrname", otherusername).toString();
        editor.putString("calleruid", mCaller).toString();
        editor.putString("receiveruid", mReceiver).toString();
        editor.putString("timestamp", mTimestamp).toString();
        editor.putString("imgurl", otherusrimg).toString();
        editor.commit();
    }
}
