package com.app.toado.activity.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.settings.SettingsActivity;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;

/**
 * Created by ghanendra on 11/08/2017.
 */

public class MediaPreviewActivity extends Activity {
    final static String TAG = "MediaPreviewActivity";

    String imagepath, sender, timestamp,caption;
    private ImageView ivSettings;
    private ImageView ivForward;
    private ImageView ivStar;
    private TextView tvname;
    private TextView tvtime;
    private ImageView imgback;
    private ZoomableImageView imgv;
    private TextView tvcaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_preview);
        initView();

        UserSession us = new UserSession(this);

        imagepath = getIntent().getStringExtra("imagepath");
        sender = getIntent().getStringExtra("sender");
        timestamp = getIntent().getStringExtra("timestamp");
        caption= getIntent().getStringExtra("caption");

        if (sender.matches(us.getUserKey())) {
            tvname.setText("You");
        }else
            tvname.setText(sender);

        tvcaption.setText(caption);

        Glide.with(this).load(imagepath).into(imgv);

        tvtime.setText(timestamp);


    }

    private void initView() {
        ivSettings = (ImageView) findViewById(R.id.ivSettings);
        ivForward = (ImageView) findViewById(R.id.ivForward);
        ivStar = (ImageView) findViewById(R.id.ivStar);
        tvname = (TextView) findViewById(R.id.tvname);
        tvtime = (TextView) findViewById(R.id.tvtime);
        imgback = (ImageView) findViewById(R.id.imgback);
        imgv = (ZoomableImageView) findViewById(R.id.imgv);
        tvcaption = (TextView) findViewById(R.id.tvcaption);
    }

    public void openMediaMenu(View view) {
        final PopupMenu popup = new PopupMenu(this, findViewById(R.id.popupsettings));
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.menu_main, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "menu item clicked");
//                switch (item.getItemId()) {
//                }
                return true;
            }
        });
        popup.show();
    }


    public void onBack(View view){
        finish();
    }
}
