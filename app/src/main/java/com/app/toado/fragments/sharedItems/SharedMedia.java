package com.app.toado.fragments.sharedItems;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.toado.R;
import com.app.toado.adapter.DistanceUserAdapter;
import com.app.toado.adapter.SharedMediaAdapter;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.model.DistanceUser;
import com.app.toado.model.User;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.settings.UserSession;
import com.app.toado.settings.UserSettingsSharedPref;
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

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_LOC;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;


public class SharedMedia extends Fragment {


    private View myFragmentView;
    private FragmentManager fmm;

    private RecyclerView mMediaRV;
    private SharedMediaAdapter mMediaAdapter;

    private String mOtherUserKey;

    private UserSession mUserSession;

    private ArrayList<ChatMessageRealm> mList;

    public static SharedMedia newInstance() {
        SharedMedia fragment = new SharedMedia();
        return fragment;
    }

    public static SharedMedia newInstance(Bundle args) {
        SharedMedia fragment = new SharedMedia();
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

        Bundle bundle = getArguments();
        mOtherUserKey = bundle.getString("OtherUserKey");

        mUserSession = new UserSession(getContext());

        Sort sort[] = {Sort.ASCENDING};
        String[] fieldNames = {"msgid"};

        mList = new ArrayList<>();

        Realm mRealm = Realm.getDefaultInstance();
        RealmResults<ChatMessageRealm> shows =
                mRealm.
                    where(ChatMessageRealm.class).
                        equalTo("chatref", mUserSession.getUserKey() + mOtherUserKey).
                    findAll();

        for(ChatMessageRealm cm : shows) {
            mList.add(cm);
        }


        mMediaRV = (RecyclerView) view.findViewById(R.id.rv_distanceUser);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mMediaRV.setLayoutManager(gridLayoutManager);
        mMediaAdapter = new SharedMediaAdapter(mList, getContext());
        mMediaRV.setAdapter(mMediaAdapter);

    }


}
