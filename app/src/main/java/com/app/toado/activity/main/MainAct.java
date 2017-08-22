package com.app.toado.activity.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.activity.BaseActivity;
import com.app.toado.activity.calls.CallScreenActivity;
import com.app.toado.activity.chat.StarMessageActivity;
import com.app.toado.activity.settings.SettingsActivity;
import com.app.toado.adapter.StarredMessageAdapter;
import com.app.toado.fragments.mainviewpager.MainpagerAdapter;
import com.app.toado.fragments.mainviewpager.MainpagerItems;
import com.app.toado.helper.MyXMPP2;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.helper.MarshmallowPermissions;
 import com.app.toado.settings.CallSession;
import com.app.toado.settings.UserSession;
import com.app.toado.services.LocServ;
import com.app.toado.services.SinchCallService;
import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import static com.app.toado.helper.ToadoConfig.DBREF_FCM;

/**
 * Created by ghanendra on 14/06/2017.
 */

public class MainAct extends BaseActivity {
    FragmentManager fmm;
    MainpagerAdapter pagad;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    TextView tv_topbar;
    private MarshmallowPermissions marshmallowPermissions;
    String usrkey;
    ToadoAlerts alr;
    SinchCallService callserv;
    boolean mServiceBound = false;
    private UserSession usess;
    int count = 0;
    LinearLayout callay;
    CallSession cs;
     private boolean mBounded;
    private static final String TAG = "MainActivity";
    private MyXMPP2 myxinstance;

    private static final String[] STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callay = (LinearLayout) findViewById(R.id.layactivecall);

        cs = new CallSession(this);
        usess = new UserSession(this);
        alr = new ToadoAlerts(this);

