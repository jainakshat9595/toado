package com.app.toado.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.toado.R;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.adapter.DistanceUserAdapter;
import com.app.toado.adapter.SettingsAdapter;
import com.app.toado.settings.UserSession;

import java.util.ArrayList;

/**
 * Created by ghanendra on 01/07/2017.
 */

public class SettingsActivity extends ToadoBaseActivity {
    private SettingsAdapter mAdapter;
    private RecyclerView recyclerView;
    ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingsmain);

        UserSession usess = new UserSession(this);

        recyclerView = (RecyclerView) findViewById(R.id.rvsettings);
        list=new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SettingsAdapter(list,this,usess);
        recyclerView.setAdapter(mAdapter);

        list.add("Account");
        list.add("Profile");
        list.add("Invite");
        list.add("App Settings");
        list.add("Chats");
        list.add("Distance and Gps");
        list.add("Help");
        list.add("Logout");
        mAdapter.notifyDataSetChanged();

    }

    public void toDistancePref(View view){
        startActivity( new Intent(this,DistancePreferencesActivity.class));
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
