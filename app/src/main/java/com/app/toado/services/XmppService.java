package com.app.toado.services;

/**
 * Created by Khushvinders on 15-Nov-16.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.app.toado.R;
import com.app.toado.helper.LocalBinder;
//import com.app.toado.helper.MyXMPP;
import com.app.toado.settings.UserSession;

//public class XmppService extends Service {
//
//    public static ConnectivityManager cm;
//    public static MyXMPP xmpp;
//
//    @Override
//    public IBinder onBind(final Intent intent) {
//        return new LocalBinder<XmppService>(this);
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        UserSession usr = new UserSession(getApplicationContext());
//        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        xmpp = MyXMPP.getInstance(XmppService.this);
//        xmpp.connect("onCreate");
//     }
//
//    @Override
//    public int onStartCommand(final Intent intent, final int flags,
//                              final int startId) {
//        return Service.START_STICKY;
//    }
//
//    @Override
//    public boolean onUnbind(final Intent intent) {
//        return super.onUnbind(intent);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        xmpp.connection.disconnect();
//    }
//
//
//}