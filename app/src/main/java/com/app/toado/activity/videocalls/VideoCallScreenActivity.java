package com.app.toado.activity.videocalls;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.activity.calls.AudioPlayer;
import com.app.toado.activity.BaseActivity;
import com.app.toado.activity.main.MainAct;
import com.app.toado.helper.CallHelper;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.model.CallDetails;
import com.app.toado.model.User;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.CallSession;
import com.app.toado.settings.UserSession;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_CALLS;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;

public class VideoCallScreenActivity extends BaseActivity {

    static final String TAG = VideoCallScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;

    private String mk, mTimestamp;
    String mCaller, mReceiver;
    String otherusername, myname;
    private String mProfpic;
    CallSession cs;
    private long mCallStart = 0;

    Button muteCall, speakerMode;

    Notification notification;
    NotificationManager notificationManager;
    private String otherusrkey;


    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            VideoCallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        setContentView(R.layout.activity_videocallscreen);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        muteCall = (Button) findViewById(R.id.muteButton);
        speakerMode = (Button) findViewById(R.id.speakerMode);
        Button endCallButton = (Button) findViewById(R.id.hangupButton);

        UserSession us = new UserSession(this);
        mk = us.getUserKey();
        myname = us.getUsername();

        cs = new CallSession(this);
        System.out.println("cs videocallscreenactivty" + cs.getCallactive());

        if (cs.getCallType().matches("audio")) {
            cs.deleteSession();
        }

        if (!cs.getCallactive()) {
            mCaller = getIntent().getStringExtra("calleruid");
            mReceiver = getIntent().getStringExtra("receiveruid");
            otherusername = getIntent().getStringExtra("otherusername");
            mTimestamp = getIntent().getStringExtra("timestamp");
            mCallId = getIntent().getStringExtra(SinchCallService.CALL_ID);

            System.out.println(mCallId + "mcaller videocallscreenact" + mCallStart + mReceiver + otherusername);
            cs.setCallData("video", mCallId, mCaller, mReceiver, otherusername, "asdf", mTimestamp);
            System.out.println(cs.getCallerUid() + "set call session videocallscreenactivity" + cs.getCallactive());

        } else {
            mCallStart = cs.getCallStarttime();
            mCaller = cs.getCallerUid();
            mReceiver = cs.getReceiverUid();
            otherusername = cs.getotherUserName();
            mCallId = cs.getCallId();
            if (!cs.getTimestamp().matches(""))
                mTimestamp = cs.getTimestamp();
        }


