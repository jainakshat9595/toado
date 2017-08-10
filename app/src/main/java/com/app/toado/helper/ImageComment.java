package com.app.toado.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.app.toado.R;
import com.app.toado.services.UploadFileService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.app.toado.helper.ToadoConfig.DBREF;

public class ImageComment extends AppCompatActivity {

    String imageuri, videouri;
    FloatingActionButton sendImage;
    EditText caption;
    ImageView imagecomment;
    VideoView videocomment;
    private String sender;
    private String mykey;
    private String otheruserkey;
    String commentString, comment_type;
    UploadFileService uploadFileService;
    boolean mServiceBound = false;
    PathFromUri pat;
    private String TAG = "ImageCOMMENT ACTIVITY";
    TextView filedesc;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_comment);

        filedesc = (TextView) findViewById(R.id.filedesc);
        sendImage = (FloatingActionButton) findViewById(R.id.sendImage);
        imagecomment = (ImageView) findViewById(R.id.imagecomment);
        caption = (EditText) findViewById(R.id.caption);
        videocomment = (VideoView) findViewById(R.id.videocomment);

        Intent intent = getIntent();
        comment_type = intent.getStringExtra("comment_type");
        sender = intent.getStringExtra("username");
        mykey = intent.getStringExtra("mykey");
        otheruserkey = intent.getStringExtra("otheruserkey");
        System.out.println("comment type imagecomment act" + comment_type);
        pat = new PathFromUri();

        switch (comment_type) {
            case "photo":
                imagecomment.setVisibility(View.VISIBLE);
                videocomment.setVisibility(View.GONE);
                imageuri = getIntent().getStringExtra("URI");
                System.out.println("image uri imagecomment " + imageuri);
                Glide.with(this).load(new File(imageuri)).error(R.drawable.whatsapplogo).into(imagecomment);
                break;
            case "video":
                imagecomment.setVisibility(View.GONE);
                videocomment.setVisibility(View.VISIBLE);
                imageuri = getIntent().getStringExtra("URI");
                Log.d(TAG, "video uri imagecomment " + imageuri);
                Glide.with(this).load(R.drawable.docsimage).error(R.drawable.whatsapplogo).into(imagecomment);
                break;
            case "location":
                imagecomment.setVisibility(View.VISIBLE);
                videocomment.setVisibility(View.GONE);
                imageuri = getIntent().getStringExtra("URI");
                Log.d(TAG, "location uri imagecomment " + imageuri);
                Glide.with(this).load(new File(imageuri)).error(R.drawable.whatsapplogo).into(imagecomment);
                break;
            case "doc":
                Glide.with(this).load(R.drawable.whatsapplogo).into(imagecomment);
                Log.d(TAG, "docs uri imagecomment " + imageuri);
                imageuri = getIntent().getStringExtra("URI");
                Glide.with(this).load(R.drawable.docsimage).error(R.drawable.whatsapplogo).into(imagecomment);
                break;
        }

        filedesc.setText(imageuri);

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (caption.getText().toString().matches(""))
                    commentString = "";
                else
                    commentString = caption.getText().toString();

                String type = "";
                switch (comment_type) {
                    case "photo":
                        type = "photo";
                        break;
                    case "video":
                        type = "video";
                        break;
                    case "location":
                        type = "location";
                        break;
                    case "doc":
                        type = "doc";
                        break;
                }

                try {
                    String path = UriHelper.getPath(ImageComment.this, Uri.parse(imageuri));
                    Log.d(TAG,"get path imagecomment"+path);

                    if(path!=null)
                        uploadFile(commentString,path , type);
                    else
                        uploadFile(commentString, imageuri, type);

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public String getPath(Uri uri) {
        System.out.println("get path called for uri" + uri);
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "nil";

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private ServiceConnection muploadserconn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UploadFileService.MyBinder myBinder = (UploadFileService.MyBinder) service;
            uploadFileService = myBinder.getService();
            mServiceBound = true;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(muploadserconn);
            mServiceBound = false;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, UploadFileService.class);
        startService(intent);
        if (!mServiceBound) {
            bindService(intent, muploadserconn, Context.BIND_AUTO_CREATE);
        }
    }

    private void uploadFile(String msg, String filePath, String type) {
        try {
            Log.d(TAG, " filepath imagecomment activity" + filePath + "   " + msg);
            if (mServiceBound) {
                uploadFileService.uploadFile(msg, filePath, type, mykey, otheruserkey, sender, ImageComment.this);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
