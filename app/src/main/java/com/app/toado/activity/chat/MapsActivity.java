package com.app.toado.activity.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.OpenFile;
import com.app.toado.settings.UserSession;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_LOC;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    public static final String FRAGTAG = "PlacePickerFragment";
    private GoogleMap mMap;
    Marker marker;
    String username, mykey;
    private UserSession session;
    private String comment_type;
    private String sender;
    private String otheruserkey;
    Double mlat, mlng;
    private String TAG = "MAPSACTIVITY";
    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        session = new UserSession(this);
        mykey = session.getUserKey();
        username = session.getUsername();

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

//        Intent intent = getIntent();
//        comment_type = intent.getStringExtra("comment_type");
//        sender = intent.getStringExtra("username");
//        mykey = intent.getStringExtra("mykey");
//        otheruserkey = intent.getStringExtra("otheruserkey");
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {return;
        }
        mMap.setMyLocationEnabled(true);


        DBREF_USER_LOC.child(mykey).child("l").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mlat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                mlng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                LatLng la = new LatLng(mlat, mlng);
                System.out.println("marker datasnap mapsact" + mlat + " " + mlng);
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(la, 15);
                mMap.moveCamera(cu);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
