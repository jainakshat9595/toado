package com.app.toado.services;

/**
 * Created by ghanendra on 16/06/2017.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.app.toado.helper.BackgroundCheck;
 import com.app.toado.settings.UserSession;
import com.app.toado.settings.UserSettingsSharedPref;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_LOC;

public class LocServ extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    String lat, lon;
    String mobile;
    DatabaseReference dbref;
    String key = "";
    DatabaseReference ref = DBREF_USER_LOC.getRef();
    GeoFire geoFire = new GeoFire(ref);
    UserSession us;
    UserSettingsSharedPref usset;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            lat = String.valueOf(mLastLocation.getLatitude());
            lon = String.valueOf(mLastLocation.getLongitude());
            mobile = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date());
        }

        usset = new UserSettingsSharedPref(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lat = String.valueOf(location.getLatitude());
        lon = String.valueOf(location.getLongitude());

        if (!key.matches("nokey") || !key.matches("nil")) {
            Boolean b = usset.getGpsBackSetting();
//            System.out.println("gpsbacksetting val=" + b);
            if (b) {
//                System.out.println("set location even when app in backgroudn locserv" + key);
                //setting location even when app in backgroud
                setGeoFireLoc();
            } else {
                //check app in foreground then only set update
                if(!BackgroundCheck.isAppIsInBackground(this)){
                    System.out.println("shared pref dont send location when not active locserv app not in background ");
                    setGeoFireLoc();
                }
            }


        }
    }

    private void setGeoFireLoc() {
        geoFire.setLocation(key, new GeoLocation(Double.parseDouble(String.valueOf(lat)), Double.parseDouble(String.valueOf(lon))), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.out.println("location not saved on db locserv " + error.getDetails());
                } else {
//                        Toast.makeText(getApplicationContext(),"Location saved on server successfully!",Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("connection failed gps");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();

        getKey();

//        System.out.println("on start command loc serv " + key);
        return START_STICKY;
    }

    private void getKey() {
        us = new UserSession(getApplicationContext());
        System.out.println(us.getUsername() + " get key called locserv " + us.getUserKey());
        try {
            key = us.getUserKey();
        } catch (NullPointerException e) {
            key = "nokey";
        }
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

}
