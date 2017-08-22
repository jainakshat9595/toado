package com.app.toado.activity.chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.VideoView;

import com.app.toado.R;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.settings.SettingsActivity;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.File;

/**
 * Created by ghanendra on 11/08/2017.
 */

public class MediaPreviewActivity extends Activity {
    final static String TAG = "MediaPreviewActivity";

    String imagepath, sender, timestamp, caption, contentype;
    private ImageView ivSettings;
    private ImageView ivForward;
    private ImageView ivStar;
    private TextView tvname;
    private TextView tvtime;
    private ImageView imgback;
    private com.github.chrisbanes.photoview.PhotoView imgv;
    private TextView tvcaption;
    private PhotoViewAttacher mAttacher;
    private VideoView vidv;
    int count = 0;
//    private ImageView ivPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_preview);
        initView();


        contentype = getIntent().getStringExtra("mediatype");
        imagepath = getIntent().getStringExtra("imagepath");
        sender = getIntent().getStringExtra("sender");
        timestamp = getIntent().getStringExtra("timestamp");
        caption = getIntent().getStringExtra("caption");

        tvname.setText(sender);

        tvcaption.setText(caption);
        tvtime.setText(timestamp);

        Log.d(TAG, contentype + " imagepath mediapreview act  " + imagepath);

        if (contentype.contains("image")) {
            Log.d(TAG, "image called ");

//            ivPlay.setVisibility(View.GONE);
            imgv.setVisibility(View.VISIBLE);
            vidv.setVisibility(View.GONE);
            try {
                imgv.setImageURI(Uri.fromFile(new File(imagepath)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "video called");

////            ivPlay.setVisibility(View.VISIBLE);
            imgv.setVisibility(View.GONE);
            vidv.setVisibility(View.VISIBLE);
            vidv.setVideoPath(imagepath);
            vidv.setMediaController(new MediaController(this));
            vidv.requestFocus();
        }

        vidv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "video clicked");
                if (count == 0) {
//                    ivPlay.setVisibility(View.GONE);
                    vidv.start();
                    count = 1;
                } else {
//                    ivPlay.setVisibility(View.VISIBLE);
                    vidv.stopPlayback();
                    count = 0;
                }
            }
        });
    }

    private void initView() {
        ivSettings = (ImageView) findViewById(R.id.ivSettings);
        ivForward = (ImageView) findViewById(R.id.ivForward);
////        ivPlay = (ImageView) findViewById(R.id.imgplay);
        ivStar = (ImageView) findViewById(R.id.ivStar);
        tvname = (TextView) findViewById(R.id.tvname);
        tvtime = (TextView) findViewById(R.id.tvtime);
        imgback = (ImageView) findViewById(R.id.imgback);
        imgv = (com.github.chrisbanes.photoview.PhotoView) findViewById(R.id.imgv);
        tvcaption = (TextView) findViewById(R.id.tvcaption);
        vidv = (VideoView) findViewById(R.id.vidv);
    }

    public void openMediaMenu(View view) {
        final PopupMenu popup = new PopupMenu(this, findViewById(R.id.ivSettings));
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.menu_media_preview, popup.getMenu());
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


    public void onBack(View view) {
        finish();
    }
}
