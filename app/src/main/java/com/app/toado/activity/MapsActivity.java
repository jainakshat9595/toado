package com.app.toado.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.OpenFile;
import com.app.toado.settings.UserMediaPrefs;
import com.app.toado.settings.UserSession;
import com.app.toado.model.ChatMessage;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_CHATS;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_LOC;
import static com.app.toado.helper.ToadoConfig.STORAGE_REFERENCE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Marker marker;
    String username, mykey;
    private UserSession session;
    private String comment_type;
    private String sender;
    private String otheruserkey;
    Double mlat, mlng;
    private String TAG = "MAPSACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        session = new UserSession(this);
        mykey = session.getUserKey();
        username = session.getUsername();

        Intent intent = getIntent();
        comment_type = intent.getStringExtra("comment_type");
        sender = intent.getStringExtra("username");
        mykey = intent.getStringExtra("mykey");
        otheruserkey = intent.getStringExtra("otheruserkey");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        DBREF_USER_LOC.child(mykey).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mlat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                mlng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                LatLng la = new LatLng(mlat, mlng);
                System.out.println("marker datasnap mapsact" + mlat + " " + mlng);
                marker = mMap.addMarker(new MarkerOptions()
                        .position(la)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(getAddressFromLatLng(la)));
                marker.showInfoWindow();
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(la, 15);
                mMap.moveCamera(cu);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                System.out.println("map onclick form oncreate mapsact");
                putMarker(latLng);
            }
        });
    }

    public void putMarker(LatLng latLng) {
        System.out.println(" put marker method called " + latLng);
        if (marker != null) {
            marker.remove();
        }
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(getAddressFromLatLng(latLng)));
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.moveCamera(cu);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        System.out.println("map clicked mapsact" + latLng);
        putMarker(latLng);
        mlat = latLng.latitude;
        mlng = latLng.longitude;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        putMarker(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);

        String address = "";
        try {
            address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get(0).getAddressLine(0);
        } catch (IOException e) {
        }

        return address;
    }

    public void sendLoc(View view) {
        final Long id = GetTimeStamp.Id();
        final String timestamp = GetTimeStamp.timeStamp();

        final String fname = timestamp + ".jpg";

        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(final Bitmap snapshot) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        Bitmap bitmap = snapshot;
                        try {
                            final File file = OpenFile.createFile(MapsActivity.this, fname, "sent");
                            Log.d(TAG, "send loc called " + file.getAbsolutePath());
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            Log.d(TAG, "send loc clicked maps act " + file.getAbsolutePath());
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("locresult", file.getAbsolutePath());
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        } catch (Exception e) {
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                            e.printStackTrace();
                        }

                        return null;
                    }
                }.execute();
            }
        };
        mMap.snapshot(callback);


    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
