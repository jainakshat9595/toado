package com.app.toado.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.toado.R;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.register.PersonalDetailsAct;
import com.app.toado.model.MobileKey;
import com.app.toado.model.ProfilePicKey;
import com.app.toado.model.User;
import com.app.toado.model.Usersession;
import com.app.toado.settings.UserSession;
import com.app.toado.settings.UserSettingsSharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_MOBS;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PICS;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_SESSIONS;

/**
 * Created by ghanendra on 24/07/2017.
 */

public class RegisterProfileFirebase {
    static String TAG = "REGISTERPROFILEFIREBASE";
    private static MyXMPP2 myxinstance;

    public static void saveDataToFirebase(String name, String mPhone1, String gender, String dob, String imgurL, final String mykey1, final UserSession sharedpref, final UserSettingsSharedPref userSettingsSharedPref, final Activity act) {
        final String mykey = mykey1.toLowerCase();
        Log.d("TAG", "save data to firebase lowercase key" + mykey);
        final User u;

        String miso = String.valueOf(MobileNumProcess.processMobNum(mPhone1).getCountryCode());

        u = new User(name, mPhone1, gender, dob, imgurL, "toadochat");

        DatabaseReference dbnewMobs = DBREF_USER_MOBS.child(mPhone1);
        MobileKey mobileKey = new MobileKey(mykey, mPhone1, miso);
        dbnewMobs.setValue(mobileKey);

        DatabaseReference dbnewpic = DBREF_USER_PICS.child(mykey);
        ProfilePicKey profp = new ProfilePicKey(mykey, imgurL);
        dbnewpic.setValue(profp);

        DatabaseReference dbSession = DBREF_USER_SESSIONS.child(mykey);
        Usersession usersession = new Usersession("", true);
        dbSession.setValue(usersession);

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                DBREF_USER_PROFILES.child(mykey).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(act, "User registration complete", Toast.LENGTH_SHORT).show();
                        sharedpref.create_oldusersession(mykey);
                        registerUser(mykey, act);
                        userSettingsSharedPref.setDistancePref("miles", 10, 1.6F);
                        Intent in = new Intent(act, MainAct.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        myxinstance = MyXMPP2.getInstance(act, act.getString(R.string.server), mykey);

                        act.startActivity(in);
                        act.finish();
                    }
                });
                return null;
            }
        }.execute();

    }


    public static void registerUser(final String userkey, final Activity act) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                regMeth(userkey, act);
                return null;
            }
        }.execute();
    }

    private static void regMeth(String usr, final Activity act) {
        RequestQueue requestQueue = Volley.newRequestQueue(act);
        //http://localhost:9090/plugins/restapi/v1/users
        String URL = "http://" + act.getString(R.string.server) + ":9090/plugins/restapi/v1/users";
        Log.d(TAG, URL + "user register meth called " + usr);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", usr);
            jsonBody.put("password", usr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonBody.toString();
        System.out.println(usr + "string request" + requestBody);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("REGISTER PROFILE FIRE", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "volley error" + error.getStackTrace());
            }

        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", act.getString(R.string.serverkey));
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requestQueue.add(stringRequest);

    }
}

