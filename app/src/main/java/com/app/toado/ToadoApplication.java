package com.app.toado;

import android.os.StrictMode;

import com.app.toado.settings.UserSession;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ToadoApplication extends android.support.multidex.MultiDexApplication {
    private static ToadoApplication mInstance;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(config);

        mInstance = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
        UserSession session = new UserSession(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        String userkey = session.getUserKey();
        if (!userkey.matches("") && !userkey.matches("nil")) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = database.getReference().child("Users").child("Usersession").child(userkey).child("online").getRef();

// stores the timestamp of my last disconnect (the last time I was seen online)
            final DatabaseReference lastOnlineRef = database.getReference().child("Users").child("Usersession").child(userkey).child("lastseen").getRef();

            final DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        // add this device to my connections list
                        // this value could contain info about the device or a timestamp too
                        myConnectionsRef.setValue(Boolean.TRUE);

                        // when this device disconnects, remove it
                        myConnectionsRef.onDisconnect().setValue(Boolean.FALSE);

                        // when I disconnect, update the last time I was seen online
                        lastOnlineRef.onDisconnect().setValue(Calendar.getInstance().getTime() + "");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled at .info/connected");
                }
            });
        }

    }

}
