package com.app.toado.activity.calls;

import com.app.toado.R;
import com.app.toado.activity.BaseActivity;
import com.app.toado.helper.MarshmallowPermissions;
import com.app.toado.model.User;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;

public class IncomingCallScreenActivity extends BaseActivity {

    static final String TAG = IncomingCallScreenActivity.class.getSimpleName();
    private String mCallId;
    private String mCallLocation;
    private AudioPlayer mAudioPlayer;
    MarshmallowPermissions marshmallowPermissions;
    ImageView mCallImg;
    TextView remoteUser;
    UserSession us;
    String mykey;
    String otherusrimg;
    String otherusrname;
//    TextView remoteUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_call_incoming);

        Button answer = (Button) findViewById(R.id.answerButton);
        answer.setOnClickListener(mClickListener);
        Button decline = (Button) findViewById(R.id.declineButton);
        decline.setOnClickListener(mClickListener);
        remoteUser = (TextView) findViewById(R.id.remoteUser);

        marshmallowPermissions = new MarshmallowPermissions(this);

        us = new UserSession(this);
        mykey = us.getUserKey();

        mCallImg = (ImageView) findViewById(R.id.imgotherusr);
        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchCallService.CALL_ID);
        mCallLocation = getIntent().getStringExtra(SinchCallService.LOCATION);
    }

    @Override
    protected void onServiceConnected() {
        final Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());

            System.out.println(call.getRemoteUserId() + "db user other");
            System.out.println("db user other2" + call.getHeaders().get("username"));
            System.out.println("db user other 2 " + call.getRemoteUserId());

            DBREF_USER_PROFILES.child(call.getRemoteUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User u = User.parse(dataSnapshot);
                        otherusrname = u.getName();
                        otherusrimg = u.getProfpicurl();
                        remoteUser.setText(otherusrname);
                        Glide.with(IncomingCallScreenActivity.this).load(u.getProfpicurl()).into(mCallImg);

                    } else {
                        remoteUser.setText(call.getHeaders().get("username"));
                        Glide.with(IncomingCallScreenActivity.this).load(call.getHeaders().get("profilepic")).into(mCallImg);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        if (marshmallowPermissions.checkPermissionForCalls()) {
            mAudioPlayer.stopRingtone();
            Call call = getSinchServiceInterface().getCall(mCallId);
            if (call != null) {
                call.answer();
                Intent intent = new Intent(this, CallScreenActivity.class);
                intent.putExtra(SinchCallService.CALL_ID, mCallId);
                intent.putExtra("calleruid", call.getRemoteUserId());
                if (mykey != null)
                    intent.putExtra("receiveruid", mykey);
                else
                    intent.putExtra("receiveruid", us.getUserKey());

                intent.putExtra("otherusername", otherusrname);
                intent.putExtra("otheruserimg", otherusrimg);
                startActivity(intent);
            } else {
                finish();
            }
        } else {
            marshmallowPermissions.requestPermissionForCalls();
        }
    }

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.answerButton:
                    answerClicked();
                    break;
                case R.id.declineButton:
                    declineClicked();
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }
}