        final LocationManager manager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alr.buildAlertMessageNoGps();
        }

        if (getIntent().getStringExtra("mykey") != null)
            usrkey = getIntent().getStringExtra("mykey");
        else
            usrkey = usess.pref.getString("userkey", "nokey");

        System.out.println("user key mainact starting xmpp connection" + usrkey);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                myxinstance = MyXMPP2.getInstance(MainAct.this, getString(R.string.server), usrkey);
                return null;
            }
        }.execute();

        marshmallowPermissions = new MarshmallowPermissions(this);

        marshmallowPermissions.reguestNewPermissions(this, STORAGE_PERMISSION);

        fmm = this.getSupportFragmentManager();

        tv_topbar = (TextView) findViewById(R.id.tvtopbar);

        String tok = FirebaseInstanceId.getInstance().getToken();
        System.out.println(usess.getAppFirstRun() + " token mainact " + tok);

        setupViewPager();


        if (usess.getAppFirstRun()) {
            ToadoAlerts.showGpsTrackingAlert(this, usess);
            usess.setAppFirstRun(false);
        }

        startLoc();

        if (!usrkey.matches("nokey")) {
            DBREF_FCM.child(usrkey).child("token").setValue(tok);
        }

        getFbAccessToken();
    }

    public void startLoc() {
        if (!usrkey.matches("nokey")) {
            checkForLoc(usrkey);
            count++;
        } else
            Toast.makeText(this, "No user key found error", Toast.LENGTH_SHORT).show();

    }

    private void checkForLoc(String key) {
        if (!marshmallowPermissions.checkPermissionForLocations()) {
            marshmallowPermissions.requestPermissionForLocations();
        } else {
            Intent in = new Intent(MainAct.this, LocServ.class);
            in.putExtra("keyval", key);
            System.out.println("starting service with key" + key);
            startService(in);
        }
    }


    private void getFbAccessToken() {

        if (AccessToken.getCurrentAccessToken() != null) {
            System.out.println("current access token" + AccessToken.getCurrentAccessToken());
        } else {
            AccessToken.refreshCurrentAccessTokenAsync(new AccessToken.AccessTokenRefreshCallback() {
                @Override
                public void OnTokenRefreshed(AccessToken accessToken) {
                    System.out.println("refreshed token" + accessToken);
                }

                @Override
                public void OnTokenRefreshFailed(FacebookException exception) {
                    System.out.println("fb error access token refresh" + exception.getStackTrace());
                }
            });
        }
    }


    private void setupViewPager() {
        pagad = new MainpagerAdapter(fmm, getApplicationContext());
        ArrayList<MainpagerItems> items = new ArrayList<>();
        items.add(new MainpagerItems(MainpagerItems.PAGE_TYPE.Chat));
        items.add(new MainpagerItems(MainpagerItems.PAGE_TYPE.Home));
        items.add(new MainpagerItems(MainpagerItems.PAGE_TYPE.GroupChats));
        items.add(new MainpagerItems(MainpagerItems.PAGE_TYPE.Calls));

        pagad.setItems(items);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.viewpagertab);
        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(pagad);
        tabLayout.setupWithViewPager(viewPager);
        setupIcons();
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(MainAct.this, R.color.white);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                        switch (tab.getPosition()) {
                            case 0:
                                tv_topbar.setText("Chat");
                                sendBroadcast(new Intent().putExtra("chatrefreshed", "yes").setAction("refreshChatlist"));
                                break;
                            case 1:
                                tv_topbar.setText("Home");
                                break;
                            case 2:
                                tv_topbar.setText("Group Chats");
                                break;
                            case 3:
                                tv_topbar.setText("Calls");
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(MainAct.this, R.color.black);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                        System.out.println("tab reselcted");
                    }
                }
        );
        selectPage(1);
        selectPage(2);
        selectPage(3);
        selectPage(0);
    }

    private void setupIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.iconchat);
        tabLayout.getTabAt(1).setIcon(R.drawable.iconhome);
        tabLayout.getTabAt(2).setIcon(R.drawable.icongrpchat);
        tabLayout.getTabAt(3).setIcon(R.drawable.iconphone);
    }

    void selectPage(int pageIndex) {
        sendBroadcast(new Intent().putExtra("chatrefreshed", "yes").setAction("refreshChatlist"));
        tabLayout.setScrollPosition(pageIndex, 0f, true);
        viewPager.setCurrentItem(pageIndex);

    }

    public void openMainMenu(View view) {
        final PopupMenu popup = new PopupMenu(this, findViewById(R.id.popupsettings));
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.menu_main, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case (R.id.menuprofile):
                        break;
                    case (R.id.menusettings):
                        startActivity(new Intent(MainAct.this, SettingsActivity.class));
                        break;
                    case (R.id.menulogout):
                        ToadoAlerts.showLogoutAlert(MainAct.this, usess);
                        break;
                    case (R.id.menustar):
                        startActivity(new Intent(MainAct.this, StarMessageActivity.class));
                        break;
                }
                return true;
            }
        });
        popup.show(); //showing popup menu
    }


    @Override
    public void onServiceConnected() {

        try {
            callserv = getSinchServiceInterface().getService();
            getSinchServiceInterface().startClient(usrkey);
            mServiceBound = true;
        } catch (NullPointerException e) {
            //getSinchServiceInterface() in doStuff below throw null pointer error.
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopbindtoserv();
         if (switchTabs != null)
            unregisterReceiver(switchTabs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SinchCallService.class);
        startService(intent);
          loginXmUser(usrkey);
        registerReceiver(this.switchTabs, new IntentFilter("MainActTabHandler"));
    }


    private void loginXmUser(String userKey) {
        System.out.println("logging user main act"+userKey);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("mainact call onresume called");
        usrkey = usess.pref.getString("userkey", "nokey");
        startLoc();
        handleCallLayout();
     }

    @Override
    protected void onPause() {
        super.onPause();
     }

    private void handleCallLayout() {
        System.out.println("call layout handle called mainact" + cs.getCallactive());
        if (cs.getCallactive()) {
            callay.setVisibility(View.VISIBLE);
        } else {
            callay.setVisibility(View.GONE);
        }
    }

    private void stopbindtoserv() {
        if (mServiceBound) {
            mServiceBound = false;
        }

    }

    BroadcastReceiver switchTabs = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringExtra("tabindex") != null) {
                System.out.println("mainact recd broadcast" + intent.getStringExtra("tabindex"));
                selectPage(Integer.parseInt(intent.getStringExtra("tabindex")));
            }
        }
    };

    public void takeToCallAct(View view) {
        System.out.println("callay clicked mainact ");
        startActivity(new Intent(MainAct.this, CallScreenActivity.class));
    }

    @Override
    public void onBackPressed() {
    }

}
