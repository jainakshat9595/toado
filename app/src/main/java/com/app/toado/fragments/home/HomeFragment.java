package com.app.toado.fragments.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.toado.R;
import com.app.toado.activity.settings.DistancePreferencesActivity;
import com.app.toado.adapter.DistanceUserAdapter;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.settings.UserSettingsSharedPref;
import com.app.toado.settings.UserSession;
import com.app.toado.model.DistanceUser;
import com.app.toado.model.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_LOC;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;


public class HomeFragment extends Fragment {
    private View myFragmentView;
    FragmentManager fmm;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    ArrayList<DistanceUser> list;
    ArrayList<String> listId;
    HashMap<String, DistanceUser> hashMap;
    DatabaseReference ref = DBREF_USER_LOC;
    GeoFire geoFire = new GeoFire(ref);
    float distancePref;
    private UserSession session;
    private String userkey;
    UserSettingsSharedPref userSettingsSharedPref;
    CoordinatorLayout laycoord;
    ToadoAlerts alr;
    Boolean alertdisp = true;
    int count = 0;

    AlertDialog.Builder builder;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public static HomeFragment newInstance(Bundle args) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        return myFragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fmm = getFragmentManager();

        laycoord = (CoordinatorLayout) view.findViewById(R.id.coordinatorlayout);

        builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);

        session = new UserSession(getActivity());

        if (getActivity().getIntent().getStringExtra("mykey") != null) {
            userkey = getActivity().getIntent().getStringExtra("mykey");
            System.out.println("1 homefragment from session" + userkey);
        } else {
            System.out.println("2 homefragment from session" + userkey);
            userkey = session.getUserKey();
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_distanceUser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        list = new ArrayList<>();
        listId = new ArrayList<>();
        hashMap = new HashMap<>();

        alr = new ToadoAlerts(getContext());

        mAdapter = new DistanceUserAdapter(list, getActivity());
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("on resume called homefragment");
        setDistance();
        loadRecyclerViewData();
        handleNoKeys();
    }

    public void setDistance() {
        System.out.println("set distance called homefragment");
        userSettingsSharedPref = new UserSettingsSharedPref(getActivity());
        distancePref = userSettingsSharedPref.getValue() * userSettingsSharedPref.getConversionFactor();//km
        distancePref = Math.round(distancePref);
        System.out.println(userSettingsSharedPref.getConversionFactor() + "distance pref calling loadrecview homefragment" + distancePref);
    }

    private void loadRecyclerViewData() {
        System.out.println("load rv data called homefragment");
        geoFire.getLocation(userkey, new com.firebase.geofire.LocationCallback() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("geo fire error homefragment " + databaseError.getDetails());
            }

            @Override
            public void onLocationResult(final String mykey, final GeoLocation mylocation) {
                System.out.println(mykey + " on locresult homefragment" + mylocation);
                if (mylocation != null) {
                    System.out.println("The location for key %s is [%f,%f]" + mykey + mylocation.latitude + mylocation.longitude);
                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mylocation.latitude, mylocation.longitude), distancePref);
                    System.out.println(geoQuery.getRadius() + " geo query new in miles homefragment " + distancePref);
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(final String key, final GeoLocation location) {
                            if (!key.equals(mykey)) {
                                DatabaseReference username = DBREF_USER_PROFILES.child(key).getRef();
                                username.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
//                                            System.out.println(dataSnapshot.toString() + "key from homefragment" + key);
                                            callMethod(dataSnapshot, mylocation, location, key);
                                        } else {
//                                            System.out.println("homefragment else no datasnapshot onkeyenetered");
                                            handleNoKeys();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        System.out.println("on location error homefrag" + databaseError.getDetails());
                                    }
                                });
                            } else {
//                                System.out.println(" my key same");
                                handleNoKeys();
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {
                            System.out.println(" remove called onkeyexited" + key);
                            DistanceUser distanceUser = hashMap.get(key);
                            removeTile(key, distanceUser);
                        }

                        @Override
                        public void onKeyMoved(final String key, final GeoLocation location) {
                            if (!key.equals(mykey)) {
                                System.out.println("remove called onkeymoved");
                                DistanceUser distanceUserOld = hashMap.get(key);
                                removeTile(key, distanceUserOld);

                                DatabaseReference username = FirebaseDatabase.getInstance().getReference().child("Users").child("Userprofiles").child(key).getRef();
                                username.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists())
                                            callMethod(dataSnapshot, mylocation, location, key);
                                        else {
//                                            System.out.println("handle nokeys from onkeymoved");
                                            handleNoKeys();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        System.out.println("on location error homefrag" + databaseError.getDetails());
                                    }
                                });

                            }
                        }

                        @Override
                        public void onGeoQueryReady() {
                            System.out.println("All initial data has been loaded and events have been fired!");
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                            System.out.println("error geo fire" + error.getDetails());
                        }
                    });

                }
            }
        });

        mAdapter.notifyDataSetChanged();
    }

    private void handleNoKeys() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                System.out.println("list size handler nokeys " + list.size());
                if (list.size() == 0)
                    showAlert();
                else if (list.size() == 0 && distancePref == 50) {
                    System.out.println("sending broadcast from onkeymoved homefragment");
                    getContext().sendBroadcast(new Intent().putExtra("tabindex", "2").setAction("MainActTabHandler"));
                }
            }
        }, 2000);
    }

    private void showAlert() {
//        if (alertdisp) {
//            builder.setTitle("Cant find any one nearby?")
//                    .setMessage("Increase your radius of search!")
//                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            alertdisp = true;
//                            getContext().startActivity(new Intent(getContext(), DistancePreferencesActivity.class));
//                            dialog.dismiss();
//
//                        }
//                    })
//                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            alertdisp = true;
//                            dialog.dismiss();
//                            System.out.println("alert dismissed sending broadcast to change tab to index 1 on main act toadoalerts");
//                            getContext().sendBroadcast(new Intent().putExtra("tabindex", "1").setAction("MainActTabHandler"));
//
//                        }
//                    })
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();
//
//            alertdisp = false;
//        }
    }

    private void callMethod(final DataSnapshot dataSnapshot, final GeoLocation mylocation, final GeoLocation location, final String key1) {
        if (listId.contains(key1)) {
            System.out.println("homefragment list id already contains key= " + key1);
        } else {

            Location myloc = new Location("");
            myloc.setLatitude(mylocation.latitude);
            myloc.setLongitude(mylocation.longitude);

            Location userLoc = new Location("");
            userLoc.setLatitude(location.latitude);
            userLoc.setLongitude(location.longitude);

//            System.out.println("myloc callmeth" + myloc.getLatitude() + myloc.getLongitude());

            float distanceInMiles = myloc.distanceTo(userLoc) / (1000 * userSettingsSharedPref.getConversionFactor());
            distanceInMiles = Math.round(distanceInMiles);

//            System.out.println("meteres = " + myloc.distanceTo(userLoc) + "distance in miles homefragment = " + distanceInMiles);

            User user = User.parse(dataSnapshot);
            DistanceUser distanceUser = new DistanceUser(key1, user.getName(), distanceInMiles);
            list.add(distanceUser);
            listId.add(key1);
            hashMap.put(key1, distanceUser);
            mAdapter.notifyDataSetChanged();
//            System.out.println(distanceUser.getName() + " distance user to add " + list.size());

        }
    }

    public void removeTile(String key, DistanceUser du) {
        hashMap.remove(key);
        list.remove(du);
        listId.remove(key);
        mAdapter.notifyItemRemoved(list.indexOf(du));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
