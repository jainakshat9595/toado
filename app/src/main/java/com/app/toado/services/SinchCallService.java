package com.app.toado.services;

/**
 * Created by ghanendra on 27/06/2017.
 */

import com.app.toado.R;
import com.app.toado.activity.calls.CallScreenActivity;
import com.app.toado.activity.calls.IncomingCallScreenActivity;
import com.app.toado.activity.videocalls.IncomingVideoCallScreenActivity;
import com.app.toado.activity.videocalls.VideoCallScreenActivity;
import com.app.toado.settings.UserSession;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoController;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Map;

import static com.app.toado.helper.ToadoConfig.SINCH_APPLICATION_KEY;
import static com.app.toado.helper.ToadoConfig.SINCH_SECRET_KEY;

public class SinchCallService extends Service {

    private static final String APP_KEY = SINCH_APPLICATION_KEY;
    private static final String APP_SECRET = SINCH_SECRET_KEY;
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    public static final String LOCATION = "LOCATION";
    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchCallService.class.getSimpleName();

    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private SinchClient mSinchClient;
    private String mUserId;

    private StartFailedListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        UserSession us = new UserSession(this);
        System.out.println("From sinchcall oncreate" + us.getUserKey());
        if (!isStarted()) {
            System.out.println("sinch not started callservice oncreate " + us.getUserKey());
            start(us.getUserKey());
        }
    }

    @Override
    public void onDestroy() {
        if (mSinchClient != null && mSinchClient.isStarted()) {
            mSinchClient.terminate();
        }
        super.onDestroy();
    }


    public void start(String userName) {
        System.out.println("sinch call service start " + userName);
        if (mSinchClient == null) {
            mUserId = userName;
            mSinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(userName)
                    .applicationKey(APP_KEY)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT).build();

            mSinchClient.setSupportCalling(true);
            mSinchClient.startListeningOnActiveConnection();

            mSinchClient.addSinchClientListener(new MySinchClientListener());
            mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
            mSinchClient.start();
            System.out.println(" sinch client started");
        }
    }

    private void stop() {
        if (mSinchClient != null) {
            mSinchClient.terminate();
            mSinchClient = null;
        }
    }

    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mSinchServiceInterface;
    }

    public class SinchServiceInterface extends Binder {

        public SinchCallService getService() {
            return SinchCallService.this;
        }

        public Call callPhoneNumber(String phoneNumber) {
            return mSinchClient.getCallClient().callPhoneNumber(phoneNumber);
        }

        public Call callUser(String userId) {
            return mSinchClient.getCallClient().callUser(userId);
        }

        public Call callUser(String userId, Map<String, String> headers) {
            if (!isStarted()) {
                UserSession us = new UserSession(getApplicationContext());
                startClient(us.getUserKey());
            }
            return mSinchClient.getCallClient().callUser(userId, headers);
        }

        public String getUserName() {
            return mUserId;
        }

        public boolean isStarted() {
            return SinchCallService.this.isStarted();
        }

        public void startClient(String userName) {
            System.out.println("startClient called sinchcallservice" + userName);

            if (!isStarted()) {
                System.out.println("startClient not started callservice  " + userName);
                start(userName);
            }
        }

        public Call callUserVideo(String userId) {
            return mSinchClient.getCallClient().callUserVideo(userId);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId) {
            return mSinchClient.getCallClient().getCall(callId);
        }


        public VideoController getVideoController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getVideoController();
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }


        public NotificationManager showNotification(String otherusername) {
            Intent notificationIntent = new Intent(getApplicationContext(), CallScreenActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("In call with " + otherusername)
                    .setSmallIcon(R.drawable.iconphone)
                    .setContentIntent(pendingIntent).build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(111 /* ID of notification */, notification);

            return notificationManager;

        }

        public NotificationManager showVideoNotification(String otherusername) {
            Intent notificationIntent = new Intent(getApplicationContext(), VideoCallScreenActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("In call with " + otherusername)
                    .setSmallIcon(R.drawable.iconphone)
                    .setContentIntent(pendingIntent).build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(112 /* ID of notification */, notification);
            return notificationManager;
        }
    }

    public interface StartFailedListener {
        void onStartFailed(SinchError error);

        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if (mListener != null) {
                mListener.onStartFailed(error);
            }
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient client) {
            Log.d(TAG, "SinchClient started");
            if (mListener != null) {
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient client) {
            Log.d(TAG, "SinchClient stopped");
        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient client,
                                                      ClientRegistration clientRegistration) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            Log.e(TAG, "Incoming call"+call.getDetails().isVideoOffered());
            if (call.getDetails().isVideoOffered() == true) {
                Intent intent = new Intent(SinchCallService.this, IncomingVideoCallScreenActivity.class);
                intent.putExtra(CALL_ID, call.getCallId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SinchCallService.this.startActivity(intent);
            } else {
                Intent intent = new Intent(SinchCallService.this, IncomingCallScreenActivity.class);
                intent.putExtra(CALL_ID, call.getCallId());
                intent.putExtra(LOCATION, call.getHeaders().get("location"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SinchCallService.this.startActivity(intent);
            }
        }

    }
}
