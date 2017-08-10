package com.app.toado.helper;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.app.toado.activity.calls.CallScreenActivity;
import com.app.toado.activity.videocalls.VideoCallScreenActivity;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.UserSession;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.calling.Call;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_FCM;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_LOC;

/**
 * Created by ghanendra on 05/07/2017.
 */

public class CallHelper {

    public static void callbtnClicked(final SinchCallService.SinchServiceInterface sinchServiceInterface, MarshmallowPermissions marshmallowPermissions, final String mykey, final String usrkey, final String otherusername, final String imgurl, final Context contx) {
        System.out.println("call button clicked " + DBREF_USER_LOC.child(mykey));
        if (marshmallowPermissions.checkPermissionForCalls()) {
            final String userName = usrkey;
            final Long id = GetTimeStamp.Id();
            DBREF_FCM.child(usrkey).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers = CallHelper.getAddress(headers, contx);
                    System.out.println(dataSnapshot + "adding data to firebase userprofileact voicecallhelper" + headers.get("username"));
                    String rectok = dataSnapshot.getValue().toString();
                    System.out.println("rectok userprofileact" + rectok);
                    CallHelper.addCallToFirebase(mykey, id, rectok);
                    if (sinchServiceInterface != null)
                        CallHelper.doCall(sinchServiceInterface, contx, userName, mykey, usrkey, otherusername, imgurl, headers);
                    else
                        Toast.makeText(contx, "Sinch Error, please try again.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            marshmallowPermissions.requestPermissionForCalls();

        }

    }


    public static void vidcallbtnClicked(final SinchCallService.SinchServiceInterface sinchServiceInterface, MarshmallowPermissions marshmallowPermissions, final String mykey, final String usrkey, final String otherusername, final String imgurl, final Context contx) {
        System.out.println("video call button clicked " + DBREF_USER_LOC.child(mykey));
        if (marshmallowPermissions.checkPermissionForCamera()) {
            final String userName = usrkey;
            final Long id = GetTimeStamp.Id();
            DBREF_FCM.child(usrkey).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println(dataSnapshot + "adding data to firebase userprofileact voicecallhelper" );
                    String rectok = dataSnapshot.getValue().toString();
                    System.out.println("rectok userprofileact" + rectok);
                    CallHelper.addCallToFirebase(mykey, id, rectok);
                    if (sinchServiceInterface != null)
                        CallHelper.doVideoCall(sinchServiceInterface, contx, userName, mykey, usrkey, otherusername, imgurl);
                    else
                        Toast.makeText(contx, "Sinch Error, please try again.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            marshmallowPermissions.requestPermissionForCamera();

        }

    }

    public static void doCall(SinchCallService.SinchServiceInterface sinchServiceInterface, Context context, String userName, String mykey, String usrkey, String otherusername, String imgurl, Map<String, String> headers) {
        try {
            Call call = sinchServiceInterface.callUser(userName, headers);

            String callId = call.getCallId();
            System.out.println("do call called userprofileact for " + callId);

            Intent callScreen = new Intent(context, CallScreenActivity.class);
            callScreen.putExtra(SinchCallService.CALL_ID, callId);
            callScreen.putExtra("calleruid", mykey);
            callScreen.putExtra("receiveruid", usrkey);
            callScreen.putExtra("otherusername", otherusername);
            callScreen.putExtra("timestamp", GetTimeStamp.timeStamp());
            callScreen.putExtra("imgurl", imgurl);
            context.startActivity(callScreen);
        } catch (NullPointerException e) {

        }
    }

    public static void doVideoCall(SinchCallService.SinchServiceInterface sinchServiceInterface, Context context, String userName, String mykey, String usrkey, String otherusername, String imgurl) {
        try {
            Call call = sinchServiceInterface.callUserVideo(userName);

            String callId = call.getCallId();
            System.out.println("do video call called userprofileact for " + callId);
            Intent callScreen = new Intent(context, VideoCallScreenActivity.class);
            callScreen.putExtra(SinchCallService.CALL_ID, callId);
            callScreen.putExtra("calleruid", mykey);
            callScreen.putExtra("receiveruid", usrkey);
            callScreen.putExtra("otherusername", otherusername);
            callScreen.putExtra("timestamp", GetTimeStamp.timeStamp());
            callScreen.putExtra("imgurl", imgurl);
            callScreen.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(callScreen);
        } catch (NullPointerException e) {

        }
    }

    public static void addCallToFirebase(String otherusrkey, Long id, String rectok) {
        System.out.println("adding data to firebase userprofileact");
        //adding to userus uservlogs for notification purposes
        DBREF.child("Users").child("UserVCAllLogs").child(otherusrkey).child(String.valueOf(id)).child("receiverToken").setValue(rectok);
    }

    public static Map<String, String> getAddress(Map<String, String> headers, Context contx) {
//        Geocoder geocoder = new Geocoder(contx, Locale.getDefault());
//        List<Address> addresses = null;
//        try {
//            addresses = geocoder.getFromLocation(mlat, mlng, 1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (addresses.size() > 0 && addresses.get(0).getAddressLine(1) != null) {
//            headers.put("location", addresses.get(0).getAddressLine(1));
//            System.out.println(addresses.get(0).getAddressLine(1) + "headrs added placecall act voicecalherlper");
//        }
        //also sending profile pic url and username of current user
        UserSession us = new UserSession(contx);
        headers.put("username", us.getUsername());
        return headers;
    }

    public static String formatTimespan(long timespan) {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }


    public static String formatTimeCallLogs(long timespan) {
        System.out.println("format duration call" + timespan);
        Long minutes = Long.valueOf(0);
        Long seconds = Long.valueOf(0);
        Long hours = Long.valueOf(0);

        if (timespan < 3600) {

            minutes = timespan / 60;
            seconds = timespan % 60;

        } else {
            minutes = timespan / 60;
            hours = minutes / 60;
            seconds = timespan % 60;
        }
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }


}
