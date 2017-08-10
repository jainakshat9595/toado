package com.app.toado.services;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.app.toado.R;
import com.app.toado.activity.chat.ChatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by ghanendra on 20/06/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG1 = "MyFireMesgService";

    Bitmap largeIcon;
    String check = "lol";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG1, "data1: " + remoteMessage.getData().get("sendertimestamp"));
        Log.d(TAG1, "data2: " + remoteMessage.getData().get("msgid"));
        Log.d(TAG1, "data3: " + remoteMessage.getData().get("chatref"));

        String title = remoteMessage.getData().get("title");
        if (!title.matches("Incoming Sinch Call")) {
            String msg = remoteMessage.getData().get("body");
            String senderuid = remoteMessage.getData().get("senderuid");

            System.out.println(check + " remote message downloaded " + senderuid);
            String timestamp = remoteMessage.getData().get("sendertimestamp");
            String chatref = remoteMessage.getData().get("chatref");
            String msgid = remoteMessage.getData().get("msgid");
            sendNotification(title, msg, timestamp, chatref, msgid, senderuid);

            if (!check.matches(senderuid)) {
                check = senderuid;
//                if (msg != null && timestamp != null && chatref != null && msgid != null)
            }
        } else {
            System.out.println("call notification arrived trying to start service from myfriebasemesg service");
            startService(new Intent(getBaseContext(), SinchCallService.class));
        }
    }


    private void sendNotification(String title, String msg, String timestamp, String chatref, String msgid, String senderuid) throws NullPointerException {
        final DatabaseReference dbr = DBREF.child("Chats").child(chatref).child("ChatMessages").child(msgid).child("status");
        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("otheruserkey", senderuid);
        intent.putExtra("dbTableKey", chatref);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String m = msg;
        if (!m.matches("nil")) {
            System.out.println(dbr + " setting value to 2 for " + chatref);
            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getValue().toString().matches("1")||dataSnapshot.getValue().toString().matches("0")) {
                            dbr.setValue("2").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    System.out.println("setValue 2 done for"+dataSnapshot);
                                }
                            });
                        }
                        else{
                            System.out.println("no data snpashot myfirebasemsging serviec");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.iconchat)
                    .setContentTitle(title)
                    .setContentText(m)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }

    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }

        return isInBackground;
    }
}
