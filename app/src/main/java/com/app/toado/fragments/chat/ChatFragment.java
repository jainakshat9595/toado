package com.app.toado.fragments.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.toado.R;
//import com.app.toado.activity.chat.ChatActivity1;
import com.app.toado.activity.chat.ForwardChatActivity;
import com.app.toado.adapter.ChatListAdapter;
import com.app.toado.helper.MarshmallowPermissions;
import com.app.toado.model.realm.ActiveChatsRealm;
import com.app.toado.settings.UserSession;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ghanendra on 14/06/2017.
 */

public class ChatFragment extends Fragment {
    private View myFragmentView;
    FragmentManager fmm;
    private ArrayList<ActiveChatsRealm> list;
    private ArrayList<String> listIds;
    private RecyclerView recyclerView;
    private String mykey;
    private ChatListAdapter mAdapter;
    MarshmallowPermissions marshper;
    private final String TAG = " CHATFRAGMENT";
    Realm mRealm;
    UserSession us;

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    public static ChatFragment newInstance(Bundle args) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        return myFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        getContext().registerReceiver(this.refreshChats, new IntentFilter("refreshChatlist"));

        Log.d(TAG, "onstart chatfragment");

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fmm = getFragmentManager();

        mRealm = Realm.getDefaultInstance();

        marshper = new MarshmallowPermissions(getActivity());

        list = new ArrayList<>();
        listIds = new ArrayList<>();
        us = new UserSession(getActivity());
        Log.d(TAG, "chatfragment onviewcreated "+us.getUserKey());

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ChatListAdapter(list, getActivity(),us.getUserKey());
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (marshper.checkPermissionForContacts()) {
                    getContext().startActivity(new Intent(getActivity(), ForwardChatActivity.class));
                } else {
                    marshper.requestPermissionForContacts();
                    getContext().startActivity(new Intent(getActivity(), ForwardChatActivity.class));
                }
            }
        });

        UserSession usersession = new UserSession(getActivity());
        if (getActivity().getIntent().getStringExtra("mykey") != null)
            mykey = getActivity().getIntent().getStringExtra("mykey");
        else
            mykey = usersession.getUserKey();

        Log.d(TAG, "chatfragment dbchatlist");


        loadData();
    }

    private void loadData() {
        Sort sort[] = {Sort.DESCENDING};
        String[] fieldNames = {"msgid"};
        RealmResults<ActiveChatsRealm> shows = mRealm.where(ActiveChatsRealm.class).findAll();
        Log.d(TAG, "load data called chatfrag " + shows.size());
        for (int i = 0; i < shows.size(); i++) {
            if (!listIds.contains(shows.get(i).getChatref())) {
                Log.d(TAG, "shows chatfrag" + shows.get(i).getChatref());
                list.add(shows.get(i));
                listIds.add(shows.get(i).getChatref());
                mAdapter.notifyDataSetChanged();
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onresume chatfragment");
        loadData();
    }

    BroadcastReceiver refreshChats = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringExtra("chatrefreshed") != null) {
                Log.d(TAG, "chatfragment recd broadcast" + intent.getStringExtra("chatrefreshed"));
                loadData();
                mAdapter.notifyDataSetChanged();
                Log.d(TAG, list.size() + "chat adapter refreshed refreshchats chat fragment" + listIds.size());
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.refreshChats);
    }

}