        mCaller = getIntent().getStringExtra("calleruid");
        mReceiver = getIntent().getStringExtra("receiveruid");
        otherusername = getIntent().getStringExtra("otherusername");
        mTimestamp = getIntent().getStringExtra("timestamp");
        System.out.println(mCaller + "on create call screen activity ongoing call" + mReceiver + otherusername);


        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            getSinchServiceInterface().showVideoNotification(otherusername);
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
        updateUI();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            doStuff(call);
            mCallState.setText(call.getState().toString());
            if (call.getDetails().isVideoOffered()) {
                addLocalView();
                if (call.getState() == CallState.ESTABLISHED) {
                    addRemoteView();
                }
            }
        }
    }

    private void doStuff(Call call) {
        otherusrkey = call.getRemoteUserId();
        Long val = Long.parseLong(String.valueOf(call.getDetails().getDuration()));
        if(otherusername!=null)
        notificationManager = getSinchServiceInterface().showVideoNotification(otherusername);
        DBREF_USER_PROFILES.child(call.getRemoteUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println("datasnapshot callscreenactivity otheruser" + dataSnapshot);
                    User u = User.parse(dataSnapshot);
                    mCallerName.setText(u.getName());
                    mProfpic = u.getProfpicurl();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        removeVideoViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        cs.deleteSession();
        mAudioPlayer.stopProgressTone();
        if (notificationManager != null) {
            System.out.println("cancelling notification in endCAll videocallscreenactivity");
            notificationManager.cancel(112);
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        System.out.println("mcallstart time vidcallscreenact " + mCallStart);
        if (mCallStart > 0) {
            mCallDuration.setText(CallHelper.formatTimespan(System.currentTimeMillis() - mCallStart));
        } else {
            mCallDuration.setText("Connecting");
        }
    }

    private void addLocalView() {
        System.out.println("mlocalview addlocalview videocallscreenact" + mLocalVideoViewAdded);
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
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

    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
            view.addView(vc.getRemoteView());
            mRemoteVideoViewAdded = true;
        }
    }


    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
            view.removeView(vc.getRemoteView());

            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.removeView(vc.getLocalView());
            mLocalVideoViewAdded = false;
            mRemoteVideoViewAdded = false;
        }
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Toast.makeText(VideoCallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();

            if (mk != null && mCaller != null && mk.matches(mCaller)) {
                Long gt = GetTimeStamp.Id();

                String cau;

                if (call.getDetails().getDuration() > 0)
                    cau = "completed";
                else
                    cau = cause.toString();

                CallDetails cd1 = new CallDetails(String.valueOf(call.getDetails().getDuration()), mCaller, mReceiver, cau, String.valueOf(gt), mTimestamp, mProfpic, mCallerName.getText().toString(), "video");
                CallDetails cd2 = new CallDetails(String.valueOf(call.getDetails().getDuration()), mCaller, mReceiver, cau, String.valueOf(gt), mTimestamp, mProfpic, myname, "video");
                System.out.println(mCaller + "end msg videocallscreenactivity" + mReceiver + " " + String.valueOf(gt));
                System.out.println("end msg callscreenactivity" + mReceiver + " " + DBREF.child("VoiceCalls").child(mCaller).child(String.valueOf(gt)));

                //setting in mCaller mykey node at voicecalls node firebase
                DBREF_CALLS.child(mCaller).child(String.valueOf(gt)).setValue(cd1);
                //setting in mReceiver otheruserkey node at voicecalls node firebase
                DBREF_CALLS.child(mReceiver).child(String.valueOf(gt)).setValue(cd2);
            }

            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            if (mCallStart == 0) {
                mCallStart = System.currentTimeMillis();
                cs.setCallStartTime(mCallStart);
            }
            if (cs.getTimestamp().matches("")) {
                mTimestamp = GetTimeStamp.timeStamp();
                cs.setTimestamp(mTimestamp);
            } else {
                mTimestamp = cs.getTimestamp();
            }

            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            Log.d(TAG, "Video track added");
            addRemoteView();
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }

    public void openMainAct(View view) {
        startMainAct();
        finish();
//        moveTaskToBack(true);
    }

    public void openChatAct(View view) {
        if (otherusrkey != null) {
//            ChatHelper.checkChatref(VideoCallScreenActivity.this, mk, otherusrkey);
//            moveTaskToBack(true);
//            finish();
        }
    }

    private void startMainAct() {
        Intent in = new Intent(VideoCallScreenActivity.this, MainAct.class);
        in.putExtra("activecall", "activecall");
        startActivity(in);
    }

    public void muteCall(View view) {
        if (getSinchServiceInterface() != null) {
            String aud = muteCall.getText().toString().toLowerCase().trim();
            System.out.println("aud from mutecall " + aud);
            if (aud.matches("mute")) {
                getSinchServiceInterface().getAudioController().mute();
                muteCall.setText("unmute");
            } else {
                getSinchServiceInterface().getAudioController().unmute();
                muteCall.setText("mute");
            }
        }
    }

    public void speakerMode(View view) {
        if (getSinchServiceInterface() != null) {
            String aud = speakerMode.getText().toString().toLowerCase().trim();
            System.out.println("aud from speaker mode " + aud);
            if (aud.matches("enable speaker")) {
                getSinchServiceInterface().getAudioController().enableSpeaker();
                speakerMode.setText("disable speaker");
            } else {
                getSinchServiceInterface().getAudioController().unmute();
                speakerMode.setText("enable speaker");
            }
        }
    }

}
