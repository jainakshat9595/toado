package com.app.toado.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.app.toado.R;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.settings.SettingsActivity;
import com.app.toado.adapter.StarredMessageAdapter;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.settings.UserSession;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by ghanendra on 21/08/2017.
 */

public class StarMessageActivity extends ToadoBaseActivity {
    RecyclerView rvstar;
    StarredMessageAdapter mAdapter;
    UserSession usr;
    final String TAG = "STARMESSAGEACTIVITY";
    ArrayList<ChatMessageRealm> arlist = new ArrayList<>();
    ImageView imgnorv;
    ImageButton btnpopup, btnsearch;
    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_messages);
        rvstar = (RecyclerView) findViewById(R.id.star_recycler_view);
        imgnorv = (ImageView) findViewById(R.id.imgnorv);

        btnsearch = (ImageButton) findViewById(R.id.imgsearch);
        btnpopup = (ImageButton) findViewById(R.id.btnpopup);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        btnsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.showSearch(true);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                mAdapter.updateList(arlist,"");
                //Do some magic
            }
        });

        Realm mRealm = Realm.getDefaultInstance();
        usr = new UserSession(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        RealmResults<ChatMessageRealm> result2 = mRealm.where(ChatMessageRealm.class)
                .equalTo("star", true)
                .findAll();

        arlist.addAll(result2);
        if (arlist.size() == 0) {
            rvstar.setVisibility(View.GONE);
            btnpopup.setVisibility(View.GONE);
            btnsearch.setVisibility(View.GONE);
            imgnorv.setVisibility(View.VISIBLE);
        } else {
            imgnorv.setVisibility(View.GONE);
            rvstar.setVisibility(View.VISIBLE);
            btnpopup.setVisibility(View.VISIBLE);
            btnsearch.setVisibility(View.VISIBLE);
            Log.d(TAG, arlist.size() + "RESULTS2" + result2.size());
            mAdapter = new StarredMessageAdapter(arlist, this, usr.getUserKey(), GetTimeStamp.timeStampDate(),"");
            rvstar.setLayoutManager(linearLayoutManager);
            rvstar.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onBack(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void openMenu(View view) {
        final PopupMenu popup = new PopupMenu(this, findViewById(R.id.btnpopup));
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.menu_staract, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case (R.id.menuunstar):
                        for (ChatMessageRealm cm : arlist) {
                            ChatMessageRealm cmnew = new ChatMessageRealm(cm.getChatref(), cm.getOtherjid(), cm.getMsgstring(), cm.getSenderjid(), cm.getSendertime(), cm.getSenderdate(), cm.getMsgtype(), cm.getMsgid(), cm.getMsgstatus(), cm.getMsgweburl(), cm.getMsglocalurl(), cm.getMediathumbnail(), false);
                            ChatHelper.starMessage(cmnew);
                        }
                        rvstar.setVisibility(View.GONE);
                        imgnorv.setVisibility(View.VISIBLE);
                        btnpopup.setVisibility(View.GONE);
                        btnsearch.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            mAdapter.updateList(arlist,"");
        } else {
            super.onBackPressed();
        }
    }

    void filter(String text) {
        ArrayList<ChatMessageRealm> temp = new ArrayList();
        for (ChatMessageRealm d : arlist) {
            //or use .contains(text)
            if (d.getMsgstring().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        //update recyclerview
        mAdapter.updateList(temp,text);
    }
}
