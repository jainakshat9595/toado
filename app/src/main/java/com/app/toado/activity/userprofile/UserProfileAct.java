package com.app.toado.activity.userprofile;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.activity.BaseActivity;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.MarshmallowPermissions;
import com.app.toado.helper.CallHelper;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.UserSession;
import com.app.toado.model.User;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONException;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;

/**
 * Created by ghanendra on 15/06/2017.
 */

public class UserProfileAct extends BaseActivity {
    private TextView tvname, tvage, tvdistance, tvinterests;
    private ImageView imgvUsrprof;
    Button btnchat;
    private String usrkey = "nil";
    UserSession usrsess;
    private String dbTablekey;
    private Button btncall;
    String mykey = "nokey", otherusername, imgurl;
    String profiletype;

    private MarshmallowPermissions marshmallowPermissions;
    private Button btnemail;
    private Button btnvid;
    private String dist;

    SinchCallService callserv;
    boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        marshmallowPermissions = new MarshmallowPermissions(this);

        if (!marshmallowPermissions.checkPermissionForCalls())
            marshmallowPermissions.requestPermissionForCalls();

        btnchat = (Button) findViewById(R.id.imgchat);
        btncall = (Button) findViewById(R.id.imgcall);
        btnvid = (Button) findViewById(R.id.imgvideo);
        btnemail = (Button) findViewById(R.id.imgemail);
        tvname = (TextView) findViewById(R.id.tvname);
        tvage = (TextView) findViewById(R.id.tvage);
        tvdistance = (TextView) findViewById(R.id.tvdist);
        tvinterests = (TextView) findViewById(R.id.tvintrsts);
        imgvUsrprof = (ImageView) findViewById(R.id.imgprofile);
        usrsess = new UserSession(this);
        mykey = usrsess.getUserKey();

        if (getIntent() != null)
            profiletype = getIntent().getStringExtra("profiletype");

        System.out.println("profiletype userprofileact" + profiletype);

        if (profiletype.matches("otherprofile")) {
            usrkey = getIntent().getStringExtra("keyval");
            dist = getIntent().getStringExtra("distance");
            btncall.setVisibility(View.VISIBLE);
            btnchat.setVisibility(View.VISIBLE);
            btnvid.setVisibility(View.VISIBLE);
            btnemail.setVisibility(View.VISIBLE);
        } else {
            UserSession us = new UserSession(this);
            usrkey = us.getUserKey();

        }

        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!usrkey.matches("nil"))
                    callButtonClicked();
            }
        });

        btnvid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!usrkey.matches("nil"))
                    videoCallButtonClicked();
            }
        });

        btnchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatHelper.goToChatActivity(UserProfileAct.this,usrkey,otherusername,imgurl);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("user key from userprof act" + usrkey);
        if (!usrkey.matches("nil"))
            getOtherUserFirebaseData(usrkey);
        else {
            System.out.println("no key matches error userprofile act");
        }

    }

    private void getOtherUserFirebaseData(final String k) {
        System.out.println(usrkey + "db ref getusr lcoal data" + dist);
        DBREF_USER_PROFILES.child(k).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println("user profiles datasnapshot" + dataSnapshot.toString());
                    User u = User.parse(dataSnapshot);
                    imgurl = u.getProfpicurl();
                    Glide.with(UserProfileAct.this).load(imgurl).into(imgvUsrprof);
                    System.out.println(u.getProfpicurl() + "user key from getuserlocaldata userprof act" + u.getName());
                    getlikes(k);
                    otherusername = u.getName();
                    tvname.setText(otherusername);
                    tvage.setText(u.getDob());
                    tvdistance.setText(dist + " miles away ");
                } else
                    System.out.println("no snapshot exists userprof act");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getlikes(final String key) {
        System.out.println("interests facebook");

        if (AccessToken.getCurrentAccessToken() != null) {
            new GraphRequest(

                    AccessToken.getCurrentAccessToken(),
                    "/" + AccessToken.getCurrentAccessToken().getUserId() + "/likes",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {

                                System.out.println("fb interests inside async task" + response.getJSONObject().getJSONArray("data").toString());

                                updateUsrLikes(response.getJSONObject().getJSONArray("data").toString(), key);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).executeAsync();
        }
    }

    private void updateUsrLikes(String likes, String key) {
        DBREF_USER_PROFILES.child(key).child("userLikes").setValue(likes);
        System.out.println(DBREF_USER_PROFILES.child(key).child("userLikes") + "likes recd in update usr likes" + likes);
        tvinterests.setText(likes);
    }


    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();
    }

    private void videoCallButtonClicked() {
        CallHelper.vidcallbtnClicked(getSinchServiceInterface(), marshmallowPermissions, mykey, usrkey, otherusername, imgurl, UserProfileAct.this);
    }

    private void callButtonClicked() {
        CallHelper.callbtnClicked(getSinchServiceInterface(), marshmallowPermissions, mykey, usrkey, otherusername, imgurl, UserProfileAct.this);
    }

    //call service BINDINGS

    @Override
    public void onServiceConnected() {

        try {
            callserv = getSinchServiceInterface().getService();
            getSinchServiceInterface().startClient(usrkey);
            mServiceBound = true;
            btncall.setBackgroundResource(R.color.light_green);
        } catch (NullPointerException e) {
            //getSinchServiceInterface() in doStuff below throw null pointer error.
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
        btncall.setBackgroundResource(R.color.bpDarker_red);
    }

    @Override
    protected void onStop() {
        super.onStop();

     }

    @Override
    protected void onStart() {
        super.onStart();

    }


}
