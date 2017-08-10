package com.app.toado.activity.videocalls;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.activity.calls.AudioPlayer;
import com.app.toado.activity.BaseActivity;
import com.app.toado.model.User;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.UserSession;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;

public class IncomingVideoCallScreenActivity extends BaseActivity {

    static final String TAG = IncomingVideoCallScreenActivity.class.getSimpleName();
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    private boolean mLocalVideoViewAdded = false;
    UserSession us;
    String mykey;
    String otherusrname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        setContentView(R.layout.activity_incomingvideocall);

        Button answer = (Button) findViewById(R.id.answerButton);
        answer.setOnClickListener(mClickListener);
        Button decline = (Button) findViewById(R.id.declineButton);
        decline.setOnClickListener(mClickListener);

        us = new UserSession(this);
        mykey = us.getUserKey();

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchCallService.CALL_ID);

    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            LinearLayout localView = (LinearLayout) findViewById(R.id.localVideo);
            localView.removeView(vc.getLocalView());
            localView.addView(vc.getLocalView());
            localView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vc.toggleCaptureDevicePosition();
                }
            });
            mLocalVideoViewAdded = true;
        }
    }

    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            final TextView remoteUser = (TextView) findViewById(R.id.remoteUser);

            DBREF_USER_PROFILES.child(call.getRemoteUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        System.out.println("datasnapshot callscreenactivity otheruser" + dataSnapshot);
                        User u = User.parse(dataSnapshot);
                        otherusrname = u.getName();
                        remoteUser.setText(otherusrname);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            addLocalView();
        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {

            LinearLayout localView = (LinearLayout) findViewById(R.id.localVideo);
            localView.removeView(vc.getLocalView());
            mLocalVideoViewAdded = false;
         }
    }

    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        removeVideoViews();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.answer();
            Intent intent = new Intent(this, VideoCallScreenActivity.class);
            intent.putExtra(SinchCallService.CALL_ID, mCallId);
            intent.putExtra("calleruid", call.getRemoteUserId());
            if (mykey != null)
                intent.putExtra("receiveruid", mykey);
            else
                intent.putExtra("receiveruid", us.getUserKey());

            intent.putExtra("otherusername", otherusrname);

            startActivity(intent);
        } else {
            finish();
        }
    }

    private void declineClicked() {
        removeVideoViews();
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements VideoCallListener {

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

        @Override
        public void onVideoTrackAdded(Call call) {
            // Display some kind of icon showing it's a video call
        }

        @Override
        public void onVideoTrackPaused(Call call) {
        }

        @Override
        public void onVideoTrackResumed(Call call) {

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
}
