package com.app.toado.fragments.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.toado.R;
import com.app.toado.adapter.CallLogsAdapter;
import com.app.toado.model.CallDetails;
import com.app.toado.settings.UserSession;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_CALLS;

/**
 * Created by ghanendra on 14/06/2017.
 */

public class CallFragment extends Fragment {
    private View myFragmentView;
    FragmentManager fmm;
    ArrayList<CallDetails> phnlist;
    ArrayList<String> phnlistIds;
    private RecyclerView recyclerView;
    private CallLogsAdapter mAdapter;
    UserSession us;
    String mk;

    public static CallFragment newInstance() {
        CallFragment fragment = new CallFragment();
        return fragment;
    }

    public static CallFragment newInstance(Bundle args) {
        CallFragment fragment = new CallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_calls, container, false);
        return myFragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setupUI(view.findViewById(R.id.relcity));
        fmm = getFragmentManager();

        us = new UserSession(getContext());
        mk = us.getUserKey();

        phnlist = new ArrayList<>();
        phnlistIds = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.rvcalllogs);
        mAdapter = new CallLogsAdapter(phnlist, getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRvData();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    private void loadRvData() {
        DBREF_CALLS.child(mk).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("ddatasnapshot callfragment " + dataSnapshot);
                CallDetails cd = CallDetails.parse(dataSnapshot);
                if (!phnlistIds.contains(cd.getCallid())) {
                    phnlist.add(cd);
                    phnlistIds.add(cd.getCallid());
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
