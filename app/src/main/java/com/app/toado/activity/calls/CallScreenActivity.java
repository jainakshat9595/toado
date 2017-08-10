package com.app.toado.activity.calls;

import com.app.toado.R;
import com.app.toado.activity.BaseActivity;
import com.app.toado.activity.main.MainAct;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.CallHelper;
import com.app.toado.model.CallDetails;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.CallSession;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_CALLS;

public class CallScreenActivity extends BaseActivity {

    static final String TAG = CallScreenActivity.class.getSimpleName();

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    String mCaller, mReceiver;
    String otherusername, myname;
    private long mCallStart = 0;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    private ImageView mCallImg;
    private String mk, mTimestamp;
    private String mProfpic;
    Button endCallButton, muteCall, speakerMode;

    Notification notification;
    NotificationManager notificationManager;

    String otherusrkey;
    String otherusrimg;
    CallSession cs;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_callscreen);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        mCallImg = (ImageView) findViewById(R.id.imgotherusr);
        endCallButton = (Button) findViewById(R.id.hangupButton);
        muteCall = (Button) findViewById(R.id.muteButton);
        speakerMode = (Button) findViewById(R.id.speakerMode);
        endCallButton = (Button) findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

        UserSession us = new UserSession(this);
        mk = us.getUserKey();
        myname = us.getUsername();

        cs = new CallSession(this);
        System.out.println("cs callscreenactivty" + cs.getCallactive());

        if (cs.getCallType().matches("video")) {
            cs.deleteSession();
        }

        if (!cs.getCallactive()) {
            mCaller = getIntent().getStringExtra("calleruid");
            mReceiver = getIntent().getStringExtra("receiveruid");
            otherusrimg = getIntent().getStringExtra("otheruserimg");
            otherusername = getIntent().getStringExtra("otherusername");
            mTimestamp = getIntent().getStringExtra("timestamp");
            mCallId = getIntent().getStringExtra(SinchCallService.CALL_ID);
            System.out.println(mCallId + "mcaller callscreenactive" + mCallStart + mReceiver + otherusername);
            cs.setCallData("audio", mCallId, mCaller, mReceiver, otherusername, otherusrimg, mTimestamp);
            System.out.println(cs.getCallerUid() + "set call session callscreenactivity" + cs.getCallactive());
        } else {
            mCallStart = cs.getCallStarttime();
            mCaller = cs.getCallerUid();
            mReceiver = cs.getReceiverUid();
            otherusrimg = cs.getImgurl();
            otherusername = cs.getotherUserName();
            mCallId = cs.getCallId();
            if (!cs.getTimestamp().matches(""))
                mTimestamp = cs.getTimestamp();
        }
        System.out.println(mCallStart + "on create call screen activity ongoing call" + mReceiver + otherusername);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onstart called callscreenact");
    }

    @Override
    public void onServiceConnected() {

        try {
            notificationManager = getSinchServiceInterface().showNotification(otherusername);
            doStuff();
        } catch (NullPointerException e) {
            //getSinchServiceInterface() in doStuff below throw null pointer error.
        }
    }

    private void doStuff() {
        System.out.println("do stuff called callscreenactivity");
        final Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            mCallState.setText(call.getState().toString());
            otherusrkey = call.getRemoteUserId();
            Glide.with(CallScreenActivity.this).load(otherusrimg).into(mCallImg);
            mCallerName.setText(otherusername);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("callscreenactivity has been destroyed android ");
    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);

        if (getIntent() != null && getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case "ongoingcall":
                    mCaller = getIntent().getStringExtra("calleruid");
                    mReceiver = getIntent().getStringExtra("receiveruid");
                    otherusername = getIntent().getStringExtra("otherusername");
                    mTimestamp = getIntent().getStringExtra("timestamp");
                    System.out.println("on resume call screen activity ongoing call" + mCaller + mReceiver + otherusername);
                    break;
                case "hangupcall":
                    mCaller = getIntent().getStringExtra("calleruid");
                    mReceiver = getIntent().getStringExtra("receiveruid");
                    otherusername = getIntent().getStringExtra("otherusername");
                    mTimestamp = getIntent().getStringExtra("timestamp");
                    endCallButton.performClick();
                    System.out.println("on resume call screen activity hangup call");
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        System.out.println("moving callscreenacitity to back onbackrpressed");
        finish();
    }

    private void endCall() {

        cs.deleteSession();

        if (notificationManager != null) {
            System.out.println("cancelling notification in endCAll callscreenactivity");
            notificationManager.cancel(111);
        }

        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private void updateCallDuration() {
        if (mCallStart > 0) {
            mCallDuration.setText(CallHelper.formatTimespan(System.currentTimeMillis() - mCallStart));
        } else {
            mCallDuration.setText("Connecting");
        }
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString() + mk + mCaller);
            if (mk != null && mCaller != null && mk.matches(mCaller)) {
                mAudioPlayer.stopProgressTone();
                setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                String endMsg = "Call ended: " + call.getDetails().toString();
                Long gt = GetTimeStamp.Id();
                Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
                System.out.println(endMsg + "mtimestamp" + mTimestamp);

                String cau;
                if (call.getDetails().getDuration() > 0)
                    cau = "completed";
                else
                    cau = cause.toString();

                CallDetails cd1 = new CallDetails(String.valueOf(call.getDetails().getDuration()), mCaller, mReceiver, cau, String.valueOf(gt), mTimestamp, mProfpic, mCallerName.getText().toString(), "voice");
                CallDetails cd2 = new CallDetails(String.valueOf(call.getDetails().getDuration()), mCaller, mReceiver, cau, String.valueOf(gt), mTimestamp, mProfpic, myname, "voice");
                System.out.println(mCaller + "end msg callscreenactivity" + mReceiver + " " + String.valueOf(gt));
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

    }

    public void openMainAct(View view) {
        startMainAct();
        finish();
//        moveTaskToBack(true);
    }

    public void openChatAct(View view) {
        if (otherusrkey != null) {
//            ChatHelper.checkChatref(CallScreenActivity.this, mk, otherusrkey);
//            moveTaskToBack(true);
//            finish();
        }
    }

    private void startMainAct() {
        Intent in = new Intent(CallScreenActivity.this, MainAct.class);
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
